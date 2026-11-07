package com.example.academy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        LessonProgressEntity::class,
        QuizAttemptEntity::class,
        RewardLedgerEntity::class,
        StudySessionEntity::class,
        VocabularyEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile private var INSTANCE: AcademyDatabase? = null

        fun getInstance(context: Context): AcademyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "bh_personal_academy.db"
                ).build().also { INSTANCE = it }
            }
    }
}
