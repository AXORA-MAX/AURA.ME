package com.example.model

import androidx.annotation.DrawableRes

enum class ProductCategory(val displayName: String) {
    ALL("All"),
    MEN("Men"),
    WOMEN("Women"),
    UNISEX("Unisex"),
    TEES("T-Shirts"),
    HOODIES("Hoodies"),
    PANTS("Pants"),
    ACCESSORIES("Accessories")
}

enum class StyleType(val displayName: String) {
    ALL("All Styles"),
    OVERSIZED("Oversized"),
    STREETWEAR("Streetwear"),
    RELAXED("Relaxed"),
    CASUAL("Casual"),
    PREMIUM("Premium")
}

data class ColorOption(
    val name: String,
    val hexColor: Long
)

data class Product(
    val id: String,
    val name: String,
    val tagline: String = "Engineered for distinct personal vibe",
    val price: Double,
    val originalPrice: Double? = null,
    val category: ProductCategory,
    val style: StyleType,
    val rating: Float = 4.8f,
    val reviewCount: Int = 42,
    val availableColors: List<ColorOption>,
    val availableSizes: List<String> = listOf("S", "M", "L", "XL", "XXL"),
    val description: String,
    val fabric: String = "100% French Terry Heavyweight Cotton (280 GSM)",
    val fit: String = "Boxy Oversized drop-shoulder silhouette",
    val care: String = "Cold wash inside out. Do not tumble dry.",
    val stockPerSize: Map<String, Int> = mapOf("S" to 12, "M" to 20, "L" to 15, "XL" to 5),
    @DrawableRes val imageRes: Int = 0,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val videoURL: String? = null,
    val categoryId: String = "",
    val collectionId: String = "",
    val sku: String = "",
    val compareAtPrice: Double? = null,
    val discountPrice: Double? = null,
    val currency: String = "BDT",
    val published: Boolean = true,
    val deleted: Boolean = false,
    val featured: Boolean = false,
    val bestSeller: Boolean = false,
    val newArrival: Boolean = false,
    val flashSale: Boolean = false,
    val lowStockThreshold: Int = 8,
    val isNew: Boolean = false,
    val isDrop: Boolean = false,
    val isFeatured: Boolean = false,
    val isAppExclusive: Boolean = false,
    val tags: List<String> = listOf("Streetwear", "Minimalist")
) {
    val totalStock: Int get() = stockPerSize.values.sum()
    val isLowStock: Boolean get() = totalStock in 1..lowStockThreshold
    val isOutOfStock: Boolean get() = totalStock == 0
}

data class StyleLook(
    val id: String,
    val title: String,
    val description: String,
    val products: List<Product>,
    val bundleDiscountPercent: Int = 15,
    @DrawableRes val coverImageRes: Int
) {
    val originalTotal: Double get() = products.sumOf { it.price }
    val bundleTotal: Double get() = originalTotal * (1 - bundleDiscountPercent / 100.0)
}

data class NewDrop(
    val id: String,
    val title: String,
    val subtitle: String,
    val dropDate: String,
    val remainingHours: Int = 8,
    val remainingMinutes: Int = 24,
    val remainingSeconds: Int = 36,
    val previewProducts: List<Product>,
    val isLive: Boolean = false,
    val isNotified: Boolean = false
)

data class CartItem(
    val id: String,
    val product: Product,
    val selectedColor: ColorOption,
    val selectedSize: String,
    val quantity: Int = 1
) {
    val totalPrice: Double get() = product.price * quantity
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    CONFIRMED("Order Confirmed", 0),
    PROCESSING("Processing", 1),
    PACKED("Packed", 2),
    SHIPPED("Shipped", 3),
    OUT_FOR_DELIVERY("Out for Delivery", 4),
    DELIVERED("Delivered", 5),
    CANCELLED("Cancelled", -1)
}

data class Order(
    val id: String,
    val userId: String = "",
    val orderDate: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double = 100.0,
    val discount: Double = 0.0,
    val total: Double,
    val status: OrderStatus,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val paymentMethod: String,
    val courier: String = "Pathao Express / RedX",
    val trackingId: String,
    val estimatedDelivery: String
)

data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val imageUrl: String = "",
    val actionType: String = "COLLECTION",
    val actionTarget: String = "",
    val active: Boolean = true
)

data class CategoryItem(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val subcategories: List<String> = emptyList(),
    val active: Boolean = true
)

data class ReviewItem(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Float = 5.0f,
    val comment: String,
    val verifiedPurchase: Boolean = true,
    val status: String = "APPROVED",
    val date: String = "Recent"
)

data class AuraUser(
    val uid: String,
    val name: String = "AURA Member",
    val email: String = "",
    val phone: String = "",
    val profileImage: String? = null,
    val isAnonymous: Boolean = false
)

data class LookbookItem(
    val id: String,
    val volume: String,
    val title: String,
    val season: String,
    val quote: String,
    @DrawableRes val imageRes: Int,
    val featuredProduct: Product
)

data class JournalArticle(
    val id: String,
    val title: String,
    val category: String,
    val author: String,
    val date: String,
    val readTime: String,
    val excerpt: String,
    val content: String,
    @DrawableRes val coverImageRes: Int
)

data class Coupon(
    val code: String,
    val discountPercent: Int,
    val minSpend: Double,
    val description: String
)

// ================= ENTERPRISE BUSINESS OPERATING MODELS =================

data class Warehouse(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val capacityUnits: Int,
    val activeUnits: Int
)

data class ProductCostBreakdown(
    val sellingPrice: Double,
    val costPrice: Double,
    val packagingCost: Double = 30.0,
    val deliveryCost: Double = 80.0,
    val paymentFee: Double = 25.0
) {
    val totalCost: Double get() = costPrice + packagingCost + deliveryCost + paymentFee
    val netProfit: Double get() = sellingPrice - totalCost
    val marginPercent: Int get() = ((netProfit / sellingPrice) * 100).toInt()
}

enum class CustomerSegment(val label: String, val colorHex: Long) {
    NEW("New Customer", 0xFF3B82F6),
    ACTIVE("Active Buyer", 0xFF10B981),
    REPEAT("Repeat Buyer", 0xFF8B5CF6),
    VIP("VIP Luxury", 0xFFF59E0B),
    INACTIVE("Lapsed / Inactive", 0xFF9CA3AF)
}

data class CustomerProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalOrders: Int,
    val totalSpent: Double,
    val segment: CustomerSegment,
    val points: Int,
    val lastActive: String
)

data class AbandonedCart(
    val id: String,
    val customerName: String,
    val phone: String,
    val itemsSummary: String,
    val cartValue: Double,
    val hoursAgo: Int,
    var isRecovered: Boolean = false
)

enum class ReturnStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    EXCHANGED("Exchange Dispatched"),
    REFUNDED("Refund Processed"),
    REJECTED("Rejected")
}

data class ReturnRequest(
    val id: String,
    val orderId: String,
    val customerName: String,
    val reason: String,
    val refundAmount: Double,
    val status: ReturnStatus,
    val date: String
)

data class FlashSaleConfig(
    val title: String = "MIDNIGHT FLASH DROP",
    val discountPercent: Int = 20,
    val remainingSeconds: Long = 14400, // 4 hours
    val isActive: Boolean = true,
    val productsIncludedCount: Int = 8
)

data class SupportTicket(
    val id: String,
    val customerName: String,
    val orderId: String,
    val issue: String,
    val priority: String = "HIGH",
    val status: String = "OPEN",
    val time: String = "20m ago"
)

data class HomepageSectionItem(
    val id: String,
    val name: String,
    val description: String,
    var isEnabled: Boolean = true
)

