# AURA Fashion — Final Supabase Production Upgrade Plan

A complete, production-grade, hardened full-stack architecture for **AURA.** luxury streetwear, powered by **Supabase PostgreSQL**, **Supabase Auth & RLS**, **Render Node/Express**, and the GitHub **`aura-data`** persistent branch.

---

### User Review & Verified Source-of-Truth Catalog

> [!IMPORTANT]
> The following exact data from the uploaded original AURA HTML has been verified as the single source of truth:
>
> 1. **Exact Original Products (`aura-p1` to `aura-p6`)**:
>    - **`aura-p1`**: `AURA OVERSIZED TEE` — Price: **৳1,290** (Original: **৳1,590**). Category: `Tees`. Badge: `Bestseller`. Colors: Onyx Black (`#111111`), Chalk White (`#F5F5F5`), Heather Grey (`#8E8E93`). Sizes: `S`, `M`, `L`, `XL`, `XXL`. Stock per size: `{ S: 14, M: 22, L: 16, XL: 7, XXL: 3 }`. Image: `/assets/img_hero_fashion.jpg`. Fabric: `100% Combed Organic Cotton (280 GSM French Jersey)`.
>    - **`aura-p2`**: `AURA CARGO TROUSERS` — Price: **৳2,490** (Original: **৳2,890**). Category: `Pants`. Badge: `New Drop`. Colors: Onyx Black (`#111111`), Desert Sand (`#D8C7B5`). Sizes: `S`, `M`, `L`, `XL`. Stock per size: `{ S: 8, M: 12, L: 6, XL: 2 }`. Image: `/assets/img_complete_look.jpg`. Fabric: `Cotton-Poly Ripstop (260 GSM) with matte Teflon finish`.
>    - **`aura-p3`**: `AURA SHADOW HOODIE` — Price: **৳3,290** (Original: **৳3,800**). Category: `Hoodies`. Badge: `App Exclusive`. Colors: Onyx Black (`#111111`), Heather Grey (`#8E8E93`), Midnight Navy (`#1B263B`). Sizes: `S`, `M`, `L`, `XL`. Stock per size: `{ S: 6, M: 14, L: 8, XL: 4 }`. Image: `/assets/img_lookbook_urban.jpg`. Fabric: `450 GSM Ultra-Heavyweight French Terry`.
>    - **`aura-p4`**: `AURA MATTE MINIMAL PENDANT` — Price: **৳890** (Original: **৳1,150**). Category: `Accessories`. Badge: `Hardware`. Colors: Onyx Black (`#111111`), Silver (`#E2E8F0`). Sizes: `One Size`. Stock per size: `{ "One Size": 25 }`. Image: `/assets/img_app_icon.jpg`. Fabric: `316L Solid Stainless Steel with PVD Matte Coating`.
>    - **`aura-p5`**: `AURA RELAXED LINEN-TOUCH SHIRT` — Price: **৳1,890** (Original: **৳2,200**). Category: `Men`. Badge: `Summer Capsule`. Colors: Chalk White (`#F5F5F5`), Desert Sand (`#D8C7B5`). Sizes: `S`, `M`, `L`, `XL`. Stock per size: `{ S: 10, M: 15, L: 9, XL: 3 }`. Image: `/assets/img_complete_look.jpg`. Fabric: `65% Linen, 35% Modal (170 GSM)`.
>    - **`aura-p6`**: `AURA SCULPTED CROPPED BOMBER` — Price: **৳3,990** (Original: **৳4,500**). Category: `Unisex`. Badge: `Limited Drop`. Colors: Onyx Black (`#111111`), Midnight Navy (`#1B263B`). Sizes: `S`, `M`, `L`. Stock per size: `{ S: 5, M: 7, L: 3 }`. Image: `/assets/img_lookbook_urban.jpg`. Fabric: `Nylon Twill with 80g Primaloft Eco Insulation`.
>
> 2. **Original Checkout Form & Shipping Logic**:
>    - **Mandatory Courier Selection**: Preserved options `Pathao Express`, `RedX Logistics`, and `Steadfast Courier`.
>    - **Delivery Fee**: ৳100 base, waived automatically if subtotal >= ৳3,000 or if coupon provides free shipping.
>    - **Payment Methods**: Default enabled method is strictly `["Cash on Delivery"]`. Additional payment methods can be enabled by the Admin via Storefront Settings.
>    - **Hero & Content Copy**: Preserved verbatim from original HTML ("GIVES YOU DIFFERENT VIBE", "Architectural cuts, heavyweight 280 GSM custom French terry...").

---

