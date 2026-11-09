# ============================================================================
# AURA FASHION — SUPABASE PRODUCTION DATABASE SCHEMA & MIGRATION
# ============================================================================

-- 1. EXTENSIONS, ENUMS & SEQUENCES
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

-- Sequence for collision-safe sequential Order IDs (AURA-YYMM-10001+)
CREATE SEQUENCE IF NOT EXISTS public.order_number_seq START 10001;

-- 2. CUSTOMER PROFILES (Extends Supabase auth.users)
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

-- 3. SAVED ADDRESSES
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

-- 4. SIZE GUIDES (is_published FALSE by default until reviewed by Admin)
CREATE TABLE IF NOT EXISTS public.size_guides (
  id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  description TEXT,
  fitting_notes TEXT,
  measurement_columns JSONB NOT NULL DEFAULT '["Chest", "Shoulder", "Length", "Sleeve"]'::jsonb,
  measurements JSONB NOT NULL DEFAULT '[]'::jsonb,
  is_published BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT pg_catalog.now(),
  updated_at TIMESTAMPTZ DEFAULT pg_catalog.now()
);

-- 5. PRODUCTS & SIZE-WISE INVENTORY
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

-- 6. RECENTLY VIEWED & WISHLISTS
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

-- 7. PROMOTIONS & COUPONS (Original AURA10 & AURANEW)
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

-- 8. ORDERS & ORDER ITEMS
CREATE TABLE IF NOT EXISTS public.orders (
  id TEXT PRIMARY KEY,
  user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
  idempotency_key TEXT UNIQUE NOT NULL,
  payload_fingerprint TEXT NOT NULL,
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

-- 9. STOREFRONT CMS & SAFE PUBLIC VIEW
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

INSERT INTO public.storefront_settings (id) 
VALUES ('aura_main_config')
ON CONFLICT (id) DO NOTHING;

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

-- 10. ADMIN SESSIONS & AUDIT LOG
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

-- 11. HARDENED ATOMIC CHECKOUT STORED PROCEDURE
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
  p_items JSONB
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
  -- 1. Authentication Check
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

  IF p_shipping_address IS NULL OR 
     pg_catalog.btrim(COALESCE(p_shipping_address->>'street', '')) = '' OR
     pg_catalog.btrim(COALESCE(p_shipping_address->>'city', '')) = '' THEN
    RAISE EXCEPTION 'Street address and city are mandatory';
  END IF;

  -- 3. Transactional Advisory Locking on Idempotency Key
  PERFORM pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtext(p_idempotency_key));

  -- 4. Idempotency Check
  SELECT * INTO v_existing_order 
  FROM public.orders 
  WHERE idempotency_key = p_idempotency_key;

  IF v_existing_order IS NOT NULL THEN
    IF v_existing_order.user_id <> v_auth_uid THEN
      RAISE EXCEPTION 'Idempotency key conflict: key already claimed by another user';
    END IF;

    IF v_existing_order.payload_fingerprint <> p_payload_fingerprint THEN
      RAISE EXCEPTION 'Idempotency key reuse error: payload does not match original submission';
    END IF;

    RETURN pg_catalog.jsonb_build_object(
      'success', true, 
      'order_id', v_existing_order.id, 
      'total_amount', v_existing_order.total_amount,
      'is_duplicate', true
    );
  END IF;

  -- 5. Rate Limit Protection on New Orders
  SELECT pg_catalog.count(*) INTO v_recent_order_count
  FROM public.orders
  WHERE user_id = v_auth_uid 
    AND created_at > (pg_catalog.now() - INTERVAL '10 minutes');

  IF v_recent_order_count >= 5 THEN
    RAISE EXCEPTION 'Order rate limit exceeded. Please wait a few moments before trying again.';
  END IF;

  -- 6. Validate Courier and Payment Method
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

  -- 7. Email Derivation
  v_derived_email := COALESCE(auth.jwt() ->> 'email', NULLIF(pg_catalog.btrim(p_customer_email), ''));

  -- 8. Collision-Safe Sequential Order ID Generation (AURA-YYMM-10001+)
  v_seq_num := pg_catalog.nextval('public.order_number_seq');
  v_order_id := 'AURA-' || pg_catalog.to_char(pg_catalog.now(), 'YYMM') || '-' || v_seq_num::TEXT;

  -- 9. VALIDATE SHOPPING BAG ITEMS & QUANTITIES
  IF p_items IS NULL OR pg_catalog.jsonb_array_length(p_items) = 0 THEN
    RAISE EXCEPTION 'Shopping bag is empty';
  END IF;

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

    IF NOT (v_product.sizes @> ARRAY[v_item.size]) THEN
      RAISE EXCEPTION 'Size % is not valid for product %', v_item.size, v_product.name;
    END IF;

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

  -- 12. Shipping Calculation (৳100 fee / ৳3000 free threshold)
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

  -- 14. Insert Line Items
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

