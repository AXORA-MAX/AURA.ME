// AURA. Firebase Integration Layer (Project: aura-f4291)
const firebaseConfig = {
  apiKey: "AIzaSyDPdl2WAQMqmeGhy357vBTnceir0GV7hQQ",
  authDomain: "aura-f4291.firebaseapp.com",
  projectId: "aura-f4291",
  storageBucket: "aura-f4291.firebasestorage.app",
  messagingSenderId: "1079994325680",
  appId: "1:1079994325680:web:aura-website"
};

// Initialize Firebase
let app, auth, db;
try {
  if (!firebase.apps.length) {
    app = firebase.initializeApp(firebaseConfig);
  } else {
    app = firebase.app();
  }
  auth = firebase.auth();
  db = firebase.firestore();
  console.log("AURA Firebase connected successfully to project:", firebaseConfig.projectId);
} catch (err) {
  console.warn("Firebase initialization warning (offline fallback active):", err);
}

// Initial Standard Products Catalog (From AURA User Android App)
const defaultProducts = [
  {
    id: "aura-p1",
    name: "AURA OVERSIZED TEE",
    tagline: "Heavyweight cotton boxy cut with dropped shoulders",
    price: 1290,
    originalPrice: 1590,
    category: "Tees",
    style: "Oversized",
    rating: 4.9,
    reviewCount: 84,
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Chalk White", hex: "#F5F5F5" },
      { name: "Heather Grey", hex: "#8E8E93" }
    ],
    sizes: ["S", "M", "L", "XL", "XXL"],
    stockPerSize: { S: 14, M: 22, L: 16, XL: 7, XXL: 3 },
    description: "The cornerstone of the AURA silhouette. Cut from custom-milled 280 GSM combed organic cotton, this oversized tee features a thick 1.25\" rib collar, double-needle cover-stitched seams, and an effortless drop shoulder drape that retains structure wash after wash.",
    fabric: "100% Combed Organic Cotton (280 GSM French Jersey)",
    fit: "Oversized Boxy drape. Size down for true-to-size look.",
    care: "Machine wash cold inside out with like colors. Air dry.",
    image: "/assets/img_hero_fashion.jpg",
    badge: "Bestseller",
    isNew: true,
    isDrop: false,
    isFeatured: true,
    published: true
  },
  {
    id: "aura-p2",
    name: "AURA CARGO TROUSERS",
    tagline: "Modular pleated cargo with bungee drawstring cuffs",
    price: 2490,
    originalPrice: 2890,
    category: "Pants",
    style: "Streetwear",
    rating: 4.8,
    reviewCount: 59,
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Desert Sand", hex: "#D8C7B5" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stockPerSize: { S: 8, M: 12, L: 6, XL: 2 },
    description: "Crafted from durable water-repellent ripstop cotton blend, our technical cargo features articulated knee darting, magnetic pocket flaps, and adjustable bungee toggles at the ankles for customizable silhouette transitions.",
    fabric: "Cotton-Poly Ripstop (260 GSM) with matte Teflon finish",
    fit: "Relaxed tapered fit with ankle cinch cords.",
    care: "Hand or delicate machine wash. Iron on reverse.",
    image: "/assets/img_complete_look.jpg",
    badge: "New Drop",
    isNew: false,
    isDrop: true,
    isFeatured: true,
    published: true
  },
  {
    id: "aura-p3",
    name: "AURA SHADOW HOODIE",
    tagline: "450 GSM French Terry heavyweight architectural hoodie",
    price: 3290,
    originalPrice: 3800,
    category: "Hoodies",
    style: "Premium",
    rating: 5.0,
    reviewCount: 112,
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Heather Grey", hex: "#8E8E93" },
      { name: "Midnight Navy", hex: "#1B263B" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stockPerSize: { S: 6, M: 14, L: 8, XL: 4 },
    description: "Engineered for maximum presence. Constructed with double-lined hood with zero drawstrings for pure minimalist luxury lines, kangaroo pouch with concealed zip key slot, and pre-shrunk heavyweight loopback terry.",
    fabric: "450 GSM Ultra-Heavyweight French Terry",
    fit: "Structured Boxy Fit with wide body & snug waist rib.",
    care: "Dry clean or cold hand wash. Lay flat to dry.",
    image: "/assets/img_lookbook_urban.jpg",
    badge: "App Exclusive",
    isNew: true,
    isDrop: true,
    isFeatured: true,
    published: true
  },
  {
    id: "aura-p4",
    name: "AURA MATTE MINIMAL PENDANT",
    tagline: "Surgical grade 316L stainless steel geometric necklace",
    price: 890,
    originalPrice: 1150,
    category: "Accessories",
    style: "Premium",
    rating: 4.7,
    reviewCount: 38,
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Silver", hex: "#E2E8F0" }
    ],
    sizes: ["One Size"],
    stockPerSize: { "One Size": 25 },
    description: "Subtle modern luxury accent. Laser engraved with the AURA. coordinates and finished in brushed matte graphite PVD coating resistant to tarnishing and sweat.",
    fabric: "316L Solid Stainless Steel with PVD Matte Coating",
    fit: "55cm curb chain with 5cm extension clasp.",
    care: "Wipe with micro-fiber cloth. Waterproof.",
    image: "/assets/img_app_icon.jpg",
    badge: "Hardware",
    isNew: false,
    isDrop: false,
    isFeatured: false,
    published: true
  },
  {
    id: "aura-p5",
    name: "AURA RELAXED LINEN-TOUCH SHIRT",
    tagline: "Camp collar breathable leisure shirt for warm evenings",
    price: 1890,
    originalPrice: 2200,
    category: "Men",
    style: "Relaxed",
    rating: 4.6,
    reviewCount: 27,
    colors: [
      { name: "Chalk White", hex: "#F5F5F5" },
      { name: "Desert Sand", hex: "#D8C7B5" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stockPerSize: { S: 10, M: 15, L: 9, XL: 3 },
    description: "Clean silhouette with an open Cuban camp collar, mother-of-pearl buttons, and lightweight textured linen blend for effortless tropical and summer vibes.",
    fabric: "65% Linen, 35% Modal (170 GSM)",
    fit: "Relaxed breezy fit with straight hem and side slits.",
    care: "Cold delicate wash. Low iron when damp.",
    image: "/assets/img_complete_look.jpg",
    badge: "Summer Capsule",
    isNew: true,
    isDrop: false,
    isFeatured: false,
    published: true
  },
  {
    id: "aura-p6",
    name: "AURA SCULPTED CROPPED BOMBER",
    tagline: "Cropped insulated bomber with high-density silver hardware",
    price: 3990,
    originalPrice: 4500,
    category: "Unisex",
    style: "Streetwear",
    rating: 4.9,
    reviewCount: 43,
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Midnight Navy", hex: "#1B263B" }
    ],
    sizes: ["S", "M", "L"],
    stockPerSize: { S: 5, M: 7, L: 3 },
    description: "A statement silhouette featuring exaggerated dropped sleeves, custom chunky two-way front zip, thermal orange interior lining, and storm flap closure.",
    fabric: "Nylon Twill with 80g Primaloft Eco Insulation",
    fit: "Cropped length with voluminous articulated sleeves.",
    care: "Specialist spot clean or professional dry clean.",
    image: "/assets/img_lookbook_urban.jpg",
    badge: "Limited Drop",
    isNew: false,
    isDrop: true,
    isFeatured: true,
    published: true
  }
];

