package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// --- CENTRAL MOBILE LAYOUT WRAPPER ---
// ==========================================

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1800)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C1B), Color(0xFF050508))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFF2B54).copy(alpha = scale * 0.2f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Logo",
                    tint = Color(0xFFFF2B54),
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ReeloAI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Short Video, Endless Earning",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = GraySub
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = CrimsonPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(DarkSurface, RoundedCornerShape(24.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegister) "Join ReeloAI" else "Welcome Back",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SoftWhite
            )
            Text(
                text = if (isRegister) "Create a real-time creator account" else "Verify your social identity on ReeloAI",
                fontSize = 12.sp,
                color = GraySub,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = CrimsonPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = CrimsonPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = CrimsonPrimary
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isRegister) {
                        viewModel.registerNewUser(username, fullName, password) { success, message ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        viewModel.loginWithPassword(username, password) { success, message ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text(
                    text = if (isRegister) "Register Account" else "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { isRegister = !isRegister }) {
                Text(
                    text = if (isRegister) "Already have an account? Sign In" else "New to Reelo? Register Now",
                    color = CrimsonPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ------------------------------------------
// --- VIDEO REELS FEED SCREEN ---
// ------------------------------------------

@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val activeVideos by viewModel.activeVideos.collectAsState()
    val adConfig by viewModel.adSettings.collectAsState()
    val currentVideoIndex by viewModel.feedVideoIndex.collectAsState()
    val scope = rememberCoroutineScope()

    if (activeVideos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.VideoLibrary, "No videos", tint = BorderColor, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No videos uploaded or active", color = GraySub)
            }
        }
    } else {
        var showCommentsSheet by remember { mutableStateOf(false) }
        var showShareAlert by remember { mutableStateOf(false) }
        val safeIndex = if (activeVideos.isNotEmpty()) {
            currentVideoIndex.coerceIn(0, activeVideos.lastIndex)
        } else {
            0
        }
        val video = activeVideos.getOrNull(safeIndex) ?: return
        val context = androidx.compose.ui.platform.LocalContext.current

        // Accumulate drag offsets for responsive gesture tracking
        var dragAccumulator by remember(video.id) { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .draggable(
                    state = rememberDraggableState { delta ->
                        dragAccumulator += delta
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        if (dragAccumulator < -100f) { // Swiped Up (Next Reel)
                            if (currentVideoIndex < activeVideos.lastIndex) {
                                viewModel.setFeedVideoIndex(currentVideoIndex + 1)
                            }
                        } else if (dragAccumulator > 100f) { // Swiped Down (Previous Reel)
                            if (currentVideoIndex > 0) {
                                viewModel.setFeedVideoIndex(currentVideoIndex - 1)
                            }
                        }
                    }
                )
        ) {
            // Interactive video content mockup (never buffers, fully responsive)
            InteractiveVideoMockup(
                video = video,
                onDoubleTap = { viewModel.toggleLikeVideo(video.id) }
            )

            // Dynamic Algorithm selector tabs overlay at the top of the Video Feed
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val algorithms = listOf("ForYou" to "✨ For You", "Trending" to "🔥 Trending", "Recent" to "🕒 Recent")
                val activeAlgo by viewModel.feedAlgorithm.collectAsState()
                algorithms.forEach { (algoKey, label) ->
                    val isSelected = activeAlgo == algoKey
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) CrimsonPrimary else Color.Transparent)
                            .clickable { viewModel.setFeedAlgorithm(algoKey) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else SoftWhite.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom fade overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Right side Action Buttons (Profile, Like, Comment, Share, Gift)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Circle (Click to view Profile)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { viewModel.navigateToProfile(video.userId) }
                ) {
                    AsyncImage(
                        model = video.avatarUrl,
                        contentDescription = "Creator avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Like Item
                val isLikedFlow = remember(video.id, viewModel.currentUserId.value) {
                    viewModel.isLiked(video.id, viewModel.currentUserId.value)
                }.collectAsState(initial = false)
                FeedActionButton(
                    imageVector = if (isLikedFlow.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = video.likesCount.toString(),
                    color = if (isLikedFlow.value) CrimsonPrimary else Color.White,
                    onClick = { viewModel.toggleLikeVideo(video.id) }
                )

                // Comments Item
                FeedActionButton(
                    imageVector = Icons.Outlined.Comment,
                    label = video.commentsCount.toString(),
                    onClick = { showCommentsSheet = true }
                )

                // Share Item
                FeedActionButton(
                    imageVector = Icons.Outlined.Share,
                    label = video.sharesCount.toString(),
                    onClick = {
                        if (video.sharingDisabled) {
                            showShareAlert = true
                        } else {
                            viewModel.shareVideo(video.id, context)
                        }
                    }
                )

                // Coin reward / Gift trigger
                FeedActionButton(
                    imageVector = Icons.Filled.MonetizationOn,
                    label = "Gift",
                    color = GoldWarning,
                    onClick = {
                        viewModel.giftCoinsToCreator(10, video.userId) { success, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // Bottom video info (Username, Caption, scrolling hashtags)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { viewModel.navigateToProfile(video.userId) }
                ) {
                    Text(
                        text = "@${video.username}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified Status",
                        tint = CyberOcean,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = video.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                // Music Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.MusicNote, "Music note", tint = Color.White, modifier = Modifier.size(12.dp))
                    Text("Original Audio - ReeloAI Synth", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Scroll arrows to swipe up/down manually
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentVideoIndex > 0) {
                    IconButton(
                        onClick = { viewModel.setFeedVideoIndex(currentVideoIndex - 1) },
                        modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Prev video", tint = Color.White)
                    }
                }
                if (currentVideoIndex < activeVideos.lastIndex) {
                    IconButton(
                        onClick = { viewModel.setFeedVideoIndex(currentVideoIndex + 1) },
                        modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Next video", tint = Color.White)
                    }
                }
            }

            // Real-time Ads management simulation overlay
            if (adConfig?.adsEnabled == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                        .border(1.dp, CrimsonPrimary.copy(0.4f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.adminRecordAdClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(CrimsonPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("SPONSORED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("Interactive Ad: Click to earn coins!", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Popups & Bottom Sheets
            var commentsList by remember { mutableStateOf<List<CommentEntity>>(emptyList()) }
            val liveCommentsFlow = remember(video.id) {
                viewModel.getCommentsForVideo(video.id)
            }.collectAsState(initial = emptyList())

            if (showCommentsSheet) {
                CommentsBottomSheet(
                    comments = liveCommentsFlow.value,
                    commentsDisabled = video.commentsDisabled,
                    onDismiss = { showCommentsSheet = false },
                    onSend = { text -> viewModel.addCommentToVideo(video.id, text) },
                    onUserClick = { userId ->
                        viewModel.navigateToProfile(userId)
                        showCommentsSheet = false
                    }
                )
            }
            if (showShareAlert) {
                AlertDialog(
                    onDismissRequest = { showShareAlert = false },
                    title = { Text("Sharing Restrained") },
                    text = { Text("The administrator of this application has disabled comment sharing parameters for this specific video content.") },
                    confirmButton = {
                        TextButton(onClick = { showShareAlert = false }) { Text("Dismiss", color = CrimsonPrimary) }
                    }
                )
            }
        }
    }
}

@Composable
fun FeedActionButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun InteractiveVideoMockup(video: VideoEntity, onDoubleTap: () -> Unit) {
    var isPlaying by remember { mutableStateOf(true) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var playPercent by remember { mutableStateOf(0.4f) }
    val scope = rememberCoroutineScope()

    // Simulate animated video timeline and rotation CD
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            rotationAngle = (rotationAngle + 2) % 360
            val nextPercent = playPercent + 0.005f
            playPercent = if (nextPercent > 1f) 0f else nextPercent
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onTap = { isPlaying = !isPlaying }
                )
            }
    ) {
        // Aesthetic dynamic gradient backgrounds that evoke short video aesthetics
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = "Video Thumbnail background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay filter
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(0.42f), Color.Transparent, Color.Black.copy(0.6f))
                    )
                )
        )

        // Rotating Cyber Record Loop on bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 120.dp)
                .size(48.dp)
                .background(Color.Black, CircleShape)
                .rotate(rotationAngle)
                .border(2.dp, Color.White.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(CrimsonPrimary, CircleShape)
            )
        }

        // Action indicator (paused)
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(Color.Black.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, "Paused icon", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        // Live Audio Progress Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.5.dp)
                .background(Color.White.copy(0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(playPercent.coerceIn(0f, 1f))
                    .background(CrimsonPrimary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    comments: List<CommentEntity>,
    commentsDisabled: Boolean,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(16.dp)
        ) {
            Text(
                text = "Comments (${comments.size})",
                fontWeight = FontWeight.Bold,
                color = SoftWhite,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (commentsDisabled) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Comments on this video are muted by Admin.", color = GraySub)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No comments yet. Start the conversation!", color = GraySub)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            if (!comment.isSpam) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUserClick(comment.userId) }
                                ) {
                                    AsyncImage(
                                        model = comment.avatarUrl,
                                        contentDescription = "Commenter",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "@${comment.username}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GraySub
                                        )
                                        Text(
                                            text = comment.text,
                                            fontSize = 13.sp,
                                            color = SoftWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Add comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BorderColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSend(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier.background(CrimsonPrimary, CircleShape)
                    ) {
                        Icon(Icons.Filled.Send, "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// --- EXPLORE WINDOWS ---
// ------------------------------------------

@Composable
fun ExploreScreen(viewModel: MainViewModel) {
    val trendingVideos by viewModel.trendingVideos.collectAsState()
    val activeVideos by viewModel.activeVideos.collectAsState()
    var search by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Search Banner
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search trending creators, tags, effects...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = GraySub) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonPrimary,
                unfocusedBorderColor = BorderColor
            ),
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Category Loops
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val categorised = listOf("🔥 Trending", "🎨 Generative AI", "🎧 Synths", "☔ Cyber Aesthetic", "🍿 Cinema", "💡 Advice")
            categorised.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .clickable { search = tag.substring(2) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = tag, color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Trending Content Loops",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Video thumbnails
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingVideos) { video ->
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .clickable {
                            val selIndex = activeVideos.indexOfFirst { it.id == video.id }
                            if (selIndex != -1) {
                                viewModel.setFeedVideoIndex(selIndex)
                                viewModel.navigateTo("feed")
                            } else {
                                viewModel.navigateToProfile(video.userId)
                            }
                        }
                ) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Caption text overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.62f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = video.caption,
                                color = SoftWhite,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Favorite, "likes", tint = CrimsonPrimary, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${video.likesCount} likes", color = GraySub, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// --- VIDEO CREATION & UPLOAD ---
// ------------------------------------------

@Composable
fun UploadScreen(viewModel: MainViewModel) {
    var descriptionInput by remember { mutableStateOf("") }
    var captionResult by remember { mutableStateOf("") }
    var generatorPrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("CyberNeon") }
    var isGeneratingState by remember { mutableStateOf(false) }
    val uploadsActive by viewModel.appUploadsEnabled.collectAsState()
    val scope = rememberCoroutineScope()

    // Standalone state for user uploaded media files
    var selectedVideoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedVideoUri = uri
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedPhotoUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Generate Reels Video",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            Text(
                text = "Describe your vision and generate tags using Gemini AI",
                fontSize = 11.sp,
                color = GraySub,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Dynamic Camera view simulator / preview panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(LightSurface, Color.Black)
                        )
                    )
                    .border(1.5.dp, if (selectedPhotoUri != null) CrimsonPrimary else BorderColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhotoUri != null) {
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Cover Image preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.48f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CheckCircle, "Loaded", tint = CrimsonPrimary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Local Cover Image & Video Loaded", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Will show globally in video feed", color = GraySub, fontSize = 11.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.VideoCameraFront, "Camera preview", tint = CrimsonPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ReeloAI Virtual Camera Simulator", color = SoftWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Loop style: $selectedStyle", color = GraySub, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choose simulated video background
            Text("Select Video Synthesis Base", color = SoftWhite, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("CyberNeon", "Rain", "Grid")
                types.forEach { style ->
                    val isSelected = style == selectedStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) CrimsonPrimary else DarkSurface,
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, if (isSelected) Color.White else BorderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedStyle = style }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(style, color = SoftWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Local file selectors (Photo / Video uploaders)
            Text("Upload Local Files", color = SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (selectedVideoUri != null) CrimsonPrimary else BorderColor),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { videoPickerLauncher.launch("video/*") }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = "Select local video icon",
                            tint = if (selectedVideoUri != null) CrimsonPrimary else GraySub,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedVideoUri != null) "Video Selected ✓" else "Upload Video file",
                            color = if (selectedVideoUri != null) CrimsonPrimary else SoftWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (selectedPhotoUri != null) CrimsonPrimary else BorderColor),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { photoPickerLauncher.launch("image/*") }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Select local image icon",
                            tint = if (selectedPhotoUri != null) CrimsonPrimary else GraySub,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedPhotoUri != null) "Photo Selected ✓" else "Upload Cover Photo",
                            color = if (selectedPhotoUri != null) CrimsonPrimary else SoftWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (selectedVideoUri != null || selectedPhotoUri != null) {
                TextButton(
                    onClick = {
                        selectedVideoUri = null
                        selectedPhotoUri = null
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.ClearAll, "Clear files", tint = CrimsonPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear uploaded media", color = CrimsonPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Prompter
            Text("AI Script assistant (Gemini)", color = SoftWhite, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = generatorPrompt,
                onValueChange = { generatorPrompt = it },
                placeholder = { Text("e.g. A gorgeous cyberpunk rainfall in downtown Tokyo with heavy neon...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberOcean,
                    unfocusedBorderColor = BorderColor
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (generatorPrompt.isNotBlank()) {
                        isGeneratingState = true
                        scope.launch {
                            val cap = viewModel.requestAICaptionGenerate(generatorPrompt)
                            captionResult = cap
                            isGeneratingState = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberOcean)
            ) {
                if (isGeneratingState) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AutoAwesome, "AI icon", tint = Color.Black)
                        Text("Generate AI Caption & Tags", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (captionResult.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, CyberOcean),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, "AI", tint = CyberOcean, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini Suggested Script", color = CyberOcean, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(captionResult, color = SoftWhite, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                descriptionInput = captionResult
                            }) {
                                Text("Apply to post", color = CyberOcean)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption Text
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Post caption & hashtags") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .testTag("upload_caption_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BorderColor
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Publish
            Button(
                onClick = {
                    if (descriptionInput.isNotBlank()) {
                        viewModel.uploadNewVideo(
                            caption = descriptionInput,
                            videoType = selectedStyle,
                            customVideoUri = selectedVideoUri?.toString(),
                            customPhotoUri = selectedPhotoUri?.toString()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                enabled = descriptionInput.isNotBlank() && (viewModel.currentUser.value?.canUpload != false) && uploadsActive && !isGeneratingState
            ) {
                Text("Synthesize & Publish Loop", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------------------------------------------
// --- REAL-TIME INBOX & NOTIFICATIONS SCREEN ---
// ------------------------------------------

@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val notifs by viewModel.notifications.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Inbox & Tasks",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SoftWhite,
            modifier = Modifier.padding(16.dp)
        )

        // Segmented Tabs: Earn Tasks List vs Alerts Message Inbox
        var sectionTab by remember { mutableStateOf("tasks") } // tasks, alerts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { sectionTab = "tasks" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sectionTab == "tasks") CrimsonPrimary else DarkSurface
                )
            ) {
                Text("Earn system goals", fontSize = 12.sp)
            }
            Button(
                onClick = { sectionTab = "alerts" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sectionTab == "alerts") CrimsonPrimary else DarkSurface
                )
            ) {
                Text("Alert Inbox", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (sectionTab == "tasks") {
            // Task views
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Balance Showcase card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE5A823), Color(0xFFC39311))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Reelo AI Reward Coins", color = Color.Black.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${user?.coinBalance ?: 0} 🪙", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            IconButton(onClick = {
                                android.widget.Toast.makeText(context, "Go to your Profile and click 'Earnings Wallet' to request a withdrawal securely 🪙", android.widget.Toast.LENGTH_LONG).show()
                                viewModel.navigateTo("profile")
                            }) {
                                Icon(Icons.Filled.AccountBalanceWallet, "Wallet Settings", tint = Color.Black, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                item {
                    Text("Complete tasks to win", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(tasks) { task ->
                    val isComplete = task.isCompleted
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(14.dp))
                            .border(1.dp, if (isComplete) EmeraldGain.copy(0.3f) else BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                if (!isComplete) {
                                    when (task.taskType) {
                                        "watch_video" -> {
                                            viewModel.watchRewardedAd(task.rewardCoins, task.id)
                                        }
                                        "daily_checkin" -> {
                                            viewModel.watchRewardedAd(task.rewardCoins, task.id)
                                        }
                                        "comment" -> {
                                            android.widget.Toast.makeText(context, "Explore any video reel in the Feed and drop a supportive comment to claim reward!", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                        "refer" -> {
                                            try {
                                                val refName = user?.username ?: "friend"
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "Hey! Download ReeloAI - watch awesome reels, upload short videos, and earn real money with me! Use my referral username: $refName 🪙")
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Share Referral Link"))
                                                viewModel.watchRewardedAd(task.rewardCoins, task.id)
                                            } catch (e: Exception) {
                                                viewModel.watchRewardedAd(task.rewardCoins, task.id)
                                            }
                                        }
                                        else -> {
                                            viewModel.watchRewardedAd(task.rewardCoins, task.id)
                                        }
                                    }
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(task.title, fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)
                                Text(task.description, color = GraySub, fontSize = 11.sp)
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(0.3f)
                            ) {
                                Text("+${task.rewardCoins} 🪙", color = GoldWarning, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isComplete) EmeraldGain.copy(alpha = 0.2f) else CrimsonPrimary.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isComplete) "Claimed" else "Action",
                                        color = if (isComplete) EmeraldGain else CrimsonPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Alerts notification view
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifs.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Your alert manager inbox is clear", color = GraySub)
                        }
                    }
                } else {
                    items(notifs) { notif ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CrimsonPrimary.copy(0.12f), CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(notif.type) {
                                            "admin" -> Icons.Filled.NotificationsActive
                                            "chat" -> Icons.Filled.Mail
                                            else -> Icons.Filled.ThumbUp
                                        },
                                        contentDescription = null,
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(notif.senderUsername, fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 12.sp)
                                    Text(notif.text, color = GraySub, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun doubleOf(dp: androidx.compose.ui.unit.Dp): Double {
    return dp.value.toDouble()
}

// ------------------------------------------
// --- REAL-TIME CHAT SCREEN ---
// ------------------------------------------

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val partner by viewModel.activeChatPartner.collectAsState()
    var inputMsg by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("feed") }) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = SoftWhite)
            }
            Box(modifier = Modifier.size(36.dp).border(1.dp, BorderColor, CircleShape)) {
                AsyncImage(
                    model = partner?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Column {
                Text(partner?.fullName ?: "Chat Room", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 14.sp)
                Text("Active simulation - instant AI typing replies!", color = EmeraldGain, fontSize = 10.sp)
            }
        }

        // Chat logs bubble lists
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == viewModel.currentUserId.value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isMe) CrimsonPrimary else DarkSurface,
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                )
                            )
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .widthIn(max = 240.dp)
                    ) {
                        Text(msg.text, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Input bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMsg,
                onValueChange = { inputMsg = it },
                placeholder = { Text("Mesaage...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BorderColor
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (inputMsg.isNotBlank()) {
                        viewModel.sendMessageToChat(inputMsg)
                        inputMsg = ""
                    }
                },
                modifier = Modifier.background(CrimsonPrimary, CircleShape)
            ) {
                Icon(Icons.Filled.Send, "Send message", tint = Color.White)
            }
        }
    }
}

// ------------------------------------------
// --- PROFILE & COIN WALLET WITHDRAWALS SCREEN ---
// ------------------------------------------

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val user by viewModel.profileUser.collectAsState()
    val videos by viewModel.profileVideos.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isMe = user?.id == currentUserId
    val viewedUserId by viewModel.viewedUserId.collectAsState()

    var walletExpanded by remember { mutableStateOf(false) }
    var paypalMailInput by remember { mutableStateOf("") }
    var coinsToWithdraw by remember { mutableStateOf("100") }
    val globalActiveVideos by viewModel.activeVideos.collectAsState()

    var editExpanded by remember { mutableStateOf(false) }
    var editFullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var editUsername by remember(user) { mutableStateOf(user?.username ?: "") }
    var editBio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var editAvatarUrl by remember(user) { mutableStateOf(user?.avatarUrl ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        if (viewedUserId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.navigateToProfile(null)
                        viewModel.navigateTo("feed")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back to Feed",
                        tint = SoftWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Creator Profile",
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Profile Info Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(2.5.dp, CrimsonPrimary, CircleShape)
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("@${user?.username}", fontWeight = FontWeight.Bold, color = SoftWhite, fontSize = 18.sp)
                if (user?.isVerified == true) {
                    Icon(Icons.Filled.Verified, "Verified", tint = CyberOcean, modifier = Modifier.size(18.dp))
                }
            }
            Text(user?.fullName ?: "Anonymous", color = GraySub, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                user?.bio ?: "No bio defined yet.",
                color = SoftWhite,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User stats row
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatsItem(number = user?.followingCount?.toString() ?: "0", label = "Following")
                StatsItem(number = user?.followerCount?.toString() ?: "0", label = "Followers")
                StatsItem(number = user?.videoCount?.toString() ?: "0", label = "Videos")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic follow/unfollow and send message action buttons if viewing another creator's profile
            if (!isMe && user != null) {
                val isFollowing by viewModel.isFollowing(user!!.id).collectAsState(initial = false)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleFollowCreator(user!!.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) DarkSurface else CrimsonPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = if (isFollowing) BorderStroke(1.dp, BorderColor) else null
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.selectChatPartner(user!!.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(
                            text = "Message",
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // User Rewards wallet card (only show if viewing own profile)
            if (isMe) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5A823).copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .border(1.dp, GoldWarning, RoundedCornerShape(16.dp))
                        .clickable { walletExpanded = !walletExpanded }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.AccountBalanceWallet, "Wallet", tint = GoldWarning)
                                Column {
                                    Text("Earnings Wallet", color = SoftWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Convert coins to real USD payouts", color = GraySub, fontSize = 10.sp)
                                }
                            }
                            Text("${user?.coinBalance ?: 0} 🪙", color = GoldWarning, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }

                        if (walletExpanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BorderColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Simulate Withdrawal (100 coins = $1.00 USD)", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = paypalMailInput,
                                onValueChange = { paypalMailInput = it },
                                placeholder = { Text("PayPal email address") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldWarning, unfocusedBorderColor = BorderColor),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = coinsToWithdraw,
                                onValueChange = { coinsToWithdraw = it },
                                placeholder = { Text("Amount of coins to cash out") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldWarning, unfocusedBorderColor = BorderColor),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val coins = coinsToWithdraw.toIntOrNull() ?: 0
                                    if (coins > 0 && paypalMailInput.isNotBlank()) {
                                        viewModel.requestWalletWithdrawal(coins, paypalMailInput)
                                        paypalMailInput = ""
                                        walletExpanded = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldWarning)
                            ) {
                                Text("Request Payout Approval", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Edit Profile Options (for editing Name, Bio, and Avatar link)
            if (isMe) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrimsonPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .border(1.dp, CrimsonPrimary, RoundedCornerShape(16.dp))
                        .clickable { editExpanded = !editExpanded }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Edit, "Edit Profile", tint = CrimsonPrimary)
                                Column {
                                    Text("Edit Account Profile", color = SoftWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Update display name, handle, avatar or bio", color = GraySub, fontSize = 10.sp)
                                }
                            }
                            Icon(
                                imageVector = if (editExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Expand Edit",
                                tint = CrimsonPrimary
                            )
                        }

                        if (editExpanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BorderColor)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Full Name", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editFullName,
                                onValueChange = { editFullName = it },
                                placeholder = { Text("Alex Mercer") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Username Handle", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editUsername,
                                onValueChange = { editUsername = it },
                                placeholder = { Text("curator") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Short Bio", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                placeholder = { Text("Tell us about yourself...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                                maxLines = 10
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Avatar Image URL", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editAvatarUrl,
                                onValueChange = { editAvatarUrl = it },
                                placeholder = { Text("https://example.com/avatar.jpg") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonPrimary, unfocusedBorderColor = BorderColor),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (editFullName.isNotBlank() && editUsername.isNotBlank()) {
                                        viewModel.updateProfile(
                                            fullName = editFullName,
                                            username = editUsername,
                                            bio = editBio,
                                            avatarUrl = editAvatarUrl
                                        )
                                        android.widget.Toast.makeText(context, "Profile updated successfully! ✨", android.widget.Toast.LENGTH_SHORT).show()
                                        editExpanded = false
                                    } else {
                                        android.widget.Toast.makeText(context, "Full Name & Username cannot be empty!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                            ) {
                                Text("Save Profile Changes", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Admin Dashboard portal entry
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1B2A), RoundedCornerShape(16.dp))
                        .border(1.dp, CyberOcean, RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo("admin_dashboard") }
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Security, "Admin Panel", tint = CyberOcean)
                            Column {
                                Text("Super Admin Panel", color = SoftWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Configure ads, monitor transactions & users", color = GraySub, fontSize = 10.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = "Enter Admin Panel",
                            tint = CyberOcean,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Real Interactive Video Uploads grid count
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoLibrary,
                    contentDescription = "Reels uploads",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Video Uploads", color = SoftWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No uploads yet", color = GraySub, fontSize = 13.sp)
                }
            } else {
                val chunkedVideos = videos.chunked(3)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chunkedVideos.forEach { rowVids ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (cellI in 0 until 3) {
                                val vItem = rowVids.getOrNull(cellI)
                                if (vItem != null) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.75f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSurface)
                                            .clickable {
                                                val selIndex = globalActiveVideos.indexOfFirst { it.id == vItem.id }
                                                if (selIndex != -1) {
                                                    viewModel.setFeedVideoIndex(selIndex)
                                                    viewModel.navigateTo("feed")
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = vItem.thumbnailUrl,
                                            contentDescription = "Video preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                                .background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Views icon",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                val disp = if (vItem.viewsCount >= 1000) "${vItem.viewsCount / 1000}K" else "${vItem.viewsCount}"
                                                Text(
                                                    text = disp,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Option (Only on own profile)
            if (isMe) {
                Button(
                    onClick = { viewModel.simulateLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Log Out", color = CrimsonPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun StatsItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = label, color = GraySub, fontSize = 11.sp)
    }
}