REVOKE ALL ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) FROM anon;
GRANT EXECUTE ON FUNCTION public.place_order_atomic(TEXT, TEXT, TEXT, TEXT, TEXT, JSONB, TEXT, TEXT, TEXT, JSONB) TO authenticated;

-- 12. ATOMIC ORDER CANCELLATION STORED PROCEDURE
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

  -- 2. Idempotency Check
  IF v_order.status = 'cancelled' THEN
    RETURN pg_catalog.jsonb_build_object(
      'success', true, 
      'order_id', p_order_id, 
      'message', 'Order is already cancelled. No inventory restored.',
      'already_cancelled', true
    );
  END IF;

  -- 3. Forbidden on delivered orders
  IF v_order.status = 'delivered' THEN
    RAISE EXCEPTION 'Cannot cancel a delivered order. Process as a customer return instead.';
  END IF;

  -- 4. Verify all inventory rows exist
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

REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM anon;
REVOKE ALL ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) FROM authenticated;
GRANT EXECUTE ON FUNCTION public.cancel_order_atomic(TEXT, TEXT) TO service_role;

-- 13. PRIVILEGES & ROW LEVEL SECURITY
REVOKE ALL ON TABLE public.storefront_settings FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.admin_sessions FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.admin_activity FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.orders FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.order_items FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE public.product_inventory FROM PUBLIC, anon, authenticated;

GRANT SELECT ON TABLE public.products TO anon, authenticated;
GRANT SELECT ON TABLE public.product_inventory TO anon, authenticated;
GRANT SELECT ON TABLE public.size_guides TO anon, authenticated;
GRANT SELECT ON TABLE public.coupons TO anon, authenticated;
GRANT SELECT ON TABLE public.v_public_storefront_settings TO anon, authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.addresses TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.recently_viewed TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.wishlists TO authenticated;
GRANT SELECT ON TABLE public.orders TO authenticated;
GRANT SELECT ON TABLE public.order_items TO authenticated;

-- Column level on profiles: update ONLY full_name and phone
GRANT SELECT ON TABLE public.profiles TO authenticated;
GRANT UPDATE (full_name, phone) ON TABLE public.profiles TO authenticated;

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

CREATE POLICY "Users view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users update own profile fields" ON public.profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users manage own addresses" ON public.addresses FOR ALL USING (auth.uid() = user_id);

CREATE POLICY "Public view published products" ON public.products FOR SELECT USING (is_published = TRUE);
CREATE POLICY "Public view inventory" ON public.product_inventory FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.products p WHERE p.id = product_inventory.product_id AND p.is_published = TRUE)
);

CREATE POLICY "Public view size guides" ON public.size_guides FOR SELECT USING (is_published = TRUE);

CREATE POLICY "Users manage recently viewed" ON public.recently_viewed FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users manage wishlist" ON public.wishlists FOR ALL USING (auth.uid() = user_id);

CREATE POLICY "Users view own orders" ON public.orders FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users view own order items" ON public.order_items FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.orders o WHERE o.id = order_items.order_id AND o.user_id = auth.uid())
);

CREATE POLICY "No public access to storefront settings" ON public.storefront_settings FOR ALL USING (FALSE);
CREATE POLICY "No public access to admin sessions" ON public.admin_sessions FOR ALL USING (FALSE);
CREATE POLICY "No public access to admin activity" ON public.admin_activity FOR ALL USING (FALSE);
