/**
 * AURA Fashion — GitHub REST API Storage Engine ('aura-product' branch)
 * Persists catalog (products.json), orders (orders.json), site settings (settings.json),
 * size guides (size-guides.json), and uploaded product images (uploads/*) directly on GitHub.
 */

const FILE_PATHS = {
  PRODUCTS: 'products.json',
  ORDERS: 'orders.json',
  SETTINGS: 'settings.json',
  SIZE_GUIDES: 'size-guides.json'
};

const DEFAULT_SETTINGS = {
  announcementBar: 'DROP 04 : MONOLITH CAPSULE NOW LIVE — COMPLIMENTARY EXPRESS DELIVERY OVER ৳3,000',
  heroTitle: 'GIVES YOU DIFFERENT VIBE',
  heroSubtitle: 'Architectural cuts, heavyweight 280 GSM custom French terry, and relaxed luxury silhouettes engineered in Dhaka for global presence.',
  deliveryFee: 100,
  freeShippingThreshold: 3000,
  recentlyViewedRetentionDays: 30,
  availablePaymentMethods: ['Cash on Delivery', 'bKash Online Payment'],
  availableCouriers: ['Pathao Express', 'RedX Logistics', 'Steadfast Courier'],
  coupons: []
};

class GitHubConfigError extends Error {
  constructor(message) {
    super(message);
    this.name = 'GitHubConfigError';
    this.statusCode = 503;
  }
}

class GitHubApiError extends Error {
  constructor(message, statusCode = 502) {
    super(message);
    this.name = 'GitHubApiError';
    this.statusCode = statusCode;
  }
}

function getGitHubConfig() {
  const token = String(process.env.GITHUB_TOKEN || '').trim();
  const owner = String(process.env.GITHUB_OWNER || '').trim();
  const repo = String(process.env.GITHUB_REPO || '').trim();
  const productBranch = String(process.env.GITHUB_PRODUCT_BRANCH || 'aura-product').trim() || 'aura-product';
  const mainBranch = String(process.env.GITHUB_MAIN_BRANCH || 'main').trim() || 'main';
  const apiBase = String(process.env.GITHUB_API_BASE || 'https://api.github.com').trim().replace(/\/+$/, '');
  const rawBase = String(process.env.GITHUB_RAW_BASE || 'https://raw.githubusercontent.com').trim().replace(/\/+$/, '');

  const missingVars = [];
  if (!token) missingVars.push('GITHUB_TOKEN');
  if (!owner) missingVars.push('GITHUB_OWNER');
  if (!repo) missingVars.push('GITHUB_REPO');

  return {
    configured: missingVars.length === 0,
    missingVars,
    token,
    owner,
    repo,
    productBranch,
    mainBranch,
    apiBase,
    rawBase
  };
}

function assertGitHubConfigured() {
  const cfg = getGitHubConfig();
  if (!cfg.configured) {
    throw new GitHubConfigError(
      `GitHub storage is not configured. Missing: ${cfg.missingVars.join(', ')}.`
    );
  }
  return cfg;
}

function githubHeaders(cfg, extra = {}) {
  return {
    'Authorization': `Bearer ${cfg.token}`,
    'Accept': 'application/vnd.github+json',
    'User-Agent': 'AURA-Fashion-Store/3.0',
    'X-GitHub-Api-Version': '2022-11-28',
    ...extra
  };
}

function encodeRepoPath(filePath) {
  return filePath
    .split('/')
    .filter(Boolean)
    .map(segment => encodeURIComponent(segment))
    .join('/');
}

function cloneValue(val) {
  return JSON.parse(JSON.stringify(val));
}

// Per-file mutex queue + ETag cache to serialize writes and minimize API rate usage
const fileLocks = new Map();
const etagCache = new Map(); // cacheKey -> { etag, sha, data }

async function withFileLock(filePath, fn) {
  const previous = fileLocks.get(filePath) || Promise.resolve();
  let release;
  const current = new Promise(resolve => {
    release = resolve;
  });
  const chained = previous.then(() => current, () => current);
  fileLocks.set(filePath, chained);

  await previous;
  try {
    return await fn();
  } finally {
    release();
    if (fileLocks.get(filePath) === chained) {
      fileLocks.delete(filePath);
    }
  }
}

function clearStoreCache() {
  etagCache.clear();
}