// Active State Cache
window.AURA_STATE = {
  products: [...defaultProducts],
  cart: JSON.parse(localStorage.getItem('aura_cart') || '[]'),
  wishlist: JSON.parse(localStorage.getItem('aura_wishlist') || '["aura-p1", "aura-p3"]'),
  user: null,
  orders: [],
  coupons: [
    { code: "AURA10", discountPercent: 10, minSpend: 1500, desc: "10% OFF over ৳1,500" },
    { code: "AURANEW", discountPercent: 15, minSpend: 2500, desc: "15% OFF for first drop order" },
    { code: "FREESHIP", discountPercent: 100, minSpend: 3000, desc: "Free Delivery over ৳3,000", isFreeShipping: true }
  ],
  appliedCoupon: null,
  firebaseConnected: false
};

// Seed Products to Firestore if empty
async function seedFirestoreProducts() {
  if (!db) return;
  try {
    const snap = await db.collection("products").get();
    if (snap.empty) {
      console.log("Seeding initial products into Firestore...");
      const batch = db.batch();
      defaultProducts.forEach(p => {
        const ref = db.collection("products").document(p.id);
        batch.set(ref, p);
      });
      await batch.commit();
      console.log("Firestore products seeded successfully.");
    }
  } catch (err) {
    console.warn("Firestore seeding notice (operating with local state):", err.message);
  }
}

