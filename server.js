const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff2': 'font/woff2',
  '.woff': 'font/woff',
  '.ttf': 'font/ttf'
};

const server = http.createServer((req, res) => {
  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  // Normalize path
  let safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  let filePath = path.join(PUBLIC_DIR, safePath);

  // Check if target file exists in public/
  fs.stat(filePath, (err, stats) => {
    if (!err && stats.isFile()) {
      const ext = path.extname(filePath).toLowerCase();
      const contentType = MIME_TYPES[ext] || 'application/octet-stream';
      res.writeHead(200, { 'Content-Type': contentType });
      fs.createReadStream(filePath).pipe(res);
      return;
    }

    // Check root directory for root-level files (e.g. index.html)
    const rootFilePath = path.join(__dirname, safePath);
    fs.stat(rootFilePath, (err2, stats2) => {
      if (!err2 && stats2.isFile()) {
        const ext = path.extname(rootFilePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || 'application/octet-stream';
        res.writeHead(200, { 'Content-Type': contentType });
        fs.createReadStream(rootFilePath).pipe(res);
        return;
      }

      // Check if file is in public/assets
      if (pathname.startsWith('/assets/')) {
        const assetPath = path.join(PUBLIC_DIR, pathname);
        if (fs.existsSync(assetPath)) {
          const ext = path.extname(assetPath).toLowerCase();
          res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'image/jpeg' });
          fs.createReadStream(assetPath).pipe(res);
          return;
        }
      }

      // Default SPA fallback: serve public/index.html or /index.html
      const publicIndex = path.join(PUBLIC_DIR, 'index.html');
      const rootIndex = path.join(__dirname, 'index.html');
      const targetIndex = fs.existsSync(publicIndex) ? publicIndex : rootIndex;

      if (fs.existsSync(targetIndex)) {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        fs.createReadStream(targetIndex).pipe(res);
      } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('AURA Website - index.html not found');
      }
    });
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`AURA Web Server running on port ${PORT}`);
});
