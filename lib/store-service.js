/**
 * AURA Fashion — Business Logic & Transactional Store Service
 * Operates exclusively on live GitHub 'aura-product' JSON files via github-store.js.
 */

const crypto = require('crypto');
const {
  FILE_PATHS,
  DEFAULT_SETTINGS,
  readJsonFile,
  mutateJsonFile
} = require('./github-store');

class ValidationError extends Error {
  constructor(message, statusCode = 400) {
    super(message);
    this.name = 'ValidationError';
    this.statusCode = statusCode;
  }
}

function sanitizeSettings(raw = {}) {
  const base = { ...DEFAULT_SETTINGS };
  const retention = Number(raw.recentlyViewedRetentionDays);
  const deliveryFee = Number(raw.deliveryFee);
  const threshold = Number(raw.freeShippingThreshold);

  return {
    announcementBar: typeof raw.announcementBar === 'string' && raw.announcementBar.trim()
      ? raw.announcementBar.trim()
      : base.announcementBar,
    heroTitle: typeof raw.heroTitle === 'string' && raw.heroTitle.trim()
      ? raw.heroTitle.trim()
      : base.heroTitle,
    heroSubtitle: typeof raw.heroSubtitle === 'string' && raw.heroSubtitle.trim()
      ? raw.heroSubtitle.trim()
      : base.heroSubtitle,
    deliveryFee: Number.isFinite(deliveryFee) && deliveryFee >= 0 ? Math.round(deliveryFee) : base.deliveryFee,
    freeShippingThreshold: Number.isFinite(threshold) && threshold >= 0 ? Math.round(threshold) : base.freeShippingThreshold,
    recentlyViewedRetentionDays: Number.isInteger(retention) && retention >= 1 && retention <= 3650
      ? retention
      : base.recentlyViewedRetentionDays,
    availablePaymentMethods: Array.isArray(raw.availablePaymentMethods) && raw.availablePaymentMethods.length > 0
      ? raw.availablePaymentMethods.map(String)
      : base.availablePaymentMethods,
    availableCouriers: Array.isArray(raw.availableCouriers) && raw.availableCouriers.length > 0
      ? raw.availableCouriers.map(String)
      : base.availableCouriers,
    coupons: Array.isArray(raw.coupons)
      ? raw.coupons
          .filter(c => c && typeof c.code === 'string' && c.code.trim())
          .map(c => ({
            code: c.code.trim().toUpperCase(),
            discountPercent: Math.max(0, Math.min(100, Number(c.discountPercent) || 0)),
            minSpend: Math.max(0, Number(c.minSpend) || 0),
            desc: String(c.desc || ''),
            isFreeShipping: Boolean(c.isFreeShipping)
          }))
      : []
  };
}

