package com.bh.personalacademy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LessonProgressEntity::class,
        QuizAttemptEntity::class,
        GameAttemptEntity::class,
        StudySessionEntity::class,
        WalletEntity::class,
        RewardEventEntity::class,
        VocabularyEntity::class,
        DailyGoalEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS game_attempts (" +
                        "attemptId TEXT NOT NULL, profileId TEXT NOT NULL, gameId TEXT NOT NULL, " +
                        "score INTEGER NOT NULL, totalQuestions INTEGER NOT NULL, completedAt INTEGER NOT NULL, " +
                        "PRIMARY KEY(attemptId))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_game_attempts_profileId ON game_attempts(profileId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_game_attempts_completedAt ON game_attempts(completedAt)")
            }
        }

        @Volatile private var instance: AcademyDatabase? = null

        fun getInstance(context: Context): AcademyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "bh_personal_academy.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