async function ensureProductBranch(cfg) {
  const branchUrl = `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/branches/${encodeURIComponent(cfg.productBranch)}`;
  const checkRes = await fetch(branchUrl, {
    method: 'GET',
    headers: githubHeaders(cfg)
  });

  if (checkRes.ok) {
    return { branch: cfg.productBranch, created: false };
  }

  if (checkRes.status !== 404) {
    const errText = await checkRes.text().catch(() => '');
    throw new GitHubApiError(`Failed checking GitHub branch "${cfg.productBranch}" (${checkRes.status}): ${errText}`, checkRes.status);
  }

  // Locate base commit SHA from mainBranch or repository default_branch
  let baseBranch = cfg.mainBranch;
  let refRes = await fetch(
    `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/git/ref/heads/${encodeURIComponent(baseBranch)}`,
    { headers: githubHeaders(cfg) }
  );

  if (refRes.status === 404) {
    const repoRes = await fetch(
      `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}`,
      { headers: githubHeaders(cfg) }
    );
    if (repoRes.ok) {
      const repoJson = await repoRes.json();
      if (repoJson && repoJson.default_branch && repoJson.default_branch !== baseBranch) {
        baseBranch = repoJson.default_branch;
        refRes = await fetch(
          `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/git/ref/heads/${encodeURIComponent(baseBranch)}`,
          { headers: githubHeaders(cfg) }
        );
      }
    }
  }

  if (!refRes.ok) {
    const errText = await refRes.text().catch(() => '');
    throw new GitHubApiError(
      `Cannot create "${cfg.productBranch}" branch because base branch "${baseBranch}" was not found (${refRes.status}): ${errText}`,
      refRes.status
    );
  }

  const refJson = await refRes.json();
  const baseSha = refJson && refJson.object && refJson.object.sha;
  if (!baseSha) {
    throw new GitHubApiError(`Missing base SHA on branch "${baseBranch}"`, 502);
  }

  const createRes = await fetch(
    `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/git/refs`,
    {
      method: 'POST',
      headers: githubHeaders(cfg, { 'Content-Type': 'application/json' }),
      body: JSON.stringify({
        ref: `refs/heads/${cfg.productBranch}`,
        sha: baseSha
      })
    }
  );

  // 422 can occur if a concurrent request created the ref moments earlier
  if (!createRes.ok && createRes.status !== 422) {
    const errText = await createRes.text().catch(() => '');
    throw new GitHubApiError(`Failed creating branch "${cfg.productBranch}" (${createRes.status}): ${errText}`, createRes.status);
  }

  return { branch: cfg.productBranch, created: true };
}

async function readJsonFile(filePath, fallbackValue, { bypassCache = false } = {}) {
  const cfg = assertGitHubConfigured();
  const encodedPath = encodeRepoPath(filePath);
  const url = `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/contents/${encodedPath}?ref=${encodeURIComponent(cfg.productBranch)}`;
  const cacheKey = `${cfg.apiBase}:${cfg.owner}/${cfg.repo}@${cfg.productBranch}:${filePath}`;
  const cached = etagCache.get(cacheKey);

  const headers = githubHeaders(cfg);
  if (!bypassCache && cached && cached.etag) {
    headers['If-None-Match'] = cached.etag;
  }

  const res = await fetch(url, { method: 'GET', headers });

  if (res.status === 304 && cached) {
    return {
      data: cloneValue(cached.data),
      sha: cached.sha,
      exists: true
    };
  }

  if (res.status === 404) {
    etagCache.delete(cacheKey);
    return {
      data: cloneValue(fallbackValue),
      sha: null,
      exists: false
    };
  }

  if (!res.ok) {
    const errText = await res.text().catch(() => '');
    throw new GitHubApiError(`Failed reading "${filePath}" from "${cfg.productBranch}" (${res.status}): ${errText}`, res.status);
  }

  const payload = await res.json();
  const rawBase64 = (payload.content || '').replace(/\n/g, '');
  let parsed = cloneValue(fallbackValue);
  if (rawBase64.length > 0) {
    const decoded = Buffer.from(rawBase64, 'base64').toString('utf8').trim();
    if (decoded.length > 0) {
      parsed = JSON.parse(decoded);
    }
  }

  const etag = res.headers.get ? res.headers.get('etag') : null;
  etagCache.set(cacheKey, {
    etag: etag || null,
    sha: payload.sha,
    data: cloneValue(parsed)
  });

  return {
    data: parsed,
    sha: payload.sha,
    exists: true
  };
}

