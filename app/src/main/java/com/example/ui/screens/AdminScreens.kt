package com.example.ui.screens

import androidx.compose.foundation.*
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (!isAuthenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(CrimsonPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Admin Lock Icon",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Admin Access Panel",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Secure authentication is required to access admin control center.",
                        color = GraySub,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it
                            errorMessage = null 
                        },
                        label = { Text("Enter Passcode") },
                        placeholder = { Text("Admin Secret Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedLabelColor = GraySub
                        ),
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Filled.Key, contentDescription = "Passcode Icon", tint = CrimsonPrimary)
                        },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description, tint = GraySub)
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                if (passwordInput == "abdulwahabxxx") {
                                    isAuthenticated = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Abolish: Incorrect Passcode Provided!"
                                }
                            }
                        )
                    )

                    if (errorMessage != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Error, contentDescription = "Error icon", tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = CrimsonPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo("feed") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftWhite),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (passwordInput == "abdulwahabxxx") {
                                    isAuthenticated = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Abolish: Incorrect Passcode Provided!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlock", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        val adminSubScreen by viewModel.adminSubScreen.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
        // 1. Sidebar Navigation - Responsive & Premium
        Column(
            modifier = Modifier
                .width(80.dp) // Sleek Icon Sidebar for mobile screens
                .fillMaxHeight()
                .background(DarkSurface)
                .border(width = 1.dp, color = BorderColor)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mini compact branding
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CrimsonPrimary.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Security, "Admin", tint = CrimsonPrimary, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sidebar navigation items
            SidebarIconItem(icon = Icons.Filled.Dashboard, label = "Home", active = adminSubScreen == "home") {
                viewModel.setAdminSubScreen("home")
            }
            SidebarIconItem(icon = Icons.Filled.People, label = "Users", active = adminSubScreen == "users") {
                viewModel.setAdminSubScreen("users")
            }
            SidebarIconItem(icon = Icons.Filled.VideoLibrary, label = "Reels", active = adminSubScreen == "videos") {
                viewModel.setAdminSubScreen("videos")
            }
            SidebarIconItem(icon = Icons.Filled.AdsClick, label = "Ads", active = adminSubScreen == "ads") {
                viewModel.setAdminSubScreen("ads")
            }
            SidebarIconItem(icon = Icons.Filled.Paid, label = "Coins", active = adminSubScreen == "earnings") {
                viewModel.setAdminSubScreen("earnings")
            }
            SidebarIconItem(icon = Icons.Filled.Flag, label = "Reports", active = adminSubScreen == "reports") {
                viewModel.setAdminSubScreen("reports")
            }
            SidebarIconItem(icon = Icons.Filled.Send, label = "Broadcast", active = adminSubScreen == "notifications") {
                viewModel.setAdminSubScreen("notifications")
            }
            SidebarIconItem(icon = Icons.Filled.Settings, label = "Config", active = adminSubScreen == "settings") {
                viewModel.setAdminSubScreen("settings")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Exit admin dashboard back to feed
            IconButton(
                onClick = { viewModel.navigateTo("feed") },
                modifier = Modifier.background(BorderColor, CircleShape)
            ) {
                Icon(Icons.Filled.ExitToApp, "Back to Feed", tint = SoftWhite)
            }
        }

        // 2. Active Screen Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            when (adminSubScreen) {
                "home" -> AdminHomePanel(viewModel)
                "users" -> AdminUsersPanel(viewModel)
                "videos" -> AdminVideosPanel(viewModel)
                "ads" -> AdminAdsPanel(viewModel)
                "earnings" -> AdminEarningsPanel(viewModel)
                "reports" -> AdminReportsPanel(viewModel)
                "notifications" -> AdminNotificationsPanel(viewModel)
                "settings" -> AdminSettingsPanel(viewModel)
                else -> AdminHomePanel(viewModel)
            }
        }
    }
}
}

