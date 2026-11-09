package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AuraTopBar(
    onSearchClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onBagClick: () -> Unit,
    onAdminClick: () -> Unit,
    wishlistCount: Int,
    cartCount: Int,
    isAdminMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AuraCream,
        contentColor = AuraBlack,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Name & Tagline from Professional Polish design
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AURA.",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                fontSize = 26.sp
                            ),
                            color = AuraBlack
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isAdminMode) AuraGold else AuraBlack)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAdminMode) "ADMIN" else "STUDIO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isAdminMode) AuraBlack else AuraWhite
                            )
                        }
                    }
                    Text(
                        text = "GIVES YOU DIFFERENT VIBE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AuraBlack.copy(alpha = 0.6f)
                    )
                }

                // Action Buttons: White circular buttons with subtle border & shadow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Admin Switch
                    TopBarActionButton(
                        onClick = onAdminClick,
                        testTag = "admin_toggle_button"
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Admin Control Suite",
                            tint = if (isAdminMode) AuraGold else AuraBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Search Button
                    TopBarActionButton(
                        onClick = onSearchClick,
                        testTag = "top_search_button"
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search Catalog",
                            tint = AuraBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Wishlist Button
                    TopBarActionButton(
                        onClick = onWishlistClick,
                        hasDot = wishlistCount > 0,
                        dotColor = AuraDanger,
                        testTag = "top_wishlist_button"
                    ) {
                        Icon(
                            imageVector = if (wishlistCount > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (wishlistCount > 0) AuraDanger else AuraBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bag Button
                    TopBarActionButton(
                        onClick = onBagClick,
                        hasDot = cartCount > 0,
                        dotColor = AuraDanger,
                        testTag = "top_bag_button"
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = "Shopping Bag",
                            tint = AuraBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasDot: Boolean = false,
    dotColor: Color = AuraDanger,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(elevation = 1.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(AuraWhite)
            .border(1.dp, AuraBorderLight, CircleShape)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()

        if (hasDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(1.5.dp, AuraWhite, CircleShape)
            )
        }
    }
}

