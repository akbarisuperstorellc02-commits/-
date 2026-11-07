package com.example.academy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    @Query("SELECT * FROM user_profiles WHERE profileId = :profileId LIMIT 1")
    fun observeProfile(profileId: String = "primary"): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM lesson_progress WHERE profileId = :profileId ORDER BY lessonId")
    fun observeLessonProgress(profileId: String = "primary"): Flow<List<LessonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLessonProgress(progress: LessonProgressEntity)

    @Query("SELECT * FROM quiz_attempts WHERE profileId = :profileId ORDER BY completedAtEpochMillis DESC")
    fun observeQuizAttempts(profileId: String = "primary"): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM reward_ledger WHERE profileId = :profileId ORDER BY createdAtEpochMillis DESC")
    fun observeRewards(profileId: String = "primary"): Flow<List<RewardLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRewardIfNew(event: RewardLedgerEntity): Long

    @Query("SELECT COALESCE(SUM(xpDelta), 0) FROM reward_ledger WHERE profileId = :profileId")
    suspend fun totalXp(profileId: String = "primary"): Long

    @Query("SELECT COALESCE(SUM(coinDelta), 0) FROM reward_ledger WHERE profileId = :profileId")
    suspend fun totalCoins(profileId: String = "primary"): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudySession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions WHERE profileId = :profileId ORDER BY startedAtEpochMillis DESC")
    fun observeStudySessions(profileId: String = "primary"): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVocabulary(entry: VocabularyEntity)

    @Query("SELECT * FROM vocabulary WHERE profileId = :profileId ORDER BY english COLLATE NOCASE")
    fun observeVocabulary(profileId: String = "primary"): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE profileId = :profileId AND needsReview = 1 ORDER BY english")
    fun observeReviewQueue(profileId: String = "primary"): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE profileId = :profileId ORDER BY hour, minute")
    fun observeReminders(profileId: String = "primary"): Flow<List<ReminderEntity>>

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId AND profileId = :profileId")
    suspend fun deleteReminder(reminderId: String, profileId: String = "primary")
}
