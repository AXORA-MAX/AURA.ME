# AURA FASHION — LIVE GITHUB STORAGE & RENDER DEPLOYMENT GUIDE

A full-stack luxury streetwear e-commerce platform powered by a **Node.js** backend (`server.js`), live **GitHub REST API** persistence targeting the **`aura-product`** branch (`products.json`, `orders.json`, `settings.json`, `size-guides.json`, and `uploads/*`), a live Customer Storefront (`index.html`), and an architectural **Grid Studio** Admin Dashboard (`admin.html`).

---

## 1. System Architecture

- **Customer Storefront (`index.html`)**: Live product catalog, size/color stock availability, wishlist, shopping bag, live guest/customer checkout (`/api/checkout`), real-time order tracking (`/api/orders/lookup`), and **Profile → Recently Viewed Items** with configurable auto-delete retention (default **30 days**, customizable by both customer and admin).
- **Admin Studio (`admin.html`)**: Separate management portal featuring a Grid Studio design and four selectable themes stored in `localStorage`. Supports live product CRUD (Add/Edit/Publish/Delete), direct image upload to GitHub (`aura-product` branch), live order management & atomic cancellation restocking, live customer aggregates, Size Guide Studio, and Storefront CMS settings.
- **GitHub Storage Engine (`lib/github-store.js`)**:
  - Automatically creates the `aura-product` branch from `main` (or repository `default_branch`) if it does not yet exist.
  - Persists catalog (`products.json`), orders (`orders.json`), settings (`settings.json`), size guides (`size-guides.json`), and uploaded product images (`uploads/{timestamp}-{filename}`).
  - Uses per-file mutex locking, ETag conditional reads, and SHA optimistic concurrency retries (`409`/`422`) to prevent lost updates.
- **Transactional Store Service (`lib/store-service.js`)**:
  - Zero hardcoded demo products, dummy orders, or mock coupons.
  - Validates inventory by `(product_id, size)`, deduplicates checkout submissions via idempotency key + SHA-256 payload fingerprint, generates collision-safe sequential order IDs (`AURA-YYMM-10001`+), and restores inventory atomically when an eligible order is cancelled.

---

## 2. Render Deployment Setup

1. Push this repository to GitHub (branch `main`).
2. Log in to [Render](https://render.com) and click **New + → Web Service**.
3. Select your repository. Render detects `render.yaml`, or you can configure:
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Health Check Path**: `/health`
4. Set the following **Environment Variables** in Render:
   - `NODE_ENV`: `production`
   - `AURA-admin` (or `AURA_ADMIN_PASSWORD`, `AURA_ADMIN`, `ADMIN_PASSWORD`): Your secret admin password (read dynamically from `process.env` at request time; never hardcoded)
   - `SESSION_SECRET`: Random 32+ character string for HttpOnly cookie signing
   - `GITHUB_TOKEN`: Fine-grained GitHub Personal Access Token with **Contents: Read and Write** permission on your repository
   - `GITHUB_OWNER`: Your GitHub username or organization
   - `GITHUB_REPO`: Your GitHub repository name
   - `GITHUB_MAIN_BRANCH`: `main`
   - `GITHUB_PRODUCT_BRANCH`: `aura-product`
5. Click **Deploy Web Service**.

---

## 3. GitHub `aura-product` Data & Image Pipeline

- When an admin uploads an image in `admin.html` (`POST /api/admin/upload-image`), the server commits the file to `uploads/{timestamp}-{filename}` on the `aura-product` branch and returns its raw GitHub CDN URL (`https://raw.githubusercontent.com/{owner}/{repo}/aura-product/uploads/...`).
- Creating, editing, publishing, or deleting products in `admin.html` writes directly to `products.json` on `aura-product` and immediately updates `GET /api/storefront` and `index.html`.
- Orders placed on `index.html` deduct inventory in `products.json` and append to `orders.json` on `aura-product`, appearing live in the Admin Studio Orders ledger.

---

## 4. Profile & Recently Viewed Items Auto-Delete

- Clicking the **Profile** icon (`#account`) opens the Profile view, defaulting to **Recently Viewed Items**.
- Every product detail view visited by the shopper is recorded with a timestamp.
- Items older than the retention period (default **30 days**, configurable by the admin in CMS Settings and overridable per user right inside the Profile Recently Viewed header) are automatically pruned.

---

## 5. Automated Verification

Run the end-to-end production test suite:
```bash
npm test
```
Uses a local HTTP mock GitHub REST API server to test `server.js`, `lib/github-store.js`, `lib/store-service.js`, dynamic admin authentication (`AURA-admin`), `aura-product` branch creation, image upload, product CRUD + live storefront sync, checkout stock deduction & idempotency, cancellation restocking, and recently viewed auto-delete retention logic.