function normalizeProduct(input, existing = null) {
  const name = typeof input.name === 'string' ? input.name.trim() : '';
  if (!name) throw new ValidationError('Product name is required.');

  const price = Number(input.price);
  if (!Number.isFinite(price) || price <= 0) {
    throw new ValidationError('Product price must be a positive number.');
  }

  let originalPrice = input.originalPrice !== undefined && input.originalPrice !== '' && input.originalPrice !== null
    ? Number(input.originalPrice)
    : null;
  if (originalPrice !== null && (!Number.isFinite(originalPrice) || originalPrice < 0)) {
    throw new ValidationError('Original price must be valid.');
  }

  const rawId = typeof input.id === 'string' && input.id.trim()
    ? input.id.trim()
    : (existing ? existing.id : `aura-${name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 24)}-${Date.now().toString(36)}`);
  const id = rawId.replace(/[^a-zA-Z0-9_-]/g, '');
  if (!id) throw new ValidationError('Valid product ID is required.');

  const stockPerSize = {};
  if (input.stockPerSize && typeof input.stockPerSize === 'object') {
    for (const [sizeKey, qtyVal] of Object.entries(input.stockPerSize)) {
      const cleanSize = String(sizeKey).trim();
      if (!cleanSize) continue;
      const qty = Math.max(0, Math.floor(Number(qtyVal) || 0));
      stockPerSize[cleanSize] = qty;
    }
  }
  const sizes = Array.isArray(input.sizes) && input.sizes.length > 0
    ? input.sizes.map(s => String(s).trim()).filter(Boolean)
    : Object.keys(stockPerSize);

  if (sizes.length === 0) {
    throw new ValidationError('At least one size is required.');
  }
  for (const s of sizes) {
    if (stockPerSize[s] === undefined) stockPerSize[s] = 0;
  }

  let colors = [];
  if (Array.isArray(input.colors) && input.colors.length > 0) {
    colors = input.colors
      .filter(c => c && typeof c.name === 'string' && c.name.trim())
      .map(c => ({
        name: c.name.trim(),
        hex: typeof c.hex === 'string' && c.hex.trim() ? c.hex.trim() : '#111111'
      }));
  }
  if (colors.length === 0) {
    colors = [{ name: 'Onyx Black', hex: '#111111' }];
  }

  const nowIso = new Date().toISOString();
  return {
    id,
    name,
    tagline: typeof input.tagline === 'string' ? input.tagline.trim() : '',
    price: Math.round(price),
    originalPrice: originalPrice ? Math.round(originalPrice) : null,
    category: typeof input.category === 'string' && input.category.trim() ? input.category.trim() : 'Tees',
    style: typeof input.style === 'string' && input.style.trim() ? input.style.trim() : 'Streetwear',
    badge: typeof input.badge === 'string' ? input.badge.trim() : '',
    description: typeof input.description === 'string' ? input.description.trim() : '',
    fabric: typeof input.fabric === 'string' ? input.fabric.trim() : '',
    fit: typeof input.fit === 'string' ? input.fit.trim() : '',
    care: typeof input.care === 'string' ? input.care.trim() : '',
    image: typeof input.image === 'string' && input.image.trim() ? input.image.trim() : '/assets/img_hero_fashion.jpg',
    gallery: Array.isArray(input.gallery) ? input.gallery.filter(Boolean).map(String) : [],
    colors,
    sizes,
    stockPerSize,
    isNew: Boolean(input.isNew),
    isDrop: Boolean(input.isDrop),
    isFeatured: input.isFeatured !== undefined ? Boolean(input.isFeatured) : true,
    published: input.published !== undefined ? Boolean(input.published) : true,
    createdAt: existing && existing.createdAt ? existing.createdAt : nowIso,
    updatedAt: nowIso
  };
}

function computePayloadFingerprint(body) {
  const canonical = JSON.stringify({
    items: body.items,
    name: body.customerName,
    phone: body.customerPhone,
    address: body.shippingAddress,
    courier: body.courier,
    paymentMethod: body.paymentMethod,
    couponCode: body.couponCode || null
  });
  return crypto.createHash('sha256').update(canonical).digest('hex');
}

function generateSequentialOrderId(existingOrders) {
  const now = new Date();
  const yy = String(now.getUTCFullYear()).slice(-2);
  const mm = String(now.getUTCMonth() + 1).padStart(2, '0');
  let maxSeq = 10000;
  for (const ord of existingOrders) {
    if (!ord || typeof ord.id !== 'string') continue;
    const match = ord.id.match(/-(\d+)$/);
    if (match) {
      const seq = parseInt(match[1], 10);
      if (Number.isFinite(seq) && seq > maxSeq) maxSeq = seq;
    }
  }
  return `AURA-${yy}${mm}-${maxSeq + 1}`;
}

async function getStorefrontData() {
  const [productsRes, settingsRes, guidesRes] = await Promise.all([
    readJsonFile(FILE_PATHS.PRODUCTS, []),
    readJsonFile(FILE_PATHS.SETTINGS, DEFAULT_SETTINGS),
    readJsonFile(FILE_PATHS.SIZE_GUIDES, [])
  ]);

  const allProducts = Array.isArray(productsRes.data) ? productsRes.data : [];
  const publishedProducts = allProducts.filter(p => p && p.published !== false);
  const settings = sanitizeSettings(settingsRes.data);
  const sizeGuides = Array.isArray(guidesRes.data)
    ? guidesRes.data.filter(g => g && g.published !== false)
    : [];

  return {
    products: publishedProducts,
    settings,
    sizeGuides
  };
}

