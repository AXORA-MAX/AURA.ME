package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.Coupon
import com.example.model.Order
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    appliedCoupon: Coupon?,
    onBackClick: () -> Unit,
    onOrderPlaced: (Order) -> Unit,
    onPlaceOrderAction: (name: String, phone: String, address: String, payment: String) -> Order,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("Tanvir Ahmed") }
    var phone by remember { mutableStateOf("+880 1712 345678") }
    var address by remember { mutableStateOf("House 42, Road 11, Banani, Dhaka-1213") }
    var selectedCity by remember { mutableStateOf("Dhaka (Inside)") }
    var selectedPayment by remember { mutableStateOf("bKash Online Payment") }
    var isProcessing by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val discount = if (appliedCoupon != null) (subtotal * (appliedCoupon.discountPercent / 100.0)) else 0.0
    val deliveryFee = if (appliedCoupon?.code == "FREESHIP" || subtotal >= 3000.0) 0.0 else (if (selectedCity.contains("Inside")) 100.0 else 150.0)
    val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

    val paymentOptions = listOf(
        "bKash Online Payment",
        "Nagad Payment",
        "Credit / Debit Card (Visa/Mastercard)",
        "Cash on Delivery (COD)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SECURE CHECKOUT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AuraWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AuraBlack)
            )
        },
        bottomBar = {
            Surface(
                color = AuraWhite,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL PAYABLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraTextSub
                            )
                        )
                        Text(
                            text = "৳${total.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = AuraBlack
                        )
                    }

                    Button(
                        onClick = {
                            isProcessing = true
                            val order = onPlaceOrderAction(name, phone, address, selectedPayment)
                            onOrderPlaced(order)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraBlack,
                            contentColor = AuraWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("confirm_place_order_btn")
                    ) {
                        Text(
                            text = "CONFIRM ORDER",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AuraCream)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Customer Contact
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AuraBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. CONTACT INFORMATION",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (+880)") },
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )
                }
            }

            // Step 2: Shipping Address
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = AuraBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. DELIVERY DESTINATION",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Dhaka (Inside)", "Outside Dhaka").forEach { city ->
                            val isSel = selectedCity == city
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCity = city },
                                label = { Text(city) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AuraBlack,
                                    selectedLabelColor = AuraWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street Address / Apartment / Area") },
                        maxLines = 3,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input")
                    )
                }
            }

            // Step 3: Payment Method
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = AuraBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. PAYMENT METHOD",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    paymentOptions.forEach { method ->
                        val isSelected = selectedPayment == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AuraCream else Color.Transparent)
                                .border(1.dp, if (isSelected) AuraBlack else AuraBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedPayment = method }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = AuraTextMain
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AuraBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Order Review Brief
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ORDER SUMMARY (${cartItems.size} ITEMS)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AuraTextSub)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.product.name} (${item.selectedSize})",
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraTextMain
                            )
                            Text(
                                text = "৳${item.totalPrice.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = AuraTextSub)
                        Text("৳${subtotal.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }

                    if (discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", style = MaterialTheme.typography.bodySmall, color = AuraSuccess)
                            Text("-৳${discount.toInt()}", style = MaterialTheme.typography.bodySmall, color = AuraSuccess)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee", style = MaterialTheme.typography.bodySmall, color = AuraTextSub)
                        Text(if (deliveryFee == 0.0) "FREE" else "৳${deliveryFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
