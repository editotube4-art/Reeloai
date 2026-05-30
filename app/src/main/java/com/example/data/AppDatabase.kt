package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppDao
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        VideoEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        FollowerEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        CoinTransactionEntity::class,
        TaskEntity::class,
        WithdrawalEntity::class,
        AdSettingsEntity::class,
        ReportEntity::class,
        AnalyticsSnapshotEntity::class,
        AdminLogEntity::class,
        BannedUserEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reeloai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
