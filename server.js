/**
 * AURA Fashion — Production Node.js Server (GitHub 'aura-product' Storage Edition)
 * - Standalone Password-Only Admin Authentication (dynamic process.env lookup including AURA-admin)
 * - HttpOnly Session Cookies with CSRF protection
 * - Live GitHub REST API storage targeting 'aura-product' branch (products.json, orders.json, settings.json, size-guides.json, uploads/*)
 * - Abuse-guarded /api/checkout with server-side stock deduction & validation
 * - Dynamic process.env.PORT handling for Render deployment
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const {
  getGitHubConfig,
  uploadImageAsset,
  GitHubConfigError,
  GitHubApiError
} = require('./lib/github-store');

const {
  ValidationError,
  getStorefrontData,
  getAdminBootstrapData,
  upsertProduct,
  deleteProduct,
  updateSettings,
  upsertSizeGuide,
  deleteSizeGuide,
  placeOrder,
  lookupOrdersByIds,
  updateOrderStatus
} = require('./lib/store-service');

// Render assigns process.env.PORT dynamically. In local container, nginx listens on 8080 and proxies to 3000.
const PORT = (process.env.PORT && process.env.PORT !== '8080') ? parseInt(process.env.PORT, 10) : 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

// Supported admin password environment variable names, evaluated dynamically per request.
const ADMIN_PASSWORD_ENV_KEYS = [
  'AURA-admin',
  'AURA_ADMIN_PASSWORD',
  'AURA_ADMIN',
  'ADMIN_PASSWORD'
];

// Ephemeral per-process fallback secret if SESSION_SECRET is unset (never a hardcoded string literal)
const EPHEMERAL_SESSION_SECRET = crypto.randomBytes(32).toString('hex');
const COMPARE_HMAC_KEY = crypto.randomBytes(32);

function getConfiguredAdminPassword() {
  for (const key of ADMIN_PASSWORD_ENV_KEYS) {
    const value = process.env[key];
    if (typeof value === 'string' && value.length > 0) {
      return { password: value, envKey: key };
    }
  }
  return null;
}

function getSessionSecret() {
  const secret = process.env.SESSION_SECRET;
  return (typeof secret === 'string' && secret.length > 0) ? secret : EPHEMERAL_SESSION_SECRET;
}

// In-Memory Rate Limiting Cache
const rateLimits = {
  adminLogin: new Map(),
  checkout: new Map()
};

function checkRateLimit(map, ip, maxAttempts, windowMs) {
  const now = Date.now();
  let record = map.get(ip);
  if (!record || now > record.resetAt) {
    record = { count: 1, resetAt: now + windowMs };
    map.set(ip, record);
    return true;
  }
  if (record.count >= maxAttempts) {
    return false;
  }
  record.count++;
  return true;
}

const cleanupTimer = setInterval(() => {
  const now = Date.now();
  for (const [ip, rec] of rateLimits.adminLogin.entries()) {
    if (now > rec.resetAt) rateLimits.adminLogin.delete(ip);
  }
  for (const [ip, rec] of rateLimits.checkout.entries()) {
    if (now > rec.resetAt) rateLimits.checkout.delete(ip);
  }
}, 600000);
if (cleanupTimer.unref) cleanupTimer.unref();

// Active Admin Sessions Cache
const activeAdminSessions = new Map(); // tokenHash -> { csrfToken, expiresAt, ip }

function parseCookies(req) {
  const list = {};
  const rc = req.headers.cookie;
  if (rc) {
    rc.split(';').forEach(cookie => {
      const idx = cookie.indexOf('=');
      if (idx === -1) return;
      const key = cookie.slice(0, idx).trim();
      const rawVal = cookie.slice(idx + 1).trim();
      try {
        list[key] = decodeURIComponent(rawVal);
      } catch (_) {
        list[key] = rawVal;
      }
    });
  }
  return list;
}

function isRequestSecure(req) {
  const forwardedProto = req.headers['x-forwarded-proto'];
  if (typeof forwardedProto === 'string') {
    return forwardedProto.split(',')[0].trim().toLowerCase() === 'https';
  }
  return Boolean(req.socket && req.socket.encrypted);
}

function timingSafeCompare(a, b) {
  if (typeof a !== 'string' || typeof b !== 'string' || a.length === 0 || b.length === 0) {
    return false;
  }
  const digestA = crypto.createHmac('sha256', COMPARE_HMAC_KEY).update(a, 'utf8').digest();
  const digestB = crypto.createHmac('sha256', COMPARE_HMAC_KEY).update(b, 'utf8').digest();
  return crypto.timingSafeEqual(digestA, digestB);
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 7 * 1024 * 1024) {
        reject(new Error('Payload too large'));
      }
    });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (err) {
        reject(new Error('Invalid JSON payload'));
      }
    });
    req.on('error', reject);
  });
}

function sendJson(res, statusCode, data, headers = {}) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    ...headers
  });
  res.end(JSON.stringify(data));
}

function handleError(res, err) {
  if (err instanceof GitHubConfigError || err instanceof ValidationError || err instanceof GitHubApiError) {
    return sendJson(res, err.statusCode || 400, { error: err.message });
  }
  const status = err && Number.isInteger(err.statusCode) ? err.statusCode : 500;
  return sendJson(res, status, { error: (err && err.message) || 'Internal server error' });
}

function verifyAdminSession(req) {
  const cookies = parseCookies(req);
  const sessionToken = cookies['aura_admin_session'];
  if (!sessionToken) return false;

  const tokenHash = crypto.createHash('sha256').update(sessionToken + getSessionSecret()).digest('hex');
  const memSession = activeAdminSessions.get(tokenHash);
  if (!memSession) return false;

  if (Date.now() > memSession.expiresAt) {
    activeAdminSessions.delete(tokenHash);
    return false;
  }
  return { tokenHash, csrfToken: memSession.csrfToken };
}

function verifyCsrfToken(req, adminSession) {
  const provided = req.headers['x-csrf-token'];
  return typeof provided === 'string' && timingSafeCompare(provided, adminSession.csrfToken);
}

function requireAdminAuth(req, res, { checkCsrf = false } = {}) {
  const adminSession = verifyAdminSession(req);
  if (!adminSession) {
    sendJson(res, 401, { error: 'Unauthorized. Admin session required.' });
    return null;
  }
  if (checkCsrf && !verifyCsrfToken(req, adminSession)) {
    sendJson(res, 403, { error: 'Invalid or missing CSRF token.' });
    return null;
  }
  return adminSession;
}

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff2': 'font/woff2',
  '.woff': 'font/woff',
  '.ttf': 'font/ttf'
};

const server = http.createServer(async (req, res) => {
  const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';

  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'SAMEORIGIN');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  if (pathname === '/health' && req.method === 'GET') {
    const gh = getGitHubConfig();
    return sendJson(res, 200, {
      status: 'healthy',
      timestamp: new Date().toISOString(),
      service: 'aura-fashion-server',
      storage: 'github',
      branch: gh.productBranch,
      adminAuthConfigured: Boolean(getConfiguredAdminPassword()),
      githubConfigured: gh.configured
    });
  }

  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-CSRF-Token'
    });
    return res.end();
  }

  // ==========================================================================
  // PUBLIC STOREFRONT API (Reads directly from GitHub 'aura-product' branch)
  // ==========================================================================
  if (pathname === '/api/storefront' && req.method === 'GET') {
    try {
      const gh = getGitHubConfig();
      if (!gh.configured) {
        return sendJson(res, 503, {
          error: 'GitHub storage is not configured. Set GITHUB_TOKEN, GITHUB_OWNER, and GITHUB_REPO environment variables.',
          githubConfigured: false,
          products: [],
          sizeGuides: []
        });
      }
      const data = await getStorefrontData();
      return sendJson(res, 200, { ...data, githubConfigured: true, branch: gh.productBranch });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/orders/lookup' && req.method === 'POST') {
    try {
      const body = await parseJsonBody(req);
      const orders = await lookupOrdersByIds(body.orderIds || []);
      return sendJson(res, 200, { orders });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/checkout' && req.method === 'POST') {
    if (!checkRateLimit(rateLimits.checkout, clientIp, 15, 15 * 60 * 1000)) {
      return sendJson(res, 429, { error: 'Checkout rate limit reached. Please wait before retrying.' });
    }

    try {
      const body = await parseJsonBody(req);

      if (process.env.TURNSTILE_SECRET_KEY && body.captchaToken) {
        const verifyRes = await fetch('https://challenges.cloudflare.com/turnstile/v0/siteverify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            secret: process.env.TURNSTILE_SECRET_KEY,
            response: body.captchaToken,
            remoteip: clientIp
          })
        });
        const verifyData = await verifyRes.json();
        if (!verifyData.success) {
          return sendJson(res, 400, { error: 'CAPTCHA challenge failed. Please try again.' });
        }
      }

      const result = await placeOrder(body);
      return sendJson(res, 200, result);
    } catch (err) {
      return handleError(res, err);
    }
  }

  // ==========================================================================
  // ADMIN AUTHENTICATION ROUTES
  // ==========================================================================
  if (pathname === '/api/admin/login' && req.method === 'POST') {
    const configured = getConfiguredAdminPassword();
    if (!configured) {
      return sendJson(res, 503, {
        error: 'Administrator password is not configured on the server. Set AURA-admin (or AURA_ADMIN_PASSWORD / AURA_ADMIN / ADMIN_PASSWORD) in Render Environment Variables.'
      });
    }

    if (!checkRateLimit(rateLimits.adminLogin, clientIp, 5, 15 * 60 * 1000)) {
      return sendJson(res, 429, { error: 'Too many failed login attempts. Please wait 15 minutes.' });
    }

    try {
      const body = await parseJsonBody(req);
      const password = typeof body.password === 'string' ? body.password : '';

      if (!password || !timingSafeCompare(password, configured.password)) {
        return sendJson(res, 401, { error: 'Invalid administrator password.' });
      }

      rateLimits.adminLogin.delete(clientIp);

      const sessionToken = crypto.randomBytes(32).toString('hex');
      const tokenHash = crypto.createHash('sha256').update(sessionToken + getSessionSecret()).digest('hex');
      const csrfToken = crypto.randomBytes(24).toString('hex');
      const expiresAt = Date.now() + 24 * 60 * 60 * 1000;

      activeAdminSessions.set(tokenHash, { csrfToken, expiresAt, ip: clientIp });

      const isSecure = isRequestSecure(req);
      const cookieHeader = `aura_admin_session=${sessionToken}; Path=/; HttpOnly; SameSite=Strict; Max-Age=86400${isSecure ? '; Secure' : ''}`;

      return sendJson(res, 200, {
        success: true,
        csrfToken,
        message: 'Administrator session established successfully.'
      }, {
        'Set-Cookie': cookieHeader
      });
    } catch (err) {
      return sendJson(res, 400, { error: err.message });
    }
  }

  if (pathname === '/api/admin/logout' && req.method === 'POST') {
    const adminSession = verifyAdminSession(req);
    if (adminSession) {
      activeAdminSessions.delete(adminSession.tokenHash);
    }
    const isSecure = isRequestSecure(req);
    return sendJson(res, 200, { success: true }, {
      'Set-Cookie': `aura_admin_session=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0${isSecure ? '; Secure' : ''}`
    });
  }

  if (pathname === '/api/admin/check-session' && req.method === 'GET') {
    const adminSession = verifyAdminSession(req);
    if (!adminSession) {
      return sendJson(res, 401, { authenticated: false });
    }
    return sendJson(res, 200, { authenticated: true, csrfToken: adminSession.csrfToken });
  }

  // ==========================================================================
  // PROTECTED ADMIN DATA ROUTES (GitHub 'aura-product' Branch)
  // ==========================================================================
  if (pathname === '/api/admin/bootstrap' && req.method === 'GET') {
    if (!requireAdminAuth(req, res)) return;
    try {
      const gh = getGitHubConfig();
      const data = await getAdminBootstrapData();
      return sendJson(res, 200, {
        ...data,
        githubConfigured: gh.configured,
        branch: gh.productBranch,
        repo: `${gh.owner}/${gh.repo}`
      });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/products' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const saved = await upsertProduct(body);
      return sendJson(res, 200, { success: true, product: saved });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname.startsWith('/api/admin/products/') && req.method === 'DELETE') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const productId = decodeURIComponent(pathname.slice('/api/admin/products/'.length));
      const result = await deleteProduct(productId);
      return sendJson(res, 200, { success: true, ...result });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/orders/status' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const result = await updateOrderStatus({
        orderId: body.orderId,
        newStatus: body.status,
        trackingId: body.trackingId,
        adminNotes: body.adminNotes
      });
      return sendJson(res, 200, result);
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/cancel-order' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const result = await updateOrderStatus({
        orderId: body.orderId,
        newStatus: 'cancelled',
        adminNotes: body.adminNotes || 'Cancelled via Admin Portal'
      });
      return sendJson(res, 200, result);
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/settings' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const settings = await updateSettings(body);
      return sendJson(res, 200, { success: true, settings });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/size-guides' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const sizeGuide = await upsertSizeGuide(body);
      return sendJson(res, 200, { success: true, sizeGuide });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname.startsWith('/api/admin/size-guides/') && req.method === 'DELETE') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const guideId = decodeURIComponent(pathname.slice('/api/admin/size-guides/'.length));
      const result = await deleteSizeGuide(guideId);
      return sendJson(res, 200, { success: true, ...result });
    } catch (err) {
      return handleError(res, err);
    }
  }

  if (pathname === '/api/admin/upload-image' && req.method === 'POST') {
    if (!requireAdminAuth(req, res, { checkCsrf: true })) return;
    try {
      const body = await parseJsonBody(req);
      const result = await uploadImageAsset(body.fileName, body.base64Data);
      return sendJson(res, 200, result);
    } catch (err) {
      return handleError(res, err);
    }
  }

  // ==========================================================================
  // STATIC FILE SERVING
  // ==========================================================================
  const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');

  if (safePath === '/admin' || safePath === '/admin.html') {
    const adminPath = path.join(__dirname, 'admin.html');
    if (fs.existsSync(adminPath)) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      return fs.createReadStream(adminPath).pipe(res);
    }
  }

  const filePath = path.join(PUBLIC_DIR, safePath);
  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
    return fs.createReadStream(filePath).pipe(res);
  }

  const rootFilePath = path.join(__dirname, safePath);
  if (fs.existsSync(rootFilePath) && fs.statSync(rootFilePath).isFile()) {
    const ext = path.extname(rootFilePath).toLowerCase();
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
    return fs.createReadStream(rootFilePath).pipe(res);
  }

  // Default SPA fallback: root index.html (prefer root index.html so edits are single-source)
  const rootIndex = path.join(__dirname, 'index.html');
  const publicIndex = path.join(PUBLIC_DIR, 'index.html');
  const targetIndex = fs.existsSync(rootIndex) ? rootIndex : publicIndex;

  if (fs.existsSync(targetIndex)) {
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    fs.createReadStream(targetIndex).pipe(res);
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('AURA Website - index.html not found');
  }
});

if (require.main === module) {
  if (!getConfiguredAdminPassword()) {
    console.warn('[AURA] Warning: No admin password environment variable set (checked AURA-admin, AURA_ADMIN_PASSWORD, AURA_ADMIN, ADMIN_PASSWORD). Admin login will return HTTP 503 until configured.');
  }
  const gh = getGitHubConfig();
  if (!gh.configured) {
    console.warn('[AURA] Warning: GitHub storage env vars (GITHUB_TOKEN, GITHUB_OWNER, GITHUB_REPO) are not set. Storefront/Admin data endpoints will return HTTP 503 until configured.');
  }
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`AURA Production Server running on port ${PORT} (GitHub branch: ${gh.productBranch})`);
  });
}

module.exports = {
  server,
  getConfiguredAdminPassword,
  getSessionSecret,
  timingSafeCompare,
  ADMIN_PASSWORD_ENV_KEYS
};
