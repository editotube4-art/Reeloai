package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?


    // --- Videos ---
    @Query("SELECT * FROM videos WHERE isHidden = 0 ORDER BY timestamp DESC")
    fun getActiveVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isTrending = 1 AND isHidden = 0 ORDER BY likesCount DESC")
    fun getTrendingVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFeatured = 1 AND isHidden = 0 ORDER BY timestamp DESC")
    fun getFeaturedVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE userId = :userId ORDER BY timestamp DESC")
    fun getVideosByUserIdFlow(userId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: String)


    // --- Comments ---
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp ASC")
    fun getCommentsByVideoFlow(videoId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllCommentsFlow(): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)


    // --- Likes ---
    @Query("SELECT * FROM likes WHERE userId = :userId")
    suspend fun getLikedVideosByUser(userId: String): List<LikeEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE videoId = :videoId AND userId = :userId)")
    fun isVideoLikedFlow(videoId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Delete
    suspend fun deleteLike(like: LikeEntity)


    // --- Followers ---
    @Query("SELECT EXISTS(SELECT 1 FROM followers WHERE followerId = :followerId AND followingId = :followingId)")
    fun isFollowingFlow(followerId: String, followingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollower(follower: FollowerEntity)

    @Delete
    suspend fun deleteFollower(follower: FollowerEntity)


    // --- Messages ---
    @Query("SELECT * FROM messages WHERE (senderId = :id1 AND receiverId = :id2) OR (senderId = :id2 AND receiverId = :id1) ORDER BY timestamp ASC")
    fun getChatMessagesFlow(id1: String, id2: String): Flow<List<MessageEntity>>

    @Query("SELECT DISTINCT senderId FROM messages WHERE receiverId = :userId UNION SELECT DISTINCT receiverId FROM messages WHERE senderId = :userId")
    fun getChatPartnersFlow(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)


    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)


    // --- Coin Transactions ---
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: String): Flow<List<CoinTransactionEntity>>

    @Query("SELECT * FROM coin_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<CoinTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransactionEntity)


    // --- Tasks ---
    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)


    // --- Withdrawals ---
    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWithdrawalsForUserFlow(userId: String): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)


    // --- Ads Settings ---
    @Query("SELECT * FROM ads_settings WHERE id = 1")
    fun getAdSettingsFlow(): Flow<AdSettingsEntity?>

    @Query("SELECT * FROM ads_settings WHERE id = 1")
    suspend fun getAdSettings(): AdSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdSettings(settings: AdSettingsEntity)


    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)


    // --- Analytics ---
    @Query("SELECT * FROM analytics WHERE id = 1")
    fun getAnalyticsFlow(): Flow<AnalyticsSnapshotEntity?>

    @Query("SELECT * FROM analytics WHERE id = 1")
    suspend fun getAnalytics(): AnalyticsSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AnalyticsSnapshotEntity)


    // --- Admin Logs ---
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogsFlow(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)


    // --- Banned Users ---
    @Query("SELECT * FROM banned_users ORDER BY bannedAt DESC")
    fun getAllBannedUsersFlow(): Flow<List<BannedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBannedUser(user: BannedUserEntity)

    @Query("DELETE FROM banned_users WHERE id = :userId")
    suspend fun removeBannedUser(userId: String)
}
