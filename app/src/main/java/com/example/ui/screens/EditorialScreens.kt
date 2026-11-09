package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JournalArticle
import com.example.model.LookbookItem
import com.example.model.Product
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookbookDetailScreen(
    lookbook: LookbookItem,
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lookbook.volume,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
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
        ) {
            Image(
                painter = painterResource(id = lookbook.imageRes),
                contentDescription = lookbook.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = lookbook.season,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraGold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = lookbook.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraTextMain
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = lookbook.quote,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = AuraTextMain
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FEATURED IN THIS EDITORIAL",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraWhite),
                    border = BorderStroke(1.dp, AuraBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(lookbook.featuredProduct) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = lookbook.featuredProduct.imageRes),
                            contentDescription = lookbook.featuredProduct.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lookbook.featuredProduct.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = lookbook.featuredProduct.fabric,
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraTextSub
                            )
                        }

                        Text(
                            text = "৳${lookbook.featuredProduct.price.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    article: JournalArticle,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AURA JOURNAL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
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
        ) {
            Image(
                painter = painterResource(id = article.coverImageRes),
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraGold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = article.readTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraTextSub
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = AuraTextMain
                )

                Text(
                    text = "By ${article.author} • ${article.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSub,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(color = AuraBorder, modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = article.excerpt,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 26.sp
                    ),
                    color = AuraTextMain
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    color = AuraTextMain
                )
            }
        }
    }
}
