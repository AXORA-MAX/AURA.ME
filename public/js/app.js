// AURA. Web Application Router & Controller

// Utility helpers
function formatBDT(amount) {
  return '৳' + Number(amount || 0).toLocaleString('en-US');
}

function showToast(msg) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = msg;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// Global UI State
let activeCategory = 'All';
let activeStyle = 'All';
let searchQuery = '';
let sortBy = 'featured';
let selectedColor = null;
let selectedSize = null;
let selectedQty = 1;

// Router Handler
function getRoute() {
  const hash = window.location.hash || '#home';
  const parts = hash.slice(1).split('/');
  return {
    view: parts[0] || 'home',
    sub: parts[1] || null,
    id: parts[2] || parts[1] || null
  };
}

function navigateTo(hash) {
  window.location.hash = hash;
  window.scrollTo(0, 0);
}

// Update Active Nav Link
function updateNavLinks(view) {
  document.querySelectorAll('.nav-link').forEach(link => {
    const target = link.getAttribute('href').replace('#', '');
    if (target === view || (view === 'home' && target === '')) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });
}

// Update Cart and Wishlist Badges
function updateCounters() {
  const totalCart = window.AURA_STATE.cart.reduce((sum, item) => sum + item.quantity, 0);
  const totalWish = window.AURA_STATE.wishlist.length;
  
  const cartBadge = document.getElementById('cart-badge');
  if (cartBadge) {
    cartBadge.textContent = totalCart;
    cartBadge.style.display = totalCart > 0 ? 'flex' : 'none';
  }
  const wishBadge = document.getElementById('wishlist-badge');
  if (wishBadge) {
    wishBadge.textContent = totalWish;
    wishBadge.style.display = totalWish > 0 ? 'flex' : 'none';
  }
}

// Update Sync Badge
function updateSyncBadge(isConnected) {
  const badge = document.getElementById('sync-status-badge');
  if (badge) {
    badge.innerHTML = `
      <span class="sync-dot" style="background: ${isConnected ? 'var(--success)' : '#f59e0b'}"></span>
      <span>${isConnected ? 'AURA CLOUD SYNC: ACTIVE' : 'AURA OFFLINE MODE'}</span>
    `;
  }
}

// Update Auth UI in Header
function updateAuthUI(user) {
  const authBtn = document.getElementById('auth-action-btn');
  if (!authBtn) return;
  if (user && !user.isAnonymous) {
    authBtn.innerHTML = '<i class="fa-solid fa-user-check"></i>';
    authBtn.title = `Account: ${user.email || 'Member'}`;
  } else {
    authBtn.innerHTML = '<i class="fa-regular fa-user"></i>';
    authBtn.title = 'Sign In';
  }
}

// Cart Drawer Controls
function toggleCartDrawer(open) {
  const drawer = document.getElementById('cart-drawer');
  const backdrop = document.getElementById('cart-backdrop');
  if (drawer && backdrop) {
    if (open) {
      renderCartDrawer();
      drawer.classList.add('open');
      backdrop.classList.add('open');
    } else {
      drawer.classList.remove('open');
      backdrop.classList.remove('open');
    }
  }
}

// Wishlist Toggle
function toggleWishlist(productId) {
  const list = window.AURA_STATE.wishlist;
  const idx = list.indexOf(productId);
  if (idx > -1) {
    list.splice(idx, 1);
    showToast("Removed from wishlist");
  } else {
    list.push(productId);
    showToast("Saved to wishlist");
  }
  localStorage.setItem('aura_wishlist', JSON.stringify(list));
  updateCounters();
  renderCurrentView();
}

// Cart Operations
function addToCart(product, color, size, quantity = 1) {
  const cart = window.AURA_STATE.cart;
  const existing = cart.find(item => item.productId === product.id && item.color.name === color.name && item.size === size);
  
  // Stock Check
  const availableStock = product.stockPerSize ? (product.stockPerSize[size] || 0) : 10;
  const currentInCart = existing ? existing.quantity : 0;
  
  if (currentInCart + quantity > availableStock) {
    showToast(`Sorry, only ${availableStock} units available in size ${size}`);
    return false;
  }

  if (existing) {
    existing.quantity += quantity;
  } else {
    cart.push({
      id: 'cart-' + Date.now(),
      productId: product.id,
      name: product.name,
      price: product.price,
      image: product.image,
      color: color,
      size: size,
      quantity: quantity
    });
  }

  localStorage.setItem('aura_cart', JSON.stringify(cart));
  updateCounters();
  showToast(`Added ${quantity}× ${product.name} (${size}) to bag`);
  toggleCartDrawer(true);
  return true;
}

function updateCartQty(cartItemId, delta) {
  const cart = window.AURA_STATE.cart;
  const item = cart.find(x => x.id === cartItemId);
  if (!item) return;

  const product = window.AURA_STATE.products.find(p => p.id === item.productId);
  const maxStock = (product && product.stockPerSize) ? (product.stockPerSize[item.size] || 99) : 99;

  item.quantity += delta;
  if (item.quantity > maxStock) {
    item.quantity = maxStock;
    showToast(`Max available stock reached (${maxStock})`);
  }
  if (item.quantity <= 0) {
    window.AURA_STATE.cart = cart.filter(x => x.id !== cartItemId);
    showToast("Item removed from bag");
  }
  localStorage.setItem('aura_cart', JSON.stringify(window.AURA_STATE.cart));
  updateCounters();
  renderCartDrawer();
  if (getRoute().view === 'cart') renderCurrentView();
}

// Calculations
function getCartTotals() {
  const items = window.AURA_STATE.cart;
  const subtotal = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  
  let discount = 0;
  const coupon = window.AURA_STATE.appliedCoupon;
  if (coupon && subtotal >= coupon.minSpend) {
    if (coupon.isFreeShipping) {
      discount = 0; // handled in delivery fee
    } else {
      discount = Math.round((subtotal * coupon.discountPercent) / 100);
    }
  }

  let deliveryFee = 100;
  if (subtotal >= 3000 || (coupon && coupon.isFreeShipping)) {
    deliveryFee = 0;
  }
  if (items.length === 0) deliveryFee = 0;

  const total = Math.max(0, subtotal - discount + deliveryFee);
  return { subtotal, discount, deliveryFee, total };
}