@Composable
fun SidebarIconItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (active) CrimsonPrimary.copy(alpha = 0.12f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (active) CrimsonPrimary.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) CrimsonPrimary else GraySub,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ------------------------------------------
// --- PANEL 1: LIVE ANALYTICS HOME ---
// ------------------------------------------

@Composable
fun AdminHomePanel(viewModel: MainViewModel) {
    val stats by viewModel.liveAnalytics.collectAsState()
    val ads by viewModel.adSettings.collectAsState()
    val creators by viewModel.allUsers.collectAsState()
    val logs by viewModel.adminLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Super Admin Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
                Text("Live Analytics system monitor", fontSize = 11.sp, color = GraySub)
            }
            Box(
                modifier = Modifier
                    .background(EmeraldGain.copy(0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(EmeraldGain, CircleShape))
                    Text("LIVE FEED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGain)
                }
            }
        }

        // Analytical Metric cards in grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Active Users",
                value = creators.size.toString(),
                subtitle2 = "+15% growth",
                color = CrimsonPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Global Video Views",
                value = stats?.totalViews?.toString() ?: "0",
                subtitle2 = "+2.4K today",
                color = CyberOcean,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Est. Ad Revenue",
                value = "$${String.format("%.2f", (ads?.revenueCents ?: 0) / 100.0)}",
                subtitle2 = "${ads?.clicksCount ?: 0} integration clicks",
                color = GoldWarning,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Watch Time",
                value = "${stats?.totalWatchTimeMinutes}m",
                subtitle2 = "Avg 23m / user",
                color = EmeraldGain,
                modifier = Modifier.weight(1f)
            )
        }

        // Custom Visual line plot
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("User Activity Curve (24h)", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val points = listOf(10f, 25f, 15f, 45f, 30f, 75f, 60f, 90f)
                    val widthSpacing = size.width / (points.size - 1)
                    val heightMax = size.height
                    val path = Path()

                    points.forEachIndexed { idx, pt ->
                        val x = idx * widthSpacing
                        val y = heightMax - (pt / 100f * heightMax)
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = CrimsonPrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        // Admin Activity logs history
        Text("Recent Admin System Directives", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                logs.take(4).forEach { log ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(log.action, color = CrimsonPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(log.details, color = SoftWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("Active", color = GraySub, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle2: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = GraySub, fontSize = 11.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = SoftWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle2, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ------------------------------------------
// --- PANEL 2: USER MANAGEMENT ---
// ------------------------------------------

@Composable
fun AdminUsersPanel(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val bannedLogs by viewModel.bannedUsers.collectAsState()
    var search by remember { mutableStateOf("") }
    var adjustAmountText by remember { mutableStateOf("100") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("User Base Manager", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search users by username, bio, country...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = GraySub) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filtered = users.filter { it.username.contains(search, ignoreCase = true) || it.fullName.contains(search, ignoreCase = true) }
            items(filtered) { u ->
                val isBanned = u.isBanned
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (isBanned) Color.Red.copy(0.4f) else BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(36.dp).border(1.dp, BorderColor, CircleShape)) {
                                    AsyncImage(model = u.avatarUrl, contentDescription = null, modifier = Modifier.clip(CircleShape), contentScale = ContentScale.Crop)
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(u.fullName, fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)
                                        if (u.isVerified) Icon(Icons.Filled.Verified, "Verified badge", tint = CyberOcean, modifier = Modifier.size(14.dp))
                                        if (u.isPremium) Icon(Icons.Filled.WorkspacePremium, "Premium badge", tint = GoldWarning, modifier = Modifier.size(14.dp))
                                    }
                                    Text("@${u.username} | ${u.coinBalance} 🪙", color = GraySub, fontSize = 11.sp)
                                }
                            }

                            // Ban Toggle button
                            Button(
                                onClick = {
                                    if (isBanned) {
                                        viewModel.adminUnbanUser(u.id)
                                    } else {
                                        viewModel.adminBanUser(u.id, "Violation of platform standards.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isBanned) EmeraldGain else CrimsonPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (isBanned) "Activate" else "Ban Account", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Give coin
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { viewModel.adminAdjustCoinBalance(u.id, 50) },
                                    modifier = Modifier.background(BorderColor, CircleShape).size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Add, "add", tint = GoldWarning, modifier = Modifier.size(14.dp))
                                }
                                Text("Add 50", fontSize = 11.sp, color = SoftWhite)
                            }

                            // Verify creator
                            TextButton(onClick = { viewModel.adminToggleVerification(u.id) }) {
                                Text(if (u.isVerified) "Unverify" else "Verify Creator", color = CyberOcean, fontSize = 11.sp)
                            }

                            // Premium badge trigger
                            TextButton(onClick = { viewModel.adminTogglePremiumBadge(u.id) }) {
                                Text("Toggle Premium", color = GoldWarning, fontSize = 11.sp)
                            }

                            // Monetization Status
                            TextButton(onClick = { viewModel.adminToggleMonetization(u.id) }) {
                                Text(if (u.monetizationApproved) "Paid Active" else "Enable Earn", color = EmeraldGain, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// --- PANEL 3: VIDEO MANAGEMENT ---
// ------------------------------------------

@Composable
fun AdminVideosPanel(viewModel: MainViewModel) {
    val videos by viewModel.allVideos.collectAsState()
    var search by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Corporate Reels Moderation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Filter content loops by caption content...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filtered = videos.filter { it.caption.contains(search, ignoreCase = true) || it.username.contains(search, ignoreCase = true) }
            items(filtered) { v ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(70.dp, 100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(model = v.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "@${v.username}", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)
                            Text(text = v.caption, color = GraySub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Likes: ${v.likesCount}", fontSize = 10.sp, color = CrimsonPrimary)
                                Text("Views: ${v.viewsCount}", fontSize = 10.sp, color = CyberOcean)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { viewModel.adminDeleteVideo(v.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Remove Video", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                TextButton(onClick = { viewModel.adminToggleFeatured(v.id) }) {
                                    Text(if (v.isFeatured) "Unfeature" else "Feature", color = CyberOcean, fontSize = 10.sp)
                                }

                                TextButton(onClick = { viewModel.adminToggleTrending(v.id) }) {
                                    Text(if (v.isTrending) "Normal" else "Trending", color = GoldWarning, fontSize = 10.sp)
                                }

                                TextButton(onClick = { viewModel.adminToggleCommentsDisabled(v.id) }) {
                                    Text(if (v.commentsDisabled) "Unmute comment" else "Mute comment", color = GraySub, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// --- PANEL 4: ADS ADMOB & ADSTERRA CONTROL ---
// ------------------------------------------

@Composable
fun AdminAdsPanel(viewModel: MainViewModel) {
    val currentSecs by viewModel.adSettings.collectAsState()
    var adsOn by remember { mutableStateOf(true) }
    var admobBannerOn by remember { mutableStateOf(true) }
    var admobInterstitialOn by remember { mutableStateOf(false) }
    var admobRewardedOn by remember { mutableStateOf(false) }
    var adsterraOn by remember { mutableStateOf(false) }
    var adIntervalText by remember { mutableStateOf("30") }
    var customScriptText by remember { mutableStateOf("") }
    var countryRestrictText by remember { mutableStateOf("All") }

    // Ad Identifier and Network unit setup states
    var admobAppIdText by remember { mutableStateOf("") }
    var admobBannerIdText by remember { mutableStateOf("") }
    var admobInterstitialIdText by remember { mutableStateOf("") }
    var admobRewardedIdText by remember { mutableStateOf("") }
    var adsterraSmartlinkText by remember { mutableStateOf("") }

    LaunchedEffect(currentSecs) {
        currentSecs?.let {
            adsOn = it.adsEnabled
            admobBannerOn = it.admobBannerEnabled
            admobInterstitialOn = it.admobInterstitialEnabled
            admobRewardedOn = it.admobRewardedEnabled
            adsterraOn = it.adsterraEnabled
            adIntervalText = it.frequencySeconds.toString()
            customScriptText = it.customAdScript
            countryRestrictText = it.adsByCountry
            admobAppIdText = it.admobAppId
            admobBannerIdText = it.admobBannerId
            admobInterstitialIdText = it.admobInterstitialId
            admobRewardedIdText = it.admobRewardedId
            adsterraSmartlinkText = it.adsterraSmartlinkUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ad Distribution Center", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonPrimary.copy(0.1f), RoundedCornerShape(12.dp))
                .border(1.dp, CrimsonPrimary, RoundedCornerShape(12.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Master Ad Switch", fontWeight = FontWeight.ExtraBold, color = CrimsonPrimary, fontSize = 15.sp)
                Text("Turn off advertisements across the app instantly.", color = GraySub, fontSize = 11.sp)
            }
            Switch(
                checked = adsOn,
                onCheckedChange = { adsOn = it },
                colors = SwitchDefaults.colors(checkedThumbColor = CrimsonPrimary)
            )
        }

        // Ad networks parameters
        Text("Ad Providers Integration", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("AdMob Banner ads on post feed", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Active at top/bottom overlay of reels list", color = GraySub, fontSize = 10.sp)
                    }
                    Checkbox(checked = admobBannerOn, onCheckedChange = { admobBannerOn = it })
                }
                HorizontalDivider(color = BorderColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("AdMob Interstitial full screen screen-change ads", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Triggers alerts when changing tabs", color = GraySub, fontSize = 10.sp)
                    }
                    Checkbox(checked = admobInterstitialOn, onCheckedChange = { admobInterstitialOn = it })
                }
                HorizontalDivider(color = BorderColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("AdMob Recurrent Rewarded Coin Video Ads", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Gives user 20 coins upon watching completely", color = GraySub, fontSize = 10.sp)
                    }
                    Checkbox(checked = admobRewardedOn, onCheckedChange = { admobRewardedOn = it })
                }
                HorizontalDivider(color = BorderColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Adsterra Smartlink redirection monetization", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Redirect profile click events", color = GraySub, fontSize = 10.sp)
                    }
                    Checkbox(checked = adsterraOn, onCheckedChange = { adsterraOn = it })
                }
            }
        }

        // Section: Configure Ad IDs
        Text("Ad Network Identifiers & Unit IDs", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AdMob Setup Configs", fontWeight = FontWeight.SemiBold, color = CrimsonPrimary, fontSize = 13.sp)

                OutlinedTextField(
                    value = admobAppIdText,
                    onValueChange = { admobAppIdText = it },
                    label = { Text("AdMob App ID") },
                    placeholder = { Text("e.g. ca-app-pub-3940256099942544~3347511713") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                OutlinedTextField(
                    value = admobBannerIdText,
                    onValueChange = { admobBannerIdText = it },
                    label = { Text("Banner Ad Unit ID") },
                    placeholder = { Text("e.g. ca-app-pub-3940256099942544/6300978111") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                OutlinedTextField(
                    value = admobInterstitialIdText,
                    onValueChange = { admobInterstitialIdText = it },
                    label = { Text("Interstitial Ad Unit ID") },
                    placeholder = { Text("e.g. ca-app-pub-3940256099942544/1033173712") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                OutlinedTextField(
                    value = admobRewardedIdText,
                    onValueChange = { admobRewardedIdText = it },
                    label = { Text("Rewarded Video Ad Unit ID") },
                    placeholder = { Text("e.g. ca-app-pub-3940256099942544/5224354917") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))

                Text("Adsterra Setup Configs", fontWeight = FontWeight.SemiBold, color = CrimsonPrimary, fontSize = 13.sp)

                OutlinedTextField(
                    value = adsterraSmartlinkText,
                    onValueChange = { adsterraSmartlinkText = it },
                    label = { Text("Adsterra Smartlink URL") },
                    placeholder = { Text("e.g. https://example.com/smartlink") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )
            }
        }

        // Frequency & Custom details
        OutlinedTextField(
            value = adIntervalText,
            onValueChange = { adIntervalText = it },
            label = { Text("Ad Rotation Frequency Interval (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        OutlinedTextField(
            value = countryRestrictText,
            onValueChange = { countryRestrictText = it },
            label = { Text("Restrict Ads by Country code (e.g. US, IN, ALL)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        OutlinedTextField(
            value = customScriptText,
            onValueChange = { customScriptText = it },
            label = { Text("Optional direct Javascript / Google tag manager script code") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        Button(
            onClick = {
                val up = AdSettingsEntity(
                    id = 1,
                    adsEnabled = adsOn,
                    admobBannerEnabled = admobBannerOn,
                    admobInterstitialEnabled = admobInterstitialOn,
                    admobRewardedEnabled = admobRewardedOn,
                    adsterraEnabled = adsterraOn,
                    frequencySeconds = adIntervalText.toIntOrNull() ?: 30,
                    clicksCount = currentSecs?.clicksCount ?: 0,
                    revenueCents = currentSecs?.revenueCents ?: 0,
                    customAdScript = customScriptText,
                    adsByCountry = countryRestrictText,
                    admobAppId = admobAppIdText.trim(),
                    admobBannerId = admobBannerIdText.trim(),
                    admobInterstitialId = admobInterstitialIdText.trim(),
                    admobRewardedId = admobRewardedIdText.trim(),
                    adsterraSmartlinkUrl = adsterraSmartlinkText.trim()
                )
                viewModel.adminSaveAdsSettings(up)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
            Text("Update Global Ads Infrastructure", fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------------------------------
// --- PANEL 5: EARNING & TASK CONTROLS ---
// ------------------------------------------

@Composable
fun AdminEarningsPanel(viewModel: MainViewModel) {
    val withdrawals by viewModel.allWithdrawals.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    var taskTitle by remember { mutableStateOf("") }
    var taskRewardCoins by remember { mutableStateOf("25") }
    var taskDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Coin Earnings Economy Guard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        // Title 1
        Text("Approve Creator Cashouts (Withdrawals)", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)

        if (withdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending payouts requests currently", color = GraySub)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                withdrawals.forEach { wd ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("@${wd.username}requested cashout", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Paypal: ${wd.details}", color = GraySub, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${wd.amountCoins} coins", color = GoldWarning, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    Text("$${String.format("%.2f", wd.amountUsd)} USD", color = EmeraldGain, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when(wd.status) {
                                                "Approved" -> EmeraldGain.copy(0.2f)
                                                "Rejected" -> CrimsonPrimary.copy(0.2f)
                                                else -> GoldWarning.copy(0.2f)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(wd.status, color = when(wd.status) { "Approved" -> EmeraldGain; "Rejected" -> CrimsonPrimary; else -> GoldWarning }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (wd.status == "pending") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.adminApproveWithdrawal(wd.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGain),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Approve Payout", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.adminRejectWithdrawal(wd.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Deny Request", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = BorderColor)

        // Title 2: Manage Reward Tasks
        Text("Create New Rewarded Incentives Task", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)

        OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Task Title (e.g. Connect Strava Account)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Instructions to display to users") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = taskRewardCoins,
                onValueChange = { taskRewardCoins = it },
                label = { Text("Reward Coins count") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
            )

            Button(
                onClick = {
                    if (taskTitle.isNotBlank() && taskDescription.isNotBlank()) {
                        viewModel.adminAddNewTask(
                            title = taskTitle,
                            desc = taskDescription,
                            reward = taskRewardCoins.toIntOrNull() ?: 25,
                            type = "custom_incentive"
                        )
                        taskTitle = ""
                        taskDescription = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier
                    .weight(1.2f)
                    .height(54.dp)
            ) {
                Text("Deploy Task Live", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------------------------------------------
// --- PANEL 6: REPORTS & AI MODERATION MODS ---
// ------------------------------------------

@Composable
fun AdminReportsPanel(viewModel: MainViewModel) {
    val reports by viewModel.allReports.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("AI Moderation & Content Flags", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (reports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reports or content flags raised currenty", color = GraySub)
                    }
                }
            } else {
                items(reports) { rep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, if (rep.status == "pending") CrimsonPrimary.copy(0.4f) else BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("Report Type: ${rep.targetType.uppercase()}", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 13.sp)
                                    Text("Reporter: @${rep.reporterUsername}", color = GraySub, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.Red.copy(0.12f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(rep.status, color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Flags / Reason for Report:", fontSize = 11.sp, color = GraySub)
                            Text(rep.reason, color = SoftWhite, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderColor)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (rep.status == "pending") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextButton(onClick = { viewModel.adminDeleteVideo(rep.targetId); viewModel.adminResolveReport(rep.id) }) {
                                            Text("Censor / Delete Target", color = CrimsonPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.adminResolveReport(rep.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BorderColor)
                                        ) {
                                            Text("Dismiss Report", fontSize = 11.sp, color = SoftWhite)
                                        }
                                    }
                                } else {
                                    Text("Resolved", color = EmeraldGain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// --- PANEL 7: BROADCAST NOTIFICATIONS SYSTEM ---
// ------------------------------------------

@Composable
fun AdminNotificationsPanel(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var title by remember { mutableStateOf("") }
    var msgText by remember { mutableStateOf("") }
    var selectedUserToNotify by remember { mutableStateOf<String?>("user_me") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Emergency Alerts Broadcast", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Draft Broadcast Alert", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Header (Alert tag)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                OutlinedTextField(
                    value = msgText,
                    onValueChange = { msgText = it },
                    label = { Text("Alert body description") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text("Target Audience Segment", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val targetSelf = selectedUserToNotify == "user_me"
                    Button(
                        onClick = { selectedUserToNotify = "user_me" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (targetSelf) CrimsonPrimary else BorderColor)
                    ) {
                        Text("Current User", fontSize = 11.sp)
                    }

                    val targetAll = selectedUserToNotify == null
                    Button(
                        onClick = { selectedUserToNotify = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (targetAll) CrimsonPrimary else BorderColor)
                    ) {
                        Text("All Registered Base", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && msgText.isNotBlank()) {
                            viewModel.adminSendPushBroadcast(title, msgText, selectedUserToNotify)
                            title = ""
                            msgText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Trigger Broadcast Directive", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ------------------------------------------
// --- PANEL 8: GLOBAL CONFIGS & MAINTENANCE ---
// ------------------------------------------

@Composable
fun AdminSettingsPanel(viewModel: MainViewModel) {
    val currentAppName by viewModel.appCustomName.collectAsState()
    val maintenanceMode by viewModel.appMaintenanceMode.collectAsState()
    val registrationsActive by viewModel.appRegistrationsEnabled.collectAsState()
    val uploadsActive by viewModel.appUploadsEnabled.collectAsState()
    val themeChoice by viewModel.appThemeColor.collectAsState()
    val apiCredentials by viewModel.apiCredentialsKey.collectAsState()

    var nameInput by remember { mutableStateOf(currentAppName) }
    var apiCredInput by remember { mutableStateOf(apiCredentials) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("System Parameters Config", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftWhite)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Master Access Settings", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Maintanence mode flag active", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Freezes the application view for client", color = GraySub, fontSize = 10.sp)
                    }
                    Switch(checked = maintenanceMode, onCheckedChange = { viewModel.appMaintenanceMode.value = it })
                }
                Divider(color = BorderColor)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Access gating: Enable user registrations", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Disallow incoming simulator database profiles if toggled", color = GraySub, fontSize = 10.sp)
                    }
                    Switch(checked = registrationsActive, onCheckedChange = { viewModel.appRegistrationsEnabled.value = it })
                }
                Divider(color = BorderColor)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Media gating: Enable video uploads", color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Disables loops synthesis pipeline for users", color = GraySub, fontSize = 10.sp)
                    }
                    Switch(checked = uploadsActive, onCheckedChange = { viewModel.appUploadsEnabled.value = it })
                }
            }
        }

        // Renaming app
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it; viewModel.appCustomName.value = it },
            label = { Text("Application launcher brand designation") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        // API configs
        OutlinedTextField(
            value = apiCredInput,
            onValueChange = { apiCredInput = it; viewModel.apiCredentialsKey.value = it },
            label = { Text("Secure Gemini AI Secret API keys credentials") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor)
        )

        // Theme colors choices
        Text("Branding accent highlights", color = SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themesList = listOf("Crimson", "Oceanic", "Emerald", "Gold")
            themesList.forEach { col ->
                val activeTheme = themeChoice == col
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeTheme) CrimsonPrimary else DarkSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable { viewModel.appThemeColor.value = col }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(col, color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BorderColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))

        // --- SUB SECTION: SUPABASE CLOUD DATABASE CONFIGS ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Cloud,
                contentDescription = "Supabase cloud",
                tint = CrimsonPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Supabase Storage & Database Sync",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
        }

        val dbMode by viewModel.appDatabaseMode.collectAsState()
        val sUrl by viewModel.supabaseUrl.collectAsState()
        val sKey by viewModel.supabaseAnonKey.collectAsState()
        val sBucket by viewModel.supabaseBucketName.collectAsState()
        val sStatus by viewModel.supabaseConnectionStatus.collectAsState()

        var urlInput by remember { mutableStateOf(sUrl) }
        var keyInput by remember { mutableStateOf(sKey) }
        var bucketInput by remember { mutableStateOf(sBucket) }
        var showSchemaScript by remember { mutableStateOf(false) }

        // Alert helper to copy
        val context = androidx.compose.ui.platform.LocalContext.current

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Configure your Supabase PostgreSQL parameters to deploy production storage and live data tables. Your selected storage choice takes precedence immediately for storing custom video photo loop files.",
                    color = GraySub,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Room Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (dbMode == "sqlite_local") CrimsonPrimary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(10.dp))
                            .border(1.dp, if (dbMode == "sqlite_local") CrimsonPrimary else BorderColor, RoundedCornerShape(10.dp))
                            .clickable { viewModel.appDatabaseMode.value = "sqlite_local" }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Storage, "Local sqlite", tint = if (dbMode == "sqlite_local") CrimsonPrimary else GraySub)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Room SQLite Local", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Supabase Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (dbMode == "supabase_cloud") CrimsonPrimary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(10.dp))
                            .border(1.dp, if (dbMode == "supabase_cloud") CrimsonPrimary else BorderColor, RoundedCornerShape(10.dp))
                            .clickable { viewModel.appDatabaseMode.value = "supabase_cloud" }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Cloud, "Supabase active", tint = if (dbMode == "supabase_cloud") CrimsonPrimary else GraySub)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Supabase Cloud", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it; viewModel.supabaseUrl.value = it },
                    label = { Text("Supabase project REST Endpoint URL") },
                    placeholder = { Text("https://xxxx.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                    singleLine = true
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it; viewModel.supabaseAnonKey.value = it },
                    label = { Text("Supabase Service / Anon API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bucketInput,
                    onValueChange = { bucketInput = it; viewModel.supabaseBucketName.value = it },
                    label = { Text("Supabase Media Storage Bucket ID (Photos/Videos)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                    singleLine = true
                )

                // Connection status card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (sStatus.contains("Success")) Color.Green else if (sStatus.contains("Error") || sStatus.contains("Failed")) CrimsonPrimary else Color.Yellow,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Database Hub Status: $sStatus", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.testSupabaseConnection() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify Endpoint Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.syncLocalDataToSupabase() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Push Local Data Sync", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Schema code block expander
        Button(
            onClick = { showSchemaScript = !showSchemaScript },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Icon(Icons.Filled.SettingsBackupRestore, "Schema code", tint = SoftWhite, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showSchemaScript) "Hide PostgreSQL Schema definitions" else "Reveal Supabase SQL Schema Generator", color = SoftWhite, fontSize = 11.sp)
        }

        if (showSchemaScript) {
            val schemaSql = """
-- ====================================================
-- REELOAI CUSTOM SUPABASE SQL SCHEMA
-- Execute this directly in your Supabase SQL editor workspace!
-- =====================================================

-- 1. Profiles & Channels table
CREATE TABLE IF NOT EXISTS public.users (
  id VARCHAR PRIMARY KEY,
  username VARCHAR NOT NULL UNIQUE,
  display_name VARCHAR,
  avatar_url VARCHAR DEFAULT 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde',
  coin_balance INTEGER DEFAULT 1000,
  can_upload BOOLEAN DEFAULT TRUE,
  is_verified BOOLEAN DEFAULT FALSE,
  is_premium BOOLEAN DEFAULT FALSE
);

-- 2. Videos Loop Storage table
CREATE TABLE IF NOT EXISTS public.videos (
  id VARCHAR PRIMARY KEY,
  user_id VARCHAR REFERENCES public.users(id) ON DELETE CASCADE,
  username VARCHAR,
  avatar_url VARCHAR,
  caption TEXT,
  video_url VARCHAR NOT NULL,
  thumbnail_url VARCHAR,
  likes_count INTEGER DEFAULT 0,
  comments_count INTEGER DEFAULT 0,
  shares_count INTEGER DEFAULT 0,
  views_count INTEGER DEFAULT 0,
  is_featured BOOLEAN DEFAULT FALSE,
  comments_disabled BOOLEAN DEFAULT FALSE,
  timestamp BIGINT
);

-- 3. Comments table
CREATE TABLE IF NOT EXISTS public.comments (
  id VARCHAR PRIMARY KEY,
  video_id VARCHAR REFERENCES public.videos(id) ON DELETE CASCADE,
  user_id VARCHAR REFERENCES public.users(id) ON DELETE CASCADE,
  username VARCHAR,
  avatar_url VARCHAR,
  text TEXT NOT NULL,
  timestamp BIGINT
);

-- 4. Direct Messages Table
CREATE TABLE IF NOT EXISTS public.messages (
  id VARCHAR PRIMARY KEY,
  sender_id VARCHAR NOT NULL,
  receiver_id VARCHAR NOT NULL,
  content TEXT NOT NULL,
  timestamp BIGINT,
  is_read BOOLEAN DEFAULT FALSE
);

-- 5. Row Level Security policies
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.videos ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public select of loops" 
ON public.videos FOR SELECT USING (true);

CREATE POLICY "Allow authenticated insert of loops" 
ON public.videos FOR INSERT WITH CHECK (true);
            """.trimIndent()

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PostgreSQL DDL Dumper (Supabase Ready)", color = CrimsonPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        IconButton(
                            onClick = {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clipData = android.content.ClipData.newPlainText("Supabase Schema", schemaSql)
                                clipboardManager.setPrimaryClip(clipData)
                                android.widget.Toast.makeText(context, "SQL Schema script copied successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, "Copy", tint = SoftWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = schemaSql,
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Copy the above standard schema queries and execute them in your Supabase dashboard to allow quick remote data storage binding.", color = GraySub, fontSize = 9.sp)
                }
            }
        }
    }
}
