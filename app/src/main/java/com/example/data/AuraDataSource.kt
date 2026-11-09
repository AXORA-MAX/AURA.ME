package com.example.data

import com.example.R
import com.example.model.*

object AuraDataSource {
    val colorBlack = ColorOption("Onyx Black", 0xFF111111)
    val colorWhite = ColorOption("Chalk White", 0xFFF5F5F5)
    val colorGrey = ColorOption("Heather Grey", 0xFF8E8E93)
    val colorBeige = ColorOption("Desert Sand", 0xFFD8C7B5)
    val colorNavy = ColorOption("Midnight Navy", 0xFF1B263B)

    val sampleProducts = listOf(
        Product(
            id = "aura-p1",
            name = "AURA OVERSIZED TEE",
            tagline = "Heavyweight cotton boxy cut with dropped shoulders",
            price = 1290.0,
            originalPrice = 1590.0,
            category = ProductCategory.TEES,
            style = StyleType.OVERSIZED,
            rating = 4.9f,
            reviewCount = 84,
            availableColors = listOf(colorBlack, colorWhite, colorGrey),
            availableSizes = listOf("S", "M", "L", "XL", "XXL"),
            description = "The cornerstone of the AURA silhouette. Cut from custom-milled 280 GSM combed organic cotton, this oversized tee features a thick 1.25\" rib collar, double-needle cover-stitched seams, and an effortless drop shoulder drape that retains structure wash after wash.",
            fabric = "100% Combed Organic Cotton (280 GSM French Jersey)",
            fit = "Oversized Boxy drape. Size down for true-to-size look.",
            care = "Machine wash cold inside out with like colors. Air dry.",
            stockPerSize = mapOf("S" to 14, "M" to 22, "L" to 16, "XL" to 7, "XXL" to 3),
            imageRes = R.drawable.img_hero_fashion,
            isNew = true,
            isDrop = false,
            isFeatured = true,
            tags = listOf("Bestseller", "Heavyweight", "Oversized")
        ),
        Product(
            id = "aura-p2",
            name = "AURA CARGO TROUSERS",
            tagline = "Modular pleated cargo with bungee drawstring cuffs",
            price = 2490.0,
            originalPrice = 2890.0,
            category = ProductCategory.PANTS,
            style = StyleType.STREETWEAR,
            rating = 4.8f,
            reviewCount = 59,
            availableColors = listOf(colorBlack, colorBeige),
            availableSizes = listOf("S", "M", "L", "XL"),
            description = "Crafted from durable water-repellent ripstop cotton blend, our technical cargo features articulated knee darting, magnetic pocket flaps, and adjustable bungee toggles at the ankles for customizable silhouette transitions.",
            fabric = "Cotton-Poly Ripstop (260 GSM) with matte Teflon finish",
            fit = "Relaxed tapered fit with ankle cinch cords.",
            care = "Hand or delicate machine wash. Iron on reverse.",
            stockPerSize = mapOf("S" to 8, "M" to 12, "L" to 6, "XL" to 2),
            imageRes = R.drawable.img_complete_look,
            isNew = false,
            isDrop = true,
            isFeatured = true,
            tags = listOf("Streetwear", "Technical", "Cinch Cuffs")
        ),
        Product(
            id = "aura-p3",
            name = "AURA SHADOW HOODIE",
            tagline = "450 GSM French Terry heavyweight architectural hoodie",
            price = 3290.0,
            originalPrice = 3800.0,
            category = ProductCategory.HOODIES,
            style = StyleType.PREMIUM,
            rating = 5.0f,
            reviewCount = 112,
            availableColors = listOf(colorBlack, colorGrey, colorNavy),
            availableSizes = listOf("S", "M", "L", "XL"),
            description = "Engineered for maximum presence. Constructed with double-lined hood with zero drawstrings for pure minimalist luxury lines, kangaroo pouch with concealed zip key slot, and pre-shrunk heavyweight loopback terry.",
            fabric = "450 GSM Ultra-Heavyweight French Terry",
            fit = "Structured Boxy Fit with wide body & snug waist rib.",
            care = "Dry clean or cold hand wash. Lay flat to dry.",
            stockPerSize = mapOf("S" to 6, "M" to 14, "L" to 8, "XL" to 4),
            imageRes = R.drawable.img_lookbook_urban,
            isNew = true,
            isDrop = true,
            isFeatured = true,
            isAppExclusive = true,
            tags = listOf("App Exclusive", "Heavyweight", "Minimalist")
        ),
        Product(
            id = "aura-p4",
            name = "AURA MATTE MINIMAL PENDANT",
            tagline = "Surgical grade 316L stainless steel geometric necklace",
            price = 890.0,
            originalPrice = 1150.0,
            category = ProductCategory.ACCESSORIES,
            style = StyleType.PREMIUM,
            rating = 4.7f,
            reviewCount = 38,
            availableColors = listOf(colorBlack, colorGrey),
            availableSizes = listOf("One Size"),
            description = "Subtle modern luxury accent. Laser engraved with the AURA. coordinates and finished in brushed matte graphite PVD coating resistant to tarnishing and sweat.",
            fabric = "316L Solid Stainless Steel with PVD Matte Coating",
            fit = "55cm curb chain with 5cm extension clasp.",
            care = "Wipe with micro-fiber cloth. Waterproof.",
            stockPerSize = mapOf("One Size" to 25),
            imageRes = R.drawable.img_app_icon,
            isNew = false,
            isDrop = false,
            isFeatured = false,
            tags = listOf("Jewelry", "Stainless Steel", "Minimalist")
        ),
        Product(
            id = "aura-p5",
            name = "AURA RELAXED LINEN-TOUCH SHIRT",
            tagline = "Camp collar breathable leisure shirt for warm evenings",
            price = 1890.0,
            originalPrice = 2200.0,
            category = ProductCategory.MEN,
            style = StyleType.RELAXED,
            rating = 4.6f,
            reviewCount = 27,
            availableColors = listOf(colorWhite, colorBeige),
            availableSizes = listOf("S", "M", "L", "XL"),
            description = "Clean silhouette with an open Cuban camp collar, mother-of-pearl buttons, and lightweight textured linen blend for effortless tropical and summer vibes.",
            fabric = "65% Linen, 35% Modal (170 GSM)",
            fit = "Relaxed breezy fit with straight hem and side slits.",
            care = "Cold delicate wash. Low iron when damp.",
            stockPerSize = mapOf("S" to 10, "M" to 15, "L" to 9, "XL" to 3),
            imageRes = R.drawable.img_complete_look,
            isNew = true,
            isDrop = false,
            isFeatured = false,
            tags = listOf("Summer", "Camp Collar", "Linen")
        ),
        Product(
            id = "aura-p6",
            name = "AURA SCULPTED CROPPED BOMBER",
            tagline = "Cropped insulated bomber with high-density silver hardware",
            price = 3990.0,
            originalPrice = 4500.0,
            category = ProductCategory.UNISEX,
            style = StyleType.STREETWEAR,
            rating = 4.9f,
            reviewCount = 43,
            availableColors = listOf(colorBlack, colorNavy),
            availableSizes = listOf("S", "M", "L"),
            description = "A statement silhouette featuring exaggerated dropped sleeves, custom chunky two-way front zip, thermal orange interior lining, and storm flap closure.",
            fabric = "Nylon Twill with 80g Primaloft Eco Insulation",
            fit = "Cropped length with voluminous articulated sleeves.",
            care = "Specialist spot clean or professional dry clean.",
            stockPerSize = mapOf("S" to 5, "M" to 7, "L" to 3),
            imageRes = R.drawable.img_lookbook_urban,
            isNew = false,
            isDrop = true,
            isFeatured = true,
            tags = listOf("Outerwear", "Bomber", "Drop")
        )
    )

