package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    wishlistedProducts: List<Product>,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onBackClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SAVED PIECES (${wishlistedProducts.size})",
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
        if (wishlistedProducts.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AuraCream)
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO SAVED PIECES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = AuraTextMain
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the heart on any garment to keep track of your desired silhouettes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextSub,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("EXPLORE STYLES")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = modifier
                    .fillMaxSize()
                    .background(AuraCream)
                    .padding(innerPadding)
            ) {
                items(wishlistedProducts) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = true,
                        onWishlistToggle = { onWishlistToggle(product.id) },
                        onClick = { onProductClick(product) },
                        onQuickAdd = { onQuickAdd(product) }
                    )
                }
            }
        }
    }
}
