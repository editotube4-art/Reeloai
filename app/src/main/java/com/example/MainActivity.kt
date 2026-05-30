package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
        
        // Dynamic dynamic permissions launcher on startup
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
          contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { _ -> }
        
        // State variables
        val isUserLoggedIn by viewModel.isLoggedIn.collectAsState()
        val activeScreen by viewModel.currentScreen.collectAsState()
        val appName by viewModel.appCustomName.collectAsState()
        val maintenanceActive by viewModel.appMaintenanceMode.collectAsState()
        val themeColorAccent by viewModel.appThemeColor.collectAsState()

        var showSplashTrigger by remember { mutableStateOf(true) }

        androidx.compose.runtime.LaunchedEffect(showSplashTrigger) {
          if (!showSplashTrigger) {
            val permissionsToRequest = mutableListOf<String>()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
              permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
              permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
              permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            } else {
              permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
              permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            try {
              permissionLauncher.launch(permissionsToRequest.toTypedArray())
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }
        }

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
                  IconButton(
                    onClick = {
                      if (activeScreen == "admin_dashboard") {
                        viewModel.navigateTo("feed")
                      } else {
                        viewModel.navigateTo("admin_dashboard")
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (activeScreen == "admin_dashboard") Icons.Filled.VideoLibrary else Icons.Filled.Settings,
                      contentDescription = "Admin Settings Toggle",
                      tint = if (activeScreen == "admin_dashboard") CrimsonPrimary else Color.White
                    )
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
              val showAdWatchDialog by viewModel.showAdWatchDialog.collectAsState()
              val adWatchReward by viewModel.adWatchReward.collectAsState()

              if (showAdWatchDialog) {
                RewardedAdLauncherDialog(
                  rewardCoins = adWatchReward,
                  onComplete = { viewModel.completeRewardedAdWatch() },
                  onClose = { viewModel.completeRewardedAdWatch() }
                )
              }

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

@Composable
fun RewardedAdLauncherDialog(
  rewardCoins: Int,
  onComplete: () -> Unit,
  onClose: () -> Unit
) {
  var timeLeft by remember { mutableStateOf(5) }
  var adFinished by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    while (timeLeft > 0) {
      kotlinx.coroutines.delay(1000)
      timeLeft--
    }
    adFinished = true
  }

  androidx.compose.ui.window.Dialog(
    onDismissRequest = { if (adFinished) onClose() }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF1E1E1E), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
        .border(2.dp, CrimsonPrimary, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .background(CrimsonPrimary, androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("AD", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
          }
          Text(
            text = if (adFinished) "Finished!" else "Watching Ad ...",
            color = Color.White.copy(0.7f),
            fontSize = 12.sp
          )
          if (adFinished) {
            IconButton(onClick = onClose) {
              Icon(Icons.Filled.Close, "Close", tint = Color.White)
            }
          } else {
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(Color.White.copy(0.1f), androidx.compose.foundation.shape.CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text("$timeLeft", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live icon ad layout
        Box(
          modifier = Modifier
            .size(90.dp)
            .background(Color.White.copy(0.05f), androidx.compose.foundation.shape.CircleShape)
            .border(1.dp, CrimsonPrimary.copy(0.3f), androidx.compose.foundation.shape.CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.PlayCircleFilled,
            contentDescription = "Ad Icon",
            tint = CrimsonPrimary,
            modifier = Modifier.size(50.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Interactive Sponsored Ad",
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
          fontSize = 16.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Watch this short sponsor clip to claim your free wallet coins reward!",
          color = GraySub,
          fontSize = 11.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (adFinished) {
          Button(
            onClick = onComplete,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGain),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Filled.Paid, "Coins", tint = Color.White, modifier = Modifier.size(22.dp))
              Text(
                "Claim Reward! (+$rewardCoins 🪙)",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
              )
            }
          }
        } else {
          Button(
            onClick = {},
            enabled = false,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              disabledContainerColor = Color.White.copy(0.1f),
              disabledContentColor = Color.White.copy(0.3f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Text(
              "Watch Ad to Claim (+$rewardCoins 🪙)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}

