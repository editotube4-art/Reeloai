package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        
        // State variables
        val isUserLoggedIn by viewModel.isLoggedIn.collectAsState()
        val activeScreen by viewModel.currentScreen.collectAsState()
        val appName by viewModel.appCustomName.collectAsState()
        val maintenanceActive by viewModel.appMaintenanceMode.collectAsState()
        val themeColorAccent by viewModel.appThemeColor.collectAsState()

        var showSplashTrigger by remember { mutableStateOf(true) }

        if (showSplashTrigger) {
          SplashScreen(onDismiss = { showSplashTrigger = false })
        } else if (!isUserLoggedIn) {
          LoginScreen(viewModel = viewModel)
        } else {
          // Main layout content
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
              CenterAlignedTopAppBar(
                title = {
                  Text(
                    text = appName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                  containerColor = DarkSurface
                ),
                actions = {
                  // Admin Mode toggle switch in header
                  Box(
                    modifier = Modifier
                      .padding(end = 12.dp)
                      .background(
                        if (activeScreen == "admin_dashboard") CrimsonPrimary else BorderColor,
                        CircleShape
                      )
                      .clickable {
                        if (activeScreen == "admin_dashboard") {
                          viewModel.navigateTo("feed")
                        } else {
                          viewModel.navigateTo("admin_dashboard")
                        }
                      }
                      .padding(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(
                        imageVector = if (activeScreen == "admin_dashboard") Icons.Filled.VerifiedUser else Icons.Filled.Lock,
                        contentDescription = "Admin Mode Switch",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                      Text(
                        text = if (activeScreen == "admin_dashboard") "Admin: ON" else "Admin Panel",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              )
            },
            bottomBar = {
              // Hide navigation bar when viewing the Admin Panel
              if (activeScreen != "admin_dashboard") {
                NavigationBar(
                  containerColor = DarkSurface,
                  tonalElevation = 8.dp,
                  windowInsets = WindowInsets.navigationBars
                ) {
                  NavigationBarItem(
                    selected = activeScreen == "feed",
                    onClick = { viewModel.navigateTo("feed") },
                    icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = "Feed") },
                    label = { Text("Feed", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = CrimsonPrimary,
                      selectedTextColor = CrimsonPrimary,
                      indicatorColor = CrimsonPrimary.copy(alpha = 0.1f)
                    )
                  )

                  NavigationBarItem(
                    selected = activeScreen == "explore",
                    onClick = { viewModel.navigateTo("explore") },
                    icon = { Icon(Icons.Filled.Explore, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = CrimsonPrimary,
                      selectedTextColor = CrimsonPrimary,
                      indicatorColor = CrimsonPrimary.copy(alpha = 0.1f)
                    )
                  )

                  NavigationBarItem(
                    selected = activeScreen == "upload",
                    onClick = { viewModel.navigateTo("upload") },
                    icon = {
                      Box(
                        modifier = Modifier
                          .size(36.dp)
                          .background(CrimsonPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(Icons.Filled.Add, contentDescription = "Upload", tint = Color.White)
                      }
                    },
                    colors = NavigationBarItemDefaults.colors(
                      indicatorColor = Color.Transparent
                    )
                  )

                  NavigationBarItem(
                    selected = activeScreen == "notifications",
                    onClick = { viewModel.navigateTo("notifications") },
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "Inbox & Tasks") },
                    label = { Text("Inbox", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = CrimsonPrimary,
                      selectedTextColor = CrimsonPrimary,
                      indicatorColor = CrimsonPrimary.copy(alpha = 0.1f)
                    )
                  )

                  NavigationBarItem(
                    selected = activeScreen == "profile",
                    onClick = { viewModel.navigateTo("profile") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "My Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = CrimsonPrimary,
                      selectedTextColor = CrimsonPrimary,
                      indicatorColor = CrimsonPrimary.copy(alpha = 0.1f)
                    )
                  )
                }
              }
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              if (maintenanceActive && activeScreen != "admin_dashboard") {
                // Application-wide simulated Maintenance Lockout layout
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(24.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                      imageVector = Icons.Filled.Construction,
                      contentDescription = "Maintenance Mode Active",
                      tint = GoldWarning,
                      modifier = Modifier.size(70.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                      text = "Scheduled App Security Upgrade",
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = SoftWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "The super admin has temporarily enabled Maintenance Mode. Please check back shortly or access the dashboard above to adjust settings.",
                      color = GraySub,
                      fontSize = 13.sp,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              } else {
                // Normal view selectors
                when (activeScreen) {
                  "feed" -> FeedScreen(viewModel)
                  "explore" -> ExploreScreen(viewModel)
                  "upload" -> UploadScreen(viewModel)
                  "notifications" -> NotificationsScreen(viewModel)
                  "chat" -> ChatScreen(viewModel)
                  "profile" -> ProfileScreen(viewModel)
                  "admin_dashboard" -> AdminDashboardScreen(viewModel)
                  else -> FeedScreen(viewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}

