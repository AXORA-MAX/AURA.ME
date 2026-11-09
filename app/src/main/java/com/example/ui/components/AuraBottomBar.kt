package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class AuraTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SHOP("Shop", Icons.Filled.Checkroom, Icons.Outlined.Checkroom),
    STYLE_AI("Style AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BAG("Bag", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    PROFILE("Me", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun AuraBottomBar(
    selectedTab: AuraTab,
    onTabSelected: (AuraTab) -> Unit,
    cartItemCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AuraWhite,
        contentColor = AuraBlack,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = AuraBorderLight)
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("nav_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        // Active dark pill from design: bg-zinc-900 rounded-full px-5 py-1.5
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AuraNearBlack)
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = tab.filledIcon,
                                    contentDescription = tab.title,
                                    tint = AuraWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (tab == AuraTab.BAG && cartItemCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(AuraDanger)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.outlinedIcon,
                                contentDescription = tab.title,
                                tint = AuraTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab == AuraTab.BAG && cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AuraDanger)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = (-0.2).sp,
                            fontSize = 10.sp
                        ),
                        color = if (isSelected) AuraBlack else AuraTextMuted
                    )
                }
            }
        }
    }
}
