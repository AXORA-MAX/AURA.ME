package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuraDataSource
import com.example.model.*
import com.example.ui.theme.*

enum class AdminTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Filled.Dashboard),
    ORDERS("Orders", Icons.Filled.LocalShipping),
    INVENTORY("Inventory", Icons.Filled.Inventory),
    PROFIT("Cost & Profit", Icons.Filled.AttachMoney),
    CRM("Customers", Icons.Filled.People),
    ABANDONED("Abandoned Carts", Icons.Filled.RemoveShoppingCart),
    RETURNS("Returns & Support", Icons.Filled.AssignmentReturn),
    CMS("Homepage CMS", Icons.Filled.Web),
    MARKETING("Flash Sales", Icons.Filled.Bolt),
    ADD_PRODUCT("New Garment", Icons.Filled.AddCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    products: List<Product>,
    orders: List<Order>,
    warehouses: List<Warehouse>,
    abandonedCarts: List<AbandonedCart>,
    customers: List<CustomerProfile>,
    returnRequests: List<ReturnRequest>,
    flashSale: FlashSaleConfig,
    supportTickets: List<SupportTicket>,
    homepageSections: List<HomepageSectionItem>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onUpdateStock: (String, String, Int) -> Unit,
    onAddProduct: (Product) -> Unit,
    onRecoverAbandonedCart: (String) -> Unit,
    onUpdateReturnStatus: (String, ReturnStatus) -> Unit,
    onResolveTicket: (String) -> Unit,
    onToggleHomepageSection: (String) -> Unit,
    onToggleFlashSale: () -> Unit,
    onExitAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    // New product form states
    var newProdName by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("1890") }
    var newProdCategory by remember { mutableStateOf(ProductCategory.TEES) }
    var newProdStyle by remember { mutableStateOf(StyleType.OVERSIZED) }
    var newProdFabric by remember { mutableStateOf("280 GSM Heavyweight Cotton") }
    var newProdDescription by remember { mutableStateOf("Boxy silhouette tailored for evening presence.") }
    var addSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuraNearBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = AuraWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text(
                                text = "AURA. BUSINESS OS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = AuraWhite
                            )
                            Text(
                                text = "ECOSYSTEM MANAGEMENT SUITE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    letterSpacing = 1.5.sp,
                                    color = AuraTextMuted
                                )
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onExitAdmin,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AuraNearBlack,
                            contentColor = AuraWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "Exit to Store",
                            modifier = Modifier.size(16.dp),
                            tint = AuraGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VIEW STORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraWhite
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AuraBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AuraCream)
                .padding(innerPadding)
        ) {
            // Horizontal Scrollable Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = AuraNearBlack,
                contentColor = AuraWhite,
                edgePadding = 12.dp,
                divider = { Divider(color = AuraZincBorder) }
            ) {
                AdminTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isSelected) AuraWhite else AuraTextMuted
                                )
                                Text(
                                    text = tab.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) AuraWhite else AuraTextMuted
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content Dispatcher
            when (selectedTab) {
                AdminTab.OVERVIEW -> {
                    OverviewTab(products = products, orders = orders)
                }
                AdminTab.ORDERS -> {
                    OrdersManagementTab(orders = orders, onUpdateOrderStatus = onUpdateOrderStatus)
                }
                AdminTab.INVENTORY -> {
                    InventoryAndWarehousesTab(
                        products = products,
                        warehouses = warehouses,
                        onUpdateStock = onUpdateStock
                    )
                }
                AdminTab.PROFIT -> {
                    CostAndProfitTab(products = products)
                }
                AdminTab.CRM -> {
                    CustomerCrmTab(customers = customers)
                }
                AdminTab.ABANDONED -> {
                    AbandonedCartsTab(
                        carts = abandonedCarts,
                        onRecoverCart = onRecoverAbandonedCart
                    )
                }
                AdminTab.RETURNS -> {
                    ReturnsAndSupportTab(
                        returns = returnRequests,
                        tickets = supportTickets,
                        onUpdateReturnStatus = onUpdateReturnStatus,
                        onResolveTicket = onResolveTicket
                    )
                }
                AdminTab.CMS -> {
                    HomepageCmsTab(
                        sections = homepageSections,
                        onToggleSection = onToggleHomepageSection
                    )
                }
                AdminTab.MARKETING -> {
                    MarketingTab(
                        flashSale = flashSale,
                        onToggleFlashSale = onToggleFlashSale
                    )
                }
                AdminTab.ADD_PRODUCT -> {
                    AddProductTab(
                        name = newProdName,
                        price = newProdPrice,
                        category = newProdCategory,
                        style = newProdStyle,
                        fabric = newProdFabric,
                        description = newProdDescription,
                        successMessage = addSuccessMessage,
                        onNameChange = { newProdName = it },
                        onPriceChange = { newProdPrice = it },
                        onCategoryChange = { newProdCategory = it },
                        onStyleChange = { newProdStyle = it },
                        onFabricChange = { newProdFabric = it },
                        onDescriptionChange = { newProdDescription = it },
                        onSubmit = {
                            if (newProdName.isNotBlank() && (newProdPrice.toDoubleOrNull() ?: 0.0) > 0) {
                                val created = Product(
                                    id = "aura-custom-${System.currentTimeMillis() % 10000}",
                                    name = newProdName.uppercase(),
                                    tagline = newProdFabric,
                                    price = newProdPrice.toDoubleOrNull() ?: 1890.0,
                                    category = newProdCategory,
                                    style = newProdStyle,
                                    availableColors = listOf(AuraDataSource.colorBlack, AuraDataSource.colorWhite),
                                    availableSizes = listOf("S", "M", "L", "XL", "XXL"),
                                    description = newProdDescription,
                                    fabric = newProdFabric,
                                    imageRes = R.drawable.img_hero_fashion,
                                    isNew = true
                                )
                                onAddProduct(created)
                                newProdName = ""
                                addSuccessMessage = "Garment added to live store!"
                            }
                        }
                    )
                }
            }
        }
    }
}

