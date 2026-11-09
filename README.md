# AURA FASHION — PRODUCTION DEPLOYMENT & OPERATION GUIDE

A complete, production-ready luxury fashion e-commerce application powered by **Supabase PostgreSQL & Auth**, a secure **Node.js/Express** backend deployed on **Render**, persistent public image hosting via GitHub **`aura-data`**, and an architectural **Grid Studio** Admin Dashboard.

---

## 1. System Overview

- **Customer Website (`index.html`)**: Fully preserved original AURA visual identity, typography (Plus Jakarta Sans), product catalog (`aura-p1` to `aura-p6`), hero banners, size guide viewer, recently viewed history, and client checkout proxy. Zero admin controls or testing bloat.
- **Admin Dashboard (`admin.html`)**: Separate management portal featuring a CSS-generated architectural drafting paper background (36px × 36px grid, #FAFAF8 base, #D6D6D2 lines, #C6A45C gold accents) and four selectable themes (Grid Studio, Dark Luxury, Clean White, Warm Neutral) stored in `localStorage`.
- **Backend API (`server.js`)**: Dynamic port resolution (`process.env.PORT || 3000`), timing-safe password authentication (`AURA_ADMIN_PASSWORD`), encrypted HttpOnly session cookies, abuse-guarded checkout proxy, and GitHub `aura-data` asset commit engine.
- **Database Engine (`schema.sql`)**: PostgreSQL schema with Row Level Security (RLS), sequence-backed collision-safe order IDs (`AURA-YYMM-10001`+), payload fingerprint idempotency, deadlock-free inventory locking, and atomic cancellation restocking.

---

## 2. Supabase Setup & SQL Migrations

1. Open your project on [Supabase](https://supabase.com).
2. Go to the **SQL Editor** in the left sidebar.
3. Open `schema.sql` from this repository, copy its entire contents, paste them into the SQL Editor, and click **Run**.
4. Confirm in **Table Editor** that the following tables and views are active:
   - `profiles` (with automated trigger on `auth.users`)
   - `products` (seeded with `aura-p1` to `aura-p6`)
   - `product_inventory` (tracked size-wise)
   - `orders` & `order_items`
   - `addresses`
   - `recently_viewed` (with automated 20-item pruning trigger)
   - `wishlists`
   - `coupons`
   - `storefront_settings` & `v_public_storefront_settings`
   - `admin_sessions` & `admin_activity`
5. Copy your **Project URL**, **anon/public key**, and **service_role key** from **Settings → API**.

---

## 3. Render Deployment Setup

1. Push this repository to GitHub (branch `main`).
2. Log in to [Render](https://render.com) and click **New + → Web Service**.
3. Select your repository. Render will automatically detect `render.yaml` or you can manually configure:
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Health Check Path**: `/health`
4. In the **Environment Variables** tab, add:
   - `NODE_ENV`: `production`
   - `AURA_ADMIN_PASSWORD`: Your secret administrator password (e.g. `your_secret_pass_2026`)
   - `SESSION_SECRET`: A 32+ character random string for cookie signing
   - `SUPABASE_URL`: `https://your-project.supabase.co`
   - `SUPABASE_ANON_KEY`: Your Supabase anon key
   - `SUPABASE_SERVICE_ROLE_KEY`: Your Supabase service role key
   - `GITHUB_TOKEN`: Fine-grained personal access token with "Contents: Read & Write"
   - `GITHUB_OWNER`: Your GitHub username or organization
   - `GITHUB_REPO`: Your repository name
   - `GITHUB_MAIN_BRANCH`: `main`
   - `GITHUB_DATA_BRANCH`: `aura-data`
5. Click **Deploy Web Service**.

---

## 4. GitHub Image Pipeline (`aura-data`)

1. Public images uploaded in `admin.html` post to `/api/admin/upload-image`.
2. The server verifies if the `aura-data` branch exists. If missing, it branches automatically from `main`.
3. The image is committed as a permanent file under `public/uploads/{timestamp}-{filename}`.
4. The server returns the raw GitHub CDN URL (`https://raw.githubusercontent.com/...`).
5. If GitHub credentials are not supplied, it seamlessly falls back to Supabase Storage (`aura-assets` bucket).

---

## 5. Automated Verification & Testing

Run the test suite anytime:
```bash
npm test
```
Validates all 13 production scenarios:
- Cross-user idempotency key reuse rejection
- Payload fingerprint alteration rejection
- Order IDs > 10,000 uniqueness
- Invalid sizes/colors rejection
- Line quantity (1..10) and size-aggregated (max 20) bounds
- Missing settings fallback to ৳100 / ৳3000
- Courier and payment method validation
- Delivered order cancellation protection
- Missing inventory cancellation protection
- Idempotent repeated cancellation
- Public view policy protection
- Profile column-level security
- Recently viewed concurrency-safe 20-item pruning