async function getAdminBootstrapData() {
  const [productsRes, ordersRes, settingsRes, guidesRes] = await Promise.all([
    readJsonFile(FILE_PATHS.PRODUCTS, []),
    readJsonFile(FILE_PATHS.ORDERS, []),
    readJsonFile(FILE_PATHS.SETTINGS, DEFAULT_SETTINGS),
    readJsonFile(FILE_PATHS.SIZE_GUIDES, [])
  ]);

  const products = Array.isArray(productsRes.data) ? productsRes.data : [];
  const orders = Array.isArray(ordersRes.data) ? ordersRes.data : [];
  const settings = sanitizeSettings(settingsRes.data);
  const sizeGuides = Array.isArray(guidesRes.data) ? guidesRes.data : [];

  // Derive live customer aggregates purely from live orders
  const customerMap = new Map();
  for (const order of orders) {
    if (!order) continue;
    const key = (order.customerPhone || order.customerName || order.id).trim();
    const existing = customerMap.get(key) || {
      name: order.customerName || 'Guest Customer',
      phone: order.customerPhone || '',
      city: order.shippingAddress && order.shippingAddress.city ? order.shippingAddress.city : '',
      orderCount: 0,
      totalSpent: 0,
      lastOrderAt: order.createdAt || ''
    };
    existing.orderCount += 1;
    if (String(order.status).toLowerCase() !== 'cancelled') {
      existing.totalSpent += Number(order.total || 0);
    }
    if (order.createdAt && (!existing.lastOrderAt || order.createdAt > existing.lastOrderAt)) {
      existing.lastOrderAt = order.createdAt;
    }
    customerMap.set(key, existing);
  }

  return {
    products,
    orders,
    settings,
    sizeGuides,
    customers: Array.from(customerMap.values()).sort((a, b) => (b.lastOrderAt || '').localeCompare(a.lastOrderAt || ''))
  };
}

async function upsertProduct(productInput) {
  return mutateJsonFile(
    FILE_PATHS.PRODUCTS,
    [],
    (products) => {
      const list = Array.isArray(products) ? products : [];
      const existingIdx = productInput.id
        ? list.findIndex(p => p && p.id === productInput.id)
        : -1;
      const existing = existingIdx >= 0 ? list[existingIdx] : null;
      const normalized = normalizeProduct(productInput, existing);

      if (existingIdx >= 0) {
        list[existingIdx] = normalized;
      } else {
        list.unshift(normalized);
      }
      return { data: list, result: normalized };
    },
    (_, saved) => `Save product ${saved.id} (${saved.name}) via AURA Admin`
  );
}

async function deleteProduct(productId) {
  if (!productId) throw new ValidationError('Product ID is required.');
  return mutateJsonFile(
    FILE_PATHS.PRODUCTS,
    [],
    (products) => {
      const list = Array.isArray(products) ? products : [];
      const idx = list.findIndex(p => p && p.id === productId);
      if (idx === -1) {
        throw new ValidationError('Product not found.', 404);
      }
      const [removed] = list.splice(idx, 1);
      return { data: list, result: { deleted: true, id: removed.id } };
    },
    () => `Delete product ${productId} via AURA Admin`
  );
}

async function updateSettings(settingsInput) {
  const sanitized = sanitizeSettings(settingsInput);
  return mutateJsonFile(
    FILE_PATHS.SETTINGS,
    DEFAULT_SETTINGS,
    () => ({ data: sanitized, result: sanitized }),
    'Update storefront settings via AURA Admin'
  );
}

async function upsertSizeGuide(guideInput) {
  if (!guideInput || !String(guideInput.category || '').trim()) {
    throw new ValidationError('Size guide category name is required.');
  }
  return mutateJsonFile(
    FILE_PATHS.SIZE_GUIDES,
    [],
    (guides) => {
      const list = Array.isArray(guides) ? guides : [];
      const id = (guideInput.id && String(guideInput.id).trim()) || `sg-${Date.now().toString(36)}`;
      const entry = {
        id,
        category: String(guideInput.category).trim(),
        columns: Array.isArray(guideInput.columns) && guideInput.columns.length > 0
          ? guideInput.columns.map(c => String(c).trim()).filter(Boolean)
          : ['Chest', 'Shoulder', 'Length', 'Sleeve'],
        rows: Array.isArray(guideInput.rows) ? guideInput.rows : [],
        published: Boolean(guideInput.published),
        updatedAt: new Date().toISOString()
      };
      const idx = list.findIndex(g => g && g.id === id);
      if (idx >= 0) list[idx] = entry;
      else list.push(entry);
      return { data: list, result: entry };
    },
    (_, saved) => `Save size guide ${saved.category} via AURA Admin`
  );
}

