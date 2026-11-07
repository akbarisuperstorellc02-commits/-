package com.bh.personalacademy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLessonProgress(progress: LessonProgressEntity)

    @Query("SELECT * FROM lesson_progress WHERE profileId = :profileId AND lessonId = :lessonId LIMIT 1")
    abstract suspend fun getLessonProgress(profileId: String, lessonId: String): LessonProgressEntity?

    @Transaction
    open suspend fun recordLessonCompletion(
        lessonId: String,
        completedAt: Long,
        score: Int,
        profileId: String = "primary"
    ) {
        val previous = getLessonProgress(profileId, lessonId)
        saveLessonProgress(
            LessonProgressEntity(
                profileId = profileId,
                lessonId = lessonId,
                completedAt = completedAt,
                bestScore = maxOf(previous?.bestScore ?: 0, score),
                attempts = (previous?.attempts ?: 0) + 1
            )
        )
    }

    @Query("SELECT * FROM lesson_progress WHERE profileId = :profileId")
    abstract fun observeLessonProgress(profileId: String = "primary"): Flow<List<LessonProgressEntity>>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE profileId = :profileId AND completedAt >= :fromMillis AND completedAt < :untilMillis")
    abstract fun observeCompletedLessonCount(fromMillis: Long, untilMillis: Long, profileId: String = "primary"): Flow<Int>

    @Insert
    abstract suspend fun insertQuizAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts WHERE profileId = :profileId ORDER BY completedAt DESC")
    abstract fun observeQuizAttempts(profileId: String = "primary"): Flow<List<QuizAttemptEntity>>

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE profileId = :profileId AND completedAt >= :fromMillis AND completedAt < :untilMillis")
    abstract fun observeQuizCount(fromMillis: Long, untilMillis: Long, profileId: String = "primary"): Flow<Int>

    @Insert
    abstract suspend fun insertGameAttempt(attempt: GameAttemptEntity)

    @Query("SELECT * FROM game_attempts WHERE profileId = :profileId ORDER BY completedAt DESC")
    abstract fun observeGameAttempts(profileId: String = "primary"): Flow<List<GameAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStudySession(session: StudySessionEntity)

    @Query("""
        SELECT COALESCE(SUM(durationMillis), 0)
        FROM study_sessions
        WHERE profileId = :profileId AND startedAt >= :fromMillis AND startedAt < :untilMillis
    """)
    abstract fun observeStudyMillis(
        fromMillis: Long,
        untilMillis: Long,
        profileId: String = "primary"
    ): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationMillis), 0) FROM study_sessions WHERE profileId = :profileId")
    abstract fun observeTotalStudyMillis(profileId: String = "primary"): Flow<Long>

    @Query("SELECT startedAt FROM study_sessions WHERE profileId = :profileId AND startedAt >= :fromMillis AND startedAt < :untilMillis")
    abstract fun observeStudySessionStarts(fromMillis: Long, untilMillis: Long, profileId: String = "primary"): Flow<List<Long>>

    @Query("SELECT * FROM study_sessions WHERE profileId = :profileId ORDER BY startedAt DESC")
    abstract fun observeStudySessions(profileId: String = "primary"): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertRewardEvent(event: RewardEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE profileId = :profileId")
    abstract fun observeWallet(profileId: String = "primary"): Flow<WalletEntity?>

    @Query("""
        UPDATE wallets
        SET xp = MAX(0, xp + :xpDelta),
            coins = MAX(0, coins + :coinDelta),
            level = CAST(MAX(0, xp + :xpDelta) / 500 AS INTEGER) + 1
        WHERE profileId = :profileId
    """)
    abstract suspend fun updateWallet(profileId: String, xpDelta: Int, coinDelta: Int)

    @Transaction
    open suspend fun applyRewardOnce(event: RewardEventEntity): Boolean {
        insertWallet(WalletEntity(profileId = event.profileId))
        if (insertRewardEvent(event) == -1L) return false
        updateWallet(event.profileId, event.xpDelta, event.coinDelta)
        return true
    }

    @Query("UPDATE wallets SET coins = coins - 6 WHERE profileId = :profileId AND coins >= 6")
    abstract suspend fun spendHintCoins(profileId: String = "primary"): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveVocabulary(word: VocabularyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertVocabularyIfMissing(word: VocabularyEntity)

    @Query("SELECT * FROM vocabulary WHERE profileId = :profileId ORDER BY english COLLATE NOCASE")
    abstract fun observeVocabulary(profileId: String = "primary"): Flow<List<VocabularyEntity>>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE profileId = :profileId AND lastPractisedAt >= :fromMillis AND lastPractisedAt < :untilMillis")
    abstract fun observeWordsPractisedCount(fromMillis: Long, untilMillis: Long, profileId: String = "primary"): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveDailyGoal(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goals WHERE profileId = :profileId AND dateIso = :dateIso LIMIT 1")
    abstract fun observeDailyGoal(dateIso: String, profileId: String = "primary"): Flow<DailyGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE profileId = :profileId ORDER BY hour, minute")
    abstract fun observeReminders(profileId: String = "primary"): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE profileId = :profileId AND enabled = 1")
    abstract suspend fun getEnabledReminders(profileId: String = "primary"): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    abstract suspend fun deleteReminder(reminderId: String)
}
