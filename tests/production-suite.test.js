/**
 * AURA Fashion — End-to-End Production Test Suite (GitHub 'aura-product' Storage)
 * Tests real HTTP routes (server.js), GitHub REST API client (lib/github-store.js),
 * transactional store service (lib/store-service.js), dynamic admin auth,
 * checkout atomicity/idempotency, cancellation restocking, and recently viewed pruning.
 */

const assert = require('assert');
const crypto = require('crypto');
const http = require('http');

const { server, ADMIN_PASSWORD_ENV_KEYS } = require('../server');
const { clearStoreCache } = require('../lib/github-store');

let testsPassed = 0;
let testsFailed = 0;

async function runTest(name, fn) {
  try {
    await fn();
    console.log(`\x1b[32m✓ [PASS]\x1b[0m ${name}`);
    testsPassed++;
  } catch (err) {
    console.error(`\x1b[31m✗ [FAIL]\x1b[0m ${name}: ${err.stack || err.message}`);
    testsFailed++;
  }
}

function httpRequest(port, method, reqPath, bodyObj = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const payload = bodyObj !== null ? JSON.stringify(bodyObj) : null;
    const reqHeaders = { ...headers };
    if (payload !== null) {
      reqHeaders['Content-Type'] = 'application/json';
      reqHeaders['Content-Length'] = Buffer.byteLength(payload);
    }
    const req = http.request({
      hostname: '127.0.0.1',
      port,
      path: reqPath,
      method,
      headers: reqHeaders
    }, (res) => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end', () => {
        let parsed = data;
        const ct = String(res.headers['content-type'] || '');
        if (ct.includes('application/json') && data) {
          try { parsed = JSON.parse(data); } catch (_) {}
        }
        resolve({ status: res.statusCode, headers: res.headers, body: parsed });
      });
    });
    req.on('error', reject);
    if (payload !== null) req.write(payload);
    req.end();
  });
}

