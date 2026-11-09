package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AuraRepository
import com.example.model.*
import com.example.ui.components.AuraBottomBar
import com.example.ui.components.AuraTab
import com.example.ui.components.AuraTopBar
import com.example.ui.screens.*
import kotlinx.coroutines.launch

sealed interface AuraScreen {
    object Main : AuraScreen
    data class ProductDetail(val product: Product) : AuraScreen
    object Checkout : AuraScreen
    data class OrderTracking(val order: Order) : AuraScreen
    object Wishlist : AuraScreen
    data class LookbookView(val lookbook: LookbookItem) : AuraScreen
    data class JournalView(val article: JournalArticle) : AuraScreen
    object Admin : AuraScreen
}

@Composable
fun AuraApp(
    repository: AuraRepository = AuraRepository.instance
) {
    val products by repository.products.collectAsState()
    val wishlistIds by repository.wishlistIds.collectAsState()
    val cartItems by repository.cartItems.collectAsState()
    val appliedCoupon by repository.appliedCoupon.collectAsState()
    val orders by repository.orders.collectAsState()
    val activeDrop by repository.activeDrop.collectAsState()
    val warehouses by repository.warehouses.collectAsState()
    val abandonedCarts by repository.abandonedCarts.collectAsState()
    val customers by repository.customers.collectAsState()
    val returnRequests by repository.returnRequests.collectAsState()
    val flashSale by repository.flashSale.collectAsState()
    val supportTickets by repository.supportTickets.collectAsState()
    val homepageSections by repository.homepageSections.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var currentTab by remember { mutableStateOf(AuraTab.HOME) }
    var currentScreen by remember { mutableStateOf<AuraScreen>(AuraScreen.Main) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Back handling
    BackHandler(enabled = currentScreen !is AuraScreen.Main) {
        currentScreen = AuraScreen.Main
    }

    val showBars = currentScreen is AuraScreen.Main

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBars) {
                AuraTopBar(
                    onSearchClick = { currentTab = AuraTab.SHOP },
                    onWishlistClick = { currentScreen = AuraScreen.Wishlist },
                    onBagClick = { currentTab = AuraTab.BAG },
                    onAdminClick = { currentScreen = AuraScreen.Admin },
                    wishlistCount = wishlistIds.size,
                    cartCount = cartItems.sumOf { it.quantity },
                    isAdminMode = false
                )
            }
        },
        bottomBar = {
            if (showBars) {
                AuraBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it },
                    cartItemCount = cartItems.sumOf { it.quantity }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AuraScreen.Main -> {
                    when (currentTab) {
                        AuraTab.HOME -> {
                            HomeScreen(
                                products = products,
                                wishlistIds = wishlistIds,
                                activeDrop = activeDrop,
                                onProductClick = { prod ->
                                    currentScreen = AuraScreen.ProductDetail(prod)
                                },
                                onWishlistToggle = { id ->
                                    repository.toggleWishlist(id)
                                    val isNow = repository.isWishlisted(id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isNow) "Added to saved pieces" else "Removed from saved pieces"
                                        )
                                    }
                                },
                                onQuickAdd = { prod ->
                                    repository.addToCart(
                                        product = prod,
                                        color = prod.availableColors.first(),
                                        size = "L"
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added ${prod.name} (L) to Bag")
                                    }
                                },
                                onShopNowClick = { currentTab = AuraTab.SHOP },
                                onExploreLookbookClick = { lb ->
                                    currentScreen = AuraScreen.LookbookView(lb)
                                },
                                onJournalArticleClick = { article ->
                                    currentScreen = AuraScreen.JournalView(article)
                                },
                                onAddLookToBag = { look ->
                                    repository.addStyleLookToCart(look)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added entire outfit bundle to Bag (-15%)")
                                    }
                                    currentTab = AuraTab.BAG
                                },
                                onToggleDropNotification = {
                                    repository.toggleDropNotification()
                                    scope.launch {
                                        val notified = repository.activeDrop.value.isNotified
                                        snackbarHostState.showSnackbar(
                                            if (notified) "You'll be notified when Drop 04 drops!" else "Notification turned off"
                                        )
                                    }
                                },
                                onOpenStyleAi = { currentTab = AuraTab.STYLE_AI }
                            )
                        }

                        AuraTab.SHOP -> {
                            ShopScreen(
                                products = products,
                                wishlistIds = wishlistIds,
                                onProductClick = { prod ->
                                    currentScreen = AuraScreen.ProductDetail(prod)
                                },
                                onWishlistToggle = { id ->
                                    repository.toggleWishlist(id)
                                },
                                onQuickAdd = { prod ->
                                    repository.addToCart(prod, prod.availableColors.first(), "L")
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added ${prod.name} (L) to Bag")
                                    }
                                }
                            )
                        }

                        AuraTab.STYLE_AI -> {
                            StyleAiScreen(
                                allProducts = products,
                                onAddLookToBag = { look ->
                                    repository.addStyleLookToCart(look)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added curated look to Bag (-15%)")
                                    }
                                    currentTab = AuraTab.BAG
                                },
                                onProductClick = { prod ->
                                    currentScreen = AuraScreen.ProductDetail(prod)
                                }
                            )
                        }

                        AuraTab.BAG -> {
                            CartScreen(
                                cartItems = cartItems,
                                appliedCoupon = appliedCoupon,
                                onUpdateQuantity = { id, delta ->
                                    repository.updateCartQuantity(id, delta)
                                },
                                onRemoveItem = { id ->
                                    repository.removeFromCart(id)
                                },
                                onApplyCoupon = { code ->
                                    val err = repository.applyCoupon(code)
                                    if (err == null) {
                                        scope.launch { snackbarHostState.showSnackbar("Coupon $code applied!") }
                                    }
                                    err
                                },
                                onRemoveCoupon = {
                                    repository.removeCoupon()
                                },
                                onCheckoutClick = {
                                    currentScreen = AuraScreen.Checkout
                                },
                                onContinueShopping = {
                                    currentTab = AuraTab.SHOP
                                }
                            )
                        }

                        AuraTab.PROFILE -> {
                            ProfileScreen(
                                orders = orders,
                                onOrderClick = { ord ->
                                    currentScreen = AuraScreen.OrderTracking(ord)
                                },
                                onAdminClick = {
                                    currentScreen = AuraScreen.Admin
                                },
                                user = currentUser,
                                onSignOut = {
                                    scope.launch {
                                        com.example.data.firebase.AuraFirebaseService.signOut()
                                        snackbarHostState.showSnackbar("Signed out successfully")
                                    }
                                },
                                onSignIn = { email, pass, onResult ->
                                    scope.launch {
                                        val res = com.example.data.firebase.AuraFirebaseService.signIn(email, pass)
                                        if (res.isSuccess) {
                                            onResult(true, null)
                                            snackbarHostState.showSnackbar("Welcome back, $email")
                                        } else {
                                            onResult(false, res.exceptionOrNull()?.message ?: "Sign in failed")
                                        }
                                    }
                                },
                                onSignUp = { name, email, pass, phone, onResult ->
                                    scope.launch {
                                        val res = com.example.data.firebase.AuraFirebaseService.signUp(email, pass, name, phone)
                                        if (res.isSuccess) {
                                            onResult(true, null)
                                            snackbarHostState.showSnackbar("Account created! Welcome to AURA.")
                                        } else {
                                            onResult(false, res.exceptionOrNull()?.message ?: "Registration failed")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                is AuraScreen.ProductDetail -> {
                    ProductDetailScreen(
                        product = screen.product,
                        allProducts = products,
                        isWishlisted = wishlistIds.contains(screen.product.id),
                        onWishlistToggle = {
                            repository.toggleWishlist(screen.product.id)
                        },
                        onBackClick = { currentScreen = AuraScreen.Main },
                        onAddToCart = { prod, col, size ->
                            repository.addToCart(prod, col, size)
                            scope.launch {
                                snackbarHostState.showSnackbar("Added ${prod.name} ($size) to Bag")
                            }
                        },
                        onBuyNow = { prod, col, size ->
                            repository.addToCart(prod, col, size)
                            currentScreen = AuraScreen.Checkout
                        },
                        onProductClick = { related ->
                            currentScreen = AuraScreen.ProductDetail(related)
                        }
                    )
                }

                is AuraScreen.Checkout -> {
                    CheckoutScreen(
                        cartItems = cartItems,
                        appliedCoupon = appliedCoupon,
                        onBackClick = { currentScreen = AuraScreen.Main },
                        onOrderPlaced = { order ->
                            currentScreen = AuraScreen.OrderTracking(order)
                            scope.launch {
                                snackbarHostState.showSnackbar("Order placed successfully! Tracking ID: ${order.trackingId}")
                            }
                        },
                        onPlaceOrderAction = { name, phone, address, payment ->
                            repository.placeOrder(name, phone, address, payment)
                        }
                    )
                }

                is AuraScreen.OrderTracking -> {
                    OrderTrackingScreen(
                        order = screen.order,
                        onBackClick = { currentScreen = AuraScreen.Main }
                    )
                }

                is AuraScreen.Wishlist -> {
                    val wishlisted = products.filter { wishlistIds.contains(it.id) }
                    WishlistScreen(
                        wishlistedProducts = wishlisted,
                        onProductClick = { prod ->
                            currentScreen = AuraScreen.ProductDetail(prod)
                        },
                        onWishlistToggle = { id ->
                            repository.toggleWishlist(id)
                        },
                        onQuickAdd = { prod ->
                            repository.addToCart(prod, prod.availableColors.first(), "L")
                            scope.launch {
                                snackbarHostState.showSnackbar("Added to Bag")
                            }
                        },
                        onBackClick = { currentScreen = AuraScreen.Main },
                        onExploreClick = {
                            currentScreen = AuraScreen.Main
                            currentTab = AuraTab.SHOP
                        }
                    )
                }

                is AuraScreen.LookbookView -> {
                    LookbookDetailScreen(
                        lookbook = screen.lookbook,
                        onBackClick = { currentScreen = AuraScreen.Main },
                        onProductClick = { prod ->
                            currentScreen = AuraScreen.ProductDetail(prod)
                        }
                    )
                }

                is AuraScreen.JournalView -> {
                    JournalDetailScreen(
                        article = screen.article,
                        onBackClick = { currentScreen = AuraScreen.Main }
                    )
                }

                is AuraScreen.Admin -> {
                    AdminPanelScreen(
                        products = products,
                        orders = orders,
                        warehouses = warehouses,
                        abandonedCarts = abandonedCarts,
                        customers = customers,
                        returnRequests = returnRequests,
                        flashSale = flashSale,
                        supportTickets = supportTickets,
                        homepageSections = homepageSections,
                        onUpdateOrderStatus = { orderId, newStatus ->
                            repository.updateOrderStatus(orderId, newStatus)
                            scope.launch {
                                snackbarHostState.showSnackbar("Order $orderId updated to ${newStatus.label}")
                            }
                        },
                        onUpdateStock = { prodId, size, newStock ->
                            repository.updateProductStock(prodId, size, newStock)
                        },
                        onAddProduct = { newProd ->
                            repository.addProduct(newProd)
                            scope.launch {
                                snackbarHostState.showSnackbar("Garment added to catalog!")
                            }
                        },
                        onRecoverAbandonedCart = { cartId ->
                            repository.recoverAbandonedCart(cartId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Recovery WhatsApp & SMS alert sent!")
                            }
                        },
                        onUpdateReturnStatus = { retId, newStatus ->
                            repository.updateReturnStatus(retId, newStatus)
                            scope.launch {
                                snackbarHostState.showSnackbar("Return $retId marked as ${newStatus.label}")
                            }
                        },
                        onResolveTicket = { ticketId ->
                            repository.resolveTicket(ticketId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Support ticket resolved")
                            }
                        },
                        onToggleHomepageSection = { sectionId ->
                            repository.toggleHomepageSection(sectionId)
                        },
                        onToggleFlashSale = {
                            repository.toggleFlashSale()
                            scope.launch {
                                snackbarHostState.showSnackbar("Flash sale status updated")
                            }
                        },
                        onExitAdmin = { currentScreen = AuraScreen.Main }
                    )
                }
            }
        }
    }
}