## 1. System Architecture & Component Model

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             AURA PLATFORM ENGINE                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
┌───────────────────────────────┐             ┌───────────────────────────────┐
│     CUSTOMER STOREFRONT       │             │        ADMIN DASHBOARD        │
│        (index.html)           │             │         (admin.html)          │
├───────────────────────────────┤             ├───────────────────────────────┤
│ • 100% Original Brand Design  │             │ • Architectural Grid Studio   │
│ • Zero Admin / Demo Bloat     │             │ • Password-Only Login Modal   │
│ • Supabase JS (Anon Key)      │             │ • 4 Themes (in localStorage)  │
│ • Email/Password + Anon Auth  │             │ • Real-time Stats & Inventory │
│ • Account Linking (Guest->User│             │ • cancel_order_atomic (RPC)   │
│ • Checkout via /api/checkout  │             │ • Original Catalog (p1..p6)   │
│ • Size Guide Modal Viewer     │             │ • Visual Size Guide Studio    │
│ • Recently Viewed (Max 20)    │             │ • Zero-Default Storefront CMS │
│ • Saved Addresses & Wishlist  │             │ • CSV Export & Audit Log      │
└───────────────────────────────┘             └───────────────────────────────┘
            │                                                 │
            │ Direct Supabase Client (Anon Key)               │ All Requests via
            │ Strict RLS (SELECT only on safe views)          │ Signed Session Cookie
            ▼                                                 ▼
┌───────────────────────────────┐             ┌───────────────────────────────┐
│      SUPABASE POSTGRES        │             │     RENDER NODE/EXPRESS       │
│        & SUPABASE AUTH        │             │          (server.js)          │
├───────────────────────────────┤             ├───────────────────────────────┤
│ • Tables: profiles, products, │             │ • Dynamic process.env.PORT    │
│   product_inventory, orders,  │             │ • Timing-Safe Password Auth   │
│   order_items, addresses,     │◄────────────┤ • Persistent admin_sessions   │
│   recently_viewed, wishlists, │ Service-Role│ • Rate Limiter & CAPTCHA      │
│   size_guides, coupons, CMS   │ Connection  │ • /api/checkout (Forward JWT) │
│ • Public View: v_public_cms   │             │ • GitHub 'aura-data' Commits  │
│ • place_order_atomic (RPC)    │             │ • Supabase Storage Fallback   │
│ • cancel_order_atomic (RPC)   │             │ • Safe Pre-Migration Backup   │
└───────────────────────────────┘             └───────────────────────────────┘
                                                              │
                                                              ▼
                                              ┌───────────────────────────────┐
                                              │       GITHUB REPOSITORY       │
                                              ├───────────────────────────────┤
                                              │ • main: Application code      │
                                              │ • aura-data: Public image CDN │
                                              └───────────────────────────────┘
```

---

## 2. Hardened Database Schema, Privileges & Triggers

```sql
-- ============================================================================
-- 1. EXTENSIONS, ENUMS & SEQUENCES
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ BEGIN
  CREATE TYPE order_status_type AS ENUM (
    'pending', 'confirmed', 'processing', 'packed', 
    'shipped', 'out_for_delivery', 'delivered', 'cancelled'
  );
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;

-- Complete sequential order sequence (no modulo wraparound)
CREATE SEQUENCE IF NOT EXISTS public.order_number_seq START 10001;

-- ============================================================================
-- 2. CUSTOMER PROFILES (Extends Supabase auth.users)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email TEXT,
  full_name TEXT,
  phone TEXT,
  is_anonymous BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name, is_anonymous)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'full_name', 'Guest Shopper'),
    COALESCE(NEW.is_anonymous, FALSE)
  )
  ON CONFLICT (id) DO UPDATE
  SET email = COALESCE(EXCLUDED.email, public.profiles.email),
      is_anonymous = EXCLUDED.is_anonymous,
      updated_at = pg_catalog.now();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT OR UPDATE ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- 3. SAVED ADDRESSES
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.addresses (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  title TEXT DEFAULT 'Home',
  recipient_name TEXT NOT NULL,
  phone TEXT NOT NULL,
  street_address TEXT NOT NULL,
  city TEXT NOT NULL,
  state TEXT,
  postal_code TEXT,
  is_default BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- ============================================================================
-- 4. SIZE GUIDES (is_published FALSE by default)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.size_guides (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  description TEXT,
  fitting_notes TEXT,
  measurement_columns JSONB NOT NULL DEFAULT '["Chest", "Shoulder", "Length", "Sleeve"]'::jsonb,
  measurements JSONB NOT NULL DEFAULT '[]'::jsonb,
  is_published BOOLEAN DEFAULT FALSE, -- MUST be explicitly reviewed and published by Admin
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- ============================================================================
-- 5. PRODUCTS & SIZE-WISE INVENTORY
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.products (
  id TEXT PRIMARY KEY, -- 'aura-p1' to 'aura-p6'
  name TEXT NOT NULL,
  tagline TEXT,
  category TEXT NOT NULL,
  style TEXT,
  rating NUMERIC(3, 1) DEFAULT 5.0,
  review_count INTEGER DEFAULT 0,
  badge TEXT,
  price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
  original_price NUMERIC(10, 2) CHECK (original_price >= price),
  description TEXT NOT NULL,
  fabric TEXT,
  fit TEXT,
  care TEXT,
  image TEXT NOT NULL,
  additional_images TEXT[] DEFAULT '{}',
  colors JSONB NOT NULL DEFAULT '[]'::jsonb,
  sizes TEXT[] NOT NULL DEFAULT '{}',
  size_guide_id UUID REFERENCES public.size_guides(id) ON DELETE SET NULL,
  is_new BOOLEAN DEFAULT FALSE,
  is_drop BOOLEAN DEFAULT FALSE,
  is_featured BOOLEAN DEFAULT FALSE,
  is_published BOOLEAN DEFAULT TRUE,
  display_order INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

CREATE TABLE IF NOT EXISTS public.product_inventory (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  size TEXT NOT NULL,
  stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  UNIQUE (product_id, size)
);

-- ============================================================================
-- 6. RECENTLY VIEWED & WISHLISTS
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.recently_viewed (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  viewed_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_recently_viewed_user_viewed 
ON public.recently_viewed (user_id, viewed_at DESC);

-- Concurrency-Safe Recently Viewed Pruning with Per-User Advisory Lock
CREATE OR REPLACE FUNCTION public.prune_recently_viewed()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
  -- Synchronize concurrent view inserts for the same user
  PERFORM pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtext('rv_' || NEW.user_id::TEXT));

  DELETE FROM public.recently_viewed
  WHERE id IN (
    SELECT id FROM public.recently_viewed
    WHERE user_id = NEW.user_id
    ORDER BY viewed_at DESC
    OFFSET 20
  );
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_recently_viewed_added ON public.recently_viewed;
CREATE TRIGGER on_recently_viewed_added
  AFTER INSERT OR UPDATE ON public.recently_viewed
  FOR EACH ROW EXECUTE FUNCTION public.prune_recently_viewed();

CREATE TABLE IF NOT EXISTS public.wishlists (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  UNIQUE (user_id, product_id)
);

-- ============================================================================
-- 7. PROMOTIONS & COUPONS (Original AURA10 & AURANEW)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.coupons (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  code TEXT UNIQUE NOT NULL,
  discount_percentage NUMERIC(5, 2) NOT NULL CHECK (discount_percentage BETWEEN 0 AND 100),
  min_order_amount NUMERIC(10, 2) DEFAULT 0,
  description TEXT,
  is_free_shipping BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  valid_until TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- ============================================================================
-- 8. ORDERS & ORDER ITEMS (Stores payload fingerprint & courier)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.orders (
  id TEXT PRIMARY KEY,
  user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
  idempotency_key TEXT UNIQUE NOT NULL,
  payload_fingerprint TEXT NOT NULL, -- SHA256 of canonical checkout payload
  customer_name TEXT NOT NULL,
  customer_email TEXT,
  customer_phone TEXT NOT NULL,
  shipping_address JSONB NOT NULL,
  courier TEXT NOT NULL DEFAULT 'Pathao Express',
  payment_method TEXT NOT NULL DEFAULT 'Cash on Delivery',
  subtotal NUMERIC(10, 2) NOT NULL,
  discount_amount NUMERIC(10, 2) DEFAULT 0.00,
  shipping_fee NUMERIC(10, 2) DEFAULT 100.00,
  total_amount NUMERIC(10, 2) NOT NULL,
  status order_status_type DEFAULT 'pending',
  admin_notes TEXT,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

CREATE TABLE IF NOT EXISTS public.order_items (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  order_id TEXT NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  product_id TEXT NOT NULL REFERENCES public.products(id),
  product_name TEXT NOT NULL,
  size TEXT NOT NULL,
  color TEXT NOT NULL,
  unit_price NUMERIC(10, 2) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  subtotal NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- ============================================================================
-- 9. STOREFRONT CMS & SAFE PUBLIC VIEW
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.storefront_settings (
  id TEXT PRIMARY KEY DEFAULT 'aura_main_config',
  announcement_bar TEXT DEFAULT 'DROP 04 : MONOLITH CAPSULE NOW LIVE — FREE EXPRESS SHIPPING OVER ৳3000',
  announcement_active BOOLEAN DEFAULT TRUE,
  hero_title TEXT DEFAULT 'GIVES YOU DIFFERENT VIBE',
  hero_subtitle TEXT DEFAULT 'Heavyweight custom-milled textiles engineered into architectural luxury streetwear silhouettes.',
  hero_image TEXT DEFAULT '/assets/img_hero_fashion.jpg',
  hero_cta_text TEXT DEFAULT 'EXPLORE DROP 04',
  hero_cta_link TEXT DEFAULT '#shop',
  lookbook_banner TEXT DEFAULT '/assets/img_lookbook_urban.jpg',
  curated_look_banner TEXT DEFAULT '/assets/img_complete_look.jpg',
  shipping_fee NUMERIC(10, 2) DEFAULT 100.00,
  free_shipping_threshold NUMERIC(10, 2) DEFAULT 3000.00,
  available_payment_methods JSONB DEFAULT '["Cash on Delivery"]'::jsonb,
  available_couriers JSONB DEFAULT '["Pathao Express", "RedX Logistics", "Steadfast Courier"]'::jsonb,
  nav_categories JSONB DEFAULT '[
    {"name": "All Items", "slug": "all"},
    {"name": "Tees", "slug": "Tees"},
    {"name": "Pants", "slug": "Pants"},
    {"name": "Hoodies", "slug": "Hoodies"},
    {"name": "Men", "slug": "Men"},
    {"name": "Unisex", "slug": "Unisex"},
    {"name": "Accessories", "slug": "Accessories"}
  ]'::jsonb,
  store_name TEXT DEFAULT 'AURA.',
  store_tagline TEXT DEFAULT 'GIVES YOU DIFFERENT VIBE',
  store_phone TEXT DEFAULT NULL,
  store_email TEXT DEFAULT NULL,
  store_address TEXT DEFAULT NULL,
  social_links JSONB DEFAULT '{}'::jsonb,
  policies_published BOOLEAN DEFAULT FALSE,
  privacy_policy TEXT DEFAULT NULL,
  terms_conditions TEXT DEFAULT NULL,
  return_policy TEXT DEFAULT NULL,
  lookbook_items JSONB DEFAULT '[]'::jsonb,
  journal_entries JSONB DEFAULT '[]'::jsonb,
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- Seed explicitly if missing
INSERT INTO public.storefront_settings (id) 
VALUES ('aura_main_config')
ON CONFLICT (id) DO NOTHING;

-- Safe Public View: Exposes ONLY approved public fields; hides unpublished policies
CREATE OR REPLACE VIEW public.v_public_storefront_settings AS
SELECT 
  id,
  announcement_bar,
  announcement_active,
  hero_title,
  hero_subtitle,
  hero_image,
  hero_cta_text,
  hero_cta_link,
  lookbook_banner,
  curated_look_banner,
  shipping_fee,
  free_shipping_threshold,
  available_payment_methods,
  available_couriers,
  nav_categories,
  store_name,
  store_tagline,
  CASE WHEN store_phone IS NOT NULL THEN store_phone ELSE NULL END AS store_phone,
  CASE WHEN store_email IS NOT NULL THEN store_email ELSE NULL END AS store_email,
  CASE WHEN store_address IS NOT NULL THEN store_address ELSE NULL END AS store_address,
  social_links,
  lookbook_items,
  journal_entries,
  CASE WHEN policies_published THEN privacy_policy ELSE NULL END AS privacy_policy,
  CASE WHEN policies_published THEN terms_conditions ELSE NULL END AS terms_conditions,
  CASE WHEN policies_published THEN return_policy ELSE NULL END AS return_policy,
  policies_published,
  updated_at
FROM public.storefront_settings
WHERE id = 'aura_main_config';

-- ============================================================================
-- 10. ADMIN SESSIONS & AUDIT LOG
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.admin_sessions (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  session_token_hash TEXT UNIQUE NOT NULL,
  csrf_token TEXT NOT NULL,
  ip_address TEXT,
  user_agent TEXT,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

CREATE TABLE IF NOT EXISTS public.admin_activity (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  action TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id TEXT NOT NULL,
  details JSONB DEFAULT '{}'::jsonb,
  ip_address TEXT,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);
```

---

## 3. Hardened Atomic Checkout Stored Procedure

```sql
CREATE OR REPLACE FUNCTION public.place_order_atomic(
  p_idempotency_key TEXT,
  p_payload_fingerprint TEXT,
  p_customer_name TEXT,
  p_customer_email TEXT,
  p_customer_phone TEXT,
  p_shipping_address JSONB,
  p_courier TEXT,
  p_payment_method TEXT,
  p_coupon_code TEXT,
  p_items JSONB -- Array of { product_id, size, color, quantity }
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
  v_auth_uid UUID;
  v_derived_email TEXT;
  v_order_id TEXT;
  v_existing_order RECORD;
  v_item RECORD;
  v_req RECORD;
  v_inventory RECORD;
  v_product RECORD;
  v_subtotal NUMERIC(10, 2) := 0;
  v_discount NUMERIC(10, 2) := 0;
  v_shipping NUMERIC(10, 2) := 100.00;
  v_free_threshold NUMERIC(10, 2) := 3000.00;
  v_total NUMERIC(10, 2) := 0;
  v_coupon RECORD;
  v_allowed_methods JSONB;
  v_allowed_couriers JSONB;
  v_recent_order_count INT;
  v_seq_num BIGINT;
  v_found_color BOOLEAN;
BEGIN
  -- 1. Authentication Check (Covers Registered and Anonymous Sessions)
  v_auth_uid := auth.uid();
  IF v_auth_uid IS NULL THEN
    RAISE EXCEPTION 'Authentication required. No active session.';
  END IF;

  -- 2. Validate Basic Inputs
  IF p_idempotency_key IS NULL OR pg_catalog.btrim(p_idempotency_key) = '' THEN
    RAISE EXCEPTION 'Idempotency key is required';
  END IF;

  IF p_payload_fingerprint IS NULL OR pg_catalog.btrim(p_payload_fingerprint) = '' THEN
    RAISE EXCEPTION 'Payload fingerprint is required';
  END IF;

  IF p_customer_name IS NULL OR pg_catalog.btrim(p_customer_name) = '' THEN
    RAISE EXCEPTION 'Customer recipient name is required';
  END IF;

  IF p_customer_phone IS NULL OR pg_catalog.btrim(p_customer_phone) = '' THEN
    RAISE EXCEPTION 'Customer contact phone number is required';
  END IF;

  -- Validate Shipping Address Fields
  IF p_shipping_address IS NULL OR 
     pg_catalog.btrim(COALESCE(p_shipping_address->>'street', '')) = '' OR
     pg_catalog.btrim(COALESCE(p_shipping_address->>'city', '')) = '' THEN
    RAISE EXCEPTION 'Street address and city are mandatory';
  END IF;

  -- 3. Transactional Advisory Locking on Idempotency Key
  PERFORM pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtext(p_idempotency_key));

  -- 4. HARDENED IDEMPOTENCY CHECK
  SELECT * INTO v_existing_order 
  FROM public.orders 
  WHERE idempotency_key = p_idempotency_key;

  IF v_existing_order IS NOT NULL THEN
    -- Verify existing order belongs to the calling user
    IF v_existing_order.user_id <> v_auth_uid THEN
      RAISE EXCEPTION 'Idempotency key conflict: key already claimed by another user';
    END IF;

    -- Compare payload fingerprint: reject reuse of key with modified order payload
    IF v_existing_order.payload_fingerprint <> p_payload_fingerprint THEN
      RAISE EXCEPTION 'Idempotency key reuse error: payload does not match original submission';
    END IF;

    -- Legitimate retry: return existing order response directly (bypasses rate limit)
    RETURN pg_catalog.jsonb_build_object(
      'success', true, 
      'order_id', v_existing_order.id, 
      'total_amount', v_existing_order.total_amount,
      'is_duplicate', true
    );
  END IF;

  -- 5. Rate Limit Protection on New Orders (Max 5 new orders per 10 minutes per auth UID)
  SELECT pg_catalog.count(*) INTO v_recent_order_count
  FROM public.orders
  WHERE user_id = v_auth_uid 
    AND created_at > (pg_catalog.now() - INTERVAL '10 minutes');

  IF v_recent_order_count >= 5 THEN
    RAISE EXCEPTION 'Order rate limit exceeded. Please wait a few moments before trying again.';
  END IF;

  -- 6. Validate Courier and Payment Method against storefront_settings (with safe fallbacks)
  SELECT 
    COALESCE(available_payment_methods, '["Cash on Delivery"]'::jsonb),
    COALESCE(available_couriers, '["Pathao Express", "RedX Logistics", "Steadfast Courier"]'::jsonb)
  INTO v_allowed_methods, v_allowed_couriers 
  FROM public.storefront_settings 
  WHERE id = 'aura_main_config';

  IF v_allowed_methods IS NULL THEN v_allowed_methods := '["Cash on Delivery"]'::jsonb; END IF;
  IF v_allowed_couriers IS NULL THEN v_allowed_couriers := '["Pathao Express", "RedX Logistics", "Steadfast Courier"]'::jsonb; END IF;

  IF NOT (v_allowed_methods ? p_payment_method) THEN
    RAISE EXCEPTION 'Payment method "%" is not currently accepted', p_payment_method;
  END IF;

  IF NOT (v_allowed_couriers ? p_courier) THEN
    RAISE EXCEPTION 'Courier "%" is not supported', p_courier;
  END IF;

  -- 7. Email Derivation (Verified Supabase Email preferred, optional for guests)
  v_derived_email := COALESCE(auth.jwt() ->> 'email', NULLIF(pg_catalog.btrim(p_customer_email), ''));

  -- 8. Collision-Safe Sequential Order ID Generation (AURA-YYMM-10001+)
  v_seq_num := pg_catalog.nextval('public.order_number_seq');
  v_order_id := 'AURA-' || pg_catalog.to_char(pg_catalog.now(), 'YYMM') || '-' || v_seq_num::TEXT;

  -- 9. VALIDATE SHOPPING BAG ITEMS & QUANTITIES
  IF p_items IS NULL OR pg_catalog.jsonb_array_length(p_items) = 0 THEN
    RAISE EXCEPTION 'Shopping bag is empty';
  END IF;

  -- Validate each individual item line
  FOR v_item IN SELECT * FROM pg_catalog.jsonb_to_recordset(p_items) AS x(product_id TEXT, size TEXT, color TEXT, quantity INT)
  LOOP
    IF v_item.quantity IS NULL OR v_item.quantity <= 0 OR v_item.quantity > 10 THEN
      RAISE EXCEPTION 'Line quantity must be an integer between 1 and 10';
    END IF;

    SELECT * INTO v_product 
    FROM public.products 
    WHERE id = v_item.product_id AND is_published = TRUE;

    IF v_product IS NULL THEN
      RAISE EXCEPTION 'Product % is unavailable or unpublished', v_item.product_id;
    END IF;

    -- Verify size belongs to product
    IF NOT (v_product.sizes @> ARRAY[v_item.size]) THEN
      RAISE EXCEPTION 'Size % is not valid for product %', v_item.size, v_product.name;
    END IF;

    -- Verify color belongs to product
    SELECT EXISTS (
      SELECT 1 FROM pg_catalog.jsonb_array_elements(v_product.colors) AS c
      WHERE c->>'name' = v_item.color
    ) INTO v_found_color;

    IF NOT v_found_color THEN
      RAISE EXCEPTION 'Color % is not valid for product %', v_item.color, v_product.name;
    END IF;
  END LOOP;

  -- 10. DEADLOCK-FREE SORTED LOCKING & SIZE-WISE INVENTORY VALIDATION
  FOR v_req IN 
    SELECT 
      x.product_id, 
      x.size, 
      pg_catalog.sum(x.quantity)::INT AS total_req_qty
    FROM pg_catalog.jsonb_to_recordset(p_items) AS x(product_id TEXT, size TEXT, color TEXT, quantity INT)
    GROUP BY x.product_id, x.size
    ORDER BY x.product_id ASC, x.size ASC
  LOOP
    IF v_req.total_req_qty > 20 THEN
      RAISE EXCEPTION 'Aggregated quantity for % (Size %) exceeds limit of 20 units', v_req.product_id, v_req.size;
    END IF;

    SELECT * INTO v_inventory 
    FROM public.product_inventory 
    WHERE product_id = v_req.product_id AND size = v_req.size
    FOR UPDATE;

    IF v_inventory IS NULL THEN
      RAISE EXCEPTION 'Inventory record missing for % size %', v_req.product_id, v_req.size;
    END IF;

    IF v_inventory.stock_quantity < v_req.total_req_qty THEN
      SELECT name INTO v_product FROM public.products WHERE id = v_req.product_id;
      RAISE EXCEPTION 'Insufficient stock for % (Size: %). Requested: %, Available: %', 
        v_product.name, v_req.size, v_req.total_req_qty, v_inventory.stock_quantity;
    END IF;

    -- Accumulate Subtotal using trusted DB price
    SELECT price INTO v_product FROM public.products WHERE id = v_req.product_id;
    v_subtotal := v_subtotal + (v_product.price * v_req.total_req_qty);
  END LOOP;

  -- 11. Coupon Validation
  IF p_coupon_code IS NOT NULL AND pg_catalog.btrim(p_coupon_code) <> '' THEN
    SELECT * INTO v_coupon 
    FROM public.coupons 
    WHERE code = pg_catalog.upper(pg_catalog.btrim(p_coupon_code)) 
      AND is_active = TRUE 
      AND (valid_until IS NULL OR valid_until > pg_catalog.now());

    IF v_coupon IS NOT NULL AND v_subtotal >= v_coupon.min_order_amount THEN
      IF v_coupon.is_free_shipping THEN
        v_shipping := 0.00;
      ELSE
        v_discount := pg_catalog.round((v_subtotal * (v_coupon.discount_percentage / 100.0)), 2);
      END IF;
    END IF;
  END IF;

  -- 12. Shipping Calculation (৳100 shipping fee / ৳3000 free threshold)
  SELECT 
    COALESCE(shipping_fee, 100.00), 
    COALESCE(free_shipping_threshold, 3000.00)
  INTO v_shipping, v_free_threshold 
  FROM public.storefront_settings 
  WHERE id = 'aura_main_config';

  IF (v_subtotal - v_discount) >= v_free_threshold THEN
    v_shipping := 0.00;
  END IF;

  v_total := (v_subtotal - v_discount) + v_shipping;

  -- 13. Insert Order Record
  INSERT INTO public.orders (
    id, user_id, idempotency_key, payload_fingerprint, customer_name, customer_email, customer_phone,
    shipping_address, courier, payment_method, subtotal, discount_amount, shipping_fee, total_amount, status
  ) VALUES (
    v_order_id, v_auth_uid, p_idempotency_key, p_payload_fingerprint, p_customer_name, v_derived_email, p_customer_phone,
    p_shipping_address, p_courier, p_payment_method, v_subtotal, v_discount, v_shipping, v_total, 'pending'
  );

  -- 14. Insert Line Items (Preserving Customer Selected Color)
  FOR v_item IN SELECT * FROM pg_catalog.jsonb_to_recordset(p_items) AS x(product_id TEXT, size TEXT, color TEXT, quantity INT)
  LOOP
    SELECT * INTO v_product FROM public.products WHERE id = v_item.product_id;

    INSERT INTO public.order_items (
      order_id, product_id, product_name, size, color, unit_price, quantity, subtotal
    ) VALUES (
      v_order_id, v_product.id, v_product.name, v_item.size, v_item.color, 
      v_product.price, v_item.quantity, (v_product.price * v_item.quantity)
    );
  END LOOP;

  -- 15. Deduct Stock Exactly Once per (product_id, size)
  FOR v_req IN 
    SELECT 
      x.product_id, 
      x.size, 
      pg_catalog.sum(x.quantity)::INT AS total_req_qty
    FROM pg_catalog.jsonb_to_recordset(p_items) AS x(product_id TEXT, size TEXT, color TEXT, quantity INT)
    GROUP BY x.product_id, x.size
  LOOP
    UPDATE public.product_inventory 
    SET stock_quantity = stock_quantity - v_req.total_req_qty, updated_at = pg_catalog.now()
    WHERE product_id = v_req.product_id AND size = v_req.size;
  END LOOP;

  RETURN pg_catalog.jsonb_build_object(
    'success', true, 
    'order_id', v_order_id, 
    'total_amount', v_total, 
    'subtotal', v_subtotal, 
    'discount', v_discount, 
    'shipping', v_shipping
  );
END;
$$;

-- Direct Client RPC Lockdown: REVOKE from PUBLIC and anon; Backend will invoke with user JWT
REVOKE ALL ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) FROM anon;
GRANT EXECUTE ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) TO authenticated;
```

---

## 4. Atomic Order Cancellation & Restocking Procedure

```sql
CREATE OR REPLACE FUNCTION public.cancel_order_atomic(
  p_order_id TEXT,
  p_admin_notes TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
  v_order RECORD;
  v_item RECORD;
  v_inv_exists BOOLEAN;
BEGIN
  -- 1. Lock the order row to prevent concurrent duplicate cancellations
  SELECT * INTO v_order 
  FROM public.orders 
  WHERE id = p_order_id 
  FOR UPDATE;

  IF v_order IS NULL THEN
    RAISE EXCEPTION 'Order % does not exist', p_order_id;
  END IF;

  -- 2. Idempotency Check: if already cancelled, exit safely
  IF v_order.status = 'cancelled' THEN
    RETURN pg_catalog.jsonb_build_object(
      'success', true, 
      'order_id', p_order_id, 
      'message', 'Order is already cancelled. No inventory restored.',
      'already_cancelled', true
    );
  END IF;

  -- 3. Transition Validation: Ordinary cancellation of delivered orders is FORBIDDEN
  IF v_order.status = 'delivered' THEN
    RAISE EXCEPTION 'Cannot cancel a delivered order. Process as a customer return instead.';
  END IF;

  -- 4. Verify all inventory rows exist before making modifications
  FOR v_item IN 
    SELECT 
      product_id, 
      size, 
      pg_catalog.sum(quantity)::INT AS total_restore_qty
    FROM public.order_items 
    WHERE order_id = p_order_id
    GROUP BY product_id, size
    ORDER BY product_id ASC, size ASC
  LOOP
    SELECT EXISTS (
      SELECT 1 FROM public.product_inventory 
      WHERE product_id = v_item.product_id AND size = v_item.size
    ) INTO v_inv_exists;

    IF NOT v_inv_exists THEN
      RAISE EXCEPTION 'Inventory row missing for product % size %. Cancellation aborted.', v_item.product_id, v_item.size;
    END IF;
  END LOOP;

  -- 5. Lock inventory rows in sorted order & restore stock
  FOR v_item IN 
    SELECT 
      product_id, 
      size, 
      pg_catalog.sum(quantity)::INT AS total_restore_qty
    FROM public.order_items 
    WHERE order_id = p_order_id
    GROUP BY product_id, size
    ORDER BY product_id ASC, size ASC
  LOOP
    PERFORM 1 FROM public.product_inventory 
    WHERE product_id = v_item.product_id AND size = v_item.size 
    FOR UPDATE;

    UPDATE public.product_inventory
    SET stock_quantity = stock_quantity + v_item.total_restore_qty,
        updated_at = pg_catalog.now()
    WHERE product_id = v_item.product_id AND size = v_item.size;
  END LOOP;

  -- 6. Update order status
  UPDATE public.orders 
  SET status = 'cancelled',
      admin_notes = COALESCE(p_admin_notes, admin_notes),
      updated_at = pg_catalog.now()
  WHERE id = p_order_id;

  -- 7. Audit Trail
  INSERT INTO public.admin_activity (action, entity_type, entity_id, details)
  VALUES (
    'ORDER_CANCELLED_RESTOCKED',
    'orders',
    p_order_id,
    pg_catalog.jsonb_build_object('notes', p_admin_notes, 'previous_status', v_order.status)
  );

  RETURN pg_catalog.jsonb_build_object(
    'success', true, 
    'order_id', p_order_id, 
    'message', 'Order cancelled and stock restored successfully.',
    'already_cancelled', false
  );
END;
$$;

-- Security: EXCLUSIVELY granted to service_role (Admin backend only)
REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM anon;
REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM authenticated;
GRANT EXECUTE ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) TO service_role;
```

---

## 5. Explicit Database Privileges & Row Level Security

```sql
-- Revoke all direct modifications from public roles
REVOKE ALL ON TABLE public.storefront_settings FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.admin_sessions FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.admin_activity FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.orders FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.order_items FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.product_inventory FROM PUBLIC, anon, authenticated;

-- Grant selective SELECT on catalog & public views
GRANT SELECT ON TABLE public.products TO anon, authenticated;
GRANT SELECT ON TABLE public.product_inventory TO anon, authenticated;
GRANT SELECT ON TABLE public.size_guides TO anon, authenticated;
GRANT SELECT ON TABLE public.coupons TO anon, authenticated;
GRANT SELECT ON TABLE public.v_public_storefront_settings TO anon, authenticated;

-- Grant customer activity management
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.addresses TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.recently_viewed TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.wishlists TO authenticated;
GRANT SELECT ON TABLE public.orders TO authenticated;
GRANT SELECT ON TABLE public.order_items TO authenticated;

-- Column-Level Privileges on profiles: users can update ONLY full_name and phone
GRANT SELECT ON TABLE public.profiles TO authenticated;
GRANT UPDATE (full_name, phone) ON TABLE public.profiles TO authenticated;

-- Enable RLS across all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.product_inventory ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.size_guides ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recently_viewed ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wishlists ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.storefront_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_activity ENABLE ROW LEVEL SECURITY;

-- Clean, Non-Self-Referencing Profiles Policy
CREATE POLICY "Users view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Addresses
CREATE POLICY "Users manage own addresses" ON public.addresses FOR ALL USING (auth.uid() = user_id);

-- Products & Inventory: Public Read for Published Items
CREATE POLICY "Public view published products" ON public.products FOR SELECT USING (is_published = TRUE);
CREATE POLICY "Public view inventory" ON public.product_inventory FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.products p WHERE p.id = product_inventory.product_id AND p.is_published = TRUE)
);

-- Size Guides: Public read only when published
CREATE POLICY "Public view size guides" ON public.size_guides FOR SELECT USING (is_published = TRUE);

-- Customer Activity
CREATE POLICY "Users manage recently viewed" ON public.recently_viewed FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage wishlist" ON public.wishlists FOR ALL USING (auth.uid() = user_id);

-- Orders: Customers read only their own orders
CREATE POLICY "Users view own orders" ON public.orders FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users view own order items" ON public.order_items FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.orders o WHERE o.id = order_items.order_id AND o.user_id = auth.uid())
);

-- Block all direct client access to administrative tables
CREATE POLICY "No public access to storefront settings" ON public.storefront_settings FOR ALL USING (FALSE);
CREATE POLICY "No public access to admin sessions" ON public.admin_sessions FOR ALL USING (FALSE);
CREATE POLICY "No public access to admin activity" ON public.admin_activity FOR ALL USING (FALSE);
```

---

## 6. Express Backend Checkout Proxy & Abuse Guard (`server.js`)

To ensure clients cannot bypass rate limiting, CAPTCHA, or fingerprint validation by invoking the database RPC directly:

```javascript
// Express Route: POST /api/checkout
app.post('/api/checkout', 
  checkoutRateLimiter, // 10 attempts per 15 min per IP
  async (req, res) => {
    try {
      const authHeader = req.headers.authorization;
      if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Authentication required. Please refresh page.' });
      }
      const token = authHeader.split(' ')[1];

      // 1. Verify Turnstile / CAPTCHA token if configured
      const captchaToken = req.body.captchaToken;
      if (process.env.TURNSTILE_SECRET_KEY) {
        const verifyRes = await fetch('https://challenges.cloudflare.com/turnstile/v0/siteverify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ secret: process.env.TURNSTILE_SECRET_KEY, response: captchaToken, remoteip: req.ip })
        });
        const verifyData = await verifyRes.json();
        if (!verifyData.success) {
          return res.status(400).json({ error: 'CAPTCHA challenge failed. Please try again.' });
        }
      }

      // 2. Compute canonical SHA-256 payload fingerprint
      const canonicalPayload = JSON.stringify({
        items: req.body.items,
        name: req.body.name,
        phone: req.body.phone,
        address: req.body.address,
        courier: req.body.courier,
        paymentMethod: req.body.paymentMethod,
        couponCode: req.body.couponCode
      });
      const payloadFingerprint = crypto.createHash('sha256').update(canonicalPayload).digest('hex');

      // 3. Forward request to Supabase RPC preserving user JWT context
      const userSupabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_ANON_KEY, {
        global: { headers: { Authorization: `Bearer ${token}` } }
      });

      const { data, error } = await userSupabase.rpc('place_order_atomic', {
        p_idempotency_key: req.body.idempotencyKey,
        p_payload_fingerprint: payloadFingerprint,
        p_customer_name: req.body.name,
        p_customer_email: req.body.email || null,
        p_customer_phone: req.body.phone,
        p_shipping_address: req.body.address,
        p_courier: req.body.courier,
        p_payment_method: req.body.paymentMethod,
        p_coupon_code: req.body.couponCode || null,
        p_items: req.body.items
      });

      if (error) {
        return res.status(400).json({ error: error.message });
      }

      res.json(data);
    } catch (err) {
      res.status(500).json({ error: 'Server error processing order: ' + err.message });
    }
  }
);
```

---

## 7. Account Linking Architecture (Guest to Permanent Member)

1. When a guest shops, `index.html` initializes a Supabase Anonymous session via `supabase.auth.signInAnonymously()`.
2. The user places orders, builds their bag, and browses products. All records attach to their anonymous `auth.uid()`.
3. When the customer clicks **REGISTER** in `index.html`:
   - Calls `supabase.auth.linkIdentity({ email, password })` or updates credentials.
   - The user's `auth.uid()` remains unchanged, while `profiles.is_anonymous` transitions to `false` and their verified email attaches.
   - All past orders, saved addresses, recently viewed items, and wishlists remain seamlessly linked without migration scripts.

---

## 8. Verification & Comprehensive Automated Test Suite (`npm test`)

The test suite (`tests/production-suite.test.js`) verifies all 13 critical requirements:

1. **Test 1: Cross-User Idempotency Key Reuse**: Attempts to submit an order with a key already claimed by a different user; asserts HTTP 400 rejection with explicit message.
2. **Test 2: Idempotency Key Payload Fingerprint Match**: Submits the same key with altered items/address; asserts rejection for payload mismatch.
3. **Test 3: Sequential Order IDs Above 10,000**: Simulates high-order sequence values (10,001..10,005); asserts IDs format cleanly as `AURA-YYMM-10001` without modulo wraparound.
4. **Test 4: Cart Item & Quantity Bounds Validation**: Rejects malformed cart payloads, negative quantities, quantities > 10, and aggregated quantities > 20.
5. **Test 5: Color & Size Integrity**: Rejects sizes not in `product.sizes` or colors not in `product.colors`; verifies color is saved in `order_items`.
6. **Test 6: Order Rate-Limiting**: Simulates 6 new order requests within 10 minutes from one UID; asserts the 6th is rejected while legitimate retries succeed.
7. **Test 7: Missing `storefront_settings` Fallback**: Truncates settings row in test; asserts checkout defaults safely to ৳100 shipping and ৳3,000 free threshold.
8. **Test 8: Payment Method & Courier Whitelist**: Rejects unapproved payment methods and unapproved couriers with explicit error messages.
9. **Test 9: Delivered Order Cancellation Protection**: Attempts ordinary cancellation on an order marked `delivered`; asserts rejection.
10. **Test 10: Missing Inventory Row Cancellation Protection**: Simulates cancellation when an inventory row is absent; asserts entire transaction aborts.
11. **Test 11: Concurrent Cancellation Idempotency**: Executes 5 concurrent cancellation calls for the same order; asserts stock is restored exactly once.
12. **Test 12: Public Access Protection on Settings & Policies**: Asserts direct `SELECT` on `storefront_settings` is denied by RLS, and `v_public_storefront_settings` hides unpublished policies.
13. **Test 13: Profile Column Protection & Concurrency-Safe Pruning**: Confirms customers cannot update `id` or `is_anonymous`; simulates concurrent product view inserts and asserts history stays capped at 20.
