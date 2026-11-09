package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.Coupon
import com.example.ui.components.AuraProductImage
import com.example.ui.theme.*

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    appliedCoupon: Coupon?,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onApplyCoupon: (String) -> String?,
    onRemoveCoupon: () -> Unit,
    onCheckoutClick: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val discount = if (appliedCoupon != null) (subtotal * (appliedCoupon.discountPercent / 100.0)) else 0.0
    val deliveryFee = if (appliedCoupon?.code == "FREESHIP" || subtotal >= 3000.0 || cartItems.isEmpty()) 0.0 else 100.0
    val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraCream)
    ) {
        // Header
        Surface(
            color = AuraWhite,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR BAG (${cartItems.sumOf { it.quantity }})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )

                if (subtotal >= 3000.0) {
                    Surface(
                        color = AuraSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FREE SHIPPING UNLOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraSuccess,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AuraWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = AuraTextSub,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "YOUR BAG IS EMPTY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore the new drops and build your personal vibe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onContinueShopping,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraBlack,
                            contentColor = AuraWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("empty_bag_shop_btn")
                    ) {
                        Text("SHOP NEW DROP")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraWhite),
                        border = BorderStroke(1.dp, AuraBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AuraProductImage(
                                imageUrl = item.product.imageUrl,
                                fallbackRes = item.product.imageRes,
                                contentDescription = item.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = item.product.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AuraTextMain,
                                        maxLines = 1
                                    )

                                    IconButton(
                                        onClick = { onRemoveItem(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove item",
                                            tint = AuraTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.selectedColor.hexColor))
                                    )
                                    Text(
                                        text = "${item.selectedColor.name} • Size ${item.selectedSize}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AuraTextSub
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "৳${item.product.price.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                        color = AuraBlack
                                    )

                                    // Quantity Selector
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .border(1.dp, AuraBorder, RoundedCornerShape(4.dp))
                                            .background(AuraCream)
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.id, -1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = AuraBlack,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = item.quantity.toString(),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AuraBlack,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = { onUpdateQuantity(item.id, 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = AuraBlack,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Promo Coupon Card
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraWhite),
                        border = BorderStroke(1.dp, AuraBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PROMO CODE / VOUCHER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextSub
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AuraSuccess.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "COUPON: ${appliedCoupon.code}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = AuraSuccess
                                            )
                                        )
                                        Text(
                                            text = appliedCoupon.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = AuraTextSub
                                        )
                                    }

                                    IconButton(
                                        onClick = onRemoveCoupon,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove coupon", tint = AuraDanger)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = {
                                            couponInput = it
                                            couponError = null
                                        },
                                        placeholder = { Text("Try: AURA10 or AURANEW", style = MaterialTheme.typography.bodySmall) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .testTag("coupon_input")
                                    )

                                    Button(
                                        onClick = {
                                            if (couponInput.isNotBlank()) {
                                                val err = onApplyCoupon(couponInput)
                                                couponError = err
                                                if (err == null) couponInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                                        modifier = Modifier
                                            .height(50.dp)
                                            .testTag("apply_coupon_btn")
                                    ) {
                                        Text("APPLY")
                                    }
                                }

                                if (couponError != null) {
                                    Text(
                                        text = couponError ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = AuraDanger,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary & Checkout Drawer Bar
            Surface(
                color = AuraWhite,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = AuraTextSub)
                        Text("৳${subtotal.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    if (discount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount (${appliedCoupon?.discountPercent}%)", style = MaterialTheme.typography.bodySmall, color = AuraSuccess)
                            Text("-৳${discount.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AuraSuccess))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee", style = MaterialTheme.typography.bodySmall, color = AuraTextSub)
                        Text(
                            if (deliveryFee == 0.0) "FREE" else "৳${deliveryFee.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (deliveryFee == 0.0) AuraSuccess else AuraTextMain
                            )
                        )
                    }

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL AMOUNT", style = MaterialTheme.typography.labelSmall.copy(color = AuraTextSub, fontWeight = FontWeight.Bold))
                            Text(
                                text = "৳${total.toInt()}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = AuraBlack
                            )
                        }

                        Button(
                            onClick = onCheckoutClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraBlack,
                                contentColor = AuraWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("proceed_checkout_btn")
                        ) {
                            Text(
                                text = "CHECKOUT",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