// Render Cart in Slide-out Drawer
function renderCartDrawer() {
  const container = document.getElementById('drawer-cart-items');
  const footer = document.getElementById('drawer-cart-footer');
  if (!container || !footer) return;

  const cart = window.AURA_STATE.cart;
  if (cart.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 60px 20px;">
        <i class="fa-solid fa-bag-shopping" style="font-size: 48px; color: var(--border-dark); margin-bottom: 16px;"></i>
        <h3 style="font-size: 18px; font-weight: 800; text-transform: uppercase;">YOUR BAG IS EMPTY</h3>
        <p style="font-size: 13px; color: var(--text-sub); margin: 8px 0 24px;">Discover our heavyweight silhouettes and new drops.</p>
        <button onclick="toggleCartDrawer(false); navigateTo('#shop');" class="btn-dark" style="width: 100%;">EXPLORE COLLECTION</button>
      </div>
    `;
    footer.style.display = 'none';
    return;
  }

  footer.style.display = 'block';
  container.innerHTML = cart.map(item => `
    <div class="cart-item">
      <img src="${item.image}" alt="${item.name}" class="cart-item-img" onerror="this.src='/assets/img_hero_fashion.jpg'">
      <div class="cart-item-details">
        <div class="cart-item-name">${item.name}</div>
        <div class="cart-item-meta">Color: ${item.color.name} • Size: ${item.size}</div>
        <div class="cart-item-bottom">
          <div class="price-current">${formatBDT(item.price)}</div>
          <div class="qty-stepper">
            <button class="qty-btn" onclick="updateCartQty('${item.id}', -1)"><i class="fa-solid fa-minus"></i></button>
            <div class="qty-val">${item.quantity}</div>
            <button class="qty-btn" onclick="updateCartQty('${item.id}', 1)"><i class="fa-solid fa-plus"></i></button>
          </div>
        </div>
      </div>
    </div>
  `).join('');

  const { subtotal, discount, deliveryFee, total } = getCartTotals();
  footer.innerHTML = `
    <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px; color: var(--text-sub);">
      <span>Subtotal</span>
      <span>${formatBDT(subtotal)}</span>
    </div>
    ${discount > 0 ? `
      <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px; color: var(--success);">
        <span>Discount (${window.AURA_STATE.appliedCoupon.code})</span>
        <span>-${formatBDT(discount)}</span>
      </div>
    ` : ''}
    <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 12px; color: var(--text-sub);">
      <span>Delivery</span>
      <span>${deliveryFee === 0 ? '<strong style="color: var(--success)">FREE</strong>' : formatBDT(deliveryFee)}</span>
    </div>
    <div style="display: flex; justify-content: space-between; font-size: 16px; font-weight: 900; margin-bottom: 18px; border-top: 1px solid var(--border); padding-top: 12px;">
      <span>Total</span>
      <span>${formatBDT(total)}</span>
    </div>
    <button onclick="toggleCartDrawer(false); navigateTo('#checkout');" class="btn-dark" style="width: 100%; margin-bottom: 8px;">CHECKOUT NOW</button>
    <button onclick="toggleCartDrawer(false); navigateTo('#cart');" class="btn-luxury-outline" style="width: 100%; border-color: var(--border); color: var(--text-main);">VIEW BAG DETAILS</button>
  `;
}

// -------------------------------------------------------------
// VIEWS RENDERING
// -------------------------------------------------------------

// 1. HOME VIEW
function renderHomeView(app) {
  const publishedProducts = window.AURA_STATE.products.filter(p => p.published !== false);
  const featured = publishedProducts.filter(p => p.isFeatured || p.isNew).slice(0, 4);
  const drops = publishedProducts.filter(p => p.isDrop).slice(0, 4);

  app.innerHTML = `
    <!-- Hero Banner -->
    <div class="container">
      <section class="hero-banner">
        <img src="/assets/img_hero_fashion.jpg" alt="AURA Hero" class="hero-bg-img" onerror="this.style.opacity='0.2'">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <div class="hero-pill">
            <span class="dot"></span>
            <span>DROP 04 : MONOLITH IS LIVE</span>
          </div>
          <h1 class="hero-title">GIVES YOU<br>DIFFERENT VIBE</h1>
          <p class="hero-subtitle">Architectural cuts, heavyweight 280 GSM custom French terry, and relaxed luxury silhouettes engineered in Dhaka for global presence.</p>
          <div class="hero-actions">
            <button onclick="navigateTo('#shop')" class="btn-luxury">
              <span>EXPLORE COLLECTION</span>
              <i class="fa-solid fa-arrow-right"></i>
            </button>
            <button onclick="navigateTo('#lookbook')" class="btn-luxury-outline">
              <span>VIEW LOOKBOOK</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Category Quick Chips -->
      <div class="category-bar">
        ${['All', 'Tees', 'Hoodies', 'Pants', 'Accessories', 'Men', 'Unisex'].map(cat => `
          <div class="cat-chip ${activeCategory === cat ? 'active' : ''}" onclick="filterCategory('${cat}')">
            ${cat === 'All' ? '<i class="fa-solid fa-border-all"></i>' : ''}
            <span>${cat}</span>
          </div>
        `).join('')}
      </div>

      <!-- Featured New Arrivals -->
      <div class="section-header">
        <div>
          <h2 class="section-title">SIGNATURE PIECES</h2>
          <div class="section-subtitle">Heavyweight essentials designed to stand the test of time</div>
        </div>
        <a href="#shop" class="section-link">VIEW ALL (${publishedProducts.length}) <i class="fa-solid fa-arrow-right"></i></a>
      </div>

      <div class="product-grid">
        ${featured.map(renderProductCardHtml).join('')}
      </div>

      <!-- Flash Sale / Drop Capsule -->
      <section class="capsule-card">
        <div>
          <div class="capsule-badge"><i class="fa-solid fa-bolt"></i> LIMITED TIME CAPSULE</div>
          <h2 class="capsule-title">MIDNIGHT SHADOW DROP</h2>
          <p class="capsule-desc">Our limited-run 450 GSM luxury French Terry hoodies and cargo silhouettes. Use code <strong style="color:#fff">AURANEW</strong> for 15% off.</p>
          <div class="countdown-box">
            <div class="countdown-unit"><div class="countdown-num" id="cd-hours">07</div><div class="countdown-lbl">Hours</div></div>
            <div class="countdown-unit"><div class="countdown-num" id="cd-mins">48</div><div class="countdown-lbl">Mins</div></div>
            <div class="countdown-unit"><div class="countdown-num" id="cd-secs">20</div><div class="countdown-lbl">Secs</div></div>
          </div>
          <button onclick="navigateTo('#shop')" class="btn-luxury">CLAIM YOUR PIECE <i class="fa-solid fa-arrow-right"></i></button>
        </div>
        <div>
          <img src="/assets/img_lookbook_urban.jpg" alt="Capsule Drop" style="border-radius: var(--radius-md); max-height: 380px; width:100%; object-fit: cover;">
        </div>
      </section>

      <!-- Curated Lookbook Grid -->
      <div class="section-header">
        <div>
          <h2 class="section-title">CURRENT DROPS & CAPSULES</h2>
          <div class="section-subtitle">Exclusive limited-edition street essentials</div>
        </div>
        <a href="#shop" class="section-link">SHOP DROPS <i class="fa-solid fa-arrow-right"></i></a>
      </div>

      <div class="product-grid">
        ${drops.map(renderProductCardHtml).join('')}
      </div>

      <!-- Journal Teaser -->
      <section style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 40px; margin: 48px 0;">
        <div style="max-width: 680px;">
          <div style="font-size: 11px; font-weight: 800; letter-spacing: 0.1em; color: var(--gold); text-transform: uppercase; margin-bottom: 8px;">AURA JOURNAL • ISSUE 04</div>
          <h3 style="font-size: 28px; font-weight: 900; letter-spacing: -0.02em; margin-bottom: 12px;">WHY 280 GSM IS THE GOLDEN RATIO FOR HEAVYWEIGHT STREETWEAR</h3>
          <p style="font-size: 14px; color: var(--text-sub); line-height: 1.6; margin-bottom: 20px;">We spent seven months experimenting with yarn twist rates and fabric densities in Dhaka mills before settling on our signature 280 GSM French jersey. It drapes with structural integrity that never clings.</p>
          <a href="#journal" class="btn-dark" style="display: inline-flex;">READ FULL ARTICLE <i class="fa-solid fa-arrow-right" style="margin-left: 8px;"></i></a>
        </div>
      </section>
    </div>
  `;

  // Start live countdown ticker
  startCountdownTimer();
}

// 2. SHOP VIEW
function renderShopView(app) {
  let products = window.AURA_STATE.products.filter(p => p.published !== false);

  if (activeCategory !== 'All') {
    products = products.filter(p => p.category.toLowerCase() === activeCategory.toLowerCase());
  }

  if (activeStyle !== 'All') {
    products = products.filter(p => p.style && p.style.toLowerCase() === activeStyle.toLowerCase());
  }

  if (searchQuery.trim()) {
    const q = searchQuery.toLowerCase();
    products = products.filter(p => p.name.toLowerCase().includes(q) || p.description.toLowerCase().includes(q) || (p.tags && p.tags.some(t => t.toLowerCase().includes(q))));
  }

  // Sorting
  if (sortBy === 'price-low') {
    products.sort((a, b) => a.price - b.price);
  } else if (sortBy === 'price-high') {
    products.sort((a, b) => b.price - a.price);
  } else if (sortBy === 'rating') {
    products.sort((a, b) => (b.rating || 0) - (a.rating || 0));
  }

  app.innerHTML = `
    <div class="container" style="padding-top: 24px;">
      <!-- Shop Header & Filters -->
      <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px; margin-bottom: 32px;">
        <div style="display: flex; flex-wrap: wrap; gap: 16px; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <div>
            <h1 style="font-size: 26px; font-weight: 900; letter-spacing: -0.02em; text-transform: uppercase;">AURA CATALOGUE</h1>
            <div style="font-size: 13px; color: var(--text-sub);">Showing ${products.length} products</div>
          </div>
          <!-- Search Box -->
          <div style="display: flex; align-items: center; background: var(--surface-sub); border: 1px solid var(--border); border-radius: 999px; padding: 8px 16px; min-width: 280px;">
            <i class="fa-solid fa-magnifying-glass" style="color: var(--text-sub); margin-right: 10px;"></i>
            <input type="text" id="shop-search-input" value="${searchQuery}" placeholder="Search oversized tee, cargo, hoodie..." style="border: none; background: transparent; outline: none; font-size: 13px; width: 100%;" oninput="handleSearch(this.value)">
            ${searchQuery ? `<button onclick="handleSearch('')" style="color: var(--text-sub); font-size: 12px;"><i class="fa-solid fa-xmark"></i></button>` : ''}
          </div>
        </div>

        <!-- Filter Rows -->
        <div style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center;">
          <span style="font-size: 12px; font-weight: 800; text-transform: uppercase; color: var(--text-sub); margin-right: 6px;">Category:</span>
          ${['All', 'Tees', 'Hoodies', 'Pants', 'Accessories', 'Men', 'Unisex'].map(c => `
            <button class="cat-chip ${activeCategory === c ? 'active' : ''}" style="padding: 6px 14px; font-size: 12px;" onclick="filterCategory('${c}')">${c}</button>
          `).join('')}

          <div style="margin-left: auto; display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 12px; font-weight: 800; text-transform: uppercase; color: var(--text-sub);">Sort:</span>
            <select onchange="handleSort(this.value)" style="padding: 8px 12px; background: var(--surface-sub); border: 1px solid var(--border); border-radius: 6px; font-size: 12px; font-weight: 700; outline: none;">
              <option value="featured" ${sortBy === 'featured' ? 'selected' : ''}>Featured Drops</option>
              <option value="price-low" ${sortBy === 'price-low' ? 'selected' : ''}>Price: Low to High</option>
              <option value="price-high" ${sortBy === 'price-high' ? 'selected' : ''}>Price: High to Low</option>
              <option value="rating" ${sortBy === 'rating' ? 'selected' : ''}>Customer Rating</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Product Grid -->
      ${products.length > 0 ? `
        <div class="product-grid">
          ${products.map(renderProductCardHtml).join('')}
        </div>
      ` : `
        <div style="text-align: center; padding: 80px 20px; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md);">
          <i class="fa-solid fa-magnifying-glass" style="font-size: 40px; color: var(--text-muted); margin-bottom: 16px;"></i>
          <h3 style="font-size: 20px; font-weight: 800;">NO MATCHING SILHOUETTES FOUND</h3>
          <p style="font-size: 13px; color: var(--text-sub); margin: 8px 0 24px;">Try searching for "tee", "cargo", or clearing your filters.</p>
          <button onclick="filterCategory('All'); handleSearch('');" class="btn-dark">RESET ALL FILTERS</button>
        </div>
      `}
    </div>
  `;
}

// 3. PRODUCT DETAILS VIEW
function renderProductDetailView(app, productId) {
  const product = window.AURA_STATE.products.find(p => p.id === productId);
  if (!product) {
    app.innerHTML = `
      <div class="container" style="text-align: center; padding: 100px 20px;">
        <h2>Product Not Found</h2>
        <button onclick="navigateTo('#shop')" class="btn-dark" style="margin-top: 16px;">Return to Shop</button>
      </div>
    `;
    return;
  }

  // Set default selection
  if (!selectedColor && product.colors && product.colors.length) selectedColor = product.colors[0];
  if (!selectedSize && product.sizes && product.sizes.length) selectedSize = product.sizes[0];
  
  const currentStock = (product.stockPerSize && selectedSize) ? (product.stockPerSize[selectedSize] || 0) : 10;
  const isOutOfStock = currentStock === 0;

  app.innerHTML = `
    <div class="container" style="padding-top: 32px;">
      <!-- Breadcrumb -->
      <div style="font-size: 12px; color: var(--text-sub); margin-bottom: 24px; text-transform: uppercase; letter-spacing: 0.06em;">
        <a href="#home">Home</a> / <a href="#shop">Shop</a> / <a href="#shop" onclick="filterCategory('${product.category}')">${product.category}</a> / <strong style="color: var(--text-main);">${product.name}</strong>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 48px; margin-bottom: 64px;" class="product-detail-layout">
        <!-- Gallery -->
        <div>
          <div style="border-radius: var(--radius-md); overflow: hidden; background: #18181b; aspect-ratio: 4/5; border: 1px solid var(--border); margin-bottom: 16px;">
            <img id="main-product-img" src="${product.image}" alt="${product.name}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='/assets/img_hero_fashion.jpg'">
          </div>
          <div style="display: flex; gap: 12px;">
            <img src="${product.image}" alt="Thumb" style="width: 80px; height: 100px; object-fit: cover; border-radius: 6px; border: 2px solid var(--primary); cursor: pointer;" onclick="document.getElementById('main-product-img').src=this.src">
            <img src="/assets/img_complete_look.jpg" alt="Thumb" style="width: 80px; height: 100px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border); cursor: pointer;" onclick="document.getElementById('main-product-img').src=this.src">
            <img src="/assets/img_lookbook_urban.jpg" alt="Thumb" style="width: 80px; height: 100px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border); cursor: pointer;" onclick="document.getElementById('main-product-img').src=this.src">
          </div>
        </div>

        <!-- Information & Purchasing -->
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <span class="badge-tag" style="position: static;">${product.badge || 'AURA DROP'}</span>
            <div style="display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 700;">
              <i class="fa-solid fa-star" style="color: var(--gold);"></i>
              <span>${product.rating || 4.9}</span>
              <span style="color: var(--text-sub);">(${product.reviewCount || 42} reviews)</span>
            </div>
          </div>

          <h1 style="font-size: 34px; font-weight: 900; letter-spacing: -0.03em; margin-bottom: 8px; line-height: 1.1;">${product.name}</h1>
          <div style="font-size: 14px; color: var(--text-sub); margin-bottom: 20px;">${product.tagline}</div>

          <!-- Price Display -->
          <div style="display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid var(--border);">
            <div style="font-size: 28px; font-weight: 900;">${formatBDT(product.price)}</div>
            ${product.originalPrice ? `<div style="font-size: 18px; color: var(--text-muted); text-decoration: line-through;">${formatBDT(product.originalPrice)}</div>` : ''}
            <span style="background: var(--success); color: #fff; font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 4px;">IN STOCK</span>
          </div>

          <!-- Color Selector -->
          ${product.colors && product.colors.length ? `
            <div style="margin-bottom: 20px;">
              <div style="font-size: 12px; font-weight: 800; text-transform: uppercase; margin-bottom: 10px;">
                COLOR: <span id="color-label" style="color: var(--text-sub);">${selectedColor ? selectedColor.name : ''}</span>
              </div>
              <div style="display: flex; gap: 10px;">
                ${product.colors.map(c => `
                  <div onclick="selectColor('${product.id}', '${c.name}', '${c.hex}')" style="width: 32px; height: 32px; border-radius: 50%; background: ${c.hex}; border: 2px solid ${selectedColor && selectedColor.name === c.name ? 'var(--primary)' : 'var(--border)'}; cursor: pointer; box-shadow: 0 0 0 2px ${selectedColor && selectedColor.name === c.name ? '#fff' : 'transparent'};" title="${c.name}"></div>
                `).join('')}
              </div>
            </div>
          ` : ''}

          <!-- Size Selector with Real Stock Indicator -->
          <div style="margin-bottom: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
              <div style="font-size: 12px; font-weight: 800; text-transform: uppercase;">
                SIZE: <span id="size-label" style="color: var(--text-sub);">${selectedSize}</span>
              </div>
              <div id="stock-feedback" style="font-size: 12px; font-weight: 700; color: ${isOutOfStock ? 'var(--danger)' : currentStock < 6 ? '#f59e0b' : 'var(--success)'};">
                ${isOutOfStock ? 'OUT OF STOCK' : currentStock < 6 ? `Only ${currentStock} pieces left` : `${currentStock} pieces available`}
              </div>
            </div>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
              ${(product.sizes || ['S', 'M', 'L', 'XL', 'XXL']).map(s => {
                const sStock = (product.stockPerSize && product.stockPerSize[s] !== undefined) ? product.stockPerSize[s] : 10;
                const isSizeOut = sStock === 0;
                return `
                  <button onclick="selectSize('${product.id}', '${s}')" class="cat-chip ${selectedSize === s ? 'active' : ''}" style="min-width: 48px; justify-content: center; ${isSizeOut ? 'opacity: 0.4; text-decoration: line-through;' : ''}">
                    ${s}
                  </button>
                `;
              }).join('')}
            </div>
          </div>

          <!-- Actions -->
          <div style="display: flex; gap: 12px; margin-bottom: 32px;">
            <button onclick="handleAddToCart('${product.id}')" class="btn-dark" style="flex: 2; padding: 16px;" ${isOutOfStock ? 'disabled' : ''}>
              <i class="fa-solid fa-bag-shopping"></i>
              <span>${isOutOfStock ? 'OUT OF STOCK' : 'ADD TO BAG'}</span>
            </button>
            <button onclick="handleBuyNow('${product.id}')" class="btn-luxury-outline" style="flex: 1; border-color: var(--primary); color: var(--primary); padding: 16px;" ${isOutOfStock ? 'disabled' : ''}>
              BUY NOW
            </button>
            <button onclick="toggleWishlist('${product.id}')" class="wishlist-toggle" style="position: static; width: 54px; height: 54px; border: 1px solid var(--border);">
              <i class="fa-${window.AURA_STATE.wishlist.includes(product.id) ? 'solid' : 'regular'} fa-heart" style="${window.AURA_STATE.wishlist.includes(product.id) ? 'color: var(--danger)' : ''}"></i>
            </button>
          </div>

          <!-- Silhouette Specs Accordion -->
          <div style="border-top: 1px solid var(--border); padding-top: 24px;">
            <h4 style="font-size: 13px; font-weight: 800; text-transform: uppercase; margin-bottom: 8px;">CRAFT & SPECIFICATIONS</h4>
            <p style="font-size: 13px; color: var(--text-sub); line-height: 1.6; margin-bottom: 16px;">${product.description}</p>
            <ul style="list-style: none; font-size: 12px; color: var(--text-sub); display: flex; flex-direction: column; gap: 8px;">
              <li><strong>Fabric:</strong> ${product.fabric || '100% French Terry Cotton'}</li>
              <li><strong>Fit:</strong> ${product.fit || 'Boxy Oversized drop shoulder'}</li>
              <li><strong>Care:</strong> ${product.care || 'Cold wash inside out'}</li>
              <li><strong>Delivery:</strong> Express Courier via Pathao (48h Dhaka / 72h Nationwide)</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  `;
}

// 4. CART FULL VIEW
function renderCartView(app) {
  const cart = window.AURA_STATE.cart;
  const { subtotal, discount, deliveryFee, total } = getCartTotals();

  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 64px;">
      <h1 style="font-size: 28px; font-weight: 900; text-transform: uppercase; margin-bottom: 24px;">YOUR BAG (${cart.reduce((s, i) => s + i.quantity, 0)} ITEMS)</h1>
      
      ${cart.length > 0 ? `
        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 40px;" class="cart-layout">
          <div>
            ${cart.map(item => `
              <div style="display: flex; gap: 20px; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px; margin-bottom: 16px; align-items: center;">
                <img src="${item.image}" alt="${item.name}" style="width: 90px; height: 110px; object-fit: cover; border-radius: 6px; background: #18181b;" onerror="this.src='/assets/img_hero_fashion.jpg'">
                <div style="flex: 1;">
                  <h3 style="font-size: 16px; font-weight: 800; margin-bottom: 4px;">${item.name}</h3>
                  <div style="font-size: 13px; color: var(--text-sub); margin-bottom: 12px;">Color: ${item.color.name} • Size: ${item.size}</div>
                  <div style="font-size: 17px; font-weight: 900;">${formatBDT(item.price * item.quantity)}</div>
                </div>
                <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 16px;">
                  <button onclick="updateCartQty('${item.id}', -${item.quantity})" style="color: var(--text-sub); font-size: 13px;"><i class="fa-solid fa-trash"></i></button>
                  <div class="qty-stepper">
                    <button class="qty-btn" onclick="updateCartQty('${item.id}', -1)"><i class="fa-solid fa-minus"></i></button>
                    <div class="qty-val">${item.quantity}</div>
                    <button class="qty-btn" onclick="updateCartQty('${item.id}', 1)"><i class="fa-solid fa-plus"></i></button>
                  </div>
                </div>
              </div>
            `).join('')}
          </div>

          <!-- Order Summary -->
          <div>
            <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px;">
              <h3 style="font-size: 16px; font-weight: 800; text-transform: uppercase; margin-bottom: 16px;">ORDER SUMMARY</h3>
              
              <!-- Coupon Box -->
              <div style="display: flex; gap: 8px; margin-bottom: 16px;">
                <input type="text" id="coupon-code-input" placeholder="Coupon (e.g. AURA10)" style="flex: 1; padding: 10px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px; outline: none; text-transform: uppercase;">
                <button onclick="applyCouponCode()" class="btn-dark" style="padding: 10px 16px; font-size: 12px;">APPLY</button>
              </div>
              ${window.AURA_STATE.appliedCoupon ? `
                <div style="font-size: 12px; color: var(--success); font-weight: 700; margin-bottom: 16px;">
                  ✓ Coupon '${window.AURA_STATE.appliedCoupon.code}' applied!
                </div>
              ` : ''}

              <div style="display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; color: var(--text-sub);">
                <span>Subtotal</span>
                <span>${formatBDT(subtotal)}</span>
              </div>
              ${discount > 0 ? `
                <div style="display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; color: var(--success);">
                  <span>Discount</span>
                  <span>-${formatBDT(discount)}</span>
                </div>
              ` : ''}
              <div style="display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 16px; color: var(--text-sub);">
                <span>Pathao Courier Delivery</span>
                <span>${deliveryFee === 0 ? '<strong style="color: var(--success)">FREE</strong>' : formatBDT(deliveryFee)}</span>
              </div>
              <div style="display: flex; justify-content: space-between; font-size: 18px; font-weight: 900; margin-bottom: 24px; border-top: 1px solid var(--border); padding-top: 16px;">
                <span>Total</span>
                <span>${formatBDT(total)}</span>
              </div>
              <button onclick="navigateTo('#checkout')" class="btn-dark" style="width: 100%; padding: 16px;">PROCEED TO CHECKOUT</button>
            </div>
          </div>
        </div>
      ` : `
        <div style="text-align: center; padding: 80px 20px; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md);">
          <i class="fa-solid fa-bag-shopping" style="font-size: 48px; color: var(--text-muted); margin-bottom: 16px;"></i>
          <h2 style="font-size: 22px; font-weight: 800;">YOUR BAG IS EMPTY</h2>
          <p style="font-size: 13px; color: var(--text-sub); margin: 8px 0 24px;">Explore our signature oversized tees and architectural silhouettes.</p>
          <button onclick="navigateTo('#shop')" class="btn-dark">START SHOPPING</button>
        </div>
      `}
    </div>
  `;
}

// 5. CHECKOUT VIEW
function renderCheckoutView(app) {
  const cart = window.AURA_STATE.cart;
  if (cart.length === 0) {
    navigateTo('#cart');
    return;
  }
  const { subtotal, discount, deliveryFee, total } = getCartTotals();

  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 80px;">
      <h1 style="font-size: 28px; font-weight: 900; text-transform: uppercase; margin-bottom: 24px;">SECURE CHECKOUT</h1>

      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 40px;" class="checkout-layout">
        <!-- Shipping & Payment Form -->
        <div>
          <!-- Step 1: Contact & Address -->
          <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px; margin-bottom: 24px;">
            <h3 style="font-size: 16px; font-weight: 800; text-transform: uppercase; margin-bottom: 16px; display: flex; align-items: center; gap: 10px;">
              <span style="width: 24px; height: 24px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px;">1</span>
              <span>DELIVERY ADDRESS</span>
            </h3>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
              <div>
                <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 6px;">Full Name *</label>
                <input type="text" id="chk-name" value="${window.AURA_STATE.user ? (window.AURA_STATE.user.displayName || '') : 'Tanvir Ahmed'}" required style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
              </div>
              <div>
                <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 6px;">Phone Number *</label>
                <input type="tel" id="chk-phone" value="+880 1712 345678" required style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
              </div>
            </div>
            <div style="margin-bottom: 16px;">
              <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 6px;">Street Address / Apartment *</label>
              <input type="text" id="chk-address" value="House 42, Road 11, Banani" required style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <div>
                <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 6px;">City / Region *</label>
                <select id="chk-city" style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
                  <option value="Dhaka">Dhaka (Inside City)</option>
                  <option value="Chittagong">Chittagong</option>
                  <option value="Sylhet">Sylhet</option>
                  <option value="Rajshahi">Rajshahi</option>
                  <option value="Other">Other Nationwide</option>
                </select>
              </div>
              <div>
                <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 6px;">Courier Service</label>
                <select id="chk-courier" style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
                  <option value="Pathao Express">Pathao Express (Recommended)</option>
                  <option value="RedX">RedX Logistics</option>
                  <option value="Steadfast">Steadfast Courier</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Step 2: Payment Method -->
          <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px;">
            <h3 style="font-size: 16px; font-weight: 800; text-transform: uppercase; margin-bottom: 16px; display: flex; align-items: center; gap: 10px;">
              <span style="width: 24px; height: 24px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px;">2</span>
              <span>PAYMENT METHOD</span>
            </h3>
            <div style="display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px;">
              <label style="display: flex; align-items: center; gap: 12px; padding: 14px; border: 1px solid var(--border); border-radius: 8px; cursor: pointer;">
                <input type="radio" name="payment-method" value="bKash Online Payment" checked>
                <div>
                  <div style="font-size: 13px; font-weight: 800;">bKash Mobile Banking</div>
                  <div style="font-size: 11px; color: var(--text-sub);">Instant automated confirmation via bKash payment gateway</div>
                </div>
              </label>
              <label style="display: flex; align-items: center; gap: 12px; padding: 14px; border: 1px solid var(--border); border-radius: 8px; cursor: pointer;">
                <input type="radio" name="payment-method" value="Cash on Delivery">
                <div>
                  <div style="font-size: 13px; font-weight: 800;">Cash on Delivery (COD)</div>
                  <div style="font-size: 11px; color: var(--text-sub);">Pay upon delivery after inspecting parcel with Pathao rider</div>
                </div>
              </label>
            </div>
            <button onclick="handlePlaceOrder()" class="btn-dark" style="width: 100%; padding: 18px; font-size: 14px;">
              <span>PLACE ORDER & CONFIRM</span>
              <i class="fa-solid fa-arrow-right"></i>
            </button>
          </div>
        </div>

        <!-- Summary Column -->
        <div>
          <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px;">
            <h3 style="font-size: 15px; font-weight: 800; text-transform: uppercase; margin-bottom: 16px;">ITEMS IN BAG</h3>
            <div style="max-height: 240px; overflow-y: auto; margin-bottom: 16px;">
              ${cart.map(item => `
                <div style="display: flex; gap: 12px; margin-bottom: 12px; align-items: center;">
                  <img src="${item.image}" alt="${item.name}" style="width: 48px; height: 60px; object-fit: cover; border-radius: 4px;" onerror="this.src='/assets/img_hero_fashion.jpg'">
                  <div style="flex: 1;">
                    <div style="font-size: 13px; font-weight: 800;">${item.name}</div>
                    <div style="font-size: 11px; color: var(--text-sub);">${item.color.name} • ${item.size} × ${item.quantity}</div>
                  </div>
                  <div style="font-size: 13px; font-weight: 800;">${formatBDT(item.price * item.quantity)}</div>
                </div>
              `).join('')}
            </div>
            <div style="border-top: 1px solid var(--border); padding-top: 12px; font-size: 13px; display: flex; justify-content: space-between; margin-bottom: 6px; color: var(--text-sub);">
              <span>Subtotal</span>
              <span>${formatBDT(subtotal)}</span>
            </div>
            ${discount > 0 ? `
              <div style="font-size: 13px; display: flex; justify-content: space-between; margin-bottom: 6px; color: var(--success);">
                <span>Discount</span>
                <span>-${formatBDT(discount)}</span>
              </div>
            ` : ''}
            <div style="font-size: 13px; display: flex; justify-content: space-between; margin-bottom: 12px; color: var(--text-sub);">
              <span>Delivery</span>
              <span>${deliveryFee === 0 ? '<strong style="color: var(--success)">FREE</strong>' : formatBDT(deliveryFee)}</span>
            </div>
            <div style="border-top: 1px solid var(--border); padding-top: 12px; font-size: 16px; font-weight: 900; display: flex; justify-content: space-between;">
              <span>Total Payable</span>
              <span>${formatBDT(total)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

// 6. ACCOUNT & ORDERS VIEW
function renderAccountView(app, subTab) {
  const user = window.AURA_STATE.user;
  const orders = window.AURA_STATE.orders;

  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 80px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <h1 style="font-size: 28px; font-weight: 900; text-transform: uppercase;">AURA MEMBER PROFILE</h1>
        ${user && !user.isAnonymous ? `
          <button onclick="handleLogout()" class="cat-chip" style="color: var(--danger);"><i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out</button>
        ` : `
          <button onclick="navigateTo('#login')" class="btn-dark" style="padding: 8px 16px; font-size: 12px;">SIGN IN / REGISTER</button>
        `}
      </div>

      <div style="display: grid; grid-template-columns: 1fr 3fr; gap: 32px;" class="account-layout">
        <!-- Sidebar -->
        <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 20px;">
          <div style="display: flex; align-items: center; gap: 12px; padding-bottom: 16px; border-bottom: 1px solid var(--border); margin-bottom: 16px;">
            <div style="width: 44px; height: 44px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 900;">
              ${user ? (user.email ? user.email.slice(0, 2).toUpperCase() : 'AU') : 'AU'}
            </div>
            <div>
              <div style="font-size: 14px; font-weight: 800;">${user ? (user.displayName || 'AURA Member') : 'AURA Member'}</div>
              <div style="font-size: 12px; color: var(--text-sub);">${user ? (user.email || 'guest@aura.fashion') : 'Guest Session'}</div>
            </div>
          </div>
          <div style="display: flex; flex-direction: column; gap: 4px;">
            <button onclick="navigateTo('#account/orders')" class="cat-chip" style="width: 100%; border: none; justify-content: flex-start; ${subTab === 'orders' || !subTab ? 'background: var(--primary); color: #fff;' : ''}">
              <i class="fa-solid fa-box-open"></i> My Orders (${orders.length})
            </button>
            <button onclick="navigateTo('#account/addresses')" class="cat-chip" style="width: 100%; border: none; justify-content: flex-start; ${subTab === 'addresses' ? 'background: var(--primary); color: #fff;' : ''}">
              <i class="fa-solid fa-location-dot"></i> Saved Addresses
            </button>
            <button onclick="navigateTo('#wishlist')" class="cat-chip" style="width: 100%; border: none; justify-content: flex-start;">
              <i class="fa-solid fa-heart"></i> My Wishlist
            </button>
          </div>
        </div>

        <!-- Content Area -->
        <div>
          ${subTab === 'addresses' ? `
            <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px;">
              <h3 style="font-size: 18px; font-weight: 800; margin-bottom: 16px;">SAVED DELIVERY ADDRESSES</h3>
              <div style="border: 1px solid var(--border); border-radius: 8px; padding: 16px; margin-bottom: 12px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                  <strong>Default Residence</strong>
                  <span style="font-size: 11px; background: var(--surface-sub); padding: 2px 8px; border-radius: 4px;">Primary</span>
                </div>
                <p style="font-size: 13px; color: var(--text-sub);">House 42, Road 11, Banani, Dhaka-1213<br>Contact: +880 1712 345678</p>
              </div>
            </div>
          ` : `
            <!-- Orders List -->
            <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 24px;">
              <h3 style="font-size: 18px; font-weight: 800; margin-bottom: 16px;">ORDER HISTORY & LIVE TRACKING</h3>
              ${orders.length > 0 ? `
                <div style="display: flex; flex-direction: column; gap: 16px;">
                  ${orders.map(order => `
                    <div style="border: 1px solid var(--border); border-radius: 8px; padding: 20px;">
                      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid var(--border); padding-bottom: 12px;">
                        <div>
                          <span style="font-size: 14px; font-weight: 900;">ORDER #${order.id}</span>
                          <span style="font-size: 12px; color: var(--text-sub); margin-left: 8px;">${order.courier || 'Pathao Express'} • ${order.trackingId || 'PTH-99214'}</span>
                        </div>
                        <span class="badge-tag" style="position: static; background: ${order.status === 'Delivered' ? 'var(--success)' : order.status === 'Cancelled' ? 'var(--danger)' : 'var(--primary)'};">
                          ${order.status || 'Processing'}
                        </span>
                      </div>
                      
                      <!-- Timeline Progress Bar -->
                      <div style="display: flex; justify-content: space-between; align-items: center; position: relative; margin: 20px 0 24px;">
                        <div style="position: absolute; top: 50%; left: 10%; right: 10%; height: 2px; background: var(--border); z-index: 1;"></div>
                        ${['Confirmed', 'Processing', 'Packed', 'Shipped', 'Delivered'].map((st, i) => {
                          const statusOrder = ['Confirmed', 'Processing', 'Packed', 'Shipped', 'Delivered'];
                          const currentIdx = statusOrder.indexOf(order.status || 'Confirmed');
                          const reached = currentIdx >= i;
                          return `
                            <div style="position: relative; z-index: 2; text-align: center;">
                              <div style="width: 24px; height: 24px; border-radius: 50%; background: ${reached ? 'var(--primary)' : 'var(--surface-sub)'}; color: ${reached ? '#fff' : 'var(--text-sub)'}; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 800; margin: 0 auto 4px; border: 2px solid #fff;">
                                ${reached ? '✓' : (i + 1)}
                              </div>
                              <div style="font-size: 10px; font-weight: 700; text-transform: uppercase; color: ${reached ? 'var(--text-main)' : 'var(--text-muted)'};">${st}</div>
                            </div>
                          `;
                        }).join('')}
                      </div>

                      <div style="display: flex; justify-content: space-between; align-items: flex-end;">
                        <div style="font-size: 13px; color: var(--text-sub);">
                          Delivery to: ${order.customerName || 'Customer'} (${order.deliveryAddress || 'Dhaka'})
                        </div>
                        <div style="font-size: 17px; font-weight: 900;">
                          ${formatBDT(order.total)}
                        </div>
                      </div>
                    </div>
                  `).join('')}
                </div>
              ` : `
                <div style="text-align: center; padding: 40px 20px;">
                  <i class="fa-solid fa-box-open" style="font-size: 36px; color: var(--text-muted); margin-bottom: 12px;"></i>
                  <p style="font-size: 14px; color: var(--text-sub);">You haven't placed any orders yet.</p>
                  <button onclick="navigateTo('#shop')" class="btn-dark" style="margin-top: 16px;">EXPLORE DROPS</button>
                </div>
              `}
            </div>
          `}
        </div>
      </div>
    </div>
  `;
}

// 7. LOGIN & REGISTER VIEW
function renderLoginView(app) {
  app.innerHTML = `
    <div class="container" style="max-width: 480px; padding: 60px 24px 100px;">
      <div style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 36px;">
        <div style="text-align: center; margin-bottom: 24px;">
          <h2 style="font-size: 24px; font-weight: 900; letter-spacing: -0.03em;">AURA ACCOUNT</h2>
          <p style="font-size: 13px; color: var(--text-sub); margin-top: 4px;">Sign in or create account to track orders and save drops.</p>
        </div>

        <div style="display: flex; gap: 8px; margin-bottom: 20px; border-bottom: 1px solid var(--border); padding-bottom: 8px;">
          <button id="tab-login" onclick="toggleAuthTab('login')" class="cat-chip active" style="flex: 1; justify-content: center;">SIGN IN</button>
          <button id="tab-register" onclick="toggleAuthTab('register')" class="cat-chip" style="flex: 1; justify-content: center;">REGISTER</button>
        </div>

        <form id="auth-form" onsubmit="handleAuthSubmit(event)">
          <div id="register-fields" style="display: none;">
            <div style="margin-bottom: 14px;">
              <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 4px;">Full Name</label>
              <input type="text" id="auth-name" placeholder="Tanvir Ahmed" style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
            </div>
            <div style="margin-bottom: 14px;">
              <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 4px;">Phone Number</label>
              <input type="tel" id="auth-phone" placeholder="+880 1712 345678" style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
            </div>
          </div>

          <div style="margin-bottom: 14px;">
            <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 4px;">Email Address</label>
            <input type="email" id="auth-email" placeholder="member@aura.fashion" required style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
          </div>

          <div style="margin-bottom: 20px;">
            <label style="font-size: 12px; font-weight: 700; display: block; margin-bottom: 4px;">Password</label>
            <input type="password" id="auth-password" placeholder="••••••••" required style="width: 100%; padding: 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 13px;">
          </div>

          <button type="submit" id="auth-submit-btn" class="btn-dark" style="width: 100%; padding: 14px;">SIGN IN</button>
        </form>
      </div>
    </div>
  `;
}

// 8. WISHLIST VIEW
function renderWishlistView(app) {
  const wishlistIds = window.AURA_STATE.wishlist;
  const products = window.AURA_STATE.products.filter(p => wishlistIds.includes(p.id));

  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 80px;">
      <h1 style="font-size: 28px; font-weight: 900; text-transform: uppercase; margin-bottom: 24px;">YOUR SAVED WISHLIST (${products.length})</h1>
      ${products.length > 0 ? `
        <div class="product-grid">
          ${products.map(renderProductCardHtml).join('')}
        </div>
      ` : `
        <div style="text-align: center; padding: 80px 20px; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md);">
          <i class="fa-regular fa-heart" style="font-size: 48px; color: var(--text-muted); margin-bottom: 16px;"></i>
          <h2 style="font-size: 22px; font-weight: 800;">NO ITEMS SAVED YET</h2>
          <p style="font-size: 13px; color: var(--text-sub); margin: 8px 0 24px;">Tap the heart icon on any silhouette to bookmark it for later.</p>
          <button onclick="navigateTo('#shop')" class="btn-dark">EXPLORE PRODUCTS</button>
        </div>
      `}
    </div>
  `;
}

// 9. LOOKBOOK VIEW
function renderLookbookView(app) {
  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 80px;">
      <div style="text-align: center; max-width: 640px; margin: 0 auto 48px;">
        <span style="font-size: 11px; font-weight: 800; letter-spacing: 0.14em; color: var(--gold); text-transform: uppercase;">AURA LOOKBOOK • VOL 01</span>
        <h1 style="font-size: 38px; font-weight: 900; letter-spacing: -0.03em; margin: 8px 0 12px;">URBAN / AFTER DARK</h1>
        <p style="font-size: 14px; color: var(--text-sub); line-height: 1.6;">"Clothes aren't just fabric; they represent the atmosphere you carry into every space."</p>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 32px; margin-bottom: 48px;">
        <img src="/assets/img_complete_look.jpg" alt="Look 1" style="width: 100%; border-radius: var(--radius-md); object-fit: cover; max-height: 600px;">
        <img src="/assets/img_lookbook_urban.jpg" alt="Look 2" style="width: 100%; border-radius: var(--radius-md); object-fit: cover; max-height: 600px;">
      </div>

      <div style="text-align: center;">
        <button onclick="navigateTo('#shop')" class="btn-dark">SHOP THE COMPLETE LOOKS</button>
      </div>
    </div>
  `;
}

// 10. JOURNAL VIEW
function renderJournalView(app) {
  app.innerHTML = `
    <div class="container" style="padding: 32px 24px 80px; max-width: 860px;">
      <div style="text-align: center; margin-bottom: 48px;">
        <span style="font-size: 11px; font-weight: 800; letter-spacing: 0.14em; color: var(--gold); text-transform: uppercase;">AURA EDITORIAL JOURNAL</span>
        <h1 style="font-size: 36px; font-weight: 900; letter-spacing: -0.03em; margin: 8px 0;">CRAFT, TEXTILE & STREETWEAR</h1>
      </div>

      <article style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 36px; margin-bottom: 32px;">
        <div style="font-size: 12px; font-weight: 800; color: var(--gold); margin-bottom: 6px;">TEXTILE SCIENCE • OCT 24, 2026</div>
        <h2 style="font-size: 24px; font-weight: 900; margin-bottom: 12px;">WHY 280 GSM IS THE GOLDEN RATIO FOR HEAVYWEIGHT STREETWEAR</h2>
        <p style="font-size: 14px; color: var(--text-sub); line-height: 1.7; margin-bottom: 16px;">In standard fast fashion, tees run around 140 to 180 GSM. They feel soft on day one, but after two laundry cycles they lose structural drape and the collar ripples. For AURA, we engineered a custom tight-knit 280 GSM combed organic cotton. It hangs with deliberate architectural drape, doesn't cling, and ages with character just like raw denim.</p>
      </article>

      <article style="background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 36px;">
        <div style="font-size: 12px; font-weight: 800; color: var(--gold); margin-bottom: 6px;">STYLE GUIDE • NOV 02, 2026</div>
        <h2 style="font-size: 24px; font-weight: 900; margin-bottom: 12px;">HOW TO STYLE OVERSIZED SILHOUETTES WITHOUT LOOKING DISPROPORTIONATE</h2>
        <p style="font-size: 14px; color: var(--text-sub); line-height: 1.7;">The key to wearing oversized silhouettes gracefully is tension and contrast. If your top is wide and boxy with dropped shoulders, pair it with structured pants that cinch at the ankle, creating a clean break right above your footwear. Add a single focused accessory like our matte steel pendant to draw the eye vertically.</p>
      </article>
    </div>
  `;
}

// -------------------------------------------------------------
// HELPER COMPONENT RENDERERS
// -------------------------------------------------------------

function renderProductCardHtml(p) {
  const isWish = window.AURA_STATE.wishlist.includes(p.id);
  const totalStock = p.stockPerSize ? Object.values(p.stockPerSize).reduce((a, b) => a + b, 0) : 10;
  const isOut = totalStock === 0;

  return `
    <div class="product-card" onclick="navigateTo('#product/${p.id}')" style="cursor: pointer;">
      <div class="product-img-box">
        <img src="${p.image}" alt="${p.name}" class="product-img" onerror="this.src='/assets/img_hero_fashion.jpg'">
        ${p.badge ? `<span class="badge-tag ${p.isDrop ? 'drop' : ''}">${p.badge}</span>` : ''}
        <button class="wishlist-toggle ${isWish ? 'active' : ''}" onclick="event.stopPropagation(); toggleWishlist('${p.id}')">
          <i class="fa-${isWish ? 'solid' : 'regular'} fa-heart"></i>
        </button>
      </div>
      <div class="product-info">
        <div class="product-cat">${p.category}</div>
        <div class="product-name">${p.name}</div>
        <div class="product-tagline">${p.tagline}</div>
        <div class="product-footer">
          <div class="price-box">
            <span class="price-current">${formatBDT(p.price)}</span>
            ${p.originalPrice ? `<span class="price-original">${formatBDT(p.originalPrice)}</span>` : ''}
          </div>
          <span class="stock-indicator ${isOut ? 'out-of-stock' : totalStock < 6 ? 'low-stock' : 'in-stock'}">
            ${isOut ? 'Out of Stock' : totalStock < 6 ? 'Low Stock' : 'In Stock'}
          </span>
        </div>
      </div>
    </div>
  `;
}

// Countdown Timer Handler
function startCountdownTimer() {
  let secondsLeft = 7 * 3600 + 48 * 60 + 19;
  const hEl = document.getElementById('cd-hours');
  const mEl = document.getElementById('cd-mins');
  const sEl = document.getElementById('cd-secs');
  
  if (window.timerInterval) clearInterval(window.timerInterval);
  window.timerInterval = setInterval(() => {
    secondsLeft--;
    if (secondsLeft < 0) secondsLeft = 24 * 3600;
    const h = Math.floor(secondsLeft / 3600);
    const m = Math.floor((secondsLeft % 3600) / 60);
    const s = secondsLeft % 60;
    if (hEl) hEl.textContent = String(h).padStart(2, '0');
    if (mEl) mEl.textContent = String(m).padStart(2, '0');
    if (sEl) sEl.textContent = String(s).padStart(2, '0');
  }, 1000);
}

// -------------------------------------------------------------
// EVENT HANDLERS
// -------------------------------------------------------------

function filterCategory(cat) {
  activeCategory = cat;
  if (getRoute().view !== 'shop') {
    navigateTo('#shop');
  } else {
    renderCurrentView();
  }
}

function handleSearch(q) {
  searchQuery = q;
  renderCurrentView();
}

function handleSort(val) {
  sortBy = val;
  renderCurrentView();
}

function selectColor(productId, colorName, hex) {
  selectedColor = { name: colorName, hex: hex };
  const lbl = document.getElementById('color-label');
  if (lbl) lbl.textContent = colorName;
}

function selectSize(productId, size) {
  selectedSize = size;
  const product = window.AURA_STATE.products.find(p => p.id === productId);
  if (!product) return;
  const stock = (product.stockPerSize && product.stockPerSize[size] !== undefined) ? product.stockPerSize[size] : 10;
  
  const lbl = document.getElementById('size-label');
  if (lbl) lbl.textContent = size;
  
  const fb = document.getElementById('stock-feedback');
  if (fb) {
    if (stock === 0) {
      fb.textContent = 'OUT OF STOCK';
      fb.style.color = 'var(--danger)';
    } else if (stock < 6) {
      fb.textContent = `Only ${stock} pieces left`;
      fb.style.color = '#f59e0b';
    } else {
      fb.textContent = `${stock} pieces available`;
      fb.style.color = 'var(--success)';
    }
  }

  // Update button state
  document.querySelectorAll('.cat-chip').forEach(btn => {
    if (btn.textContent.trim() === size) {
      btn.classList.add('active');
    } else if (['S', 'M', 'L', 'XL', 'XXL', 'One Size'].includes(btn.textContent.trim())) {
      btn.classList.remove('active');
    }
  });
}

function handleAddToCart(productId) {
  const product = window.AURA_STATE.products.find(p => p.id === productId);
  if (!product) return;
  const color = selectedColor || (product.colors && product.colors[0]) || { name: 'Standard', hex: '#111' };
  const size = selectedSize || (product.sizes && product.sizes[0]) || 'L';
  addToCart(product, color, size, selectedQty);
}

function handleBuyNow(productId) {
  const ok = handleAddToCart(productId);
  if (ok) {
    toggleCartDrawer(false);
    navigateTo('#checkout');
  }
}

function applyCouponCode() {
  const input = document.getElementById('coupon-code-input');
  if (!input) return;
  const code = input.value.trim().toUpperCase();
  const coupon = window.AURA_STATE.coupons.find(c => c.code === code);
  
  if (!coupon) {
    showToast("Invalid coupon code");
    return;
  }
  
  const totals = getCartTotals();
  if (totals.subtotal < coupon.minSpend) {
    showToast(`Minimum order of ৳${coupon.minSpend} required for this coupon`);
    return;
  }

  window.AURA_STATE.appliedCoupon = coupon;
  showToast(`Coupon '${code}' applied successfully!`);
  renderCurrentView();
}

async function handlePlaceOrder() {
  const name = document.getElementById('chk-name').value.trim();
  const phone = document.getElementById('chk-phone').value.trim();
  const address = document.getElementById('chk-address').value.trim();
  const city = document.getElementById('chk-city').value;
  const courier = document.getElementById('chk-courier').value;
  const paymentMethod = document.querySelector('input[name="payment-method"]:checked').value;

  if (!name || !phone || !address) {
    showToast("Please complete all shipping details");
    return;
  }

  const { subtotal, discount, deliveryFee, total } = getCartTotals();
  const orderId = 'AURA-' + Math.floor(1000 + Math.random() * 9000);
  const trackingId = 'PTH-DH-' + Math.floor(100000 + Math.random() * 900000);

  const newOrder = {
    id: orderId,
    userId: window.AURA_STATE.user ? window.AURA_STATE.user.uid : 'guest',
    customerName: name,
    customerPhone: phone,
    deliveryAddress: `${address}, ${city}`,
    courier: courier,
    trackingId: trackingId,
    paymentMethod: paymentMethod,
    items: [...window.AURA_STATE.cart],
    subtotal: subtotal,
    discount: discount,
    deliveryFee: deliveryFee,
    total: total,
    status: 'Confirmed',
    createdAt: Date.now()
  };

  // Clear Cart
  window.AURA_STATE.cart = [];
  window.AURA_STATE.appliedCoupon = null;
  localStorage.setItem('aura_cart', '[]');
  updateCounters();

  // Save to Firebase
  await createFirebaseOrder(newOrder);

  showToast(`Order #${orderId} Confirmed!`);
  navigateTo('#account/orders');
}

// Auth Handlers
function toggleAuthTab(tab) {
  const regFields = document.getElementById('register-fields');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const btn = document.getElementById('auth-submit-btn');

  if (tab === 'register') {
    regFields.style.display = 'block';
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    btn.textContent = 'CREATE AURA ACCOUNT';
  } else {
    regFields.style.display = 'none';
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    btn.textContent = 'SIGN IN';
  }
}

async function handleAuthSubmit(e) {
  e.preventDefault();
  const email = document.getElementById('auth-email').value.trim();
  const password = document.getElementById('auth-password').value;
  const isRegister = document.getElementById('tab-register').classList.contains('active');
  const name = document.getElementById('auth-name') ? document.getElementById('auth-name').value.trim() : '';

  if (!auth) {
    showToast("Firebase Auth unavailable, operating in demo session");
    navigateTo('#account');
    return;
  }

  try {
    if (isRegister) {
      const cred = await auth.createUserWithEmailAndPassword(email, password);
      if (name && cred.user) {
        await cred.user.updateProfile({ displayName: name });
      }
      showToast("Welcome to AURA!");
    } else {
      await auth.signInWithEmailAndPassword(email, password);
      showToast("Signed in successfully");
    }
    navigateTo('#account');
  } catch (err) {
    showToast(err.message || "Authentication failed");
  }
}

async function handleLogout() {
  if (auth) {
    await auth.signOut();
  }
  window.AURA_STATE.user = null;
  showToast("Signed out");
  navigateTo('#home');
}

// -------------------------------------------------------------
// ADMIN LIVE SYNC TESTING DRAWER
// -------------------------------------------------------------
function toggleAdminDrawer(open) {
  let drawer = document.getElementById('admin-drawer');
  let backdrop = document.getElementById('admin-backdrop');
  if (!drawer) {
    drawer = document.createElement('div');
    drawer.id = 'admin-drawer';
    drawer.className = 'drawer-panel';
    drawer.style.maxWidth = '460px';
    backdrop = document.createElement('div');
    backdrop.id = 'admin-backdrop';
    backdrop.className = 'drawer-backdrop';
    backdrop.onclick = () => toggleAdminDrawer(false);
    document.body.appendChild(backdrop);
    document.body.appendChild(drawer);
  }

  if (open) {
    renderAdminDrawer(drawer);
    drawer.classList.add('open');
    backdrop.classList.add('open');
  } else {
    drawer.classList.remove('open');
    backdrop.classList.remove('open');
  }
}

function renderAdminDrawer(drawer) {
  const products = window.AURA_STATE.products;
  const orders = window.AURA_STATE.orders;

  drawer.innerHTML = `
    <div class="drawer-header">
      <div class="drawer-title" style="display: flex; align-items: center; gap: 8px;">
        <i class="fa-solid fa-server" style="color: var(--success)"></i>
        <span>FIREBASE LIVE SYNC PANEL</span>
      </div>
      <button class="drawer-close" onclick="toggleAdminDrawer(false)"><i class="fa-solid fa-xmark"></i></button>
    </div>
    <div class="drawer-body">
      <div style="font-size: 12px; color: var(--text-sub); margin-bottom: 20px; line-height: 1.5; background: var(--surface-sub); padding: 12px; border-radius: 6px;">
        <strong>Shared Firebase (aura-f4291) Live Tester:</strong><br>
        Verify tests #1 - #10. Modifying any value writes directly to Firestore and reflects instantly on the user website!
      </div>

      <!-- Test 1 & 2: Edit Price -->
      <div style="border-bottom: 1px solid var(--border); padding-bottom: 20px; margin-bottom: 20px;">
        <h4 style="font-size: 13px; font-weight: 800; margin-bottom: 8px;">TEST 2: CHANGE PRODUCT PRICE (FIRESTORE)</h4>
        <div style="display: flex; gap: 8px; margin-bottom: 8px;">
          <select id="adm-price-prod" style="flex: 2; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
            ${products.map(p => `<option value="${p.id}">${p.name} (৳${p.price})</option>`).join('')}
          </select>
          <input type="number" id="adm-price-val" placeholder="New Price (BDT)" style="flex: 1; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
        </div>
        <button onclick="executeAdminPriceChange()" class="btn-dark" style="width: 100%; padding: 8px; font-size: 12px;">UPDATE PRICE LIVE</button>
      </div>

      <!-- Test 3: Set Variant Stock to 0 -->
      <div style="border-bottom: 1px solid var(--border); padding-bottom: 20px; margin-bottom: 20px;">
        <h4 style="font-size: 13px; font-weight: 800; margin-bottom: 8px;">TEST 3: SET VARIANT STOCK TO 0 (OUT OF STOCK)</h4>
        <div style="display: flex; gap: 8px; margin-bottom: 8px;">
          <select id="adm-stock-prod" style="flex: 2; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
            ${products.map(p => `<option value="${p.id}">${p.name}</option>`).join('')}
          </select>
          <select id="adm-stock-size" style="flex: 1; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
            <option value="S">Size S</option>
            <option value="M">Size M</option>
            <option value="L">Size L</option>
            <option value="XL">Size XL</option>
            <option value="XXL">Size XXL</option>
          </select>
        </div>
        <div style="display: flex; gap: 8px;">
          <button onclick="executeAdminStockChange(0)" class="btn-dark" style="flex: 1; padding: 8px; font-size: 12px; background: var(--danger);">SET STOCK = 0</button>
          <button onclick="executeAdminStockChange(15)" class="btn-luxury-outline" style="flex: 1; padding: 8px; font-size: 12px; border-color: var(--border); color: var(--text-main);">RESTOCK (15)</button>
        </div>
      </div>

      <!-- Test 4: Toggle Publish -->
      <div style="border-bottom: 1px solid var(--border); padding-bottom: 20px; margin-bottom: 20px;">
        <h4 style="font-size: 13px; font-weight: 800; margin-bottom: 8px;">TEST 4: UNPUBLISH / PUBLISH PRODUCT</h4>
        <div style="display: flex; gap: 8px;">
          <select id="adm-pub-prod" style="flex: 2; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
            ${products.map(p => `<option value="${p.id}">${p.name} [${p.published !== false ? 'PUBLISHED' : 'HIDDEN'}]</option>`).join('')}
          </select>
          <button onclick="executeAdminPublishToggle()" class="btn-dark" style="flex: 1; padding: 8px; font-size: 12px;">TOGGLE</button>
        </div>
      </div>

      <!-- Test 7: Update Order Status -->
      <div>
        <h4 style="font-size: 13px; font-weight: 800; margin-bottom: 8px;">TEST 7: ADMIN ADVANCES ORDER STATUS</h4>
        ${orders.length > 0 ? `
          <div style="display: flex; gap: 8px; margin-bottom: 8px;">
            <select id="adm-order-id" style="flex: 1; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
              ${orders.map(o => `<option value="${o.id}">#${o.id} - ${o.status || 'Confirmed'}</option>`).join('')}
            </select>
            <select id="adm-order-status" style="flex: 1; padding: 8px; font-size: 12px; border: 1px solid var(--border); border-radius: 4px;">
              <option value="Confirmed">Confirmed</option>
              <option value="Processing">Processing</option>
              <option value="Packed">Packed</option>
              <option value="Shipped">Shipped</option>
              <option value="Out for Delivery">Out for Delivery</option>
              <option value="Delivered">Delivered</option>
              <option value="Cancelled">Cancelled</option>
            </select>
          </div>
          <button onclick="executeAdminOrderStatusChange()" class="btn-dark" style="width: 100%; padding: 8px; font-size: 12px;">UPDATE ORDER STATUS</button>
        ` : `
          <p style="font-size: 12px; color: var(--text-sub);">Place an order first to test live status progression.</p>
        `}
      </div>
    </div>
  `;
}

function executeAdminPriceChange() {
  const prodId = document.getElementById('adm-price-prod').value;
  const price = document.getElementById('adm-price-val').value;
  if (!price) return;
  adminUpdatePrice(prodId, price);
  showToast(`Updated price to ${formatBDT(price)}`);
  renderAdminDrawer(document.getElementById('admin-drawer'));
}

function executeAdminStockChange(qty) {
  const prodId = document.getElementById('adm-stock-prod').value;
  const size = document.getElementById('adm-stock-size').value;
  adminUpdateStock(prodId, size, qty);
  showToast(`Updated ${size} stock to ${qty}`);
  renderAdminDrawer(document.getElementById('admin-drawer'));
}

function executeAdminPublishToggle() {
  const prodId = document.getElementById('adm-pub-prod').value;
  adminTogglePublish(prodId);
  showToast("Product publication status updated");
  renderAdminDrawer(document.getElementById('admin-drawer'));
}

function executeAdminOrderStatusChange() {
  const orderId = document.getElementById('adm-order-id').value;
  const status = document.getElementById('adm-order-status').value;
  adminUpdateOrderStatus(orderId, status);
  showToast(`Order #${orderId} marked as ${status}`);
  renderAdminDrawer(document.getElementById('admin-drawer'));
}

// -------------------------------------------------------------
// MAIN ROUTING DISPATCHER
// -------------------------------------------------------------
function renderCurrentView() {
  const route = getRoute();
  const app = document.getElementById('app-main');
  if (!app) return;

  updateNavLinks(route.view);
  updateCounters();

  switch (route.view) {
    case 'home':
      renderHomeView(app);
      break;
    case 'shop':
    case 'category':
      renderShopView(app);
      break;
    case 'product':
      renderProductDetailView(app, route.id);
      break;
    case 'cart':
      renderCartView(app);
      break;
    case 'wishlist':
      renderWishlistView(app);
      break;
    case 'checkout':
      renderCheckoutView(app);
      break;
    case 'account':
      renderAccountView(app, route.sub);
      break;
    case 'login':
    case 'register':
      renderLoginView(app);
      break;
    case 'lookbook':
      renderLookbookView(app);
      break;
    case 'journal':
      renderJournalView(app);
      break;
    default:
      renderHomeView(app);
  }
}

// Application Boot
window.addEventListener('DOMContentLoaded', () => {
  window.renderCurrentView = renderCurrentView;

  // Initialize Firebase realtime sync
  initFirebaseListeners(
    liveProducts => {
      renderCurrentView();
    },
    liveOrders => {
      renderCurrentView();
    }
  );

  // Hash Routing Listener
  window.addEventListener('hashchange', renderCurrentView);

  // Initial Render
  renderCurrentView();
});
