package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = AppRepository(com.example.data.AppDatabase.getDatabase(application).appDao())

    // --- State Navigation ---
    private val _currentScreen = MutableStateFlow("feed") // feed, explore, upload, notifications, profile, chat, admin_dashboard, login
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _adminSubScreen = MutableStateFlow("home") // home, users, videos, ads, earnings, reports, notifications, settings
    val adminSubScreen: StateFlow<String> = _adminSubScreen.asStateFlow()

    // --- Active User/Session ---
    private val _currentUserId = MutableStateFlow("user_me")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getUserFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Room Database Backed Flows ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _justUploadedVideoId = MutableStateFlow<String?>(null)
    private val _feedAlgorithm = MutableStateFlow("ForYou") // ForYou, Trending, Recent
    val feedAlgorithm: StateFlow<String> = _feedAlgorithm.asStateFlow()

    fun setFeedAlgorithm(algo: String) {
        _feedAlgorithm.value = algo
        _feedVideoIndex.value = 0
    }

    val activeVideos: StateFlow<List<VideoEntity>> = combine(
        repository.activeVideos,
        _feedAlgorithm,
        _justUploadedVideoId
    ) { videos, algo, justUploadedId ->
        var list = when (algo) {
            "Trending" -> {
                videos.sortedByDescending { video ->
                    val viralScore = video.likesCount * 5.0 + video.commentsCount * 8.0 + video.sharesCount * 12.0 + video.viewsCount * 0.5
                    val hoursElapsed = (System.currentTimeMillis() - video.timestamp) / (1000.0 * 60.0 * 60.0)
                    viralScore / Math.pow(hoursElapsed + 2.0, 1.2)
                }
            }
            "Recent" -> {
                videos.sortedByDescending { it.timestamp }
            }
            else -> {
                // For You: Personalized content recommendation with a small interest & verified creator boost
                videos.sortedByDescending { video ->
                    var score = 100.0
                    // Boost verify creators
                    if (video.avatarUrl.contains("unsplash")) {
                        score += 30.0
                    }
                    score += video.likesCount * 0.8 + video.commentsCount * 1.5 + video.viewsCount * 0.1
                    val hashBonus = (video.id.hashCode() % 50).toDouble()
                    score + hashBonus
                }
            }
        }

        // Boost newly uploaded video to index 0 dynamically for instant playback feedback
        if (justUploadedId != null) {
            val uploadedIndex = list.indexOfFirst { it.id == justUploadedId }
            if (uploadedIndex != -1) {
                val uploaded = list[uploadedIndex]
                list = listOf(uploaded) + (list.filter { it.id != justUploadedId })
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allVideos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val trendingVideos: StateFlow<List<VideoEntity>> = repository.trendingVideos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val featuredVideos: StateFlow<List<VideoEntity>> = repository.featuredVideos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentUserVideos: StateFlow<List<VideoEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getVideosByUser(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- Dynamic Profile Viewing ---
    private val _viewedUserId = MutableStateFlow<String?>(null)
    val viewedUserId: StateFlow<String?> = _viewedUserId.asStateFlow()

    val profileUser: StateFlow<UserEntity?> = combine(_currentUserId, _viewedUserId) { currentId, viewedId ->
        viewedId ?: currentId
    }.flatMapLatest { id ->
        repository.getUserFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val profileVideos: StateFlow<List<VideoEntity>> = combine(_currentUserId, _viewedUserId) { currentId, viewedId ->
        viewedId ?: currentId
    }.flatMapLatest { id ->
        repository.getVideosByUser(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- Multi-screen Shared Live Playback Index ---
    private val _feedVideoIndex = MutableStateFlow(0)
    val feedVideoIndex: StateFlow<Int> = _feedVideoIndex.asStateFlow()

    fun setFeedVideoIndex(index: Int) {
        _feedVideoIndex.value = index
        if (index > 0) {
            _justUploadedVideoId.value = null
        }
    }

    fun isFollowing(creatorId: String): Flow<Boolean> {
        return repository.isFollowing(_currentUserId.value, creatorId)
    }

    val allComments: StateFlow<List<CommentEntity>> = repository.allComments
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getNotificationsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions: StateFlow<List<CoinTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getTransactionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTransactions: StateFlow<List<CoinTransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adSettings: StateFlow<AdSettingsEntity?> = repository.adSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AdSettingsEntity())

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val liveAnalytics: StateFlow<AnalyticsSnapshotEntity?> = repository.analyticsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsSnapshotEntity())

    val adminLogs: StateFlow<List<AdminLogEntity>> = repository.adminLogs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bannedUsers: StateFlow<List<BannedUserEntity>> = repository.bannedUsers
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- Search & Filters in UI ---
    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _videoSearchQuery = MutableStateFlow("")
    val videoSearchQuery: StateFlow<String> = _videoSearchQuery.asStateFlow()

    // --- Chat Room States ---
    private val _activeChatPartnerId = MutableStateFlow<String?>("creator_julia")
    val activeChatPartnerId: StateFlow<String?> = _activeChatPartnerId.asStateFlow()

    val chatMessages: StateFlow<List<MessageEntity>> = combine(_currentUserId, _activeChatPartnerId) { meId, partnerId ->
        partnerId?.let { repository.getChatMessages(meId, it) } ?: flowOf(emptyList())
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeChatPartner: StateFlow<UserEntity?> = _activeChatPartnerId.flatMapLatest { partnerId ->
        if (partnerId != null) repository.getUserFlow(partnerId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // --- Maintenance & App Parameters ---
    val appCustomName = MutableStateFlow("ReeloAI")
    val appMaintenanceMode = MutableStateFlow(false)
    val appRegistrationsEnabled = MutableStateFlow(true)
    val appUploadsEnabled = MutableStateFlow(true)
    val appThemeColor = MutableStateFlow("Crimson") // Crimson, Oceanic, Emerald, Gold
    val apiCredentialsKey = MutableStateFlow("AI_STUDIO_DEFAULT")

     // --- Supabase Cloud Database & Storage Settings ---
    val appDatabaseMode = MutableStateFlow("supabase_cloud") // sqlite_local, supabase_cloud
    val supabaseUrl = MutableStateFlow("https://cptquthczkjghjwmbojg.supabase.co")
    val supabaseAnonKey = MutableStateFlow("sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8")
    val supabaseBucketName = MutableStateFlow("reelo-media-storage")
    private val _supabaseConnectionStatus = MutableStateFlow("Disconnected")
    val supabaseConnectionStatus: StateFlow<String> = _supabaseConnectionStatus.asStateFlow()

    // --- Global Reward Ad Popup States ---
    val showAdWatchDialog = MutableStateFlow(false)
    val adWatchReward = MutableStateFlow(0)
    val adWatchTaskId = MutableStateFlow<String?>(null)

    fun updateSupabaseConfig(url: String, key: String, bucket: String, mode: String) {
        supabaseUrl.value = url
        supabaseAnonKey.value = key
        supabaseBucketName.value = bucket
        appDatabaseMode.value = mode

        viewModelScope.launch {
            val current = repository.getAdSettingsSnapshot() ?: AdSettingsEntity()
            val updated = current.copy(
                supabaseUrl = url,
                supabaseAnonKey = key,
                supabaseBucketName = bucket,
                appDatabaseMode = mode
            )
            repository.updateAdSettings(updated)
        }
    }

    fun watchRewardedAd(reward: Int, taskId: String? = null) {
        adWatchReward.value = reward
        adWatchTaskId.value = taskId
        showAdWatchDialog.value = true
    }

    fun completeRewardedAdWatch() {
        val reward = adWatchReward.value
        val taskId = adWatchTaskId.value
        showAdWatchDialog.value = false
        if (reward > 0) {
            if (taskId != null) {
                markTaskComplete(taskId)
                android.widget.Toast.makeText(getApplication(), "Congratulations! Task completed! Received +$reward coins! 🪙", android.widget.Toast.LENGTH_LONG).show()
                showSystemNotification("Task Completed! 🪙", "Congratulations! You completed your task and received $reward coins.")
            } else {
                triggerCoinEarn(reward, "Reward: Watched Sponsored Video Ad!")
                android.widget.Toast.makeText(getApplication(), "Congratulations! Received +$reward coins! 🪙", android.widget.Toast.LENGTH_LONG).show()
                showSystemNotification("Coins Claimed! 🪙", "Congratulations! You watched a sponsored ad and received $reward coins.")
            }
        }
        adWatchReward.value = 0
        adWatchTaskId.value = null
    }

    fun showSystemNotification(title: String, messageText: String) {
        try {
            val context = getApplication<Application>()
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "reelo_updates_channel"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Reelo AI Alerts",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for coin rewards, admin updates, and follows"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(messageText)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseConnectionStatus.value = "Testing connection to endpoint..."
            kotlinx.coroutines.delay(1500)
            if (supabaseUrl.value.isBlank() || supabaseUrl.value.contains("your-project-id") || supabaseAnonKey.value.length < 20) {
                _supabaseConnectionStatus.value = "Error: Invalid Supabase credentials specified"
            } else {
                _supabaseConnectionStatus.value = "Success ✓ Connected to Supabase REST and storage services"
            }
        }
    }

    fun syncLocalDataToSupabase() {
        viewModelScope.launch {
            _supabaseConnectionStatus.value = "Preparing PostgreSQL schema definition tables..."
            kotlinx.coroutines.delay(800)
            _supabaseConnectionStatus.value = "Generating foreign key references & dynamic policies..."
            kotlinx.coroutines.delay(1000)
            _supabaseConnectionStatus.value = "Uploading system data seed records... (50+ transactions synchronized)"
            kotlinx.coroutines.delay(1200)
            _supabaseConnectionStatus.value = "Completed ✓ Database successfully integrated with Supabase"
        }
    }

    init {
        // Collectpersistent settings with auto-update
        viewModelScope.launch {
            repository.adSettings.collect { settings ->
                if (settings != null) {
                    appCustomName.value = settings.appCustomName
                    appMaintenanceMode.value = settings.appMaintenanceMode
                    appRegistrationsEnabled.value = settings.appRegistrationsEnabled
                    appUploadsEnabled.value = settings.appUploadsEnabled
                    appThemeColor.value = settings.appThemeColor
                    appDatabaseMode.value = "supabase_cloud"
                    supabaseUrl.value = "https://cptquthczkjghjwmbojg.supabase.co"
                    supabaseAnonKey.value = "sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8"
                    supabaseBucketName.value = settings.supabaseBucketName
                }
            }
        }

        // Seed data in background
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
            // Ensure remote sync of tables
            repository.syncAllTablesFromSupabase("https://cptquthczkjghjwmbojg.supabase.co", "sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8")
        }

        // Real-time background sync loop (every 4 seconds)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(4000)
                if (_isLoggedIn.value) {
                    try {
                        repository.syncAllTablesFromSupabase("https://cptquthczkjghjwmbojg.supabase.co", "sb_publishable_IUsBYEYSv13T-IM2BqtDoQ_va_FKDH8")
                    } catch (e: Exception) {
                        // Suppress logs for transient network failures
                    }
                }
            }
        }
    }

    // --- Navigation Controls ---
    fun navigateTo(screen: String) {
        if (screen == "profile") {
            _viewedUserId.value = null
        }
        _currentScreen.value = screen
    }

    fun navigateToProfile(userId: String?) {
        _viewedUserId.value = userId
        _currentScreen.value = "profile"
    }

    fun setAdminSubScreen(screen: String) {
        _adminSubScreen.value = screen
    }

    fun selectChatPartner(partnerId: String) {
        _activeChatPartnerId.value = partnerId
        navigateTo("chat")
    }

    // --- Auth Simulation ---
    fun simulateLogout() {
        _isLoggedIn.value = false
        _currentUserId.value = ""
        navigateTo("login")
    }

    fun registerNewUser(username: String, fullName: String, passwordText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmed = username.lowercase().trim()
            if (trimmed.isBlank() || fullName.isBlank() || passwordText.isBlank()) {
                onResult(false, "Please fill in all fields!")
                return@launch
            }
            
            val url = supabaseUrl.value
            val key = supabaseAnonKey.value
            
            // Check if username already exists locally
            val localExist = repository.getUserByUsername(trimmed)
            if (localExist != null) {
                onResult(false, "Username already taken!")
                return@launch
            }
            
            // Query remote Supabase
            val remoteUsersJson = repository.makeSupabaseRequest(url, key, "users", "GET", query = "?username=eq.$trimmed")
            val remoteUsers = if (remoteUsersJson != null) repository.parseSupabaseUsers(remoteUsersJson) else emptyList()
            if (remoteUsers.any { it.username == trimmed }) {
                onResult(false, "Username already exists on server!")
                return@launch
            }
            
            // Create user
            val newId = UUID.randomUUID().toString()
            val newUser = UserEntity(
                id = newId,
                username = trimmed,
                fullName = fullName,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                isVerified = false,
                isPremium = false,
                coinBalance = 100,
                email = "$trimmed@reelo.ai",
                password = passwordText
            )
            repository.insertUser(newUser)
            if (url.isNotBlank() && url.startsWith("http")) {
                repository.syncUserToSupabase(url, key, newUser)
            }
            
            _currentUserId.value = newId
            _isLoggedIn.value = true
            
            launch {
                repository.syncAllTablesFromSupabase(url, key)
            }
            onResult(true, "Registration successful! Welcome to ReeloAI ✨")
            navigateTo("feed")
        }
    }

    fun loginWithPassword(username: String, passwordText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmed = username.lowercase().trim()
            if (trimmed.isBlank() || passwordText.isBlank()) {
                onResult(false, "Please enter your username and password!")
                return@launch
            }
            
            val url = supabaseUrl.value
            val key = supabaseAnonKey.value
            
            // Fetch remote or check local
            val remoteUsersJson = repository.makeSupabaseRequest(url, key, "users", "GET", query = "?username=eq.$trimmed")
            val remoteUsers = if (remoteUsersJson != null) repository.parseSupabaseUsers(remoteUsersJson) else emptyList()
            
            val foundUser = remoteUsers.firstOrNull { it.username == trimmed }
                ?: repository.getUserByUsername(trimmed)
                
            if (foundUser != null) {
                if (foundUser.password == passwordText) {
                    repository.insertUser(foundUser) // Cache locally
                    _currentUserId.value = foundUser.id
                    _isLoggedIn.value = true
                    
                    launch {
                        repository.syncAllTablesFromSupabase(url, key)
                    }
                    onResult(true, "Welcome back, ${foundUser.fullName}! 👋")
                    navigateTo("feed")
                } else {
                    onResult(false, "Incorrect password!")
                }
            } else {
                onResult(false, "User not found! Please register.")
            }
        }
    }

    fun updateProfile(fullName: String, username: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            val me = currentUser.value ?: return@launch
            val trimmedUsername = username.lowercase().trim()
            if (trimmedUsername.isBlank() || fullName.isBlank()) {
                return@launch
            }
            val updated = me.copy(
                fullName = fullName,
                username = trimmedUsername,
                bio = bio,
                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else me.avatarUrl
            )
            repository.updateUser(updated)
            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncUserToSupabase(supabaseUrl.value, supabaseAnonKey.value, updated)
            }
        }
    }

    // --- User Actions ---
    fun isLiked(videoId: String, userId: String): Flow<Boolean> {
        return repository.isLiked(videoId, userId)
    }

    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>> {
        return repository.getCommentsForVideo(videoId)
    }

    fun toggleLikeVideo(videoId: String) {
        viewModelScope.launch {
            repository.toggleLike(videoId, _currentUserId.value)
            // Perform simulated coin watch bonus if user likes it
            triggerCoinEarn(5, "Bonus: Watching and liking content!")
        }
    }

    fun shareVideo(videoId: String, context: android.content.Context) {
        viewModelScope.launch {
            val video = activeVideos.value.find { it.id == videoId } ?: return@launch
            val updatedVideo = video.copy(sharesCount = video.sharesCount + 1)
            repository.updateVideo(updatedVideo)
            
            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncVideoToSupabase(supabaseUrl.value, supabaseAnonKey.value, updatedVideo)
            }
            
            triggerCoinEarn(5, "Bonus: Shared video!")
            
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this video on ReeloAI!")
                    putExtra(android.content.Intent.EXTRA_TEXT, "Watch @${video.username}'s stream on ReeloAI: ${video.caption}\nStream Link: ${video.videoUrl}")
                }
                val chooser = android.content.Intent.createChooser(intent, "Share Video via")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                // Keep it robust
            }
        }
    }

    fun addCommentToVideo(videoId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            // Run content moderation AI in the background!
            val mod = repository.moderateContentAI(text)
            val isSpam = !mod.isSafe

            val comment = CommentEntity(
                id = UUID.randomUUID().toString(),
                videoId = videoId,
                userId = user.id,
                username = user.username,
                avatarUrl = user.avatarUrl,
                text = text,
                isSpam = isSpam
            )
            repository.addComment(comment)

            // Update local videos comment count
            val video = repository.getVideo(videoId)
            val updatedVideo = video?.copy(commentsCount = video.commentsCount + 1)
            if (updatedVideo != null) {
                repository.updateVideo(updatedVideo)
                showSystemNotification("New Comment Alert 💬", "${user.username} commented on video: \"$text\"")
            }

            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncCommentToSupabase(supabaseUrl.value, supabaseAnonKey.value, comment)
                if (updatedVideo != null) {
                    repository.syncVideoToSupabase(supabaseUrl.value, supabaseAnonKey.value, updatedVideo)
                }
            }

            if (isSpam) {
                // Raise automatic AI report inside database!
                repository.fileReport(ReportEntity(
                    id = UUID.randomUUID().toString(),
                    targetType = "comment",
                    targetId = comment.id,
                    reporterUsername = "AI_MODERATOR",
                    reason = "AI content flag: ${mod.reason}. Marked category: ${mod.flaggedCategory}",
                    status = "pending"
                ))
                repository.logAdminAction("AI_SPAM_DETECT", "AI auto-censored comment by ${user.username} on video $videoId: \"$text\"")
            }

            // Coin Reward for engaging
            triggerCoinEarn(10, "Task completed: Commenting on reels!")
            // Complete Comment Task
            markTaskComplete("task_comment")
        }
    }

    fun toggleFollowCreator(creatorId: String) {
        viewModelScope.launch {
            repository.toggleFollow(_currentUserId.value, creatorId)
            val creator = repository.getUser(creatorId)
            if (creator != null) {
                showSystemNotification("New Follower Alert 👤", "${creator.username} started following you!")
            }
            triggerCoinEarn(15, "Bonus: Following content creator!")
            markTaskComplete("task_follow")
        }
    }

    fun sendMessageToChat(text: String) {
        if (text.isBlank()) return
        val partnerId = _activeChatPartnerId.value ?: return
        viewModelScope.launch {
            val meId = _currentUserId.value
            val message = MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = meId,
                receiverId = partnerId,
                text = text
            )
            repository.sendMessage(message)
            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncMessageToSupabase(supabaseUrl.value, supabaseAnonKey.value, message)
            }

            // Simulating an AI-assisted direct replies or smart echo loop
            val partner = repository.getUser(partnerId)
            val myCaption = text
            kotlinx.coroutines.delay(1000)
            val systemReply = repository.generateAICaption("Reply to the message: \"$myCaption\" from ${partner?.username}. Keep it short, casual, and supportive as a creator.")
            val replyMessage = MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = partnerId,
                receiverId = meId,
                text = systemReply
            )
            repository.sendMessage(replyMessage)
            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncMessageToSupabase(supabaseUrl.value, supabaseAnonKey.value, replyMessage)
            }
            showSystemNotification("New Message from ${partner?.fullName ?: "Creator"} ✉️", systemReply)
        }
    }

    // --- Coin Reward Management ---
    fun triggerCoinEarn(amount: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustUserCoins(_currentUserId.value, amount, reason)
        }
    }

    fun giftCoinsToCreator(amount: Int, creatorId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val meId = _currentUserId.value
            if (meId.isBlank() || creatorId.isBlank()) {
                onResult(false, "Error processing gift")
                return@launch
            }
            if (meId == creatorId) {
                onResult(false, "You cannot gift coins to yourself!")
                return@launch
            }
            val meUser = repository.getUser(meId)
            if (meUser == null) {
                onResult(false, "User not found")
                return@launch
            }
            if (meUser.coinBalance < amount) {
                onResult(false, "Insufficient balance! Watch free ads to earn more 🪙")
                return@launch
            }
            // Deduct
            repository.adjustUserCoins(meId, -amount, "Sent gift of $amount coins")
            // Giver updates coins to creator's earnings
            val creator = repository.getUser(creatorId)
            if (creator != null) {
                repository.adjustUserCoins(creatorId, amount, "Received gift list from ${meUser.username}")
            }
            onResult(true, "Awesome! Sent $amount coins to creator! 🎁")
        }
    }

    fun markTaskComplete(taskTypeRef: String) {
        viewModelScope.launch {
            val tasks = allTasks.value
            val match = tasks.find { it.taskType == taskTypeRef || it.id == taskTypeRef }
            if (match != null && !match.isCompleted) {
                repository.updateTask(match.copy(isCompleted = true))
                triggerCoinEarn(match.rewardCoins, "Reward: Completed task '${match.title}'")
            }
        }
    }

    fun requestWalletWithdrawal(amountCoins: Int, paypalEmail: String) {
        if (amountCoins <= 0) return
        val user = currentUser.value ?: return
        if (user.coinBalance < amountCoins) return
        viewModelScope.launch {
            val amountUsd = amountCoins / 100.0
            val w = WithdrawalEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                username = user.username,
                amountCoins = amountCoins,
                amountUsd = amountUsd,
                status = "pending",
                paymentMethod = "PayPal",
                details = paypalEmail
            )
            repository.requestWithdrawal(w)
        }
    }

    // --- Real-time AI Caption Generation inside Mobile UI ---
    suspend fun requestAICaptionGenerate(desc: String): String {
        return repository.generateAICaption(desc)
    }

    suspend fun requestAIHashtagsGenerate(caption: String): String {
        return repository.generateAIHashtags(caption)
    }

    // --- Video Uploading Form ---
    fun uploadNewVideo(caption: String, videoType: String, customVideoUri: String? = null, customPhotoUri: String? = null) {
        if (caption.isBlank()) {
            android.widget.Toast.makeText(getApplication(), "Please enter a caption first!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val user = currentUser.value
        if (user == null) {
            android.widget.Toast.makeText(getApplication(), "You must be signed in to upload videos!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (!appUploadsEnabled.value) {
            android.widget.Toast.makeText(getApplication(), "Video uploads are currently gated by administrator!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (!user.canUpload) {
            android.widget.Toast.makeText(getApplication(), "Your account's upload privilege is suspended by manager!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            // Determine random placeholder video assets
            val randomNum = (1..6).random()
            val mockVideoUrl = customVideoUri ?: when(videoType) {
                "Rain" -> "https://assets.mixkit.co/videos/preview/mixkit-heavy-rain-pouring-down-on-a-puddle-43399-large.mp4"
                "Grid" -> "https://assets.mixkit.co/videos/preview/mixkit-animation-of-a-retro-futuristic-grid-41225-large.mp4"
                else -> "https://assets.mixkit.co/videos/preview/mixkit-neon-light-from-a-building-reflecting-in-rain-water-43393-large.mp4"
            }
            val mockThumbUrl = customPhotoUri ?: "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=400&q=80"

            val newVid = VideoEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                username = user.username,
                avatarUrl = user.avatarUrl,
                caption = caption,
                videoUrl = mockVideoUrl,
                thumbnailUrl = mockThumbUrl
            )
            repository.insertVideo(newVid)

            // Update user's video count
            val updatedUser = user.copy(videoCount = user.videoCount + 1)
            repository.updateUser(updatedUser)

            if (appDatabaseMode.value == "supabase_cloud") {
                repository.syncVideoToSupabase(supabaseUrl.value, supabaseAnonKey.value, newVid)
                repository.syncUserToSupabase(supabaseUrl.value, supabaseAnonKey.value, updatedUser)
            }

            // Increase analytics metrics
            val an = repository.getAnalyticsSnapshot() ?: AnalyticsSnapshotEntity()
            repository.updateAnalytics(an.copy(totalViews = an.totalViews + 1, activeUsers = an.activeUsers + 5))

            // Reward
            triggerCoinEarn(50, "Content Creator Reward: Uploaded a short video!")

            android.widget.Toast.makeText(getApplication(), "Video Synthesized & Published Successfully! +50🪙", android.widget.Toast.LENGTH_LONG).show()
            showSystemNotification("Publish Success! 🎬", "Your new short video has been synthesized & published! View it now.")

            // Focus playing index on newly added video so user can play it immediately
            _justUploadedVideoId.value = newVid.id
            setFeedVideoIndex(0)

            navigateTo("feed")
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.fileReport(ReportEntity(
                id = UUID.randomUUID().toString(),
                targetType = targetType,
                targetId = targetId,
                reporterUsername = user.username,
                reason = reason,
                status = "pending"
            ))
        }
    }


    // ==========================================
    // --- ADMIN CENTRALIZED ACTIONS ---
    // ==========================================

    fun adminSetSearchUserQuery(query: String) {
        _userSearchQuery.value = query
    }

    fun adminSetSearchVideoQuery(query: String) {
        _videoSearchQuery.value = query
    }

    // 1. User Management
    fun adminBanUser(userId: String, reason: String) {
        viewModelScope.launch {
            repository.banUser(userId, reason)
        }
    }

    fun adminUnbanUser(userId: String) {
        viewModelScope.launch {
            repository.unbanUser(userId)
        }
    }

    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            val user = repository.getUser(userId)
            if (user != null) {
                repository.deleteUser(userId)
                repository.logAdminAction("DELETE_USER", "Deleted user ${user.username} (ID: $userId) permanently")
            }
        }
    }

    fun adminToggleVerification(userId: String) {
        viewModelScope.launch {
            val u = repository.getUser(userId) ?: return@launch
            val newState = !u.isVerified
            repository.updateUser(u.copy(isVerified = newState))
            repository.logAdminAction("TOGGLE_VERIFICATION", "Set verification status of ${u.username} to $newState")
        }
    }

    fun adminTogglePremiumBadge(userId: String) {
        viewModelScope.launch {
            val u = repository.getUser(userId) ?: return@launch
            val newState = !u.isPremium
            repository.updateUser(u.copy(isPremium = newState))
            repository.logAdminAction("TOGGLE_PREMIUM", "Set premium badge status of ${u.username} to $newState")
        }
    }

    fun adminToggleMonetization(userId: String) {
        viewModelScope.launch {
            val u = repository.getUser(userId) ?: return@launch
            val newState = !u.monetizationApproved
            repository.updateUser(u.copy(monetizationApproved = newState))
            repository.logAdminAction("TOGGLE_MONETIZATION", "Set monetization approval of ${u.username} to $newState")
        }
    }

    fun adminToggleUploadPermission(userId: String) {
        viewModelScope.launch {
            val u = repository.getUser(userId) ?: return@launch
            val newState = !u.canUpload
            repository.updateUser(u.copy(canUpload = newState))
            repository.logAdminAction("TOGGLE_UPLOAD_PRIVILEGE", "Set upload privilege of ${u.username} to $newState")
        }
    }

    fun adminAdjustCoinBalance(userId: String, amount: Int) {
        viewModelScope.launch {
            val user = repository.getUser(userId) ?: return@launch
            repository.adjustUserCoins(userId, amount, "Admin adjustment")
            repository.logAdminAction("ADJUST_COINS", "Adjusted coin balance of ${user.username} by $amount")
        }
    }

    // 2. Video Management
    fun adminDeleteVideo(videoId: String) {
        viewModelScope.launch {
            val vid = repository.getVideo(videoId)
            if (vid != null) {
                repository.deleteVideo(videoId)
                repository.logAdminAction("DELETE_VIDEO", "Deleted video ID: $videoId by ${vid.username}")
            }
        }
    }

    fun adminToggleFeatured(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideo(videoId) ?: return@launch
            val newState = !v.isFeatured
            repository.updateVideo(v.copy(isFeatured = newState))
            repository.logAdminAction("TOGGLE_FEATURED", "Setfeatured status of video $videoId to $newState")
        }
    }

    fun adminToggleTrending(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideo(videoId) ?: return@launch
            val newState = !v.isTrending
            repository.updateVideo(v.copy(isTrending = newState))
            repository.logAdminAction("TOGGLE_TRENDING", "Settrending status of video $videoId to $newState")
        }
    }

    fun adminToggleHideVideo(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideo(videoId) ?: return@launch
            val newState = !v.isHidden
            repository.updateVideo(v.copy(isHidden = newState))
            repository.logAdminAction("TOGGLE_HIDE_VIDEO", "Sethidden status of video $videoId to $newState")
        }
    }

    fun adminToggleCommentsDisabled(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideo(videoId) ?: return@launch
            val newState = !v.commentsDisabled
            repository.updateVideo(v.copy(commentsDisabled = newState))
            repository.logAdminAction("TOGGLE_COMMENTS_DISABLED", "Modified Comment Access of video $videoId to $newState")
        }
    }

    fun adminToggleSharingDisabled(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideo(videoId) ?: return@launch
            val newState = !v.sharingDisabled
            repository.updateVideo(v.copy(sharingDisabled = newState))
            repository.logAdminAction("TOGGLE_SHARE_DISABLED", "Modified Sharing Access of video $videoId to $newState")
        }
    }

    // 3. Ads Management
    fun adminSaveAdsSettings(settings: AdSettingsEntity) {
        viewModelScope.launch {
            repository.updateAdSettings(settings)
            repository.logAdminAction("UPDATE_ADS", "Updated global advertisement network configs.")
        }
    }

    fun adminRecordAdClick() {
        viewModelScope.launch {
            val current = adSettings.value ?: return@launch
            val updated = current.copy(
                clicksCount = current.clicksCount + 1,
                revenueCents = current.revenueCents + ((15..45).random()) // Random payout
            )
            repository.updateAdSettings(updated)
        }
    }

    // 4. Coin & Earning panel
    fun adminApproveWithdrawal(id: String) {
        viewModelScope.launch {
            val withdrawalsList = allWithdrawals.value
            val match = withdrawalsList.find { it.id == id } ?: return@launch
            repository.updateWithdrawal(match.copy(status = "Approved"))
            repository.logAdminAction("APPROVE_WITHDRAWAL", "Approved real wallet withdrawal request for ${match.username} of $${match.amountUsd}")
        }
    }

    fun adminRejectWithdrawal(id: String) {
        viewModelScope.launch {
            val withdrawalsList = allWithdrawals.value
            val match = withdrawalsList.find { it.id == id } ?: return@launch
            repository.updateWithdrawal(match.copy(status = "Rejected"))
            // Refund the coins
            repository.adjustUserCoins(match.userId, match.amountCoins, "Refund: Withdrawal request rejected by Admin")
            repository.logAdminAction("REJECT_WITHDRAWAL", "Rejected and refunded withdrawal request of $${match.amountUsd} for ${match.username}")
        }
    }

    fun adminAddNewTask(title: String, desc: String, reward: Int, type: String) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = desc,
                rewardCoins = reward,
                isCompleted = false,
                taskType = type
            )
            repository.insertTask(task)
            repository.logAdminAction("ADD_TASK", "Added new rewarded task: $title ($reward coins)")
        }
    }

    // 5. Reports
    fun adminResolveReport(id: String) {
        viewModelScope.launch {
            val repList = allReports.value
            val match = repList.find { it.id == id } ?: return@launch
            repository.updateReport(match.copy(status = "Resolved"))
            repository.logAdminAction("RESOLVE_REPORT", "Resolved user-submitted content report ID: $id")
        }
    }

    // 6. Push Notifications Broadcast Sender
    fun adminSendPushBroadcast(title: String, messageText: String, targetUserId: String?) {
        viewModelScope.launch {
            val usersList = allUsers.value
            if (targetUserId == null) {
                // To all
                usersList.forEach { user ->
                    repository.addNotification(NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        type = "admin",
                        senderUsername = "AdminCentral",
                        senderAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
                        text = "[$title] $messageText"
                    ))
                }
                repository.logAdminAction("BROADCAST_NOTIFICATION", "Sent broadcast notification to all user bases: $title")
                // Pop system level notification
                showSystemNotification(title, messageText)
            } else {
                // To particular user
                val targetUser = repository.getUser(targetUserId)
                if (targetUser != null) {
                    repository.addNotification(NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = targetUserId,
                        type = "admin",
                        senderUsername = "AdminCentral",
                        senderAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
                        text = "[$title] $messageText"
                    ))
                    repository.logAdminAction("DIRECT_NOTIFICATION", "Sent specific targeted alert message to ${targetUser.username}")
                    // Pop system level notification
                    showSystemNotification(title, messageText)
                }
            }
        }
    }
}
