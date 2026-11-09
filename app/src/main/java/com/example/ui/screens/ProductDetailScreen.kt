package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Straighten
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.AuraProductImage
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    allProducts: List<Product>,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onBackClick: () -> Unit,
    onAddToCart: (Product, ColorOption, String) -> Unit,
    onBuyNow: (Product, ColorOption, String) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColor by remember(product) { mutableStateOf(product.availableColors.first()) }
    var selectedSize by remember(product) { mutableStateOf(product.availableSizes.first()) }
    var showSizeGuide by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            Surface(
                color = AuraWhite,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wishlist toggle
                    OutlinedIconButton(
                        onClick = onWishlistToggle,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AuraBorder),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("detail_wishlist_btn")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save to wishlist",
                            tint = if (isWishlisted) AuraDanger else AuraBlack
                        )
                    }

                    // Add to Bag
                    OutlinedButton(
                        onClick = { onAddToCart(product, selectedColor, selectedSize) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, AuraBlack),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraBlack),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_to_bag_btn")
                    ) {
                        Text(
                            text = "ADD TO BAG",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Buy Now
                    Button(
                        onClick = { onBuyNow(product, selectedColor, selectedSize) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraBlack,
                            contentColor = AuraWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Text(
                            text = "BUY NOW",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
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
                .verticalScroll(scrollState)
        ) {
            // Image Gallery Header with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(AuraNearBlack)
            ) {
                AuraProductImage(
                    imageUrl = product.imageUrl,
                    fallbackRes = product.imageRes,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(AuraBlack.copy(alpha = 0.5f))
                        .size(40.dp)
                        .testTag("detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraWhite
                    )
                }

                if (product.isDrop) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AuraBlack,
                        contentColor = AuraWhite,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "LIMITED CAPSULE DROP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Product Main Details Card
            Card(
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraTextSub,
                                letterSpacing = 1.sp
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AuraGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${product.rating} (${product.reviewCount} reviews)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AuraTextMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = AuraTextMain
                    )

                    Text(
                        text = product.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price Block
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "৳${product.price.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = AuraBlack
                        )
                        if (product.originalPrice != null && product.originalPrice > product.price) {
                            Text(
                                text = "৳${product.originalPrice.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = AuraTextMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AuraDanger.copy(alpha = 0.1f),
                                contentColor = AuraDanger
                            ) {
                                val discountPct = (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
                                Text(
                                    text = "-$discountPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 16.dp))

                    // Color Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLOR: ${selectedColor.name.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AuraTextMain
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        product.availableColors.forEach { colorOpt ->
                            val isSelected = selectedColor == colorOpt
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorOpt.hexColor))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AuraBlack else AuraBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorOpt },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorOpt.name.contains("White", true)) AuraBlack else AuraWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Size Selector & Size Guide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SIZE: $selectedSize",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AuraTextMain
                        )

                        TextButton(
                            onClick = { showSizeGuide = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Straighten,
                                contentDescription = null,
                                tint = AuraTextSub,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SIZE GUIDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = AuraTextSub
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(product.availableSizes) { size ->
                            val isSelected = selectedSize == size
                            val stock = product.stockPerSize[size] ?: 10
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AuraBlack else AuraWhite)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AuraBlack else AuraBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedSize = size },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = size,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) AuraWhite else AuraTextMain
                                    )
                                    if (stock in 1..4) {
                                        Text(
                                            text = "$stock left",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                color = if (isSelected) AuraGold else AuraDanger
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fabric, Fit & Care Specifications Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SPECIFICATIONS & CRAFTSMANSHIP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextMain
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SpecItem(label = "FABRIC", value = product.fabric)
                    SpecItem(label = "SILHOUETTE / FIT", value = product.fit)
                    SpecItem(label = "GARMENT CARE", value = product.care)
                    SpecItem(label = "DELIVERY TIME", value = "Dhaka 24-48 hrs, Outside Dhaka 72 hrs")
                    SpecItem(label = "RETURN & EXCHANGE", value = "7 Days hassle-free size exchange")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Related Products
            val relatedProducts = allProducts.filter { it.id != product.id }.take(3)
            if (relatedProducts.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "YOU MIGHT ALSO VIBE WITH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(relatedProducts) { related ->
                            Box(modifier = Modifier.width(170.dp)) {
                                ProductCard(
                                    product = related,
                                    isWishlisted = false,
                                    onWishlistToggle = { onWishlistToggle() },
                                    onClick = { onProductClick(related) },
                                    onQuickAdd = { onAddToCart(related, related.availableColors.first(), "L") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Size Guide Modal
    if (showSizeGuide) {
        ModalBottomSheet(
            onDismissRequest = { showSizeGuide = false },
            containerColor = AuraWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "AURA. SIZE CHART (INCHES)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )
                Text(
                    text = "Measured flat on garment. Boxy oversized streetwear cut.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSub,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SizeRow("SIZE", "CHEST", "LENGTH", "SHOULDER", isHeader = true)
                SizeRow("S", "42\"", "27.5\"", "20.5\"")
                SizeRow("M", "44\"", "28.5\"", "21.5\"")
                SizeRow("L", "46\"", "29.5\"", "22.5\"")
                SizeRow("XL", "48\"", "30.5\"", "23.5\"")
                SizeRow("XXL", "50\"", "31.5\"", "24.5\"")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showSizeGuide = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE GUIDE")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AuraTextSub
            ),
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = AuraTextMain,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SizeRow(col1: String, col2: String, col3: String, col4: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) AuraNearBlack else AuraCream)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = col1,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isHeader) AuraWhite else AuraTextMain,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = col2,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHeader) AuraWhite else AuraTextMain,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = col3,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHeader) AuraWhite else AuraTextMain,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = col4,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHeader) AuraWhite else AuraTextMain,
            modifier = Modifier.weight(1f)
        )
    }
}
