const fs = require('fs');

let html = fs.readFileSync('index.html', 'utf8');

// 1. Enhance renderProductDetailView with recordProductView
const oldDetailViewStart = 'function renderProductDetailView(app, productId) {';
const newDetailViewStart = `function renderProductDetailView(app, productId) {
  const pRecord = window.AURA_STATE.products.find(x => x.id === productId);
  if (pRecord && typeof recordProductView === 'function') recordProductView(pRecord);`;

if (html.includes(oldDetailViewStart)) {
  html = html.replace(oldDetailViewStart, newDetailViewStart);
}

// 2. Add Size Guide link
if (html.includes('<span>SELECT SIZE</span>')) {
  html = html.replace(
    '<span>SELECT SIZE</span>',
    `<div style="display:flex; justify-content:space-between; align-items:center;">
      <span>SELECT SIZE</span>
      <button type="button" onclick="toggleSizeGuide(true)" style="background:transparent; border:none; color:var(--gold); font-size:12px; font-weight:800; cursor:pointer; text-decoration:underline;">
        <i class="fa-solid fa-ruler-horizontal"></i> SIZE GUIDE
      </button>
    </div>`
  );
}

// 3. Connect footer Size Guide link
html = html.replace(
  '<li><a href="#journal">Size & Fit Guide</a></li>',
  '<li><a href="javascript:void(0)" onclick="toggleSizeGuide(true)">Size & Fit Guide</a></li>'
);

// 4. Update handlePlaceOrder
const oldPlaceOrder = 'function handlePlaceOrder() {';
const newPlaceOrder = `async function handlePlaceOrder() {
  const cart = window.AURA_STATE.cart;
  if (cart.length === 0) {
    showToast("Your bag is empty");
    return;
  }
  const nameInput = document.getElementById("chk-name");
  const phoneInput = document.getElementById("chk-phone");
  const addressInput = document.getElementById("chk-address");
  const citySelect = document.getElementById("chk-city");
  const courierSelect = document.getElementById("chk-courier");
  const paymentRadio = document.querySelector('input[name="payment-method"]:checked');

  const name = nameInput ? nameInput.value.trim() : "";
  const phone = phoneInput ? phoneInput.value.trim() : "";
  const street = addressInput ? addressInput.value.trim() : "";
  const city = citySelect ? citySelect.value : "Dhaka";
  const courier = courierSelect ? courierSelect.value : "Pathao Express";
  const paymentMethod = paymentRadio ? paymentRadio.value : "Cash on Delivery";

  if (!name || !phone || !street) {
    showToast("Please fill in all required delivery details");
    return;
  }

  const itemsPayload = cart.map(item => ({
    product_id: item.id,
    size: item.size,
    color: item.color.name,
    quantity: item.quantity
  }));

  const idempotencyKey = "aura_order_" + Date.now() + "_" + Math.random().toString(36).substring(2, 9);
  showToast("Processing order securely...");

  try {
    let token = "guest_token";
    if (typeof supabase !== "undefined" && supabase) {
      const session = await ensureAuthSession();
      if (session) token = session.access_token;
    }

    const res = await fetch("/api/checkout", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
      },
      body: JSON.stringify({
        idempotencyKey: idempotencyKey,
        customerName: name,
        customerEmail: window.AURA_STATE.user ? window.AURA_STATE.user.email : null,
        customerPhone: phone,
        shippingAddress: { street: street, city: city },
        courier: courier,
        paymentMethod: paymentMethod,
        couponCode: window.AURA_STATE.appliedCoupon ? window.AURA_STATE.appliedCoupon.code : null,
        items: itemsPayload
      })
    });

    const data = await res.json();
    if (!res.ok) {
      showToast(data.error || "Order checkout error");
      return;
    }

    const placedOrder = {
      id: data.order_id,
      customerName: name,
      deliveryAddress: street + ", " + city,
      courier: courier,
      trackingId: "PTH-" + Math.floor(Math.random() * 89999 + 10000),
      status: "Confirmed",
      total: data.total_amount,
      createdAt: new Date().toISOString(),
      items: [...cart]
    };

    window.AURA_STATE.cart = [];
    localStorage.removeItem("aura_cart");
    window.AURA_STATE.orders.unshift(placedOrder);
    localStorage.setItem("aura_orders", JSON.stringify(window.AURA_STATE.orders));

    updateCounters();
    showToast("Order Placed Successfully! #" + data.order_id);
    navigateTo("#account/orders");
  } catch (err) {
    showToast("Error connecting to checkout: " + err.message);
  }
}
function dummyOldPlaceOrder() {`;

if (html.includes(oldPlaceOrder)) {
  html = html.replace(oldPlaceOrder, newPlaceOrder);
}

// 5. Account View Recently Viewed Tab
const searchPoint = `<div style="display: flex; flex-direction: column; gap: 4px;">`;
if (html.includes(searchPoint)) {
  html = html.replace(
    `<i class="fa-solid fa-location-dot"></i> Saved Addresses\n            </button>`,
    `<i class="fa-solid fa-location-dot"></i> Saved Addresses\n            </button>\n            <button onclick="navigateTo('#account/recent')" class="cat-chip" style="width: 100%; border: none; justify-content: flex-start;">\n              <i class="fa-solid fa-clock-rotate-left"></i> Recently Viewed\n            </button>`
  );
}

fs.writeFileSync('index.html', html);
fs.writeFileSync('public/index.html', html);
console.log('Customer storefront successfully patched with Size Guide, Recently Viewed and Checkout API!');
