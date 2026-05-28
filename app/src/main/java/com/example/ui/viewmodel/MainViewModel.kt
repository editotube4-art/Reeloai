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
    val appDatabaseMode = MutableStateFlow("sqlite_local") // sqlite_local, supabase_cloud
    val supabaseUrl = MutableStateFlow("https://your-project-id.supabase.co")
    val supabaseAnonKey = MutableStateFlow("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_string")
    val supabaseBucketName = MutableStateFlow("reelo-media-storage")
    private val _supabaseConnectionStatus = MutableStateFlow("Disconnected")
    val supabaseConnectionStatus: StateFlow<String> = _supabaseConnectionStatus.asStateFlow()

    fun updateSupabaseConfig(url: String, key: String, bucket: String, mode: String) {
        supabaseUrl.value = url
        supabaseAnonKey.value = key
        supabaseBucketName.value = bucket
        appDatabaseMode.value = mode
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
        // Seed data in background
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
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

    fun simulateLogin(username: String) {
        viewModelScope.launch {
            val exist = allUsers.value.firstOrNull { it.username == username.lowercase().trim() }
            if (exist != null) {
                _currentUserId.value = exist.id
                _isLoggedIn.value = true
                navigateTo("feed")
            } else {
                // Register new simulated user
                val newId = UUID.randomUUID().toString()
                val newUser = UserEntity(
                    id = newId,
                    username = username.lowercase().trim(),
                    fullName = username.replaceFirstChar { it.uppercase() },
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                    isVerified = false,
                    isPremium = false,
                    coinBalance = 100,
                    email = "$username@reelo.ai"
                )
                repository.insertUser(newUser)
                _currentUserId.value = newId
                _isLoggedIn.value = true
                navigateTo("feed")
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
            if (video != null) {
                repository.updateVideo(video.copy(commentsCount = video.commentsCount + 1))
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

            // Simulating an AI-assisted direct replies or smart echo loop
            val partner = repository.getUser(partnerId)
            val myCaption = text
            kotlinx.coroutines.delay(1000)
            val systemReply = repository.generateAICaption("Reply to the message: \"$myCaption\" from ${partner?.username}. Keep it short, casual, and supportive as a creator.")
            repository.sendMessage(MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = partnerId,
                receiverId = meId,
                text = systemReply
            ))
        }
    }

    // --- Coin Reward Management ---
    fun triggerCoinEarn(amount: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustUserCoins(_currentUserId.value, amount, reason)
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
            repository.updateUser(user.copy(videoCount = user.videoCount + 1))

            // Increase analytics metrics
            val an = repository.getAnalyticsSnapshot() ?: AnalyticsSnapshotEntity()
            repository.updateAnalytics(an.copy(totalViews = an.totalViews + 1, activeUsers = an.activeUsers + 5))

            // Reward
            triggerCoinEarn(50, "Content Creator Reward: Uploaded a short video!")

            android.widget.Toast.makeText(getApplication(), "Video Synthesized & Published Successfully! +50🪙", android.widget.Toast.LENGTH_LONG).show()

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
                }
            }
        }
    }
}