async function deleteSizeGuide(guideId) {
  if (!guideId) throw new ValidationError('Size guide ID is required.');
  return mutateJsonFile(
    FILE_PATHS.SIZE_GUIDES,
    [],
    (guides) => {
      const list = Array.isArray(guides) ? guides : [];
      const idx = list.findIndex(g => g && g.id === guideId);
      if (idx === -1) throw new ValidationError('Size guide not found.', 404);
      list.splice(idx, 1);
      return { data: list, result: { deleted: true, id: guideId } };
    },
    () => `Delete size guide ${guideId} via AURA Admin`
  );
}

async function placeOrder(body) {
  const customerName = typeof body.customerName === 'string' ? body.customerName.trim() : '';
  const customerPhone = typeof body.customerPhone === 'string' ? body.customerPhone.trim() : '';
  const shippingObj = body.shippingAddress && typeof body.shippingAddress === 'object'
    ? body.shippingAddress
    : { street: typeof body.shippingAddress === 'string' ? body.shippingAddress.trim() : '', city: body.city || 'Dhaka' };
  const street = typeof shippingObj.street === 'string' ? shippingObj.street.trim() : '';
  const city = typeof shippingObj.city === 'string' && shippingObj.city.trim() ? shippingObj.city.trim() : 'Dhaka';

  if (!customerName || !customerPhone || !street) {
    throw new ValidationError('Customer name, phone number, and street address are required.');
  }
  if (!Array.isArray(body.items) || body.items.length === 0) {
    throw new ValidationError('Shopping bag cannot be empty.');
  }

  const idempotencyKey = typeof body.idempotencyKey === 'string' && body.idempotencyKey.trim()
    ? body.idempotencyKey.trim()
    : `aura_order_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
  const fingerprint = computePayloadFingerprint(body);

  const settingsRes = await readJsonFile(FILE_PATHS.SETTINGS, DEFAULT_SETTINGS);
  const settings = sanitizeSettings(settingsRes.data);

  const courier = typeof body.courier === 'string' && body.courier.trim() ? body.courier.trim() : settings.availableCouriers[0];
  if (!settings.availableCouriers.includes(courier)) {
    throw new ValidationError(`Courier "${courier}" is not supported.`);
  }

  const paymentMethod = typeof body.paymentMethod === 'string' && body.paymentMethod.trim()
    ? body.paymentMethod.trim()
    : 'Cash on Delivery';
  if (!settings.availablePaymentMethods.includes(paymentMethod)) {
    throw new ValidationError(`Payment method "${paymentMethod}" is not accepted.`);
  }

  // Idempotency check against existing orders
  const ordersSnapshot = await readJsonFile(FILE_PATHS.ORDERS, [], { bypassCache: true });
  const existingOrders = Array.isArray(ordersSnapshot.data) ? ordersSnapshot.data : [];
  const existingMatch = existingOrders.find(o => o && o.idempotencyKey === idempotencyKey);
  if (existingMatch) {
    if (existingMatch.payloadFingerprint && existingMatch.payloadFingerprint !== fingerprint) {
      throw new ValidationError('Idempotency key reuse error: payload does not match original submission.', 409);
    }
    return {
      success: true,
      is_duplicate: true,
      order_id: existingMatch.id,
      total_amount: existingMatch.total,
      order: existingMatch
    };
  }

  // Aggregate stock deductions & build authoritative line items from live products.json
  const aggregatedDeductions = new Map();
  let authoritativeSubtotal = 0;
  let resolvedLineItems = [];

  await mutateJsonFile(
    FILE_PATHS.PRODUCTS,
    [],
    (products) => {
      const catalog = Array.isArray(products) ? products : [];
      const byId = new Map(catalog.map(p => [p.id, p]));
      aggregatedDeductions.clear();
      authoritativeSubtotal = 0;
      resolvedLineItems = [];

      for (const item of body.items) {
        if (!item || typeof item.product_id !== 'string') {
          throw new ValidationError('Invalid cart item product reference.');
        }
        const qty = Number(item.quantity);
        if (!Number.isInteger(qty) || qty < 1 || qty > 10) {
          throw new ValidationError('Line quantity must be an integer between 1 and 10.');
        }

        const prod = byId.get(item.product_id);
        if (!prod || prod.published === false) {
          throw new ValidationError(`Product "${item.product_id}" is unavailable or unpublished.`);
        }

        const size = typeof item.size === 'string' ? item.size.trim() : '';
        if (!Array.isArray(prod.sizes) || !prod.sizes.includes(size)) {
          throw new ValidationError(`Size "${size}" is not valid for ${prod.name}.`);
        }

        const colorName = typeof item.color === 'string' ? item.color.trim() : '';
        if (Array.isArray(prod.colors) && prod.colors.length > 0 && !prod.colors.some(c => c.name === colorName)) {
          throw new ValidationError(`Color "${colorName}" is not valid for ${prod.name}.`);
        }

        const stockKey = `${prod.id}::${size}`;
        aggregatedDeductions.set(stockKey, (aggregatedDeductions.get(stockKey) || 0) + qty);

        authoritativeSubtotal += prod.price * qty;
        resolvedLineItems.push({
          product_id: prod.id,
          name: prod.name,
          image: prod.image,
          size,
          color: colorName || (prod.colors[0] && prod.colors[0].name) || 'Standard',
          unitPrice: prod.price,
          quantity: qty,
          lineTotal: prod.price * qty
        });
      }

      // Check aggregated stock & deduct once per (product_id, size)
      for (const [stockKey, totalQty] of aggregatedDeductions.entries()) {
        if (totalQty > 20) {
          throw new ValidationError('Aggregated quantity per product & size cannot exceed 20 units.');
        }
        const [prodId, size] = stockKey.split('::');
        const prod = byId.get(prodId);
        const available = prod.stockPerSize && prod.stockPerSize[size] !== undefined
          ? Number(prod.stockPerSize[size])
          : 0;
        if (available < totalQty) {
          throw new ValidationError(`Insufficient stock for ${prod.name} (Size ${size}). Available: ${available}.`);
        }
        prod.stockPerSize[size] = available - totalQty;
      }

      return { data: catalog, result: resolvedLineItems };
    },
    () => `Reserve stock for new order (${body.items.length} item lines)`
  );

  // Compute coupon & shipping server-side
  let discount = 0;
  let freeShipCoupon = false;
  let appliedCouponCode = null;
  if (typeof body.couponCode === 'string' && body.couponCode.trim()) {
    const codeUpper = body.couponCode.trim().toUpperCase();
    const coupon = settings.coupons.find(c => c.code === codeUpper);
    if (coupon && authoritativeSubtotal >= coupon.minSpend) {
      appliedCouponCode = coupon.code;
      if (coupon.isFreeShipping) {
        freeShipCoupon = true;
      } else {
        discount = Math.round((authoritativeSubtotal * coupon.discountPercent) / 100);
      }
    }
  }

  const shippingFee = (authoritativeSubtotal >= settings.freeShippingThreshold || freeShipCoupon)
    ? 0
    : settings.deliveryFee;
  const totalAmount = Math.max(0, authoritativeSubtotal - discount + shippingFee);

  let savedOrder;
  try {
    savedOrder = await mutateJsonFile(
      FILE_PATHS.ORDERS,
      [],
      (orders) => {
        const list = Array.isArray(orders) ? orders : [];
        const dup = list.find(o => o && o.idempotencyKey === idempotencyKey);
        if (dup) {
          return { skipWrite: true, result: dup };
        }
        const orderId = generateSequentialOrderId(list);
        const newOrder = {
          id: orderId,
          idempotencyKey,
          payloadFingerprint: fingerprint,
          customerName,
          customerEmail: typeof body.customerEmail === 'string' && body.customerEmail.trim() ? body.customerEmail.trim() : null,
          customerPhone,
          shippingAddress: { street, city },
          deliveryAddress: `${street}, ${city}`,
          courier,
          trackingId: `AURA-TRK-${orderId.split('-').pop()}`,
          paymentMethod,
          couponCode: appliedCouponCode,
          items: resolvedLineItems,
          subtotal: authoritativeSubtotal,
          discount,
          shipping: shippingFee,
          total: totalAmount,
          status: 'confirmed',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        list.unshift(newOrder);
        return { data: list, result: newOrder };
      },
      (_, ord) => `Place order ${ord.id} via AURA Checkout`
    );
  } catch (err) {
    // Compensating stock rollback if writing orders.json failed
    await mutateJsonFile(
      FILE_PATHS.PRODUCTS,
      [],
      (products) => {
        const catalog = Array.isArray(products) ? products : [];
        const byId = new Map(catalog.map(p => [p.id, p]));
        for (const [stockKey, totalQty] of aggregatedDeductions.entries()) {
          const [prodId, size] = stockKey.split('::');
          const prod = byId.get(prodId);
          if (prod && prod.stockPerSize && prod.stockPerSize[size] !== undefined) {
            prod.stockPerSize[size] += totalQty;
          }
        }
        return { data: catalog, result: true };
      },
      'Rollback inventory after failed order write'
    ).catch(() => {});
    throw err;
  }

  return {
    success: true,
    order_id: savedOrder.id,
    total_amount: savedOrder.total,
    subtotal: savedOrder.subtotal,
    discount: savedOrder.discount,
    shipping: savedOrder.shipping,
    order: savedOrder
  };
}

async function lookupOrdersByIds(orderIds) {
  if (!Array.isArray(orderIds) || orderIds.length === 0) return [];
  const wanted = new Set(orderIds.filter(id => typeof id === 'string' && id.trim()).map(id => id.trim()));
  if (wanted.size === 0) return [];
  const ordersRes = await readJsonFile(FILE_PATHS.ORDERS, []);
  const all = Array.isArray(ordersRes.data) ? ordersRes.data : [];
  return all.filter(o => o && wanted.has(o.id));
}

async function updateOrderStatus({ orderId, newStatus, trackingId, adminNotes }) {
  if (!orderId) throw new ValidationError('Order ID is required.');
  const allowedStatuses = ['pending', 'confirmed', 'processing', 'packed', 'shipped', 'delivered', 'cancelled'];
  const normalizedStatus = String(newStatus || '').trim().toLowerCase();
  if (!allowedStatuses.includes(normalizedStatus)) {
    throw new ValidationError(`Invalid order status "${newStatus}".`);
  }

  let shouldRestockItems = null;
  const updatedOrder = await mutateJsonFile(
    FILE_PATHS.ORDERS,
    [],
    (orders) => {
      const list = Array.isArray(orders) ? orders : [];
      const order = list.find(o => o && o.id === orderId);
      if (!order) {
        throw new ValidationError('Order does not exist.', 404);
      }

      const prevStatus = String(order.status || '').toLowerCase();
      if (normalizedStatus === 'cancelled') {
        if (prevStatus === 'cancelled') {
          return { skipWrite: true, result: { ...order, already_cancelled: true } };
        }
        if (prevStatus === 'delivered') {
          throw new ValidationError('Cannot cancel a delivered order.');
        }
        shouldRestockItems = Array.isArray(order.items) ? order.items : [];
      }

      order.status = normalizedStatus;
      if (typeof trackingId === 'string' && trackingId.trim()) {
        order.trackingId = trackingId.trim();
      }
      if (typeof adminNotes === 'string' && adminNotes.trim()) {
        order.adminNotes = adminNotes.trim();
      }
      order.updatedAt = new Date().toISOString();
      return { data: list, result: order };
    },
    () => `Update order ${orderId} status to ${normalizedStatus}`
  );

  // Atomically restore inventory when transitioning into cancelled
  if (shouldRestockItems && shouldRestockItems.length > 0) {
    await mutateJsonFile(
      FILE_PATHS.PRODUCTS,
      [],
      (products) => {
        const catalog = Array.isArray(products) ? products : [];
        const byId = new Map(catalog.map(p => [p.id, p]));
        for (const item of shouldRestockItems) {
          const prod = byId.get(item.product_id);
          if (prod && prod.stockPerSize && prod.stockPerSize[item.size] !== undefined) {
            prod.stockPerSize[item.size] += Number(item.quantity || 0);
          }
        }
        return { data: catalog, result: true };
      },
      () => `Restore inventory for cancelled order ${orderId}`
    );
  }

  return {
    success: true,
    order_id: updatedOrder.id,
    already_cancelled: Boolean(updatedOrder.already_cancelled),
    order: updatedOrder
  };
}

module.exports = {
  ValidationError,
  sanitizeSettings,
  normalizeProduct,
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
};
