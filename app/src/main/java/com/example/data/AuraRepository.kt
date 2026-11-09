package com.example.data

import android.util.Log
import com.example.data.firebase.AuraFirebaseService
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuraRepository private constructor() {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "AuraRepository"

    private val _products = MutableStateFlow(AuraDataSource.sampleProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(setOf("aura-p1", "aura-p3"))
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(
        listOf(
            CartItem(
                id = "ci-init-1",
                product = AuraDataSource.sampleProducts[0],
                selectedColor = AuraDataSource.colorBlack,
                selectedSize = "L",
                quantity = 1
            ),
            CartItem(
                id = "ci-init-2",
                product = AuraDataSource.sampleProducts[1],
                selectedColor = AuraDataSource.colorBlack,
                selectedSize = "M",
                quantity = 1
            )
        )
    )
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _orders = MutableStateFlow(AuraDataSource.initialOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _activeDrop = MutableStateFlow(AuraDataSource.sampleDrop)
    val activeDrop: StateFlow<NewDrop> = _activeDrop.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("black oversized tee", "cargo pants", "hoodie", "minimalist accessories"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _currentUser = MutableStateFlow<AuraUser?>(
        AuraUser(uid = "aura-guest-uid", name = "AURA Member", email = "member@aura.fashion")
    )
    val currentUser: StateFlow<AuraUser?> = _currentUser.asStateFlow()

    // Enterprise Operational Data
    private val _warehouses = MutableStateFlow(
        listOf(
            Warehouse("wh-1", "Dhaka Central Fulfilment", "Dhaka", "Tejgaon I/A, Dhaka-1208", 5000, 3420),
            Warehouse("wh-2", "Chittagong Coastal Hub", "Chittagong", "Agrabad C/A, Chittagong", 2500, 1180),
            Warehouse("wh-3", "Sylhet Express Hub", "Sylhet", "Subidbazar, Sylhet", 1200, 430)
        )
    )
    val warehouses: StateFlow<List<Warehouse>> = _warehouses.asStateFlow()

    private val _abandonedCarts = MutableStateFlow(
        listOf(
            AbandonedCart("ac-1", "Zubair Rahman", "+880 1711 928341", "Acid Wash Boxy Tee (L) × 1, Cargo (32) × 1", 4780.0, 2),
            AbandonedCart("ac-2", "Fahim Ahmed", "+880 1819 443210", "Monolith Raw Edge Hoodie (XL) × 1", 3490.0, 5),
            AbandonedCart("ac-3", "Tasmia Chowdhury", "+880 1912 889901", "Midnight Black Minimal Tee (M) × 2", 3380.0, 11)
        )
    )
    val abandonedCarts: StateFlow<List<AbandonedCart>> = _abandonedCarts.asStateFlow()

    private val _customers = MutableStateFlow(
        listOf(
            CustomerProfile("cust-1", "Tanvir Hasan", "tanvir.h@gmail.com", "+880 1712 345678", 12, 28450.0, CustomerSegment.VIP, 380, "2 hrs ago"),
            CustomerProfile("cust-2", "Nusrat Jahan", "nusrat.j@yahoo.com", "+880 1819 876543", 6, 14200.0, CustomerSegment.REPEAT, 150, "Yesterday"),
            CustomerProfile("cust-3", "Abrar Sakib", "abrar.sakib@outlook.com", "+880 1911 223344", 2, 4980.0, CustomerSegment.ACTIVE, 60, "3 days ago"),
            CustomerProfile("cust-4", "Mahir Faysal", "mahir.f@gmail.com", "+880 1610 998877", 1, 1890.0, CustomerSegment.NEW, 20, "Today"),
            CustomerProfile("cust-5", "Sadia Karim", "sadia.k@gmail.com", "+880 1515 443322", 4, 8600.0, CustomerSegment.INACTIVE, 90, "45 days ago")
        )
    )
    val customers: StateFlow<List<CustomerProfile>> = _customers.asStateFlow()

    private val _returnRequests = MutableStateFlow(
        listOf(
            ReturnRequest("ret-101", "AURA-1021", "Tanvir Hasan", "Wrong Size (Need XL instead of L)", 1890.0, ReturnStatus.PENDING, "Today"),
            ReturnRequest("ret-102", "AURA-1019", "Nusrat Jahan", "Stitching imperfection on cuff", 3490.0, ReturnStatus.APPROVED, "Yesterday")
        )
    )
    val returnRequests: StateFlow<List<ReturnRequest>> = _returnRequests.asStateFlow()

    private val _flashSale = MutableStateFlow(FlashSaleConfig())
    val flashSale: StateFlow<FlashSaleConfig> = _flashSale.asStateFlow()

    private val _supportTickets = MutableStateFlow(
        listOf(
            SupportTicket("tkt-1", "Zubair Rahman", "AURA-1024", "When will my order ship via Pathao?", "HIGH", "OPEN", "15m ago"),
            SupportTicket("tkt-2", "Fahim Ahmed", "AURA-1020", "Can I change my delivery address to Banani?", "MEDIUM", "OPEN", "1h ago")
        )
    )
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _homepageSections = MutableStateFlow(
        listOf(
            HomepageSectionItem("sec-hero", "Hero Editorial Campaign", "Main video/image banner with drop countdown", true),
            HomepageSectionItem("sec-tiles", "Curated Outfit & Style Guide Tiles", "Dual interactive routing cards", true),
            HomepageSectionItem("sec-drop", "Scheduled Drop Strip", "Live drop alert and notification pill", true),
            HomepageSectionItem("sec-styles", "Style with AURA. Bundles", "Curated full lookbook outfit builder with 15% discount", true),
            HomepageSectionItem("sec-trending", "Core Trending Collection", "Product 2-column fashion grid", true),
            HomepageSectionItem("sec-journal", "Editorial Journal & Culture", "Brand articles and design philosophy", true)
        )
    )
    val homepageSections: StateFlow<List<HomepageSectionItem>> = _homepageSections.asStateFlow()

    init {
        initializeFirebaseSync()
    }

    private fun initializeFirebaseSync() {
        repositoryScope.launch {
            try {
                // Ensure authentication with Firebase Auth
                val uid = AuraFirebaseService.ensureAuthenticated()
                _currentUser.value = AuraUser(
                    uid = uid,
                    name = "AURA Member",
                    email = AuraFirebaseService.auth.currentUser?.email ?: "member@aura.fashion",
                    phone = "+880 1700 000000",
                    isAnonymous = AuraFirebaseService.auth.currentUser?.isAnonymous ?: true
                )

                // Seed products in Firestore if empty
                AuraFirebaseService.seedProductsIfEmpty(AuraDataSource.sampleProducts)

                // Listen to Firestore products in real time
                launch {
                    AuraFirebaseService.listenToProducts().collect { liveProducts ->
                        if (liveProducts.isNotEmpty()) {
                            Log.d(TAG, "Received ${liveProducts.size} live products from Firestore")
                            _products.value = liveProducts
                        }
                    }
                }

                // Listen to Firestore orders in real time
                launch {
                    AuraFirebaseService.listenToOrders(uid).collect { liveOrders ->
                        if (liveOrders.isNotEmpty()) {
                            Log.d(TAG, "Received ${liveOrders.size} live orders from Firestore")
                            _orders.value = liveOrders
                        }
                    }
                }

                // Listen to Auth changes
                launch {
                    AuraFirebaseService.currentUserFlow.collect { fbUser ->
                        if (fbUser != null) {
                            _currentUser.value = AuraUser(
                                uid = fbUser.uid,
                                name = fbUser.displayName ?: "AURA Member",
                                email = fbUser.email ?: "",
                                phone = fbUser.phoneNumber ?: "",
                                isAnonymous = fbUser.isAnonymous
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase initialization notice (local fallback active): ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // Wishlist functions
    // -------------------------------------------------------------
    fun toggleWishlist(productId: String) {
        val current = _wishlistIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _wishlistIds.value = current

        val uid = _currentUser.value?.uid ?: "aura-guest-uid"
        repositoryScope.launch {
            AuraFirebaseService.saveWishlist(uid, current)
        }
    }

    fun isWishlisted(productId: String): Boolean {
        return _wishlistIds.value.contains(productId)
    }

    // -------------------------------------------------------------
    // Cart functions
    // -------------------------------------------------------------
    fun addToCart(product: Product, color: ColorOption, size: String, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.product.id == product.id && it.selectedColor.name == color.name && it.selectedSize == size
        }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    id = UUID.randomUUID().toString(),
                    product = product,
                    selectedColor = color,
                    selectedSize = size,
                    quantity = quantity
                )
            )
        }
        _cartItems.value = current

        val uid = _currentUser.value?.uid ?: "aura-guest-uid"
        repositoryScope.launch {
            AuraFirebaseService.saveCart(uid, current)
        }
    }

    fun addStyleLookToCart(look: StyleLook) {
        look.products.forEach { prod ->
            addToCart(
                product = prod,
                color = prod.availableColors.first(),
                size = prod.availableSizes.firstOrNull { it == "L" } ?: prod.availableSizes.first()
            )
        }
    }

    fun updateCartQuantity(cartItemId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == cartItemId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current

            val uid = _currentUser.value?.uid ?: "aura-guest-uid"
            repositoryScope.launch {
                AuraFirebaseService.saveCart(uid, current)
            }
        }
    }

    fun removeFromCart(cartItemId: String) {
        _cartItems.value = _cartItems.value.filter { it.id != cartItemId }
        val uid = _currentUser.value?.uid ?: "aura-guest-uid"
        repositoryScope.launch {
            AuraFirebaseService.saveCart(uid, _cartItems.value)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
        val uid = _currentUser.value?.uid ?: "aura-guest-uid"
        repositoryScope.launch {
            AuraFirebaseService.saveCart(uid, emptyList())
        }
    }

    // -------------------------------------------------------------
    // Coupon functions
    // -------------------------------------------------------------
    fun applyCoupon(code: String): String? {
        val cleanCode = code.trim().uppercase()
        val found = AuraDataSource.sampleCoupons.find { it.code.equals(cleanCode, ignoreCase = true) }
        return if (found != null) {
            _appliedCoupon.value = found
            null // success
        } else {
            "Invalid coupon code. Try 'AURA10' or 'AURANEW'"
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // -------------------------------------------------------------
    // Drop notification
    // -------------------------------------------------------------
    fun toggleDropNotification() {
        val current = _activeDrop.value
        _activeDrop.value = current.copy(isNotified = !current.isNotified)
    }

    // -------------------------------------------------------------
    // Orders & Checkout
    // -------------------------------------------------------------
    fun placeOrder(
        customerName: String,
        phone: String,
        address: String,
        paymentMethod: String
    ): Order {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.totalPrice }
        val coupon = _appliedCoupon.value
        val discount = if (coupon != null) (subtotal * (coupon.discountPercent / 100.0)) else 0.0
        val delivery = if (coupon?.code == "FREESHIP" || subtotal >= 3000.0) 0.0 else 100.0
        val total = (subtotal - discount + delivery).coerceAtLeast(0.0)
        val uid = _currentUser.value?.uid ?: "aura-guest-uid"

        val newOrder = Order(
            id = "AURA-${(1025..9999).random()}",
            userId = uid,
            orderDate = "Just now",
            items = items,
            subtotal = subtotal,
            deliveryFee = delivery,
            discount = discount,
            total = total,
            status = OrderStatus.CONFIRMED,
            customerName = customerName.ifBlank { "AURA Member" },
            customerPhone = phone.ifBlank { "+880 1700 000000" },
            deliveryAddress = address.ifBlank { "Dhaka, Bangladesh" },
            paymentMethod = paymentMethod,
            trackingId = "PTH-DH-${(100000..999999).random()}",
            estimatedDelivery = "2-3 business days"
        )

        _orders.value = listOf(newOrder) + _orders.value
        clearCart()

        repositoryScope.launch {
            AuraFirebaseService.saveOrder(newOrder, uid)
        }

        return newOrder
    }

    // -------------------------------------------------------------
    // Admin operations & Realtime Sync
    // -------------------------------------------------------------
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(status = newStatus) else it
        }
        repositoryScope.launch {
            AuraFirebaseService.updateOrderStatus(orderId, newStatus)
        }
    }

    fun updateProductStock(productId: String, size: String, newStock: Int) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                val updatedStock = prod.stockPerSize.toMutableMap()
                updatedStock[size] = newStock.coerceAtLeast(0)
                prod.copy(stockPerSize = updatedStock)
            } else prod
        }
        repositoryScope.launch {
            AuraFirebaseService.updateProductStock(productId, size, newStock)
        }
    }

    fun addProduct(newProduct: Product) {
        _products.value = listOf(newProduct) + _products.value
        repositoryScope.launch {
            AuraFirebaseService.saveProduct(newProduct)
        }
    }

    fun setProductPublished(productId: String, published: Boolean) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(published = published) else it
        }
        repositoryScope.launch {
            AuraFirebaseService.setProductPublished(productId, published)
        }
    }

    fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            val list = _recentSearches.value.toMutableList()
            list.remove(query)
            list.add(0, query)
            _recentSearches.value = list.take(8)
        }
    }

    fun recoverAbandonedCart(id: String) {
        _abandonedCarts.value = _abandonedCarts.value.map {
            if (it.id == id) it.copy(isRecovered = true) else it
        }
    }

    fun updateReturnStatus(id: String, newStatus: ReturnStatus) {
        _returnRequests.value = _returnRequests.value.map {
            if (it.id == id) it.copy(status = newStatus) else it
        }
    }

    fun resolveTicket(id: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == id) it.copy(status = "RESOLVED") else it
        }
    }

    fun toggleHomepageSection(id: String) {
        _homepageSections.value = _homepageSections.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun toggleFlashSale() {
        _flashSale.value = _flashSale.value.copy(isActive = !_flashSale.value.isActive)
    }

    companion object {
        val instance by lazy { AuraRepository() }
    }
}