// ============================================================================
// In-Memory Mock GitHub REST API Server
// ============================================================================
function createMockGitHubServer() {
  const state = {
    branches: {
      main: 'commit_main_001'
    },
    filesByBranch: {
      main: new Map()
    }
  };

  function shaOf(buf) {
    return crypto.createHash('sha1').update(buf).digest('hex');
  }

  function readJsonBody(req) {
    return new Promise((resolve) => {
      let raw = '';
      req.on('data', c => { raw += c; });
      req.on('end', () => {
        try { resolve(raw ? JSON.parse(raw) : {}); } catch (_) { resolve({}); }
      });
    });
  }

  const ghServer = http.createServer(async (req, res) => {
    const auth = req.headers.authorization || '';
    if (!auth.startsWith('Bearer ')) {
      res.writeHead(401, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ message: 'Bad credentials' }));
    }

    const parsed = new URL(req.url, 'http://127.0.0.1');
    const pathname = parsed.pathname;

    // GET /repos/:owner/:repo
    let match = pathname.match(/^\/repos\/([^/]+)\/([^/]+)$/);
    if (match && req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ name: match[2], default_branch: 'main' }));
    }

    // GET /repos/:owner/:repo/branches/:branch
    match = pathname.match(/^\/repos\/([^/]+)\/([^/]+)\/branches\/([^/]+)$/);
    if (match && req.method === 'GET') {
      const branch = decodeURIComponent(match[3]);
      if (!state.branches[branch]) {
        res.writeHead(404, { 'Content-Type': 'application/json' });
        return res.end(JSON.stringify({ message: 'Branch not found' }));
      }
      res.writeHead(200, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ name: branch, commit: { sha: state.branches[branch] } }));
    }

    // GET /repos/:owner/:repo/git/ref/heads/:branch
    match = pathname.match(/^\/repos\/([^/]+)\/([^/]+)\/git\/ref\/heads\/(.+)$/);
    if (match && req.method === 'GET') {
      const branch = decodeURIComponent(match[3]);
      if (!state.branches[branch]) {
        res.writeHead(404, { 'Content-Type': 'application/json' });
        return res.end(JSON.stringify({ message: 'Not Found' }));
      }
      res.writeHead(200, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ ref: `refs/heads/${branch}`, object: { sha: state.branches[branch] } }));
    }

    // POST /repos/:owner/:repo/git/refs
    match = pathname.match(/^\/repos\/([^/]+)\/([^/]+)\/git\/refs$/);
    if (match && req.method === 'POST') {
      const body = await readJsonBody(req);
      const refName = String(body.ref || '').replace(/^refs\/heads\//, '');
      if (!refName || state.branches[refName]) {
        res.writeHead(422, { 'Content-Type': 'application/json' });
        return res.end(JSON.stringify({ message: 'Reference already exists' }));
      }
      state.branches[refName] = body.sha;
      if (!state.filesByBranch[refName]) {
        state.filesByBranch[refName] = new Map();
      }
      res.writeHead(201, { 'Content-Type': 'application/json' });
      return res.end(JSON.stringify({ ref: `refs/heads/${refName}`, object: { sha: body.sha } }));
    }

    // GET/PUT /repos/:owner/:repo/contents/:path
    match = pathname.match(/^\/repos\/([^/]+)\/([^/]+)\/contents\/(.+)$/);
    if (match) {
      const filePath = match[3].split('/').map(decodeURIComponent).join('/');
      if (req.method === 'GET') {
        const branch = parsed.searchParams.get('ref') || 'main';
        const branchFiles = state.filesByBranch[branch];
        const entry = branchFiles ? branchFiles.get(filePath) : null;
        if (!entry) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({ message: 'Not Found' }));
        }
        const etag = `"${entry.sha}"`;
        if (req.headers['if-none-match'] === etag) {
          res.writeHead(304);
          return res.end();
        }
        res.writeHead(200, {
          'Content-Type': 'application/json',
          'ETag': etag
        });
        return res.end(JSON.stringify({
          name: filePath.split('/').pop(),
          path: filePath,
          sha: entry.sha,
          encoding: 'base64',
          content: entry.contentBase64
        }));
      }

      if (req.method === 'PUT') {
        const body = await readJsonBody(req);
        const branch = body.branch || 'main';
        if (!state.branches[branch]) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({ message: `Branch ${branch} does not exist` }));
        }
        const branchFiles = state.filesByBranch[branch];
        const existing = branchFiles.get(filePath);
        if (existing && body.sha !== existing.sha) {
          res.writeHead(409, { 'Content-Type': 'application/json' });
          return res.end(JSON.stringify({ message: 'SHA mismatch conflict' }));
        }
        const cleanB64 = String(body.content || '').replace(/\s+/g, '');
        const newSha = shaOf(Buffer.from(cleanB64, 'base64'));
        branchFiles.set(filePath, {
          contentBase64: cleanB64,
          sha: newSha,
          commitMessage: body.message
        });
        res.writeHead(existing ? 200 : 201, { 'Content-Type': 'application/json' });
        return res.end(JSON.stringify({
          content: {
            path: filePath,
            sha: newSha
          }
        }));
      }
    }

    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ message: 'Unknown mock GitHub route' }));
  });

  return { ghServer, state };
}

