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
        StudySessionEntity::class,
        WalletEntity::class,
        RewardEventEntity::class,
        VocabularyEntity::class,
        DailyGoalEntity::class,
        ReminderEntity::class,
        GameAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_attempts (
                        attemptId TEXT NOT NULL,
                        profileId TEXT NOT NULL,
                        gameId TEXT NOT NULL,
                        score INTEGER NOT NULL,
                        totalQuestions INTEGER NOT NULL,
                        completedAt INTEGER NOT NULL,
                        PRIMARY KEY(attemptId)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_game_attempts_profileId ON game_attempts(profileId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_game_attempts_completedAt ON game_attempts(completedAt)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE vocabulary ADD COLUMN lastPractisedAt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS vocabulary_new (
                        wordId TEXT NOT NULL,
                        profileId TEXT NOT NULL,
                        english TEXT NOT NULL,
                        dariMeaning TEXT NOT NULL,
                        pronunciation TEXT NOT NULL,
                        exampleEnglish TEXT NOT NULL,
                        exampleDari TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        favourite INTEGER NOT NULL,
                        mastery INTEGER NOT NULL,
                        lastPractisedAt INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(profileId, wordId)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO vocabulary_new
                    (wordId, profileId, english, dariMeaning, pronunciation, exampleEnglish,
                     exampleDari, categoryId, favourite, mastery, lastPractisedAt)
                    SELECT wordId, profileId, english, dariMeaning, pronunciation, exampleEnglish,
                           exampleDari, categoryId, favourite, mastery, lastPractisedAt
                    FROM vocabulary
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE vocabulary")
                database.execSQL("ALTER TABLE vocabulary_new RENAME TO vocabulary")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_vocabulary_profileId ON vocabulary(profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_vocabulary_english ON vocabulary(english)")
            }
        }

        @Volatile
        private var instance: AcademyDatabase? = null

        fun getInstance(context: Context): AcademyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "bh_personal_academy.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
    }
}