async function writeJsonFile(filePath, data, commitMessage, expectedSha = null) {
  const cfg = assertGitHubConfigured();
  await ensureProductBranch(cfg);

  const encodedPath = encodeRepoPath(filePath);
  const url = `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/contents/${encodedPath}`;
  const contentBase64 = Buffer.from(JSON.stringify(data, null, 2) + '\n', 'utf8').toString('base64');

  const body = {
    message: commitMessage,
    content: contentBase64,
    branch: cfg.productBranch
  };
  if (expectedSha) {
    body.sha = expectedSha;
  }

  const res = await fetch(url, {
    method: 'PUT',
    headers: githubHeaders(cfg, { 'Content-Type': 'application/json' }),
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    const errText = await res.text().catch(() => '');
    const err = new GitHubApiError(`Failed writing "${filePath}" to "${cfg.productBranch}" (${res.status}): ${errText}`, res.status);
    throw err;
  }

  const result = await res.json();
  const newSha = result && result.content && result.content.sha ? result.content.sha : null;
  const cacheKey = `${cfg.apiBase}:${cfg.owner}/${cfg.repo}@${cfg.productBranch}:${filePath}`;
  etagCache.set(cacheKey, {
    etag: null,
    sha: newSha,
    data: cloneValue(data)
  });

  return { sha: newSha, data: cloneValue(data) };
}

async function mutateJsonFile(filePath, fallbackValue, mutatorFn, commitMessage) {
  return withFileLock(filePath, async () => {
    const maxAttempts = 3;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      const { data: currentData, sha } = await readJsonFile(filePath, fallbackValue, { bypassCache: true });
      const outcome = await mutatorFn(cloneValue(currentData));

      if (outcome && outcome.skipWrite) {
        return outcome.result;
      }

      const nextData = outcome && Object.prototype.hasOwnProperty.call(outcome, 'data') ? outcome.data : outcome;
      const resultValue = outcome && Object.prototype.hasOwnProperty.call(outcome, 'result') ? outcome.result : nextData;
      const message = typeof commitMessage === 'function' ? commitMessage(nextData, resultValue) : commitMessage;

      try {
        await writeJsonFile(filePath, nextData, message, sha);
        return resultValue;
      } catch (err) {
        if ((err.statusCode === 409 || err.statusCode === 422) && attempt < maxAttempts) {
          continue;
        }
        throw err;
      }
    }
  });
}

async function uploadImageAsset(fileName, base64Data) {
  const cfg = assertGitHubConfigured();
  if (!fileName || !base64Data) {
    throw new GitHubApiError('fileName and base64Data are required', 400);
  }

  const cleanBase64 = String(base64Data).replace(/^data:image\/[a-zA-Z0-9.+-]+;base64,/, '').replace(/\s+/g, '');
  if (!cleanBase64) {
    throw new GitHubApiError('Invalid image payload', 400);
  }

  const fileBuffer = Buffer.from(cleanBase64, 'base64');
  if (fileBuffer.length === 0 || fileBuffer.length > 5 * 1024 * 1024) {
    throw new GitHubApiError('Image size must be between 1 byte and 5 MB', 400);
  }

  await ensureProductBranch(cfg);

  const timestamp = Date.now();
  const sanitizedName = pathSanitizeFileName(fileName);
  const assetPath = `uploads/${timestamp}-${sanitizedName}`;
  const encodedPath = encodeRepoPath(assetPath);
  const url = `${cfg.apiBase}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/contents/${encodedPath}`;

  const res = await fetch(url, {
    method: 'PUT',
    headers: githubHeaders(cfg, { 'Content-Type': 'application/json' }),
    body: JSON.stringify({
      message: `Upload product image ${sanitizedName} via AURA Admin`,
      content: cleanBase64,
      branch: cfg.productBranch
    })
  });

  if (!res.ok) {
    const errText = await res.text().catch(() => '');
    throw new GitHubApiError(`GitHub image upload failed (${res.status}): ${errText}`, res.status);
  }

  const rawUrl = `${cfg.rawBase}/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/${encodeURIComponent(cfg.productBranch)}/${assetPath}`;
  return {
    success: true,
    url: rawUrl,
    path: assetPath,
    branch: cfg.productBranch,
    storage: 'github-aura-product'
  };
}

function pathSanitizeFileName(name) {
  const base = String(name || 'asset.jpg').split(/[\\/]/).pop();
  const cleaned = base.replace(/[^a-zA-Z0-9._-]/g, '_').replace(/^_+/, '');
  return cleaned || `asset_${Date.now()}.jpg`;
}

module.exports = {
  FILE_PATHS,
  DEFAULT_SETTINGS,
  GitHubConfigError,
  GitHubApiError,
  getGitHubConfig,
  assertGitHubConfigured,
  ensureProductBranch,
  readJsonFile,
  writeJsonFile,
  mutateJsonFile,
  uploadImageAsset,
  clearStoreCache
};