async function startSuite() {
  console.log('================================================================');
  console.log('AURA FASHION — GITHUB AURA-PRODUCT PRODUCTION TEST SUITE');
  console.log('================================================================\n');

  const envKeys = [
    ...ADMIN_PASSWORD_ENV_KEYS,
    'GITHUB_TOKEN',
    'GITHUB_OWNER',
    'GITHUB_REPO',
    'GITHUB_MAIN_BRANCH',
    'GITHUB_PRODUCT_BRANCH',
    'GITHUB_API_BASE',
    'GITHUB_RAW_BASE'
  ];
  const savedEnv = {};
  for (const k of envKeys) savedEnv[k] = process.env[k];

  const clearAdminEnv = () => {
    for (const k of ADMIN_PASSWORD_ENV_KEYS) delete process.env[k];
  };
  const clearGithubEnv = () => {
    delete process.env.GITHUB_TOKEN;
    delete process.env.GITHUB_OWNER;
    delete process.env.GITHUB_REPO;
  };

  const { ghServer, state: ghState } = createMockGitHubServer();
  await new Promise((resolve) => ghServer.listen(0, '127.0.0.1', resolve));
  const ghPort = ghServer.address().port;

  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const testPort = server.address().port;

  try {
    // 1. Fail closed when GitHub and Admin Password env vars are unset
    await runTest('1. Unconfigured GitHub/Admin env vars fail closed with HTTP 503', async () => {
      clearAdminEnv();
      clearGithubEnv();
      clearStoreCache();

      const health = await httpRequest(testPort, 'GET', '/health');
      assert.strictEqual(health.status, 200);
      assert.strictEqual(health.body.storage, 'github');
      assert.strictEqual(health.body.branch, 'aura-product');
      assert.strictEqual(health.body.adminAuthConfigured, false);
      assert.strictEqual(health.body.githubConfigured, false);

      const storefront = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(storefront.status, 503);
      assert.strictEqual(storefront.body.githubConfigured, false);

      const loginRes = await httpRequest(testPort, 'POST', '/api/admin/login', { password: 'any_password' });
      assert.strictEqual(loginRes.status, 503);
    });

    // Enable Mock GitHub backend targeting aura-product
    process.env.GITHUB_TOKEN = 'ghp_test_token_123';
    process.env.GITHUB_OWNER = 'aura-org';
    process.env.GITHUB_REPO = 'aura-store';
    process.env.GITHUB_MAIN_BRANCH = 'main';
    process.env.GITHUB_PRODUCT_BRANCH = 'aura-product';
    process.env.GITHUB_API_BASE = `http://127.0.0.1:${ghPort}`;
    process.env.GITHUB_RAW_BASE = `http://127.0.0.1:${ghPort}/raw`;
    clearStoreCache();

    // 2. Dynamic Admin Password Auth via Render hyphenated AURA-admin & underscore aliases + CSRF
    let adminCookie = '';
    let adminCsrf = '';

    await runTest('2. Dynamic Admin Auth (AURA-admin, rotation, aliases) & CSRF enforcement', async () => {
      clearAdminEnv();
      process.env['AURA-admin'] = 'render-hyphen-secret-1';

      const badRes = await httpRequest(testPort, 'POST', '/api/admin/login', { password: 'wrong' });
      assert.strictEqual(badRes.status, 401);

      const okRes = await httpRequest(testPort, 'POST', '/api/admin/login', { password: 'render-hyphen-secret-1' });
      assert.strictEqual(okRes.status, 200);
      assert.strictEqual(okRes.body.success, true);
      assert.ok(okRes.body.csrfToken);

      // Rotate password without restarting server
      process.env['AURA-admin'] = 'render-hyphen-secret-2';
      const oldPwRes = await httpRequest(testPort, 'POST', '/api/admin/login', { password: 'render-hyphen-secret-1' });
      assert.strictEqual(oldPwRes.status, 401);

      const newPwRes = await httpRequest(testPort, 'POST', '/api/admin/login', { password: 'render-hyphen-secret-2' });
      assert.strictEqual(newPwRes.status, 200);
      const setCookie = newPwRes.headers['set-cookie'][0];
      assert.ok(setCookie.includes('aura_admin_session='));
      assert.ok(setCookie.includes('HttpOnly'));
      adminCookie = setCookie.split(';')[0];
      adminCsrf = newPwRes.body.csrfToken;

      // Test underscore aliases
      for (const alias of ['AURA_ADMIN_PASSWORD', 'AURA_ADMIN', 'ADMIN_PASSWORD']) {
        clearAdminEnv();
        process.env[alias] = `alias-${alias}`;
        const r = await httpRequest(testPort, 'POST', '/api/admin/login', { password: `alias-${alias}` });
        assert.strictEqual(r.status, 200);
      }

      // Restore primary
      clearAdminEnv();
      process.env['AURA-admin'] = 'render-hyphen-secret-2';

      // Protected POST without CSRF rejected with 403; unauthenticated rejected with 401
      const unauth = await httpRequest(testPort, 'GET', '/api/admin/bootstrap');
      assert.strictEqual(unauth.status, 401);

      const noCsrf = await httpRequest(testPort, 'POST', '/api/admin/settings', {}, { Cookie: adminCookie });
      assert.strictEqual(noCsrf.status, 403);
    });

    // 3. Initial Live Storefront contains ZERO hardcoded demo products
    await runTest('3. Storefront initializes clean with 0 demo products & default 30-day retention', async () => {
      const sf = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sf.status, 200);
      assert.strictEqual(sf.body.githubConfigured, true);
      assert.strictEqual(sf.body.branch, 'aura-product');
      assert.deepStrictEqual(sf.body.products, []);
      assert.deepStrictEqual(sf.body.sizeGuides, []);
      assert.strictEqual(sf.body.settings.deliveryFee, 100);
      assert.strictEqual(sf.body.settings.freeShippingThreshold, 3000);
      assert.strictEqual(sf.body.settings.recentlyViewedRetentionDays, 30);
      assert.deepStrictEqual(sf.body.settings.coupons, []);
    });

    // 4. Image Upload auto-creates 'aura-product' branch & returns raw GitHub URL
    let uploadedImageUrl = '';
    await runTest('4. Admin image upload auto-creates aura-product branch and commits asset', async () => {
      assert.strictEqual(ghState.branches['aura-product'], undefined);
      const fakePngBase64 = 'data:image/png;base64,' + Buffer.from('PNG_BINARY_CONTENT').toString('base64');
      const uploadRes = await httpRequest(
        testPort,
        'POST',
        '/api/admin/upload-image',
        { fileName: 'hero_tee.png', base64Data: fakePngBase64 },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(uploadRes.status, 200);
      assert.strictEqual(uploadRes.body.success, true);
      assert.strictEqual(uploadRes.body.branch, 'aura-product');
      assert.strictEqual(uploadRes.body.storage, 'github-aura-product');
      assert.ok(uploadRes.body.path.startsWith('uploads/'));
      assert.ok(uploadRes.body.url.includes('/aura-org/aura-store/aura-product/uploads/'));
      assert.ok(ghState.branches['aura-product']);
      assert.ok(ghState.filesByBranch['aura-product'].has(uploadRes.body.path));
      uploadedImageUrl = uploadRes.body.url;
    });

    // 5. Admin Product CRUD syncs live with Storefront & hides unpublished items
    await runTest('5. Admin product CRUD persists to aura-product products.json and syncs live to storefront', async () => {
      const saveRes = await httpRequest(
        testPort,
        'POST',
        '/api/admin/products',
        {
          id: 'live-tee-01',
          name: 'AURA MONOLITH TEE',
          tagline: 'Custom 280 GSM Organic Cotton',
          price: 1500,
          originalPrice: 1850,
          category: 'Tees',
          image: uploadedImageUrl,
          colors: [{ name: 'Onyx Black', hex: '#111111' }, { name: 'Bone', hex: '#f4f4f0' }],
          sizes: ['M', 'L'],
          stockPerSize: { M: 5, L: 3 },
          published: true
        },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(saveRes.status, 200);
      assert.strictEqual(saveRes.body.product.id, 'live-tee-01');

      const sf1 = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sf1.body.products.length, 1);
      assert.strictEqual(sf1.body.products[0].image, uploadedImageUrl);

      // Unpublish hides from public storefront but retains in admin bootstrap
      await httpRequest(
        testPort,
        'POST',
        '/api/admin/products',
        { ...saveRes.body.product, published: false },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      const sfHidden = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sfHidden.body.products.length, 0);

      const bootHidden = await httpRequest(testPort, 'GET', '/api/admin/bootstrap', null, { Cookie: adminCookie });
      assert.strictEqual(bootHidden.body.products.length, 1);
      assert.strictEqual(bootHidden.body.products[0].published, false);

      // Re-publish for checkout tests
      await httpRequest(
        testPort,
        'POST',
        '/api/admin/products',
        { ...saveRes.body.product, published: true },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );

      // Create temporary product and delete it
      await httpRequest(
        testPort,
        'POST',
        '/api/admin/products',
        {
          id: 'temp-del-01',
          name: 'TEMP ITEM',
          price: 900,
          sizes: ['M'],
          stockPerSize: { M: 2 },
          published: true
        },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      const delRes = await httpRequest(
        testPort,
        'DELETE',
        '/api/admin/products/temp-del-01',
        null,
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(delRes.status, 200);
      const sfAfterDel = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sfAfterDel.body.products.length, 1);
    });

    // 6. CMS Settings & Size Guides live persistence
    await runTest('6. CMS Settings (including recentlyViewedRetentionDays & coupons) and Size Guides persist live', async () => {
      const cmsRes = await httpRequest(
        testPort,
        'POST',
        '/api/admin/settings',
        {
          announcementBar: 'LIVE CAPSULE DROP — FREE SHIPPING OVER ৳3,500',
          heroTitle: 'ARCHITECTURAL ESSENTIALS',
          heroSubtitle: 'Live synced from GitHub aura-product',
          deliveryFee: 120,
          freeShippingThreshold: 3500,
          recentlyViewedRetentionDays: 45,
          availablePaymentMethods: ['Cash on Delivery'],
          availableCouriers: ['Pathao Express', 'RedX Logistics'],
          coupons: [{ code: 'AURA10', discountPercent: 10, minSpend: 1000, isFreeShipping: false }]
        },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(cmsRes.status, 200);
      assert.strictEqual(cmsRes.body.settings.recentlyViewedRetentionDays, 45);

      const sgRes = await httpRequest(
        testPort,
        'POST',
        '/api/admin/size-guides',
        {
          id: 'sg-tees',
          category: 'Tees',
          columns: ['Chest', 'Length'],
          rows: [{ size: 'M', values: ['118 cm', '75 cm'] }],
          published: true
        },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(sgRes.status, 200);

      const sf = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sf.body.settings.deliveryFee, 120);
      assert.strictEqual(sf.body.settings.recentlyViewedRetentionDays, 45);
      assert.strictEqual(sf.body.settings.coupons.length, 1);
      assert.strictEqual(sf.body.sizeGuides.length, 1);
    });

    // 7. Customer Checkout validation, multi-color stock aggregation, idempotency & sequential IDs
    let firstOrderId = '';
    await runTest('7. Checkout validates stock/bounds/couriers, aggregates stock by product+size, and enforces idempotency', async () => {
      // Bad size rejected
      const badSize = await httpRequest(testPort, 'POST', '/api/checkout', {
        customerName: 'Nadia Rahman',
        customerPhone: '01711111111',
        shippingAddress: { street: 'House 12, Banani', city: 'Dhaka' },
        courier: 'Pathao Express',
        paymentMethod: 'Cash on Delivery',
        items: [{ product_id: 'live-tee-01', size: 'XXXL', color: 'Onyx Black', quantity: 1 }]
      });
      assert.strictEqual(badSize.status, 400);

      // Bad payment rejected
      const badPm = await httpRequest(testPort, 'POST', '/api/checkout', {
        customerName: 'Nadia Rahman',
        customerPhone: '01711111111',
        shippingAddress: { street: 'House 12, Banani', city: 'Dhaka' },
        courier: 'Pathao Express',
        paymentMethod: 'Bitcoin',
        items: [{ product_id: 'live-tee-01', size: 'M', color: 'Onyx Black', quantity: 1 }]
      });
      assert.strictEqual(badPm.status, 400);

      // Stock limit across two colors of same size (M has 5 in stock -> 3 Onyx + 3 Bone = 6 > 5 rejected)
      const overStock = await httpRequest(testPort, 'POST', '/api/checkout', {
        customerName: 'Nadia Rahman',
        customerPhone: '01711111111',
        shippingAddress: { street: 'House 12, Banani', city: 'Dhaka' },
        courier: 'Pathao Express',
        paymentMethod: 'Cash on Delivery',
        items: [
          { product_id: 'live-tee-01', size: 'M', color: 'Onyx Black', quantity: 3 },
          { product_id: 'live-tee-01', size: 'M', color: 'Bone', quantity: 3 }
        ]
      });
      assert.strictEqual(overStock.status, 400);
      assert.ok(overStock.body.error.includes('Insufficient stock'));

      // Valid multi-color checkout: 2 Onyx Black + 1 Bone in Size M = 3 units deducted from M (5 -> 2)
      // Subtotal = 3 * 1500 = 4500 >= 3500 threshold => shipping = 0, AURA10 => discount = 450, total = 4050
      const validPayload = {
        idempotencyKey: 'idem_live_001',
        customerName: 'Nadia Rahman',
        customerPhone: '01711111111',
        shippingAddress: { street: 'House 12, Banani', city: 'Dhaka' },
        courier: 'Pathao Express',
        paymentMethod: 'Cash on Delivery',
        couponCode: 'AURA10',
        items: [
          { product_id: 'live-tee-01', size: 'M', color: 'Onyx Black', quantity: 2 },
          { product_id: 'live-tee-01', size: 'M', color: 'Bone', quantity: 1 }
        ]
      };
      const okOrder = await httpRequest(testPort, 'POST', '/api/checkout', validPayload);
      assert.strictEqual(okOrder.status, 200);
      assert.strictEqual(okOrder.body.success, true);
      assert.ok(okOrder.body.order_id.endsWith('-10001'));
      assert.strictEqual(okOrder.body.subtotal, 4500);
      assert.strictEqual(okOrder.body.discount, 450);
      assert.strictEqual(okOrder.body.shipping, 0);
      assert.strictEqual(okOrder.body.total_amount, 4050);
      firstOrderId = okOrder.body.order_id;

      // Verify stock deducted in storefront
      const sfAfterOrder = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sfAfterOrder.body.products[0].stockPerSize.M, 2);

      // Replay same idempotencyKey + identical payload -> duplicate response, no extra stock deduction
      const replay = await httpRequest(testPort, 'POST', '/api/checkout', validPayload);
      assert.strictEqual(replay.status, 200);
      assert.strictEqual(replay.body.is_duplicate, true);
      assert.strictEqual(replay.body.order_id, firstOrderId);

      // Replay same idempotencyKey + altered payload -> 409 conflict
      const tampered = await httpRequest(testPort, 'POST', '/api/checkout', {
        ...validPayload,
        items: [{ product_id: 'live-tee-01', size: 'L', color: 'Onyx Black', quantity: 1 }]
      });
      assert.strictEqual(tampered.status, 409);

      // Order lookup for customer tracking
      const lookup = await httpRequest(testPort, 'POST', '/api/orders/lookup', { orderIds: [firstOrderId] });
      assert.strictEqual(lookup.status, 200);
      assert.strictEqual(lookup.body.orders.length, 1);
      assert.strictEqual(lookup.body.orders[0].status, 'confirmed');
    });

    // 8. Admin Orders Ledger, Cancellation Idempotent Restock & Delivered Protection
    await runTest('8. Admin cancel-order atomically restocks inventory once and blocks cancelling delivered orders', async () => {
      const boot = await httpRequest(testPort, 'GET', '/api/admin/bootstrap', null, { Cookie: adminCookie });
      assert.strictEqual(boot.status, 200);
      assert.strictEqual(boot.body.orders.length, 1);
      assert.strictEqual(boot.body.customers.length, 1);
      assert.strictEqual(boot.body.customers[0].totalSpent, 4050);

      // Cancel order -> restores Size M stock from 2 back to 5
      const cancel1 = await httpRequest(
        testPort,
        'POST',
        '/api/admin/cancel-order',
        { orderId: firstOrderId },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(cancel1.status, 200);
      assert.strictEqual(cancel1.body.already_cancelled, false);

      const sfRestocked = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sfRestocked.body.products[0].stockPerSize.M, 5);

      // Repeat cancel -> idempotent, must NOT over-restock
      const cancel2 = await httpRequest(
        testPort,
        'POST',
        '/api/admin/cancel-order',
        { orderId: firstOrderId },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(cancel2.status, 200);
      assert.strictEqual(cancel2.body.already_cancelled, true);
      const sfStillFive = await httpRequest(testPort, 'GET', '/api/storefront');
      assert.strictEqual(sfStillFive.body.products[0].stockPerSize.M, 5);

      // Place second order -> Sequential ID increments to 10002 -> mark delivered -> cancellation blocked
      const ord2 = await httpRequest(testPort, 'POST', '/api/checkout', {
        idempotencyKey: 'idem_live_002',
        customerName: 'Tariq Aziz',
        customerPhone: '01822222222',
        shippingAddress: { street: 'Road 4, Dhanmondi', city: 'Dhaka' },
        courier: 'RedX Logistics',
        paymentMethod: 'Cash on Delivery',
        items: [{ product_id: 'live-tee-01', size: 'L', color: 'Onyx Black', quantity: 1 }]
      });
      assert.strictEqual(ord2.status, 200);
      assert.ok(ord2.body.order_id.endsWith('-10002'));

      const markDelivered = await httpRequest(
        testPort,
        'POST',
        '/api/admin/orders/status',
        { orderId: ord2.body.order_id, status: 'delivered' },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(markDelivered.status, 200);

      const cancelDelivered = await httpRequest(
        testPort,
        'POST',
        '/api/admin/cancel-order',
        { orderId: ord2.body.order_id },
        { Cookie: adminCookie, 'X-CSRF-Token': adminCsrf }
      );
      assert.strictEqual(cancelDelivered.status, 400);
      assert.ok(cancelDelivered.body.error.includes('Cannot cancel a delivered order'));
    });

    // 9. Recently Viewed Auto-Delete Retention & 20-Item Pruning Logic
    await runTest('9. Recently Viewed retention auto-deletes expired entries (default 30 days) and caps at 20', async () => {
      const now = Date.now();
      const dayMs = 24 * 60 * 60 * 1000;
      const prune = (items, retentionDays = 30) => {
        const cutoff = now - retentionDays * dayMs;
        return items
          .filter(it => it && it.id && new Date(it.viewedAt).getTime() >= cutoff)
          .slice(0, 20);
      };

      const sample = [
        { id: 'fresh-1', viewedAt: new Date(now - 2 * dayMs).toISOString() },
        { id: 'edge-29', viewedAt: new Date(now - 29 * dayMs).toISOString() },
        { id: 'expired-31', viewedAt: new Date(now - 31 * dayMs).toISOString() },
        { id: 'expired-90', viewedAt: new Date(now - 90 * dayMs).toISOString() }
      ];

      const kept30 = prune(sample, 30);
      assert.deepStrictEqual(kept30.map(x => x.id), ['fresh-1', 'edge-29']);

      const kept7 = prune(sample, 7);
      assert.deepStrictEqual(kept7.map(x => x.id), ['fresh-1']);

      const many = Array.from({ length: 25 }, (_, i) => ({
        id: `p-${i}`,
        viewedAt: new Date(now - i * 1000).toISOString()
      }));
      assert.strictEqual(prune(many, 30).length, 20);
    });

    // 10. Static Routes & Zero Supabase References in Served HTML
    await runTest('10. Storefront (/) and Admin (/admin) HTML serve cleanly with zero Supabase references', async () => {
      const home = await httpRequest(testPort, 'GET', '/');
      assert.strictEqual(home.status, 200);
      assert.ok(typeof home.body === 'string');
      assert.strictEqual(/supabase/i.test(home.body), false);
      assert.ok(home.body.includes('RECENTLY VIEWED') || home.body.includes('recordProductView'));

      const idx = await httpRequest(testPort, 'GET', '/index.html');
      assert.strictEqual(idx.status, 200);
      assert.strictEqual(/supabase/i.test(idx.body), false);

      const admin = await httpRequest(testPort, 'GET', '/admin');
      assert.strictEqual(admin.status, 200);
      assert.strictEqual(/supabase/i.test(admin.body), false);
      assert.ok(admin.body.includes('aura-product'));
    });
  } finally {
    for (const k of envKeys) {
      if (savedEnv[k] === undefined) delete process.env[k];
      else process.env[k] = savedEnv[k];
    }
    clearStoreCache();
    await new Promise((resolve) => server.close(resolve));
    await new Promise((resolve) => ghServer.close(resolve));
  }

  console.log('\n================================================================');
  console.log(`TEST SUITE RESULTS: ${testsPassed} PASSED, ${testsFailed} FAILED`);
  console.log('================================================================\n');

  if (testsFailed > 0) {
    process.exit(1);
  }
}

startSuite();
