package com.example.data.repository

import com.example.BuildConfig
import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

// --- Gemini REST Models ---
data class GeminiPart(val text: String? = null)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(val content: GeminiContent)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class AppRepository(private val appDao: AppDao) {

    // --- Gemini API Call implementations ---
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val geminiService = retrofit.create(GeminiApiService::class.java)

    suspend fun generateAICaption(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Enjoying this summer vibe! ✨ #summer #foryou"
        }
        try {
            val system = GeminiContent(parts = listOf(GeminiPart("You are a viral social media caption copywriter. Create a trendy, short, engaging caption for a video described by the prompt. Include 2-3 viral hashtags.")))
            val req = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
                systemInstruction = system
            )
            val resp = geminiService.generateContent(apiKey, req)
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Loving this moment! 🚀 #viral"
        } catch (e: Exception) {
            "Feeling good today! ✨ #goodvibes #reelo"
        }
    }

    suspend fun generateAIHashtags(caption: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "#foryou #trending #reels #viral #shortvideo"
        }
        try {
            val system = GeminiContent(parts = listOf(GeminiPart("Generate exactly 5 viral hashtags based on this video details. Output ONLY the hashtags separated by spaces.")))
            val req = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(caption)))),
                systemInstruction = system
            )
            val resp = geminiService.generateContent(apiKey, req)
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "#foryou #viral #trending"
        } catch (e: Exception) {
            "#trending #reels #ai"
        }
    }

    suspend fun moderateContentAI(text: String): AIModerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Default simulated moderation for demo purposes
            val containsBadWord = text.lowercase().contains("spam") || text.lowercase().contains("fake") || text.lowercase().contains("cheat")
            return@withContext AIModerationResult(
                isSafe = !containsBadWord,
                reason = if (containsBadWord) "Spam / suspicious activity detected" else "Content approved",
                flaggedCategory = if (containsBadWord) "SPAM" else "NONE"
            )
        }
        try {
            val prompt = "Moderator request: Analyze the following text and determine if it violates safety guidelines (spam, offensive, NSFW, hate speech, or harassment):\n\n\"$text\"\n\nRespond strictly in this format: SAFE or FLAGGED | <category: SPAM, OFFENSIVE, HARASSMENT, NONE> | <short reason>"
            val req = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
            )
            val resp = geminiService.generateContent(apiKey, req)
            val resultText = resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "SAFE | NONE | Approved"
            val parts = resultText.split("|")
            val isSafe = !resultText.trim().uppercase().startsWith("FLAGGED")
            val category = parts.getOrNull(1)?.trim() ?: "NONE"
            val reason = parts.getOrNull(2)?.trim() ?: "AI Screened"
            AIModerationResult(isSafe = isSafe, reason = reason, flaggedCategory = category)
        } catch (e: Exception) {
            AIModerationResult(isSafe = true, reason = "Standard fallback approval", flaggedCategory = "NONE")
        }
    }

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsersFlow()
    fun getUserFlow(userId: String): Flow<UserEntity?> = appDao.getUserByIdFlow(userId)
    suspend fun getUser(userId: String): UserEntity? = appDao.getUserById(userId)
    suspend fun insertUser(user: UserEntity) = appDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = appDao.updateUser(user)
    suspend fun deleteUser(userId: String) = appDao.deleteUserById(userId)

    // --- Videos ---
    val activeVideos: Flow<List<VideoEntity>> = appDao.getActiveVideosFlow()
    val allVideos: Flow<List<VideoEntity>> = appDao.getAllVideosFlow()
    val trendingVideos: Flow<List<VideoEntity>> = appDao.getTrendingVideosFlow()
    val featuredVideos: Flow<List<VideoEntity>> = appDao.getFeaturedVideosFlow()
    fun getVideosByUser(userId: String): Flow<List<VideoEntity>> = appDao.getVideosByUserIdFlow(userId)
    suspend fun getVideo(videoId: String): VideoEntity? = appDao.getVideoById(videoId)
    suspend fun insertVideo(video: VideoEntity) = appDao.insertVideo(video)
    suspend fun updateVideo(video: VideoEntity) = appDao.updateVideo(video)
    suspend fun deleteVideo(videoId: String) = appDao.deleteVideoById(videoId)

    // --- Comments ---
    val allComments: Flow<List<CommentEntity>> = appDao.getAllCommentsFlow()
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>> = appDao.getCommentsByVideoFlow(videoId)
    suspend fun addComment(comment: CommentEntity) = appDao.insertComment(comment)
    suspend fun deleteComment(commentId: String) = appDao.deleteCommentById(commentId)

    // --- Likes ---
    fun isLiked(videoId: String, userId: String): Flow<Boolean> = appDao.isVideoLikedFlow(videoId, userId)
    suspend fun toggleLike(videoId: String, userId: String) {
        val exists = isLiked(videoId, userId)
        val list = appDao.getLikedVideosByUser(userId)
        val isAlreadyLiked = list.any { it.videoId == videoId }
        val video = appDao.getVideoById(videoId)
        if (isAlreadyLiked) {
            appDao.deleteLike(LikeEntity(videoId, userId))
            if (video != null) {
                appDao.updateVideo(video.copy(likesCount = maxOf(0, video.likesCount - 1)))
            }
        } else {
            appDao.insertLike(LikeEntity(videoId, userId))
            if (video != null) {
                appDao.updateVideo(video.copy(likesCount = video.likesCount + 1))
            }
        }
    }

    // --- Followers ---
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean> = appDao.isFollowingFlow(followerId, followingId)
    suspend fun toggleFollow(followerId: String, followingId: String) {
        // Find if already following
        // For simplicity:
        val userToFollow = appDao.getUserById(followingId) ?: return
        val me = appDao.getUserById(followerId) ?: return

        // We can check and toggle:
        // (Due to flow context, we can attempt to insert and catch or we can do a normal read/write)
        // Since we have composite PK in SQLite, we write toggle logic:
        try {
            appDao.insertFollower(FollowerEntity(followerId, followingId))
            // Increment counts
            appDao.updateUser(userToFollow.copy(followerCount = userToFollow.followerCount + 1))
            appDao.updateUser(me.copy(followingCount = me.followingCount + 1))
        } catch (e: Exception) {
            appDao.deleteFollower(FollowerEntity(followerId, followingId))
            appDao.updateUser(userToFollow.copy(followerCount = maxOf(0, userToFollow.followerCount - 1)))
            appDao.updateUser(me.copy(followingCount = maxOf(0, me.followingCount - 1)))
        }
    }

    // --- Chats & Messages ---
    fun getChatMessages(id1: String, id2: String): Flow<List<MessageEntity>> = appDao.getChatMessagesFlow(id1, id2)
    fun getChatPartners(userId: String): Flow<List<String>> = appDao.getChatPartnersFlow(userId)
    suspend fun sendMessage(message: MessageEntity) = appDao.insertMessage(message)

    // --- Notifications ---
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = appDao.getNotificationsForUserFlow(userId)
    suspend fun addNotification(notif: NotificationEntity) = appDao.insertNotification(notif)
    suspend fun markNotificationsRead(userId: String) = appDao.markAllAsRead(userId)

    // --- Earn/Coins & Transactions ---
    val allTransactions: Flow<List<CoinTransactionEntity>> = appDao.getAllTransactionsFlow()
    fun getTransactionsForUser(userId: String): Flow<List<CoinTransactionEntity>> = appDao.getTransactionsForUserFlow(userId)
    
    suspend fun adjustUserCoins(userId: String, amount: Int, reason: String) {
        val user = appDao.getUserById(userId) ?: return
        val newBalance = maxOf(0, user.coinBalance + amount)
        appDao.updateUser(user.copy(coinBalance = newBalance))
        appDao.insertTransaction(CoinTransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            amount = amount,
            description = reason
        ))
    }

    // --- Tasks ---
    val allTasks: Flow<List<TaskEntity>> = appDao.getAllTasksFlow()
    suspend fun insertTask(task: TaskEntity) = appDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = appDao.updateTask(task)
    suspend fun completeTask(userId: String, taskId: String) {
        val tasks = appDao.getAllTasksFlow() // Just get the task
        // We'll update completed task and reward user
        // Find task from an analytical or database level
    }

    // --- Withdrawals ---
    val allWithdrawals: Flow<List<WithdrawalEntity>> = appDao.getAllWithdrawalsFlow()
    fun getWithdrawalsByUser(userId: String): Flow<List<WithdrawalEntity>> = appDao.getWithdrawalsForUserFlow(userId)
    suspend fun requestWithdrawal(withdrawal: WithdrawalEntity) {
        appDao.insertWithdrawal(withdrawal)
        // Deduct user coins automatically
        adjustUserCoins(withdrawal.userId, -withdrawal.amountCoins, "Withdrawal Request: ${withdrawal.paymentMethod}")
    }
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) {
        appDao.updateWithdrawal(withdrawal)
    }

    // --- Ads Settings ---
    val adSettings: Flow<AdSettingsEntity?> = appDao.getAdSettingsFlow()
    suspend fun getAdSettingsSnapshot(): AdSettingsEntity? = appDao.getAdSettings()
    suspend fun updateAdSettings(settings: AdSettingsEntity) = appDao.insertAdSettings(settings)

    // --- Reports ---
    val allReports: Flow<List<ReportEntity>> = appDao.getAllReportsFlow()
    suspend fun fileReport(report: ReportEntity) = appDao.insertReport(report)
    suspend fun updateReport(report: ReportEntity) = appDao.updateReport(report)

    // --- Live Analytics ---
    val analyticsFlow: Flow<AnalyticsSnapshotEntity?> = appDao.getAnalyticsFlow()
    suspend fun getAnalyticsSnapshot(): AnalyticsSnapshotEntity? = appDao.getAnalytics()
    suspend fun updateAnalytics(analytics: AnalyticsSnapshotEntity) = appDao.insertAnalytics(analytics)

    // --- Admin Logs ---
    val adminLogs: Flow<List<AdminLogEntity>> = appDao.getAllAdminLogsFlow()
    suspend fun logAdminAction(action: String, details: String) {
        appDao.insertAdminLog(AdminLogEntity(
            id = UUID.randomUUID().toString(),
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        ))
    }

    // --- Banned Users ---
    val bannedUsers: Flow<List<BannedUserEntity>> = appDao.getAllBannedUsersFlow()
    suspend fun banUser(userId: String, reason: String) {
        val user = appDao.getUserById(userId) ?: return
        appDao.updateUser(user.copy(isBanned = true))
        appDao.insertBannedUser(BannedUserEntity(
            id = userId,
            username = user.username,
            email = user.email,
            reason = reason,
            bannedAt = System.currentTimeMillis()
        ))
        logAdminAction("BAN_USER", "Banned ${user.username} (ID: $userId). Reason: $reason")
    }

    suspend fun unbanUser(userId: String) {
        val user = appDao.getUserById(userId)
        if (user != null) {
            appDao.updateUser(user.copy(isBanned = false))
        }
        appDao.removeBannedUser(userId)
        logAdminAction("UNBAN_USER", "Unbanned user ID: $userId")
    }

    // --- DB Prepulated Data ---
    suspend fun populateInitialDataIfEmpty() {
        // Check if users exist
        val currentUsers = withContext(Dispatchers.IO) { appDao.getAllUsersFlow() }
        // To query safely, let's look up if there is at least one setup user
        val ads = appDao.getAdSettings()
        if (ads == null) {
            // Seed base configuration
            appDao.insertAdSettings(AdSettingsEntity())
            appDao.insertAnalytics(AnalyticsSnapshotEntity())

            // Seed Users
            val me = UserEntity(
                id = "user_me",
                username = "curator",
                fullName = "Alex Mercer",
                email = "curator@reelo.ai",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                bio = "AI Creator & Explorer. Content supervisor on @ReeloAI.",
                isVerified = true,
                isPremium = true,
                coinBalance = 500
            )
            val creator1 = UserEntity(
                id = "creator_julia",
                username = "julia.waves",
                fullName = "Julia Chen",
                email = "julia@waves.ai",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
                bio = "Catching sunset loops and cyber aesthetics 💫 ✨",
                isVerified = true,
                followerCount = 42900,
                followingCount = 120,
                videoCount = 14,
                monetizationApproved = true,
                creatorIncome = 420
            )
            val creator2 = UserEntity(
                id = "creator_zephyr",
                username = "zephyr.tech",
                fullName = "Zephyr AI Project",
                email = "zephyr@project.ai",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80",
                bio = "Daily generative neon futuristic dreams. Synthesizer loops.",
                isVerified = false,
                isPremium = true,
                followerCount = 12400,
                followingCount = 80,
                videoCount = 8
            )
            val userNormal = UserEntity(
                id = "user_normal",
                username = "shadow_rebel",
                fullName = "John Rebel",
                email = "john@rebel.ai",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                bio = "Lover of late night electronic beats.",
                followerCount = 125,
                followingCount = 400
            )

            appDao.insertUser(me)
            appDao.insertUser(creator1)
            appDao.insertUser(creator2)
            appDao.insertUser(userNormal)

            // Seed Videos
            val v1 = VideoEntity(
                id = "vid_1",
                userId = "creator_julia",
                username = "julia.waves",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
                caption = "Chasing the neon cyber dawn in Tokyo. 🌌 Generated via Veo. #cyberpunk #tokyo #neon #future",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-neon-light-from-a-building-reflecting-in-rain-water-43393-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?auto=format&fit=crop&w=400&q=80",
                likesCount = 8122,
                commentsCount = 241,
                sharesCount = 1400,
                viewsCount = 28400,
                isFeatured = true,
                isTrending = true
            )
            val v2 = VideoEntity(
                id = "vid_2",
                userId = "creator_zephyr",
                username = "zephyr.tech",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80",
                caption = "Infinite procedural loop. Synced synthesizer music 🎧 Tell me in the comments if you want the track. #synthwave #loop #music #procedural",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-animation-of-a-retro-futuristic-grid-41225-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=400&q=80",
                likesCount = 4122,
                commentsCount = 98,
                sharesCount = 422,
                viewsCount = 12050,
                isTrending = true
            )
            val v3 = VideoEntity(
                id = "vid_3",
                userId = "user_normal",
                username = "shadow_rebel",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                caption = "Watching storm clouds gather. Nature is hypnotic. 🌧️⚡ #storm #nature #slowmo",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-heavy-rain-pouring-down-on-a-puddle-43399-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?auto=format&fit=crop&w=400&q=80",
                likesCount = 210,
                commentsCount = 12,
                sharesCount = 8,
                viewsCount = 1040
            )

            appDao.insertVideo(v1)
            appDao.insertVideo(v2)
            appDao.insertVideo(v3)

            // Seed Comments
            appDao.insertComment(CommentEntity("c1", "vid_1", "user_me", "curator", me.avatarUrl, "This aesthetic is absolutely gorgeous! Great render quality."))
            appDao.insertComment(CommentEntity("c2", "vid_1", "user_normal", "shadow_rebel", userNormal.avatarUrl, "Where can I get this loop? Need it as my desktop background now."))
            appDao.insertComment(CommentEntity("c3", "vid_2", "creator_julia", "julia.waves", creator1.avatarUrl, "Yes! Definitely release this synthesizer track, it slaps! 🔥"))

            // Seed Tasks
            appDao.insertTask(TaskEntity("task_daily", "Daily Check-in", "Open ReeloAI daily and collect your reward", 10, false, "daily_checkin"))
            appDao.insertTask(TaskEntity("task_watch", "Watch & Win", "Watch 3 reels videos today to support creators", 25, false, "watch_video"))
            appDao.insertTask(TaskEntity("task_comment", "Vocal Critic", "Post a supportive comment on any video", 15, false, "comment"))
            appDao.insertTask(TaskEntity("task_refer", "Refer a Friend", "Share your referral code with a friend", 100, false, "refer"))

            // Seed Admin Logs
            appDao.insertAdminLog(AdminLogEntity("log1", "SYSTEM_START", "ReeloAI system started. Initial seed data injected.", System.currentTimeMillis() - 86400000))
            appDao.insertAdminLog(AdminLogEntity("log2", "SETTINGS_UPDATE", "Enabled global AdMob banner ads on Feed page.", System.currentTimeMillis() - 40000000))

            // Seed Reports
            appDao.insertReport(ReportEntity("rep1", "video", "vid_3", "julia.waves", "Violates content tags (tagged storm but video is high energy rain puddle spam)", "pending"))
        }
    }
}

data class AIModerationResult(
    val isSafe: Boolean,
    val reason: String,
    val flaggedCategory: String
)
