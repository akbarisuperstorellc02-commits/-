package com.bh.personalacademy.data

import com.bh.personalacademy.data.local.AcademyDao
import com.bh.personalacademy.data.local.GameAttemptEntity
import com.bh.personalacademy.data.local.LessonProgressEntity
import com.bh.personalacademy.data.local.QuizAttemptEntity
import com.bh.personalacademy.data.local.ReminderEntity
import com.bh.personalacademy.data.local.RewardEventEntity
import com.bh.personalacademy.data.local.StudySessionEntity
import com.bh.personalacademy.data.local.VocabularyEntity
import com.bh.personalacademy.data.local.WalletEntity
import com.bh.personalacademy.data.preferences.AcademyPreferences
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Single data boundary for the local-first academy.
 * Compose screens observe repository flows; all writes stay on the repository boundary.
 */
class AcademyRepository(
    private val preferences: AcademyPreferences,
    private val dao: AcademyDao
) {
    val profile = preferences.profile
    val wallet = dao.observeWallet()
    val lessonProgress = dao.observeLessonProgress()
    val quizAttempts = dao.observeQuizAttempts()
    val gameAttempts = dao.observeGameAttempts()
    val vocabulary = dao.observeVocabulary()
    val reminders = dao.observeReminders()
    val studySessions = dao.observeStudySessions()
    val totalStudyMillis = dao.observeTotalStudyMillis()

    fun studyMillis(fromMillis: Long, untilMillis: Long): Flow<Long> =
        dao.observeStudyMillis(fromMillis, untilMillis)

    fun completedLessons(fromMillis: Long, untilMillis: Long): Flow<Int> =
        dao.observeCompletedLessonCount(fromMillis, untilMillis)

    fun quizCount(fromMillis: Long, untilMillis: Long): Flow<Int> =
        dao.observeQuizCount(fromMillis, untilMillis)

    fun practisedWordCount(fromMillis: Long, untilMillis: Long): Flow<Int> =
        dao.observeWordsPractisedCount(fromMillis, untilMillis)

    suspend fun initializeProfileWallet() {
        dao.insertWallet(WalletEntity(profileId = "primary", coins = 30))
    }

    suspend fun completeLesson(lessonId: String, score: Int = 100) {
        dao.recordLessonCompletion(lessonId, System.currentTimeMillis(), score)
        dao.applyRewardOnce(
            RewardEventEntity(
                eventId = UUID.randomUUID().toString(),
                source = "LESSON",
                sourceId = lessonId,
                xpDelta = 25,
                coinDelta = 5,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveStudySession(lessonId: String, startedAt: Long, durationMillis: Long) {
        if (durationMillis <= 0L) return
        dao.insertStudySession(
            StudySessionEntity(
                sessionId = UUID.randomUUID().toString(),
                lessonId = lessonId,
                startedAt = startedAt,
                durationMillis = durationMillis,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun practiseWord(word: VocabularyEntity) {
        dao.saveVocabulary(
            word.copy(
                mastery = (word.mastery + 1).coerceAtMost(5),
                lastPractisedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveReminder(reminder: ReminderEntity) = dao.saveReminder(reminder)

    suspend fun deleteReminder(reminderId: String) = dao.deleteReminder(reminderId)

    suspend fun recordQuiz(attempt: QuizAttemptEntity) = dao.insertQuizAttempt(attempt)

    suspend fun recordGame(attempt: GameAttemptEntity) = dao.insertGameAttempt(attempt)

    suspend fun rewardOnce(event: RewardEventEntity): Boolean = dao.applyRewardOnce(event)
}
