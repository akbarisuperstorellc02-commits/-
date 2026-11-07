package com.example.academy.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val profileId: String = "primary",
    val displayName: String = "زهرا",
    val username: String = "zahra",
    val avatarUri: String? = null,
    val localeTag: String = "fa-AF",
    val themeId: String = "SOFT_PINK",
    val fontId: String = "SANS_SERIF",
    val dailyGoalMinutes: Int = 20,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_progress", indices = [Index("profileId"), Index("lessonId")])
data class LessonProgressEntity(
    @PrimaryKey val recordId: String,
    val profileId: String = "primary",
    val lessonId: String,
    val completed: Boolean = false,
    val bestScore: Int = 0,
    val completedAtEpochMillis: Long? = null
)

@Entity(tableName = "quiz_attempts", indices = [Index("profileId"), Index("quizId")])
data class QuizAttemptEntity(
    @PrimaryKey val attemptId: String,
    val profileId: String = "primary",
    val quizId: String,
    val score: Int,
    val totalQuestions: Int,
    val incorrectQuestionIdsCsv: String = "",
    val completedAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "reward_ledger", indices = [Index(value = ["profileId", "source", "sourceId"], unique = true)])
data class RewardLedgerEntity(
    @PrimaryKey val eventId: String,
    val profileId: String = "primary",
    val source: String,
    val sourceId: String,
    val xpDelta: Int,
    val coinDelta: Int,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions", indices = [Index("profileId"), Index("startedAtEpochMillis")])
data class StudySessionEntity(
    @PrimaryKey val sessionId: String,
    val profileId: String = "primary",
    val lessonId: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val activeDurationMillis: Long = 0L
)

@Entity(tableName = "vocabulary", indices = [Index("profileId"), Index("english")])
data class VocabularyEntity(
    @PrimaryKey val vocabularyId: String,
    val profileId: String = "primary",
    val english: String,
    val dariMeaning: String,
    val pronunciation: String,
    val exampleEnglish: String,
    val exampleDari: String,
    val categoryId: String,
    val favourite: Boolean = false,
    val mastery: Int = 0,
    val needsReview: Boolean = false
)

@Entity(tableName = "reminders", indices = [Index("profileId")])
data class ReminderEntity(
    @PrimaryKey val reminderId: String,
    val profileId: String = "primary",
    val title: String,
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int,
    val enabled: Boolean = true,
    val soundUri: String? = null
)