    val sampleStyleLooks = listOf(
        StyleLook(
            id = "look-01",
            title = "THE URBAN ARCHITECT",
            description = "A clean monochrome balance: Heavyweight Oversized Tee paired with Technical Cinch Cargo and Minimal Matte Pendant.",
            products = listOf(sampleProducts[0], sampleProducts[1], sampleProducts[3]),
            bundleDiscountPercent = 15,
            coverImageRes = R.drawable.img_complete_look
        ),
        StyleLook(
            id = "look-02",
            title = "AFTER DARK CAPSULE",
            description = "Structured silhouette for night prowls: Heavyweight 450 GSM Hoodie, Relaxed Cinch Bottoms & Sculpted Hardware.",
            products = listOf(sampleProducts[2], sampleProducts[1]),
            bundleDiscountPercent = 12,
            coverImageRes = R.drawable.img_lookbook_urban
        )
    )

    val sampleDrop = NewDrop(
        id = "drop-01",
        title = "DROP 04 : MONOLITH",
        subtitle = "Limited run capsule of heavyweight silhouettes and technical garments. Once sold out, never reproduced.",
        dropDate = "Sunday 9:00 PM BST",
        remainingHours = 7,
        remainingMinutes = 48,
        remainingSeconds = 19,
        previewProducts = listOf(sampleProducts[0], sampleProducts[2], sampleProducts[5]),
        isLive = false,
        isNotified = false
    )