// ================= TAB 1: OVERVIEW =================
@Composable
private fun OverviewTab(products: List<Product>, orders: List<Order>) {
    val totalRevenue = orders.sumOf { it.total } + 124850.0
    val totalOrdersCount = orders.size + 84
    val aov = totalRevenue / totalOrdersCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TODAY'S EXECUTIVE SUMMARY",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )
                Text(
                    text = "Real-time commerce telemetry across web & mobile",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AuraTextMuted
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AuraSuccess.copy(alpha = 0.15f),
                contentColor = AuraSuccess
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AuraSuccess))
                    Text(
                        text = "SYSTEM ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }

        // Metrics Grid (4 cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "GROSS REVENUE",
                value = "৳${totalRevenue.toInt()}",
                subtitle = "+24.5% vs last week",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "TOTAL ORDERS",
                value = "$totalOrdersCount",
                subtitle = "98.2% fulfillment rate",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "AVG. ORDER VALUE",
                value = "৳${aov.toInt()}",
                subtitle = "Oversized bundles lead",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "EST. GROSS PROFIT",
                value = "৳${(totalRevenue * 0.54).toInt()}",
                subtitle = "54% blended margin",
                modifier = Modifier.weight(1f)
            )
        }

        // Conversion Funnel
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONVERSION FUNNEL (7 DAYS)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )
                    Text(
                        text = "4.2% Blended CR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraSuccess
                        )
                    )
                }

                FunnelRow(label = "Unique Visitors", count = "10,240", percent = 1.0f)
                FunnelRow(label = "Product Views", count = "4,610", percent = 0.45f)
                FunnelRow(label = "Added to Bag", count = "1,280", percent = 0.125f)
                FunnelRow(label = "Checkout Initiated", count = "620", percent = 0.06f)
                FunnelRow(label = "Completed Orders", count = "431", percent = 0.042f)
            }
        }

        // Live Operational Activity Feed
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "LIVE ACTIVITY FEED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )

                ActivityItem(icon = Icons.Filled.ShoppingBag, text = "New order received: 2x Acid Wash Boxy Tee (৳3,780)", time = "3m ago")
                ActivityItem(icon = Icons.Filled.PersonAdd, text = "New VIP customer registered: Tanvir Hasan (Dhaka)", time = "14m ago")
                ActivityItem(icon = Icons.Filled.LocalShipping, text = "Order #AURA-1024 shipped via Pathao Courier", time = "28m ago")
                ActivityItem(icon = Icons.Filled.Warning, text = "Low Stock: Acid Wash Boxy Tee (XL) has 5 remaining", time = "1h ago", isWarning = true)
            }
        }
    }
}

