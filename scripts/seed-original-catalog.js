/**
 * AURA Fashion - Original Catalog Seeder (Source of Truth)
 * Exact data extracted from original index.html
 */

const originalProducts = [
  {
    id: "aura-p1",
    name: "AURA OVERSIZED TEE",
    tagline: "Heavyweight cotton boxy cut with dropped shoulders",
    price: 1290,
    original_price: 1590,
    category: "Tees",
    style: "Oversized",
    rating: 4.9,
    review_count: 84,
    badge: "Bestseller",
    description: "The cornerstone of the AURA silhouette. Cut from custom-milled 280 GSM combed organic cotton, this oversized tee features a thick 1.25\" rib collar, double-needle cover-stitched seams, and an effortless drop shoulder drape that retains structure wash after wash.",
    fabric: "100% Combed Organic Cotton (280 GSM French Jersey)",
    fit: "Oversized Boxy drape. Size down for true-to-size look.",
    care: "Machine wash cold inside out with like colors. Air dry.",
    image: "/assets/img_hero_fashion.jpg",
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Chalk White", hex: "#F5F5F5" },
      { name: "Heather Grey", hex: "#8E8E93" }
    ],
    sizes: ["S", "M", "L", "XL", "XXL"],
    stock_per_size: { S: 14, M: 22, L: 16, XL: 7, XXL: 3 },
    is_new: true,
    is_drop: false,
    is_featured: true,
    is_published: true
  },
  {
    id: "aura-p2",
    name: "AURA CARGO TROUSERS",
    tagline: "Modular pleated cargo with bungee drawstring cuffs",
    price: 2490,
    original_price: 2890,
    category: "Pants",
    style: "Streetwear",
    rating: 4.8,
    review_count: 59,
    badge: "New Drop",
    description: "Crafted from durable water-repellent ripstop cotton blend, our technical cargo features articulated knee darting, magnetic pocket flaps, and adjustable bungee toggles at the ankles for customizable silhouette transitions.",
    fabric: "Cotton-Poly Ripstop (260 GSM) with matte Teflon finish",
    fit: "Relaxed tapered fit with ankle cinch cords.",
    care: "Hand or delicate machine wash. Iron on reverse.",
    image: "/assets/img_complete_look.jpg",
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Desert Sand", hex: "#D8C7B5" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stock_per_size: { S: 8, M: 12, L: 6, XL: 2 },
    is_new: false,
    is_drop: true,
    is_featured: true,
    is_published: true
  },
  {
    id: "aura-p3",
    name: "AURA SHADOW HOODIE",
    tagline: "450 GSM French Terry heavyweight architectural hoodie",
    price: 3290,
    original_price: 3800,
    category: "Hoodies",
    style: "Premium",
    rating: 5.0,
    review_count: 112,
    badge: "App Exclusive",
    description: "Engineered for maximum presence. Constructed with double-lined hood with zero drawstrings for pure minimalist luxury lines, kangaroo pouch with concealed zip key slot, and pre-shrunk heavyweight loopback terry.",
    fabric: "450 GSM Ultra-Heavyweight French Terry",
    fit: "Structured Boxy Fit with wide body & snug waist rib.",
    care: "Dry clean or cold hand wash. Lay flat to dry.",
    image: "/assets/img_lookbook_urban.jpg",
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Heather Grey", hex: "#8E8E93" },
      { name: "Midnight Navy", hex: "#1B263B" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stock_per_size: { S: 6, M: 14, L: 8, XL: 4 },
    is_new: true,
    is_drop: true,
    is_featured: true,
    is_published: true
  },
  {
    id: "aura-p4",
    name: "AURA MATTE MINIMAL PENDANT",
    tagline: "Surgical grade 316L stainless steel geometric necklace",
    price: 890,
    original_price: 1150,
    category: "Accessories",
    style: "Premium",
    rating: 4.7,
    review_count: 38,
    badge: "Hardware",
    description: "Subtle modern luxury accent. Laser engraved with the AURA. coordinates and finished in brushed matte graphite PVD coating resistant to tarnishing and sweat.",
    fabric: "316L Solid Stainless Steel with PVD Matte Coating",
    fit: "55cm curb chain with 5cm extension clasp.",
    care: "Wipe with micro-fiber cloth. Waterproof.",
    image: "/assets/img_app_icon.jpg",
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Silver", hex: "#E2E8F0" }
    ],
    sizes: ["One Size"],
    stock_per_size: { "One Size": 25 },
    is_new: false,
    is_drop: false,
    is_featured: false,
    is_published: true
  },
  {
    id: "aura-p5",
    name: "AURA RELAXED LINEN-TOUCH SHIRT",
    tagline: "Camp collar breathable leisure shirt for warm evenings",
    price: 1890,
    original_price: 2200,
    category: "Men",
    style: "Relaxed",
    rating: 4.6,
    review_count: 27,
    badge: "Summer Capsule",
    description: "Clean silhouette with an open Cuban camp collar, mother-of-pearl buttons, and lightweight textured linen blend for effortless tropical and summer vibes.",
    fabric: "65% Linen, 35% Modal (170 GSM)",
    fit: "Relaxed breezy fit with straight hem and side slits.",
    care: "Cold delicate wash. Low iron when damp.",
    image: "/assets/img_complete_look.jpg",
    colors: [
      { name: "Chalk White", hex: "#F5F5F5" },
      { name: "Desert Sand", hex: "#D8C7B5" }
    ],
    sizes: ["S", "M", "L", "XL"],
    stock_per_size: { S: 10, M: 15, L: 9, XL: 3 },
    is_new: true,
    is_drop: false,
    is_featured: false,
    is_published: true
  },
  {
    id: "aura-p6",
    name: "AURA SCULPTED CROPPED BOMBER",
    tagline: "Cropped insulated bomber with high-density silver hardware",
    price: 3990,
    original_price: 4500,
    category: "Unisex",
    style: "Streetwear",
    rating: 4.9,
    review_count: 43,
    badge: "Limited Drop",
    description: "A statement silhouette featuring exaggerated dropped sleeves, custom chunky two-way front zip, thermal orange interior lining, and storm flap closure.",
    fabric: "Nylon Twill with 80g Primaloft Eco Insulation",
    fit: "Cropped length with voluminous articulated sleeves.",
    care: "Specialist spot clean or professional dry clean.",
    image: "/assets/img_lookbook_urban.jpg",
    colors: [
      { name: "Onyx Black", hex: "#111111" },
      { name: "Midnight Navy", hex: "#1B263B" }
    ],
    sizes: ["S", "M", "L"],
    stock_per_size: { S: 5, M: 7, L: 3 },
    is_new: false,
    is_drop: true,
    is_featured: true,
    is_published: true
  }
];

const originalCoupons = [
  { code: "AURA10", discount_percentage: 10, min_order_amount: 1500, description: "10% OFF over ৳1,500", is_free_shipping: false },
  { code: "AURANEW", discount_percentage: 15, min_order_amount: 2500, description: "15% OFF for first drop order", is_free_shipping: false },
  { code: "FREESHIP", discount_percentage: 100, min_order_amount: 3000, description: "Free Delivery over ৳3,000", is_free_shipping: true }
];

module.exports = {
  originalProducts,
  originalCoupons
};
