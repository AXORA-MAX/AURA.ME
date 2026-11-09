package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuraUser
import com.example.model.Order
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    orders: List<Order>,
    onOrderClick: (Order) -> Unit,
    onAdminClick: () -> Unit,
    user: AuraUser? = null,
    onSignOut: () -> Unit = {},
    onSignIn: (String, String, (Boolean, String?) -> Unit) -> Unit = { _, _, _ -> },
    onSignUp: (String, String, String, String, (Boolean, String?) -> Unit) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showBrandStoryModal by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authName by remember { mutableStateOf("") }
    var authPhone by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authLoading by remember { mutableStateOf(false) }

    val displayName = user?.name?.ifBlank { "AURA Member" } ?: "AURA Member"
    val displayEmail = user?.email?.ifBlank { "guest@aura.fashion" } ?: "guest@aura.fashion"
    val initials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
        .map { it.first().uppercase() }.joinToString("").ifBlank { "AU" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraCream)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraBlack)
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AuraNearBlack)
                                .border(1.5.dp, AuraGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = AuraGold
                                )
                            )
                        }

                        Column {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraWhite
                                )
                            )
                            Text(
                                text = displayEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraTextSub
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AuraGold,
                        contentColor = AuraBlack
                    ) {
                        Text(
                            text = "AURA ELITE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Loyalty Points / Perks Card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraNearBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "450 VIBE REWARD POINTS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = AuraWhite,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Early Access Unlocked for Drop 04",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AuraGold
                            )
                        }

                        Button(
                            onClick = { /* redeem points */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraWhite,
                                contentColor = AuraBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("REDEEM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order History Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "YOUR ORDERS (${orders.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = AuraTextMain
            )

            Spacer(modifier = Modifier.height(10.dp))

            orders.forEach { order ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraWhite),
                    border = BorderStroke(1.dp, AuraBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onOrderClick(order) }
                        .testTag("order_item_${order.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.id,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = AuraTextMain
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AuraCream,
                                contentColor = AuraBlack
                            ) {
                                Text(
                                    text = order.status.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${order.items.size} item(s) • ৳${order.total.toInt()} • Placed ${order.orderDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTextSub
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Track: ${order.trackingId}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraGold
                                )
                            )

                            Text(
                                text = "TAP TO TRACK →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraBlack
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Quick Actions
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = AuraWhite),
            border = BorderStroke(1.dp, AuraBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                AccountOptionRow(
                    icon = Icons.Outlined.CloudSync,
                    title = "Firebase Cloud Sync",
                    subtitle = "Project: aura-f4291 • ${if (user?.isAnonymous == false) "Logged in as ${user.email}" else "Guest Session Active"}",
                    onClick = { showAuthModal = true },
                    accentColor = AuraSuccess
                )
                Divider(color = AuraBorder)
                AccountOptionRow(
                    icon = Icons.Outlined.AdminPanelSettings,
                    title = "Business Admin Suite",
                    subtitle = "Inventory, orders, drops & revenue metrics",
                    onClick = onAdminClick,
                    accentColor = AuraGold
                )
                Divider(color = AuraBorder)
                AccountOptionRow(
                    icon = Icons.Outlined.Info,
                    title = "About AURA.",
                    subtitle = "\"We don't just create clothes. We create a feeling.\"",
                    onClick = { showBrandStoryModal = true }
                )
                Divider(color = AuraBorder)
                AccountOptionRow(
                    icon = Icons.Outlined.LocalShipping,
                    title = "Saved Delivery Addresses",
                    subtitle = "Banani, Dhaka (Default)",
                    onClick = { /* addresses */ }
                )
                Divider(color = AuraBorder)
                AccountOptionRow(
                    icon = Icons.Outlined.SupportAgent,
                    title = "Concierge Support & WhatsApp",
                    subtitle = "Instant responses from Dhaka Design Studio",
                    onClick = { /* concierge */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Brand Story Modal
    if (showBrandStoryModal) {
        ModalBottomSheet(
            onDismissRequest = { showBrandStoryModal = false },
            containerColor = AuraWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "AURA.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = AuraBlack
                )
                Text(
                    text = "GIVES YOU DIFFERENT VIBE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraGold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Brand Concept & Mission",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AuraTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AURA. is a modern luxury streetwear and lifestyle house founded with a single uncompromising belief: clothing should not be disposable merch. It should define the atmosphere you bring into every room.\n\nFrom our signature 280 GSM heavyweight organic cottons to articulated cargo silhouettes and minimalist stainless steel jewelry, every detail is engineered for presence, durability, and distinction.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextMain
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showBrandStoryModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Firebase Auth Modal
    if (showAuthModal) {
        ModalBottomSheet(
            onDismissRequest = { showAuthModal = false },
            containerColor = AuraWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (user?.isAnonymous == false) "FIREBASE ACCOUNT" else if (isRegisterMode) "JOIN AURA CLUB" else "AURA SIGN IN",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuraBlack
                )
                Text(
                    text = "Cloud Project: aura-f4291 • Realtime Auth",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraTextSub
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (user?.isAnonymous == false) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Logged In User",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AuraTextSub
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = AuraTextMain
                            )
                            Text(
                                text = "UID: ${user.uid}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSignOut()
                            showAuthModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraDanger),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SIGN OUT", color = AuraWhite, fontWeight = FontWeight.Bold)
                    }
                } else {
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = authName,
                            onValueChange = { authName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = authPhone,
                            onValueChange = { authPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = authEmail,
                        onValueChange = { authEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = authPassword,
                        onValueChange = { authPassword = it },
                        label = { Text("Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    authError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = AuraDanger, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authLoading = true
                            authError = null
                            if (isRegisterMode) {
                                onSignUp(authName, authEmail, authPassword, authPhone) { success, error ->
                                    authLoading = false
                                    if (success) {
                                        showAuthModal = false
                                    } else {
                                        authError = error ?: "Registration failed"
                                    }
                                }
                            } else {
                                onSignIn(authEmail, authPassword) { success, error ->
                                    authLoading = false
                                    if (success) {
                                        showAuthModal = false
                                    } else {
                                        authError = error ?: "Authentication failed"
                                    }
                                }
                            }
                        },
                        enabled = !authLoading && authEmail.isNotBlank() && authPassword.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraBlack),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(color = AuraWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isRegisterMode) "CREATE ACCOUNT" else "SIGN IN", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            authError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign In" else "New to AURA? Create Account",
                            color = AuraTextMain,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accentColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor ?: AuraBlack,
            modifier = Modifier.size(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AuraTextMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = AuraTextSub
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AuraTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
