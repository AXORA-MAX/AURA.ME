package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuraDataSource
import com.example.model.*
import com.example.ui.components.CountDownTimer
import com.example.ui.components.ProductCard
import com.example.ui.components.ProfessionalMonoCountDown
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    activeDrop: NewDrop,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onShopNowClick: () -> Unit,
    onExploreLookbookClick: (LookbookItem) -> Unit,
    onJournalArticleClick: (JournalArticle) -> Unit,
    onAddLookToBag: (StyleLook) -> Unit,
    onToggleDropNotification: () -> Unit,
    onOpenStyleAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraCream)
            .verticalScroll(scrollState)
    ) {
        // ================= HERO SECTION (Professional Polish Theme) =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(460.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(36.dp))
                .clip(RoundedCornerShape(36.dp))
                .background(AuraBlack)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_fashion),
                contentDescription = "Fashion Hero",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient: from black/90 via black/20 to transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            )

            // Top-left floating pill badge: bg-black/40 backdrop-blur border border-white/20 with pulsing red dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AuraDanger)
                    )
                    Text(
                        text = "NEW DROP LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.8.sp
                        ),
                        color = AuraWhite
                    )
                }
            }

            // Bottom editorial hero content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "URBAN\nAFTER DARK",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-1).sp,
                        fontSize = 36.sp,
                        lineHeight = 34.sp
                    ),
                    color = AuraWhite
                )

                Text(
                    text = "The premium streetwear collection designed for the night energy.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = AuraWhite.copy(alpha = 0.75f),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .widthIn(max = 240.dp)
                )

                // Divider and Action Bar
                Divider(
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onShopNowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraWhite,
                            contentColor = AuraBlack
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_shop_drop_btn")
                    ) {
                        Text(
                            text = "SHOP NOW",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            )
                        )
                    }

                    ProfessionalMonoCountDown(
                        initialHours = activeDrop.remainingHours,
                        initialMinutes = activeDrop.remainingMinutes,
                        initialSeconds = activeDrop.remainingSeconds
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= QUICK ACTION EDITORIAL TILES =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Curated / Shop The Look Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AuraWhite),
                border = BorderStroke(1.dp, AuraZincBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clickable { onShopNowClick() }
                    .testTag("tile_curated_looks")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AuraCream),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = "Style",
                            tint = AuraBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CURATED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 9.sp
                            ),
                            color = AuraTextMuted
                        )
                        Text(
                            text = "Shop The Look",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = AuraTextMain
                        )
                    }
                }
            }

            // Journal / Style Guide Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AuraNearBlack),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clickable { onOpenStyleAi() }
                    .testTag("tile_style_guide")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Stylist",
                            tint = AuraWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "JOURNAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 9.sp
                            ),
                            color = Color.White.copy(alpha = 0.45f)
                        )
                        Text(
                            text = "Style Guide →",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp
                            ),
                            color = AuraWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= DROP NOTIFICATION PILL / STRIP =================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraZincBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeDrop.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = AuraTextMain
                    )
                    Text(
                        text = "Unlock ${activeDrop.dropDate} • ${activeDrop.subtitle}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AuraTextMuted
                    )
                }

                FilledTonalButton(
                    onClick = onToggleDropNotification,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (activeDrop.isNotified) AuraSuccess else AuraNearBlack,
                        contentColor = AuraWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("drop_notify_btn")
                ) {
                    Icon(
                        imageVector = if (activeDrop.isNotified) Icons.Filled.Check else Icons.Outlined.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activeDrop.isNotified) "NOTIFIED" else "NOTIFY ME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ================= SECTION: TRENDING & NEW ARRIVALS =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEW ARRIVALS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )
                Text(
                    text = "Engineered for pure presence",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSub
                )
            }

            TextButton(onClick = onShopNowClick) {
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = AuraBlack
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AuraBlack,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(products) { product ->
                Box(modifier = Modifier.width(185.dp)) {
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onWishlistToggle = { onWishlistToggle(product.id) },
                        onClick = { onProductClick(product) },
                        onQuickAdd = { onQuickAdd(product) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ================= STYLE WITH AURA : COMPLETE LOOK =================
        val featuredLook = AuraDataSource.sampleStyleLooks.first()
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    Image(
                        painter = painterResource(id = featuredLook.coverImageRes),
                        contentDescription = featuredLook.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = AuraBlack,
                        contentColor = AuraWhite,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "STYLE WITH AURA.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = AuraGold,
                        contentColor = AuraBlack,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${featuredLook.bundleDiscountPercent}% BUNDLE OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = featuredLook.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )

                    Text(
                        text = featuredLook.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Individual Items Included in the Look
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        featuredLook.products.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${item.name}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = AuraTextMain
                                )
                                Text(
                                    text = "৳${item.price.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AuraTextSub
                                )
                            }
                        }
                    }

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COMPLETE OUTFIT BUNDLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AuraTextSub
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "৳${featuredLook.bundleTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = AuraBlack
                                )
                                Text(
                                    text = "৳${featuredLook.originalTotal.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AuraTextMuted
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onAddLookToBag(featuredLook) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraBlack,
                                contentColor = AuraWhite
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("add_entire_look_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ADD ENTIRE LOOK",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ================= EDITORIAL LOOKBOOK =================
        val lookbook = AuraDataSource.sampleLookbooks.first()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onExploreLookbookClick(lookbook) }
        ) {
            Image(
                painter = painterResource(id = lookbook.imageRes),
                contentDescription = lookbook.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = lookbook.volume,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = AuraGold
                )
                Text(
                    text = lookbook.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraWhite
                )
                Text(
                    text = lookbook.quote,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = AuraSilver,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EXPLORE EDITORIAL LOOKBOOK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuraWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = AuraWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ================= AURA JOURNAL =================
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "AURA JOURNAL",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = AuraTextMain
            )
            Text(
                text = "Stories on craftsmanship, proportion & personal vibe",
                style = MaterialTheme.typography.bodySmall,
                color = AuraTextSub
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuraDataSource.sampleJournal.forEach { article ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraWhite),
                    border = BorderStroke(1.dp, AuraBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onJournalArticleClick(article) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = article.coverImageRes),
                            contentDescription = article.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = article.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AuraGold
                                )
                                Text(
                                    text = article.readTime,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = AuraTextSub
                                )
                            }
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AuraTextMain,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = article.date,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraTextSub
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= BRAND PHILOSOPHY FOOTER =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraBlack)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AURA.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = AuraWhite
                )
                Text(
                    text = "GIVES YOU DIFFERENT VIBE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp
                    ),
                    color = AuraGold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "\"We don't just create clothes. We create a feeling.\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = AuraSilver,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Dhaka Design Studio • Worldwide Dispatch\n© 2026 AURA. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = AuraTextSub,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
