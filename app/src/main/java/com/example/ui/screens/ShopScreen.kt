package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

enum class SortOption(val title: String) {
    FEATURED("Featured"),
    NEWEST("Newest"),
    PRICE_LOW_TO_HIGH("Price: Low → High"),
    PRICE_HIGH_TO_LOW("Price: High → Low"),
    RATING("Best Selling")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onQuickAdd: (Product) -> Unit,
    initialCategory: ProductCategory = ProductCategory.ALL,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedStyle by remember { mutableStateOf(StyleType.ALL) }
    var selectedSort by remember { mutableStateOf(SortOption.FEATURED) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var maxPrice by remember { mutableStateOf(5000f) }

    // Filter and Sort Logic
    val filteredProducts = remember(products, selectedCategory, selectedStyle, selectedSort, searchQuery, maxPrice) {
        products.filter { product ->
            val matchCat = selectedCategory == ProductCategory.ALL || product.category == selectedCategory
            val matchStyle = selectedStyle == StyleType.ALL || product.style == selectedStyle
            val matchPrice = product.price <= maxPrice
            val matchQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true) ||
                    product.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchCat && matchStyle && matchPrice && matchQuery
        }.let { list ->
            when (selectedSort) {
                SortOption.FEATURED -> list.sortedByDescending { it.isFeatured }
                SortOption.NEWEST -> list.sortedByDescending { it.isNew }
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
                SortOption.RATING -> list.sortedByDescending { it.rating }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraCream)
    ) {
        // Search & Filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraWhite)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search tees, cargo, hoodies, vibe...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AuraTextSub,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = AuraTextSub)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraBlack,
                    unfocusedBorderColor = AuraBorder,
                    focusedContainerColor = AuraCream,
                    unfocusedContainerColor = AuraCream
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ProductCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AuraBlack,
                            selectedLabelColor = AuraWhite,
                            containerColor = AuraCream,
                            labelColor = AuraTextMain
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) AuraBlack else AuraBorder,
                            selectedBorderColor = AuraBlack,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Style & Sort quick bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} STYLES AVAILABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraTextSub,
                        letterSpacing = 0.5.sp
                    )
                )

                OutlinedButton(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AuraBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("open_filter_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = AuraBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FILTERS & SORT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraBlack
                        )
                    )
                }
            }
        }

        // Product Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO PRODUCTS FOUND",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuraTextMain
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing filters or search query to explore other drops.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedCategory = ProductCategory.ALL
                            selectedStyle = StyleType.ALL
                            searchQuery = ""
                            maxPrice = 5000f
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraBlack,
                            contentColor = AuraWhite
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("RESET ALL FILTERS")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onWishlistToggle = { onWishlistToggle(product.id) },
                        onClick = { onProductClick(product) },
                        onQuickAdd = { onQuickAdd(product) }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "YOU'VE VIEWED ALL PIECES IN THIS SELECTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraTextMuted,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Filter & Sort Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = AuraWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "FILTER & SORT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sort Options
                Text(
                    text = "SORT BY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraTextSub
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SortOption.values()) { opt ->
                        val isSel = selectedSort == opt
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSort = opt },
                            label = { Text(opt.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuraBlack,
                                selectedLabelColor = AuraWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Style Filter
                Text(
                    text = "STYLE SILHOUETTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraTextSub
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StyleType.values()) { style ->
                        val isSel = selectedStyle == style
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedStyle = style },
                            label = { Text(style.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuraBlack,
                                selectedLabelColor = AuraWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MAX PRICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraTextSub
                        )
                    )
                    Text(
                        text = "৳${maxPrice.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = AuraBlack
                        )
                    )
                }

                Slider(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    valueRange = 800f..5000f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraBlack,
                        activeTrackColor = AuraBlack,
                        inactiveTrackColor = AuraBorder
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraBlack,
                        contentColor = AuraWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "APPLY FILTERS (${filteredProducts.size} RESULTS)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