// Start Realtime Listeners
function initFirebaseListeners(onProductsUpdate, onOrdersUpdate) {
  if (!db) {
    onProductsUpdate(window.AURA_STATE.products);
    return;
  }

  // Realtime Products Listener
  db.collection("products").onSnapshot(snapshot => {
    window.AURA_STATE.firebaseConnected = true;
    updateSyncBadge(true);
    if (!snapshot.empty) {
      const liveList = [];
      snapshot.forEach(doc => {
        const data = doc.data();
        liveList.push({ id: doc.id, ...data });
      });
      window.AURA_STATE.products = liveList;
      onProductsUpdate(liveList);
    } else {
      seedFirestoreProducts();
      onProductsUpdate(window.AURA_STATE.products);
    }
  }, err => {
    console.warn("Products listener error:", err.message);
    window.AURA_STATE.firebaseConnected = false;
    updateSyncBadge(false);
    onProductsUpdate(window.AURA_STATE.products);
  });

  // Auth State Listener
  if (auth) {
    auth.onAuthStateChanged(user => {
      window.AURA_STATE.user = user;
      updateAuthUI(user);
      if (user) {
        listenToUserOrders(user.uid, onOrdersUpdate);
      } else {
        // Anonymous or guest mode
        auth.signInAnonymously().catch(() => {});
      }
    });
  }
}

// Listen to User's Orders in Firestore
function listenToUserOrders(uid, onOrdersUpdate) {
  if (!db) return;
  db.collection("orders")
    .where("userId", "in", [uid, "guest", ""])
    .onSnapshot(snapshot => {
      const orders = [];
      snapshot.forEach(doc => {
        orders.push({ id: doc.id, ...doc.data() });
      });
      // Sort newest first
      orders.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
      window.AURA_STATE.orders = orders;
      if (onOrdersUpdate) onOrdersUpdate(orders);
    }, err => {
      console.warn("Orders listener notice:", err.message);
    });
}

// Save Order to Firestore
async function createFirebaseOrder(orderData) {
  if (db) {
    try {
      await db.collection("orders").doc(orderData.id).set({
        ...orderData,
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      });
      console.log("Order saved to Firestore:", orderData.id);
      return true;
    } catch (e) {
      console.warn("Could not write order to Firestore, saving locally:", e.message);
    }
  }
  // Local fallback
  window.AURA_STATE.orders.unshift(orderData);
  return true;
}

// Admin Operations for Live Testing
async function adminUpdatePrice(productId, newPrice) {
  const p = window.AURA_STATE.products.find(x => x.id === productId);
  if (p) p.price = Number(newPrice);
  if (db) {
    try {
      await db.collection("products").doc(productId).update({ price: Number(newPrice) });
    } catch (e) { console.warn(e); }
  }
  window.renderCurrentView();
}

async function adminUpdateStock(productId, size, stock) {
  const p = window.AURA_STATE.products.find(x => x.id === productId);
  if (p && p.stockPerSize) {
    p.stockPerSize[size] = Number(stock);
  }
  if (db) {
    try {
      await db.collection("products").doc(productId).update({
        [`stockPerSize.${size}`]: Number(stock)
      });
    } catch (e) { console.warn(e); }
  }
  window.renderCurrentView();
}

async function adminTogglePublish(productId) {
  const p = window.AURA_STATE.products.find(x => x.id === productId);
  if (p) {
    p.published = !p.published;
    if (db) {
      try {
        await db.collection("products").doc(productId).update({ published: p.published });
      } catch (e) { console.warn(e); }
    }
    window.renderCurrentView();
  }
}

async function adminUpdateOrderStatus(orderId, newStatus) {
  const o = window.AURA_STATE.orders.find(x => x.id === orderId);
  if (o) o.status = newStatus;
  if (db) {
    try {
      await db.collection("orders").doc(orderId).update({ status: newStatus });
    } catch (e) { console.warn(e); }
  }
  window.renderCurrentView();
}