// ================= TAB 2: ORDERS MANAGEMENT =================
@Composable
private fun OrdersManagementTab(
    orders: List<Order>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "CUSTOMER ORDERS (${orders.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = AuraTextMain
            )
        }

        items(orders) { order ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = order.id,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = AuraTextMain
                            )
                            Text(
                                text = "${order.orderDate} • ${order.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (order.status) {
                                OrderStatus.DELIVERED -> AuraSuccess.copy(alpha = 0.15f)
                                OrderStatus.CANCELLED -> AuraDanger.copy(alpha = 0.15f)
                                else -> AuraGold.copy(alpha = 0.15f)
                            },
                            contentColor = when (order.status) {
                                OrderStatus.DELIVERED -> AuraSuccess
                                OrderStatus.CANCELLED -> AuraDanger
                                else -> AuraGold
                            }
                        ) {
                            Text(
                                text = order.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Divider(color = AuraZincBorder)

                    Text(
                        text = "Customer: ${order.customerName} (${order.customerPhone})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Address: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AuraTextMuted
                    )
                    Text(
                        text = "Courier Tracking: ${order.trackingId} (${order.courier})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AuraTextMuted
                    )

                    // Garments list summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AuraCream)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.product.name} (${item.selectedSize})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "৳${item.totalPrice.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Total: ৳${order.total.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )

                        // Action to update status
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                                FilledTonalButton(
                                    onClick = {
                                        val next = when (order.status) {
                                            OrderStatus.CONFIRMED -> OrderStatus.PROCESSING
                                            OrderStatus.PROCESSING -> OrderStatus.PACKED
                                            OrderStatus.PACKED -> OrderStatus.SHIPPED
                                            OrderStatus.SHIPPED -> OrderStatus.OUT_FOR_DELIVERY
                                            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
                                            else -> OrderStatus.DELIVERED
                                        }
                                        onUpdateOrderStatus(order.id, next)
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = AuraNearBlack,
                                        contentColor = AuraWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "ADVANCE STEP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 3: INVENTORY & MULTI-WAREHOUSE =================
@Composable
private fun InventoryAndWarehousesTab(
    products: List<Product>,
    warehouses: List<Warehouse>,
    onUpdateStock: (String, String, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "MULTI-WAREHOUSE HUBS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = AuraTextMain
            )
        }

        // Warehouse cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                warehouses.forEach { wh ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraWhite),
                        border = BorderStroke(1.dp, AuraZincBorder),
                        modifier = Modifier.width(220.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = wh.city.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = AuraTextMuted
                                )
                                Box(
                                    modifier = Modifier.size(7.dp).clip(CircleShape).background(AuraSuccess)
                                )
                            }
                            Text(
                                text = wh.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                maxLines = 1
                            )
                            Text(
                                text = "${wh.activeUnits} / ${wh.capacityUnits} units stored",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                            LinearProgressIndicator(
                                progress = wh.activeUnits.toFloat() / wh.capacityUnits.toFloat(),
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = AuraBlack,
                                trackColor = AuraZincBorder
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "GARMENT STOCK MONITOR (${products.size} STYLES)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = AuraTextMain,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "${prod.fabric} • ৳${prod.price.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (prod.isOutOfStock) AuraDanger.copy(alpha = 0.15f)
                            else if (prod.isLowStock) AuraGold.copy(alpha = 0.15f)
                            else AuraSuccess.copy(alpha = 0.15f),
                            contentColor = if (prod.isOutOfStock) AuraDanger
                            else if (prod.isLowStock) AuraGold
                            else AuraSuccess
                        ) {
                            Text(
                                text = if (prod.isOutOfStock) "SOLD OUT"
                                else if (prod.isLowStock) "LOW STOCK (${prod.totalStock})"
                                else "IN STOCK (${prod.totalStock})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Stock adjust per size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prod.stockPerSize.forEach { (size, count) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraCream)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = size,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                    color = AuraTextMuted
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 13.sp)
                                )
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AuraWhite)
                                            .clickable { onUpdateStock(prod.id, size, count - 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AuraBlack)
                                            .clickable { onUpdateStock(prod.id, size, count + 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = AuraWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 4: COST & PROFIT TRACKING =================
@Composable
private fun CostAndProfitTab(products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "UNIT ECONOMICS & GROSS PROFIT",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = AuraTextMain
                )
                Text(
                    text = "Transparent breakdown of Garment Cost, Packaging, Logistics, and Net Profit",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AuraTextMuted
                )
            }
        }

        items(products) { prod ->
            val costPrice = (prod.price * 0.35).coerceAtLeast(450.0)
            val breakdown = ProductCostBreakdown(
                sellingPrice = prod.price,
                costPrice = costPrice
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AuraSuccess.copy(alpha = 0.15f),
                            contentColor = AuraSuccess
                        ) {
                            Text(
                                text = "${breakdown.marginPercent}% MARGIN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Cost line items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CostItem(label = "Retail Price", value = "৳${breakdown.sellingPrice.toInt()}", isPositive = true)
                        CostItem(label = "Garment Mfg", value = "-৳${breakdown.costPrice.toInt()}")
                        CostItem(label = "Packaging", value = "-৳${breakdown.packagingCost.toInt()}")
                        CostItem(label = "Logistics", value = "-৳${breakdown.deliveryCost.toInt()}")
                        CostItem(label = "Gateway", value = "-৳${breakdown.paymentFee.toInt()}")
                    }

                    Divider(color = AuraZincBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Net Profit / Unit",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "৳${breakdown.netProfit.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = AuraSuccess
                            )
                        )
                    }
                }
            }
        }
    }
}

// ================= TAB 5: CUSTOMER CRM & SEGMENTS =================
@Composable
private fun CustomerCrmTab(customers: List<CustomerProfile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CUSTOMER CRM & SEGMENTATION (${customers.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = AuraTextMain
                )
                Text(
                    text = "Segmenting customers by lifetime spend and brand affinity",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AuraTextMuted
                )
            }
        }

        items(customers) { cust ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cust.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "${cust.phone} • ${cust.email}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(cust.segment.colorHex).copy(alpha = 0.15f),
                            contentColor = Color(cust.segment.colorHex)
                        ) {
                            Text(
                                text = cust.segment.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Divider(color = AuraZincBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "ORDERS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraTextMuted)
                            Text(text = "${cust.totalOrders}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "LIFETIME SPEND", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraTextMuted)
                            Text(text = "৳${cust.totalSpent.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "AURA POINTS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraTextMuted)
                            Text(text = "${cust.points} pts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AuraGold))
                        }
                        Column {
                            Text(text = "LAST ACTIVE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraTextMuted)
                            Text(text = cust.lastActive, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 6: ABANDONED CARTS =================
@Composable
private fun AbandonedCartsTab(
    carts: List<AbandonedCart>,
    onRecoverCart: (String) -> Unit
) {
    val totalValue = carts.filter { !it.isRecovered }.sumOf { it.cartValue }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraNearBlack),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "POTENTIAL REVENUE AT RISK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "৳${totalValue.toInt()}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = AuraWhite
                        )
                    )
                    Text(
                        text = "${carts.count { !it.isRecovered }} carts abandoned in checkout funnel today",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AuraTextMuted)
                    )
                }
            }
        }

        items(carts) { cart ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cart.customerName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "${cart.phone} • ${cart.hoursAgo} hours ago",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                        }

                        Text(
                            text = "৳${cart.cartValue.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                    }

                    Text(
                        text = cart.itemsSummary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AuraTextMain
                    )

                    Divider(color = AuraZincBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (cart.isRecovered) "✓ Reminder Dispatched" else "Pending Contact",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (cart.isRecovered) AuraSuccess else AuraTextMuted
                            )
                        )

                        FilledTonalButton(
                            onClick = { onRecoverCart(cart.id) },
                            enabled = !cart.isRecovered,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AuraNearBlack,
                                contentColor = AuraWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cart.isRecovered) "DISPATCHED" else "TRIGGER RECOVERY SMS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 7: RETURNS & SUPPORT =================
@Composable
private fun ReturnsAndSupportTab(
    returns: List<ReturnRequest>,
    tickets: List<SupportTicket>,
    onUpdateReturnStatus: (String, ReturnStatus) -> Unit,
    onResolveTicket: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "RETURN & EXCHANGE REQUESTS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = AuraTextMain
            )
        }

        items(returns) { ret ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${ret.customerName} (${ret.orderId})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "Reason: ${ret.reason}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (ret.status) {
                                ReturnStatus.APPROVED, ReturnStatus.EXCHANGED -> AuraSuccess.copy(alpha = 0.15f)
                                ReturnStatus.REJECTED -> AuraDanger.copy(alpha = 0.15f)
                                else -> AuraGold.copy(alpha = 0.15f)
                            },
                            contentColor = when (ret.status) {
                                ReturnStatus.APPROVED, ReturnStatus.EXCHANGED -> AuraSuccess
                                ReturnStatus.REJECTED -> AuraDanger
                                else -> AuraGold
                            }
                        ) {
                            Text(
                                text = ret.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (ret.status == ReturnStatus.PENDING) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateReturnStatus(ret.id, ReturnStatus.REJECTED) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("REJECT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { onUpdateReturnStatus(ret.id, ReturnStatus.APPROVED) },
                                colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("APPROVE EXCHANGE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "SUPPORT DESK TICKETS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = AuraTextMain,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(tickets) { tkt ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tkt.customerName} • ${tkt.orderId}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = tkt.time,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = AuraTextMuted
                        )
                    }

                    Text(
                        text = "\"${tkt.issue}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AuraDanger.copy(alpha = 0.15f),
                            contentColor = AuraDanger
                        ) {
                            Text(
                                text = "${tkt.priority} PRIORITY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (tkt.status != "RESOLVED") {
                            FilledTonalButton(
                                onClick = { onResolveTicket(tkt.id) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = AuraSuccess,
                                    contentColor = AuraWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("MARK RESOLVED", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("✓ Resolved", color = AuraSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 8: HOMEPAGE CMS BUILDER =================
@Composable
private fun HomepageCmsTab(
    sections: List<HomepageSectionItem>,
    onToggleSection: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "VISUAL HOMEPAGE BUILDER",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = AuraTextMain
                )
                Text(
                    text = "Instantly toggle and control storefront sections without writing code",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AuraTextMuted
                )
            }
        }

        items(sections) { sec ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sec.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = sec.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AuraTextMuted
                        )
                    }

                    Switch(
                        checked = sec.isEnabled,
                        onCheckedChange = { onToggleSection(sec.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AuraWhite,
                            checkedTrackColor = AuraBlack
                        )
                    )
                }
            }
        }
    }
}

// ================= TAB 9: MARKETING & FLASH DROPS =================
@Composable
private fun MarketingTab(
    flashSale: FlashSaleConfig,
    onToggleFlashSale: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CAMPAIGNS & FLASH SALES",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
            color = AuraTextMain
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = flashSale.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "${flashSale.discountPercent}% Discount applied on ${flashSale.productsIncludedCount} styles",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AuraTextMuted
                        )
                    }

                    Switch(
                        checked = flashSale.isActive,
                        onCheckedChange = { onToggleFlashSale() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AuraWhite,
                            checkedTrackColor = AuraBlack
                        )
                    )
                }

                Divider(color = AuraZincBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Status: ${if (flashSale.isActive) "Active on Store" else "Paused"}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "Countdown: 04:00:00", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Coupons overview
        Text(
            text = "ACTIVE PROMOTIONAL CODES",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
            color = AuraTextMain
        )

        listOf(
            "AURA10" to "10% off entire order (Min spend ৳2,000)",
            "FREESHIP" to "Free express delivery across Bangladesh",
            "DROP04" to "৳500 flat off Drop 04 styles"
        ).forEach { (code, desc) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = code, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = AuraGold))
                        Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = AuraTextMuted)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AuraSuccess.copy(alpha = 0.15f),
                        contentColor = AuraSuccess
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================= TAB 10: ADD GARMENT =================
@Composable
private fun AddProductTab(
    name: String,
    price: String,
    category: ProductCategory,
    style: StyleType,
    fabric: String,
    description: String,
    successMessage: String?,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCategoryChange: (ProductCategory) -> Unit,
    onStyleChange: (StyleType) -> Unit,
    onFabricChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ADD NEW GARMENT TO CATALOG",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
            color = AuraTextMain
        )

        successMessage?.let {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AuraSuccess.copy(alpha = 0.15f),
                contentColor = AuraSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ $it",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Garment Name (e.g. Acid Wash Boxy Tee)") },
            modifier = Modifier.fillMaxWidth().testTag("input_garment_name")
        )

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Retail Price in BDT (৳)") },
            modifier = Modifier.fillMaxWidth().testTag("input_garment_price")
        )

        OutlinedTextField(
            value = fabric,
            onValueChange = onFabricChange,
            label = { Text("Fabric & GSM (e.g. 280 GSM French Terry)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Editorial Description & Fit Notes") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_add_garment")
        ) {
            Text(
                text = "PUBLISH GARMENT TO STORE",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
        }
    }
}

// ================= REUSABLE COMPONENT HELPERS =================
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AuraWhite),
        border = BorderStroke(1.dp, AuraZincBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 9.sp
                ),
                color = AuraTextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = AuraTextMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = AuraSuccess
            )
        }
    }
}

@Composable
private fun FunnelRow(label: String, count: String, percent: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium))
            Text(text = count, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
        LinearProgressIndicator(
            progress = percent,
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = AuraNearBlack,
            trackColor = AuraZincBorder
        )
    }
}

@Composable
private fun ActivityItem(icon: ImageVector, text: String, time: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isWarning) AuraGold.copy(alpha = 0.15f) else AuraCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isWarning) AuraGold else AuraBlack
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium))
            Text(text = time, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuraTextMuted)
        }
    }
}

@Composable
private fun CostItem(label: String, value: String, isPositive: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = AuraTextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isPositive) AuraTextMain else AuraTextMuted
            )
        )
    }
}
