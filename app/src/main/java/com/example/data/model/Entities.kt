package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val isBanned: Boolean = false,
    val canUpload: Boolean = true,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val videoCount: Int = 0,
    val coinBalance: Int = 0,
    val email: String = "",
    val bio: String = "",
    val registeredAt: Long = System.currentTimeMillis(),
    val monetizationApproved: Boolean = false,
    val creatorEarningPercentage: Int = 80,
    val creatorIncome: Int = 0
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val caption: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isHidden: Boolean = false,
    val commentsDisabled: Boolean = false,
    val sharingDisabled: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val text: String,
    val isSpam: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "likes", primaryKeys = ["videoId", "userId"])
data class LikeEntity(
    val videoId: String,
    val userId: String
)

@Entity(tableName = "followers", primaryKeys = ["followerId", "followingId"])
data class FollowerEntity(
    val followerId: String,
    val followingId: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "like", "comment", "follow", "gift", "admin"
    val senderUsername: String,
    val senderAvatar: String,
    val text: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val taskType: String // "watch_video", "follow", "comment", "daily_checkin", "refer"
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val amountCoins: Int,
    val amountUsd: Double,
    val status: String, // "pending", "approved", "rejected"
    val paymentMethod: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ads_settings")
data class AdSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val adsEnabled: Boolean = true,
    val admobBannerEnabled: Boolean = true,
    val admobInterstitialEnabled: Boolean = false,
    val admobRewardedEnabled: Boolean = false,
    val adsterraEnabled: Boolean = false,
    val frequencySeconds: Int = 30,
    val clicksCount: Int = 0,
    val revenueCents: Int = 0,
    val customAdScript: String = "",
    val adsByCountry: String = "All",
    val adsByPage: String = "Feed",
    val admobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val admobBannerId: String = "ca-app-pub-3940256099942544/6300978111",
    val admobInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    val admobRewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    val adsterraSmartlinkUrl: String = "https://example.com/smartlink"
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val targetType: String, // "video", "user", "comment"
    val targetId: String,
    val reporterUsername: String,
    val reason: String,
    val status: String, // "pending", "reviewed", "resolved"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analytics")
data class AnalyticsSnapshotEntity(
    @PrimaryKey val id: Int = 1,
    val totalViews: Int = 12584,
    val totalWatchTimeMinutes: Int = 45290,
    val activeUsers: Int = 1420
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey val id: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "banned_users")
data class BannedUserEntity(
    @PrimaryKey val id: String, // userId
    val username: String,
    val email: String,
    val reason: String,
    val bannedAt: Long = System.currentTimeMillis()
)
