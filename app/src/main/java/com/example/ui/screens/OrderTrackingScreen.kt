package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.AuraProductImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    order: Order,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Order Confirmed" to "Order accepted & payment recorded",
        "Processing" to "Fabric quality check & folding",
        "Packed" to "Sealed in AURA signature matte black box",
        "Shipped" to "Handed to ${order.courier}",
        "Out for Delivery" to "Courier rider is on the way to you",
        "Delivered" to "Successfully received by customer"
    )

    val currentStep = order.status.stepIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ORDER TRACKING",
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
            // Header summary card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraBlack),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ORDER ID: ${order.id}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = AuraWhite
                            )
                            Text(
                                text = "Placed on ${order.orderDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraTextSub
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AuraGold,
                            contentColor = AuraBlack
                        ) {
                            Text(
                                text = order.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AuraNearBlack, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COURIER TRACKING CODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = AuraTextSub,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = order.trackingId,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = AuraWhite
                            )
                            Text(
                                text = "Carrier: ${order.courier}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraSilver
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy code",
                            tint = AuraGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Estimated Delivery: ${order.estimatedDelivery}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = AuraGold
                    )
                }
            }

            // Timeline Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DISPATCH TIMELINE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = AuraTextMain
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    steps.forEachIndexed { index, (stepTitle, stepDesc) ->
                        val isDone = index <= currentStep
                        val isCurrent = index == currentStep

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (isDone) AuraBlack else AuraTagBg)
                                        .border(1.dp, if (isDone) AuraBlack else AuraBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AuraWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AuraBorder)
                                        )
                                    }
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(38.dp)
                                            .background(if (index < currentStep) AuraBlack else AuraBorder)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 18.dp else 0.dp)) {
                                Text(
                                    text = stepTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isDone) AuraTextMain else AuraTextMuted
                                )
                                Text(
                                    text = stepDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AuraTextSub
                                )
                            }
                        }
                    }
                }
            }

            // Items Purchased Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PACKAGE CONTENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraTextSub,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraProductImage(
                                imageUrl = item.product.imageUrl,
                                fallbackRes = item.product.imageRes,
                                contentDescription = item.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = AuraTextMain
                                )
                                Text(
                                    text = "${item.selectedColor.name} • Size ${item.selectedSize} (Qty: ${item.quantity})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AuraTextSub
                                )
                            }

                            Text(
                                text = "৳${item.totalPrice.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = AuraBlack
                            )
                        }
                    }
                }
            }

            // Delivery Details & Support
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DELIVER TO: ${order.customerName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuraTextMain
                    )
                    Text(
                        text = order.deliveryAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub
                    )
                    Text(
                        text = "Contact: ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub
                    )

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Need help with this order?",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTextSub
                        )

                        OutlinedButton(
                            onClick = { /* call hotline */ },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AuraBlack),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraBlack)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AURA SUPPORT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
