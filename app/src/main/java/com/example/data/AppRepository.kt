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

    companion object {
        const val SUPABASE_URL = "https://cptquthczkjghjwmbojg.supabase.co"
        const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNwdHF1dGhjemtqZ2hqd21ib2pnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk5NDMwMzEsImV4cCI6MjA5NTUxOTAzMX0.hSS32HzvDpYySun3izGwmbvAtDeBxGXqVRPmDUo7oRU"
    }

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

    // --- Dynamic Supabase REST Connection helper & JSON robust Parser ---
    suspend fun makeSupabaseRequest(
        url: String,
        anonKey: String,
        table: String,
        method: String,
        jsonBody: String? = null,
        query: String = ""
    ): String? = withContext(Dispatchers.IO) {
        if (url.isBlank() || url.contains("your-project-id") || anonKey.length < 20) {
            return@withContext null
        }
        try {
            val endpoint = if (url.endsWith("/")) "${url}rest/v1/$table$query" else "$url/rest/v1/$table$query"
            val requestBuilder = okhttp3.Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                if (method.uppercase() == "POST" || method.uppercase() == "PATCH") {
                    requestBuilder.addHeader("Prefer", "resolution=merge-duplicates")
                }
                
            val req = when (method.uppercase()) {
                "GET" -> requestBuilder.get()
                "POST" -> {
                    val body = jsonBody ?: "{}"
                    requestBuilder.post(okhttp3.RequestBody.create("application/json".toMediaType(), body))
                }
                "PATCH" -> {
                    val body = jsonBody ?: "{}"
                    requestBuilder.patch(okhttp3.RequestBody.create("application/json".toMediaType(), body))
                }
                "DELETE" -> requestBuilder.delete()
                else -> requestBuilder.get()
            }.build()

            val response = okHttpClient.newCall(req).execute()
            val bodyStr = response.body?.string()
            if (response.isSuccessful) {
                bodyStr
            } else {
                android.util.Log.e("Supabase", "Request failed: code=${response.code}, body=$bodyStr")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "Exception: ${e.message}", e)
            null
        }
    }

    private fun escapeJsonString(str: String): String {
        return "\"" + str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "\""
    }

    suspend fun syncUserToSupabase(url: String, anonKey: String, user: UserEntity) {
        val json = """{
            "id":"${user.id}",
            "username":"${user.username}",
            "display_name":${escapeJsonString(user.fullName)},
            "avatar_url":"${user.avatarUrl}",
            "bio":${escapeJsonString(user.bio)},
            "email":"${user.email}",
            "password":"${user.password}",
            "coin_balance":${user.coinBalance},
            "can_upload":${user.canUpload},
            "is_verified":${user.isVerified},
            "is_premium":${user.isPremium},
            "is_banned":${user.isBanned},
            "follower_count":${user.followerCount},
            "following_count":${user.followingCount},
            "video_count":${user.videoCount},
            "monetization_approved":${user.monetizationApproved},
            "creator_earning_percentage":${user.creatorEarningPercentage},
            "creator_income":${user.creatorIncome},
            "registered_at":${user.registeredAt}
        }""".trimIndent()
        makeSupabaseRequest(url, anonKey, "users", "POST", json)
    }

    suspend fun syncVideoToSupabase(url: String, anonKey: String, video: VideoEntity) {
        val json = """
            {
                "id": "${video.id}",
                "user_id": "${video.userId}",
                "username": "${video.username}",
                "avatar_url": "${video.avatarUrl}",
                "caption": ${escapeJsonString(video.caption)},
                "video_url": "${video.videoUrl}",
                "thumbnail_url": "${video.thumbnailUrl}",
                "likes_count": ${video.likesCount},
                "comments_count": ${video.commentsCount},
                "shares_count": ${video.sharesCount},
                "views_count": ${video.viewsCount},
                "is_featured": ${video.isFeatured},
                "comments_disabled": ${video.commentsDisabled},
                "timestamp": ${video.timestamp}
            }
        """.trimIndent()
        makeSupabaseRequest(url, anonKey, "videos", "POST", json)
    }

    suspend fun syncCommentToSupabase(url: String, anonKey: String, comment: CommentEntity) {
        val json = """
            {
                "id": "${comment.id}",
                "video_id": "${comment.videoId}",
                "user_id": "${comment.userId}",
                "username": "${comment.username}",
                "avatar_url": "${comment.avatarUrl}",
                "text": ${escapeJsonString(comment.text)},
                "timestamp": ${comment.timestamp}
            }
        """.trimIndent()
        makeSupabaseRequest(url, anonKey, "comments", "POST", json)
    }

    suspend fun syncMessageToSupabase(url: String, anonKey: String, msg: MessageEntity) {
        val json = """
            {
                "id": "${msg.id}",
                "sender_id": "${msg.senderId}",
                "receiver_id": "${msg.receiverId}",
                "content": ${escapeJsonString(msg.text)},
                "timestamp": ${msg.timestamp}
            }
        """.trimIndent()
        makeSupabaseRequest(url, anonKey, "messages", "POST", json)
    }

    fun parseSupabaseUsers(jsonStr: String): List<UserEntity> {
        val list = mutableListOf<UserEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "")
                val username = obj.optString("username", "")
                val displayName = obj.optString("display_name", username)
                val avatarUrl = obj.optString("avatar_url", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde")
                val coinBalance = obj.optInt("coin_balance", 100)
                val canUpload = obj.optBoolean("can_upload", true)
                val isVerified = obj.optBoolean("is_verified", false)
                val isPremium = obj.optBoolean("is_premium", false)
                val password = obj.optString("password", "")
                
                if (id.isNotBlank() && username.isNotBlank()) {
                    list.add(UserEntity(
                        id = id,
                        username = username,
                        fullName = displayName,
                        avatarUrl = avatarUrl,
                        coinBalance = coinBalance,
                        canUpload = canUpload,
                        isVerified = isVerified,
                        isPremium = isPremium,
                        password = password
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseUsers error: ${e.message}")
        }
        return list
    }

    fun parseSupabaseVideos(jsonStr: String): List<VideoEntity> {
        val list = mutableListOf<VideoEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "")
                val userId = obj.optString("user_id", "")
                val username = obj.optString("username", "creator")
                val avatarUrl = obj.optString("avatar_url", "https://images.unsplash.com/photo-1544005313-94ddf0286df2")
                val caption = obj.optString("caption", "")
                val videoUrl = obj.optString("video_url", "")
                val thumbnailUrl = obj.optString("thumbnail_url", "")
                val likes = obj.optInt("likes_count", 0)
                val comments = obj.optInt("comments_count", 0)
                val shares = obj.optInt("shares_count", 0)
                val views = obj.optInt("views_count", 0)
                val isFeatured = obj.optBoolean("is_featured", false)
                val commentsDisabled = obj.optBoolean("comments_disabled", false)
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                
                if (id.isNotBlank()) {
                    list.add(VideoEntity(
                        id = id,
                        userId = userId,
                        username = username,
                        avatarUrl = avatarUrl,
                        caption = caption,
                        videoUrl = videoUrl,
                        thumbnailUrl = thumbnailUrl,
                        likesCount = likes,
                        commentsCount = comments,
                        sharesCount = shares,
                        viewsCount = views,
                        isFeatured = isFeatured,
                        commentsDisabled = commentsDisabled,
                        timestamp = timestamp
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseVideos error: ${e.message}")
        }
        return list
    }

    fun parseSupabaseComments(jsonStr: String): List<CommentEntity> {
        val list = mutableListOf<CommentEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "")
                val videoId = obj.optString("video_id", "")
                val userId = obj.optString("user_id", "")
                val username = obj.optString("username", "commenter")
                val avatarUrl = obj.optString("avatar_url", "")
                val text = obj.optString("text", "")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                
                if (id.isNotBlank()) {
                    list.add(CommentEntity(
                        id = id,
                        videoId = videoId,
                        userId = userId,
                        username = username,
                        avatarUrl = avatarUrl,
                        text = text,
                        timestamp = timestamp
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseComments error: ${e.message}")
        }
        return list
    }

    fun parseSupabaseMessages(jsonStr: String): List<MessageEntity> {
        val list = mutableListOf<MessageEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "")
                val senderId = obj.optString("sender_id", "")
                val receiverId = obj.optString("receiver_id", "")
                val content = obj.optString("content", "")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                
                if (id.isNotBlank()) {
                    list.add(MessageEntity(
                        id = id,
                        senderId = senderId,
                        receiverId = receiverId,
                        text = content,
                        timestamp = timestamp
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseMessages error: ${e.message}")
        }
        return list
    }

    private fun extractJsonVal(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\""
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)
    }

    private fun extractJsonIntVal(json: String, key: String, default: Int): Int {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)"
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: default
    }

    private fun extractJsonBoolVal(json: String, key: String, default: Boolean): Boolean {
        val pattern = "\"$key\"\\s*:\\s*(true|false)"
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)?.toBoolean() ?: default
    }

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
    suspend fun getUserByUsername(username: String): UserEntity? = appDao.getUserByUsername(username)
    suspend fun insertUser(user: UserEntity) {
        appDao.insertUser(user)
        syncUserToSupabase(SUPABASE_URL, SUPABASE_KEY, user)
    }
    suspend fun updateUser(user: UserEntity) {
        appDao.updateUser(user)
        syncUserToSupabase(SUPABASE_URL, SUPABASE_KEY, user)
    }
    suspend fun deleteUser(userId: String) {
        appDao.deleteUserById(userId)
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "users", "DELETE", query = "?id=eq.$userId")
    }

    // --- Videos ---
    val activeVideos: Flow<List<VideoEntity>> = appDao.getActiveVideosFlow()
    val allVideos: Flow<List<VideoEntity>> = appDao.getAllVideosFlow()
    val trendingVideos: Flow<List<VideoEntity>> = appDao.getTrendingVideosFlow()
    val featuredVideos: Flow<List<VideoEntity>> = appDao.getFeaturedVideosFlow()
    fun getVideosByUser(userId: String): Flow<List<VideoEntity>> = appDao.getVideosByUserIdFlow(userId)
    suspend fun getVideo(videoId: String): VideoEntity? = appDao.getVideoById(videoId)
    suspend fun insertVideo(video: VideoEntity) {
        appDao.insertVideo(video)
        syncVideoToSupabase(SUPABASE_URL, SUPABASE_KEY, video)
    }
    suspend fun updateVideo(video: VideoEntity) {
        appDao.updateVideo(video)
        syncVideoToSupabase(SUPABASE_URL, SUPABASE_KEY, video)
    }
    suspend fun deleteVideo(videoId: String) {
        appDao.deleteVideoById(videoId)
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "videos", "DELETE", query = "?id=eq.$videoId")
        // Also delete associated comments and likes from Supabase
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "comments", "DELETE", query = "?video_id=eq.$videoId")
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "likes", "DELETE", query = "?video_id=eq.$videoId")
    }

    // --- Comments ---
    val allComments: Flow<List<CommentEntity>> = appDao.getAllCommentsFlow()
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>> = appDao.getCommentsByVideoFlow(videoId)
    suspend fun addComment(comment: CommentEntity) {
        appDao.insertComment(comment)
        // Write to Supabase immediately
        syncCommentToSupabase(SUPABASE_URL, SUPABASE_KEY, comment)
    }
    suspend fun deleteComment(commentId: String) {
        appDao.deleteCommentById(commentId)
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "comments", "DELETE", query = "?id=eq.$commentId")
    }

    // --- Likes ---
    fun isLiked(videoId: String, userId: String): Flow<Boolean> = appDao.isVideoLikedFlow(videoId, userId)
    suspend fun toggleLike(videoId: String, userId: String) {
        val sbUrl = SUPABASE_URL
        val sbKey = SUPABASE_KEY
        val list = appDao.getLikedVideosByUser(userId)
        val isAlreadyLiked = list.any { it.videoId == videoId }
        val video = appDao.getVideoById(videoId)
        if (isAlreadyLiked) {
            appDao.deleteLike(LikeEntity(videoId, userId))
            if (video != null) {
                val updated = video.copy(likesCount = maxOf(0, video.likesCount - 1))
                appDao.updateVideo(updated)
                // Delete like from Supabase
                makeSupabaseRequest(sbUrl, sbKey, "likes", "DELETE",
                    query = "?video_id=eq.$videoId&user_id=eq.$userId")
                // Update likes_count in Supabase videos table
                makeSupabaseRequest(sbUrl, sbKey, "videos", "PATCH",
                    jsonBody = """{"likes_count":${updated.likesCount}}""",
                    query = "?id=eq.$videoId")
            }
        } else {
            appDao.insertLike(LikeEntity(videoId, userId))
            if (video != null) {
                val updated = video.copy(likesCount = video.likesCount + 1)
                appDao.updateVideo(updated)
                // Insert like to Supabase
                val likeJson = """{"video_id":"$videoId","user_id":"$userId"}"""
                makeSupabaseRequest(sbUrl, sbKey, "likes", "POST", likeJson)
                // Update likes_count in Supabase videos table
                makeSupabaseRequest(sbUrl, sbKey, "videos", "PATCH",
                    jsonBody = """{"likes_count":${updated.likesCount}}""",
                    query = "?id=eq.$videoId")
            }
        }
    }

    // --- Followers ---
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean> = appDao.isFollowingFlow(followerId, followingId)
    suspend fun toggleFollow(followerId: String, followingId: String) {
        val sbUrl = SUPABASE_URL
        val sbKey = SUPABASE_KEY
        val userToFollow = appDao.getUserById(followingId) ?: return
        val me = appDao.getUserById(followerId) ?: return
        try {
            appDao.insertFollower(FollowerEntity(followerId, followingId))
            val updatedFollowee = userToFollow.copy(followerCount = userToFollow.followerCount + 1)
            val updatedMe = me.copy(followingCount = me.followingCount + 1)
            appDao.updateUser(updatedFollowee)
            appDao.updateUser(updatedMe)
            // Insert follow to Supabase
            val followJson = """{"follower_id":"$followerId","following_id":"$followingId"}"""
            makeSupabaseRequest(sbUrl, sbKey, "followers", "POST", followJson)
            // Update follower/following counts
            makeSupabaseRequest(sbUrl, sbKey, "users", "PATCH",
                jsonBody = """{"follower_count":${updatedFollowee.followerCount}}""",
                query = "?id=eq.$followingId")
            makeSupabaseRequest(sbUrl, sbKey, "users", "PATCH",
                jsonBody = """{"following_count":${updatedMe.followingCount}}""",
                query = "?id=eq.$followerId")
        } catch (e: Exception) {
            appDao.deleteFollower(FollowerEntity(followerId, followingId))
            val updatedFollowee = userToFollow.copy(followerCount = maxOf(0, userToFollow.followerCount - 1))
            val updatedMe = me.copy(followingCount = maxOf(0, me.followingCount - 1))
            appDao.updateUser(updatedFollowee)
            appDao.updateUser(updatedMe)
            // Delete follow from Supabase
            makeSupabaseRequest(sbUrl, sbKey, "followers", "DELETE",
                query = "?follower_id=eq.$followerId&following_id=eq.$followingId")
            makeSupabaseRequest(sbUrl, sbKey, "users", "PATCH",
                jsonBody = """{"follower_count":${updatedFollowee.followerCount}}""",
                query = "?id=eq.$followingId")
            makeSupabaseRequest(sbUrl, sbKey, "users", "PATCH",
                jsonBody = """{"following_count":${updatedMe.followingCount}}""",
                query = "?id=eq.$followerId")
        }
    }

    // --- Chats & Messages ---
    fun getChatMessages(id1: String, id2: String): Flow<List<MessageEntity>> = appDao.getChatMessagesFlow(id1, id2)
    fun getChatPartners(userId: String): Flow<List<String>> = appDao.getChatPartnersFlow(userId)
    suspend fun sendMessage(message: MessageEntity) {
        appDao.insertMessage(message)
        syncMessageToSupabase(SUPABASE_URL, SUPABASE_KEY, message)
    }

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
        val updated = user.copy(coinBalance = newBalance)
        appDao.updateUser(updated)
        appDao.insertTransaction(CoinTransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            amount = amount,
            description = reason
        ))
        
        // Ensure accurate server representation is synced immediately to our Supabase Cloud for remote admin monitoring
        syncUserToSupabase(SUPABASE_URL, SUPABASE_KEY, updated)
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
        // Sync to Supabase
        val json = """{
            "id":"${withdrawal.id}",
            "user_id":"${withdrawal.userId}",
            "username":"${withdrawal.username}",
            "amount_coins":${withdrawal.amountCoins},
            "amount_usd":${withdrawal.amountUsd},
            "status":"${withdrawal.status}",
            "payment_method":"${withdrawal.paymentMethod}",
            "details":${escapeJsonString(withdrawal.details)},
            "timestamp":${withdrawal.timestamp}
        }""".trimIndent()
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "withdrawals", "POST", json)
        // Deduct user coins automatically
        adjustUserCoins(withdrawal.userId, -withdrawal.amountCoins, "Withdrawal Request: ${withdrawal.paymentMethod}")
    }
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) {
        appDao.updateWithdrawal(withdrawal)
        // Sync status update to Supabase
        val patchJson = """{"status":"${withdrawal.status}"}"""
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "withdrawals", "PATCH",
            jsonBody = patchJson, query = "?id=eq.${withdrawal.id}")
    }

    // --- Ads Settings ---
    val adSettings: Flow<AdSettingsEntity?> = appDao.getAdSettingsFlow()
    suspend fun getAdSettingsSnapshot(): AdSettingsEntity? = appDao.getAdSettings()
    suspend fun updateAdSettings(settings: AdSettingsEntity) {
        appDao.insertAdSettings(settings)
        // Sync ads settings to Supabase
        val json = """{
            "id":${settings.id},
            "ads_enabled":${settings.adsEnabled},
            "admob_banner_enabled":${settings.admobBannerEnabled},
            "admob_interstitial_enabled":${settings.admobInterstitialEnabled},
            "admob_rewarded_enabled":${settings.admobRewardedEnabled},
            "adsterra_enabled":${settings.adsterraEnabled},
            "frequency_seconds":${settings.frequencySeconds},
            "admob_app_id":"${settings.admobAppId}",
            "admob_banner_id":"${settings.admobBannerId}",
            "admob_interstitial_id":"${settings.admobInterstitialId}",
            "admob_rewarded_id":"${settings.admobRewardedId}",
            "adsterra_smartlink_url":"${settings.adsterraSmartlinkUrl}",
            "app_custom_name":${escapeJsonString(settings.appCustomName)},
            "app_maintenance_mode":${settings.appMaintenanceMode},
            "app_registrations_enabled":${settings.appRegistrationsEnabled},
            "app_uploads_enabled":${settings.appUploadsEnabled},
            "app_theme_color":"${settings.appThemeColor}",
            "clicks_count":${settings.clicksCount},
            "revenue_cents":${settings.revenueCents}
        }""".trimIndent()
        makeSupabaseRequest(SUPABASE_URL, SUPABASE_KEY, "ads_settings", "POST", json)
    }

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
        val banned = user.copy(isBanned = true)
        appDao.updateUser(banned)
        syncUserToSupabase(SUPABASE_URL, SUPABASE_KEY, banned)
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
            val unbanned = user.copy(isBanned = false)
            appDao.updateUser(unbanned)
            syncUserToSupabase(SUPABASE_URL, SUPABASE_KEY, unbanned)
        }
        appDao.removeBannedUser(userId)
        logAdminAction("UNBAN_USER", "Unbanned user ID: $userId")
    }

    suspend fun syncAllTablesFromSupabase(url: String, anonKey: String) {
        if (url.isBlank() || url.contains("your-project-id") || anonKey.length < 20) return
        
        // 1. Fetch Users
        val usersJson = makeSupabaseRequest(url, anonKey, "users", "GET", query = "?select=*")
        if (usersJson != null) {
            val users = parseSupabaseUsers(usersJson)
            for (user in users) { appDao.insertUser(user) }
        }
        
        // 2. Fetch Videos
        val videosJson = makeSupabaseRequest(url, anonKey, "videos", "GET", query = "?select=*&order=timestamp.desc")
        if (videosJson != null) {
            val videos = parseSupabaseVideos(videosJson)
            for (video in videos) { appDao.insertVideo(video) }
        }
        
        // 3. Fetch Comments
        val commentsJson = makeSupabaseRequest(url, anonKey, "comments", "GET", query = "?select=*")
        if (commentsJson != null) {
            val comments = parseSupabaseComments(commentsJson)
            for (comment in comments) { appDao.insertComment(comment) }
        }
        
        // 4. Fetch Messages
        val messagesJson = makeSupabaseRequest(url, anonKey, "messages", "GET", query = "?select=*")
        if (messagesJson != null) {
            val messages = parseSupabaseMessages(messagesJson)
            for (message in messages) { appDao.insertMessage(message) }
        }

        // 5. Fetch Withdrawals
        val wJson = makeSupabaseRequest(url, anonKey, "withdrawals", "GET", query = "?select=*")
        if (wJson != null) {
            val withdrawals = parseSupabaseWithdrawals(wJson)
            for (w in withdrawals) { appDao.insertWithdrawal(w) }
        }

        // 6. Fetch Likes
        val likesJson = makeSupabaseRequest(url, anonKey, "likes", "GET", query = "?select=*")
        if (likesJson != null) {
            val likes = parseSupabaseLikes(likesJson)
            for (l in likes) { appDao.insertLike(l) }
        }

        // 7. Fetch Followers
        val followersJson = makeSupabaseRequest(url, anonKey, "followers", "GET", query = "?select=*")
        if (followersJson != null) {
            val followers = parseSupabaseFollowers(followersJson)
            for (f in followers) { appDao.insertFollower(f) }
        }
    }

    fun parseSupabaseWithdrawals(jsonStr: String): List<WithdrawalEntity> {
        val list = mutableListOf<WithdrawalEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", "")
                if (id.isNotBlank()) {
                    list.add(WithdrawalEntity(
                        id = id,
                        userId = obj.optString("user_id", ""),
                        username = obj.optString("username", ""),
                        amountCoins = obj.optInt("amount_coins", 0),
                        amountUsd = obj.optDouble("amount_usd", 0.0),
                        status = obj.optString("status", "pending"),
                        paymentMethod = obj.optString("payment_method", ""),
                        details = obj.optString("details", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseWithdrawals error: ${e.message}")
        }
        return list
    }

    fun parseSupabaseLikes(jsonStr: String): List<LikeEntity> {
        val list = mutableListOf<LikeEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val videoId = obj.optString("video_id", "")
                val userId = obj.optString("user_id", "")
                if (videoId.isNotBlank() && userId.isNotBlank()) {
                    list.add(LikeEntity(videoId, userId))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseLikes error: ${e.message}")
        }
        return list
    }

    fun parseSupabaseFollowers(jsonStr: String): List<FollowerEntity> {
        val list = mutableListOf<FollowerEntity>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val followerId = obj.optString("follower_id", "")
                val followingId = obj.optString("following_id", "")
                if (followerId.isNotBlank() && followingId.isNotBlank()) {
                    list.add(FollowerEntity(followerId, followingId))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Supabase", "parseFollowers error: ${e.message}")
        }
        return list
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
