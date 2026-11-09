package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuraDataSource
import com.example.model.Product
import com.example.model.StyleLook
import com.example.ui.theme.*

@Composable
fun StyleAiScreen(
    allProducts: List<Product>,
    onAddLookToBag: (StyleLook) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var userPrompt by remember { mutableStateOf("") }
    var activeLookTitle by remember { mutableStateOf("CURATED: MONOCHROME AFTER DARK") }
    var activeLookDescription by remember {
        mutableStateOf("Calculated for evening presence. High-contrast boxy drape paired with structured cinch bottoms and understated steel accents.")
    }
    var activeVibeTag by remember { mutableStateOf("CASUAL STREETWEAR") }

    // Quick suggestion prompts
    val quickVibes = listOf(
        "Casual Black Outfit",
        "Oversized Streetwear",
        "Minimalist Night Out",
        "Summer Linen Relaxed",
        "Technical Rainy Day"
    )

    // Curated items based on selected vibe
    val currentSelectedProducts = remember(activeVibeTag, allProducts) {
        when (activeVibeTag) {
            "Casual Black Outfit" -> listOf(allProducts[0], allProducts[1], allProducts[3])
            "Oversized Streetwear" -> listOf(allProducts[0], allProducts[2])
            "Minimalist Night Out" -> listOf(allProducts[2], allProducts[1], allProducts[3])
            "Summer Linen Relaxed" -> listOf(allProducts[4], allProducts[1])
            else -> listOf(allProducts[0], allProducts[1], allProducts[3])
        }
    }

    val originalTotal = currentSelectedProducts.sumOf { it.price }
    val bundleDiscountPercent = 15
    val bundleTotal = originalTotal * (1 - bundleDiscountPercent / 100.0)

    val currentStyleLook = remember(activeLookTitle, currentSelectedProducts) {
        StyleLook(
            id = "ai-look-${activeVibeTag.hashCode()}",
            title = activeLookTitle,
            description = activeLookDescription,
            products = currentSelectedProducts,
            bundleDiscountPercent = bundleDiscountPercent,
            coverImageRes = R.drawable.img_complete_look
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraCream)
            .verticalScroll(scrollState)
    ) {
        // AI Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraBlack)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AuraGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AuraBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AURA STYLE LAB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = AuraWhite
                        )
                        Text(
                            text = "Intelligent outfit curation tailored to your vibe",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTextSub
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User prompt bar
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    placeholder = {
                        Text(
                            text = "e.g. \"I want a casual black outfit for night\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTextMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (userPrompt.isNotBlank()) {
                                    activeLookTitle = "AURA LOOK: ${userPrompt.uppercase()}"
                                    activeLookDescription = "Custom algorithmic curation matching '$userPrompt' with boxy proportions & tonal synergy."
                                    activeVibeTag = userPrompt
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = AuraGold)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraGold,
                        unfocusedBorderColor = AuraNearBlack,
                        focusedContainerColor = AuraNearBlack,
                        unfocusedContainerColor = AuraNearBlack,
                        focusedTextColor = AuraWhite,
                        unfocusedTextColor = AuraWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_style_prompt_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Vibe Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickVibes) { vibe ->
                        val isSel = activeVibeTag == vibe
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                activeVibeTag = vibe
                                activeLookTitle = "AURA LOOK: ${vibe.uppercase()}"
                                activeLookDescription = "Optimized silhouette and drape for $vibe. Harmonious weights and complementary textures."
                            },
                            label = {
                                Text(
                                    text = vibe,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuraGold,
                                selectedLabelColor = AuraBlack,
                                containerColor = AuraNearBlack,
                                labelColor = AuraSilver
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Curated Look Output Card
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
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_complete_look),
                        contentDescription = "Complete Outfit",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = AuraBlack,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AI GENERATED CAPSULE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = AuraWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = AuraGold,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "15% BUNDLE SAVING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraBlack
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
                        text = activeLookTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = AuraTextMain
                    )

                    Text(
                        text = activeLookDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Text(
                        text = "PIECES IN THIS OUTFIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraTextSub,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )

                    // Individual Items List
                    currentSelectedProducts.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraCream)
                                .clickable { onProductClick(item) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = item.imageRes),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AuraTextMain
                                    )
                                    Text(
                                        text = item.fabric,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = AuraTextSub
                                    )
                                }
                            }

                            Text(
                                text = "৳${item.price.toInt()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = AuraBlack
                            )
                        }
                    }

                    Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 12.dp))

                    // Pricing and Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BUNDLE TOTAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextSub
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "৳${bundleTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = AuraBlack
                                )
                                Text(
                                    text = "৳${originalTotal.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AuraTextMuted
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onAddLookToBag(currentStyleLook) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraBlack,
                                contentColor = AuraWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("shop_this_look_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHOP THIS LOOK",
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

        Spacer(modifier = Modifier.height(30.dp))
    }
}