    val sampleLookbooks = listOf(
        LookbookItem(
            id = "lb-01",
            volume = "LOOKBOOK 01",
            title = "URBAN / AFTER DARK",
            season = "AUTUMN / WINTER CAPSULE",
            quote = "\"Clothes aren't just fabric; they represent the atmosphere you carry into every space.\"",
            imageRes = R.drawable.img_lookbook_urban,
            featuredProduct = sampleProducts[2]
        ),
        LookbookItem(
            id = "lb-02",
            volume = "LOOKBOOK 02",
            title = "THE MONOCHROME MONOLITH",
            season = "SIGNATURE STREETWEAR",
            quote = "\"Minimalism is not the absence of energy. It is the purest distillation of presence.\"",
            imageRes = R.drawable.img_hero_fashion,
            featuredProduct = sampleProducts[0]
        )
    )

    val sampleJournal = listOf(
        JournalArticle(
            id = "j-01",
            title = "Why 280 GSM Is The Golden Ratio For Heavyweight Streetwear",
            category = "Fabric & Craft",
            author = "AURA Design Lab",
            date = "Oct 24, 2026",
            readTime = "4 min read",
            excerpt = "We spent seven months experimenting with yarn twist rates and fabric densities in Dhaka mills before settling on our signature 280 GSM French jersey.",
            content = "In standard fast fashion, tees run around 140 to 180 GSM. They feel soft on day one, but after two laundry cycles they lose structural drape and the collar ripples. For AURA, we engineered a custom tight-knit 280 GSM combed cotton. It hangs with deliberate architectural drape, doesn't cling to the body, and ages with character just like raw denim.",
            coverImageRes = R.drawable.img_hero_fashion
        ),
        JournalArticle(
            id = "j-02",
            title = "How To Style Oversized Silhouettes Without Looking Disproportionate",
            category = "Style Guide",
            author = "Nafis R.",
            date = "Nov 02, 2026",
            readTime = "3 min read",
            excerpt = "Mastering boxy proportions relies on intentional contrasts: pairing dropped shoulders with tapered hem trousers or ankle cinch cords.",
            content = "The key to wearing oversized silhouettes gracefully is tension and contrast. If your top is wide and boxy with dropped shoulders, pair it with structured pants that cinch at the ankle, creating a clean break right above your footwear. Add a single focused accessory like our matte steel pendant to draw the eye vertically.",
            coverImageRes = R.drawable.img_complete_look
        ),
        JournalArticle(
            id = "j-03",
            title = "Behind AURA: Building A Distinctive Aesthetic From Dhaka To Global",
            category = "Brand Story",
            author = "Founder's Desk",
            date = "Nov 15, 2026",
            readTime = "5 min read",
            excerpt = "We didn't set out to make another clothing label. We set out to give you a different vibe.",
            content = "AURA was born from a simple dissatisfaction with generic graphic merchandise and soulless fashion drops. We believe modern streetwear should feel like high fashion — thoughtful cuts, premium weight fabrics, understated branding, and an unmistakable personal aura.",
            coverImageRes = R.drawable.img_lookbook_urban
        )
    )

    val sampleCoupons = listOf(
        Coupon("AURA10", 10, 1500.0, "10% OFF on orders over ৳1,500"),
        Coupon("AURANEW", 15, 2500.0, "15% OFF for your first drop order"),
        Coupon("FREESHIP", 100, 3000.0, "Free Delivery on cart above ৳3,000")
    )

    val initialOrders = listOf(
        Order(
            id = "AURA-1024",
            orderDate = "Yesterday, 3:45 PM",
            items = listOf(
                CartItem(
                    id = "ci-01",
                    product = sampleProducts[0],
                    selectedColor = colorBlack,
                    selectedSize = "L",
                    quantity = 1
                ),
                CartItem(
                    id = "ci-02",
                    product = sampleProducts[1],
                    selectedColor = colorBlack,
                    selectedSize = "M",
                    quantity = 1
                )
            ),
            subtotal = 3780.0,
            deliveryFee = 100.0,
            discount = 0.0,
            total = 3880.0,
            status = OrderStatus.SHIPPED,
            customerName = "Tanvir Ahmed",
            customerPhone = "+880 1712 345678",
            deliveryAddress = "House 42, Road 11, Banani, Dhaka-1213",
            paymentMethod = "bKash Online Payment",
            courier = "Pathao Courier Express",
            trackingId = "PTH-DH-982143",
            estimatedDelivery = "Tomorrow by 6:00 PM"
        )
    )
}
