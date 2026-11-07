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
import com.example.academy.domain.model.RewardEventIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Single data boundary for the local-first academy.
 * Compose screens observe repository flows; all writes stay on the repository boundary.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AcademyRepository(
    private val preferences: AcademyPreferences,
    private val dao: AcademyDao
) {
    val profile = preferences.profile
    private val activeProfileId = profile.map { it.profileId }.distinctUntilChanged()
    val profiles = preferences.profiles
    val wallet = activeProfileId.flatMapLatest { dao.observeWallet(it) }
    val lessonProgress = activeProfileId.flatMapLatest { dao.observeLessonProgress(it) }
    val quizAttempts = activeProfileId.flatMapLatest { dao.observeQuizAttempts(it) }
    val gameAttempts = activeProfileId.flatMapLatest { dao.observeGameAttempts(it) }
    val vocabulary = activeProfileId.flatMapLatest { dao.observeVocabulary(it) }
    val reminders = activeProfileId.flatMapLatest { dao.observeReminders(it) }
    val studySessions = activeProfileId.flatMapLatest { dao.observeStudySessions(it) }
    val totalStudyMillis = activeProfileId.flatMapLatest { dao.observeTotalStudyMillis(it) }

    fun studyMillis(fromMillis: Long, untilMillis: Long): Flow<Long> =
        activeProfileId.flatMapLatest { dao.observeStudyMillis(fromMillis, untilMillis, it) }

    fun completedLessons(fromMillis: Long, untilMillis: Long): Flow<Int> =
        activeProfileId.flatMapLatest { dao.observeCompletedLessonCount(fromMillis, untilMillis, it) }

    fun quizCount(fromMillis: Long, untilMillis: Long): Flow<Int> =
        activeProfileId.flatMapLatest { dao.observeQuizCount(fromMillis, untilMillis, it) }

    fun practisedWordCount(fromMillis: Long, untilMillis: Long): Flow<Int> =
        activeProfileId.flatMapLatest { dao.observeWordsPractisedCount(fromMillis, untilMillis, it) }

    suspend fun initializeProfileWallet() {
        val id = preferences.profile.map { it.profileId }.distinctUntilChanged().first()
        dao.insertWallet(WalletEntity(profileId = id, coins = 30))
    }

    suspend fun completeLesson(lessonId: String, score: Int = 100, profileId: String = "primary") {
        dao.recordLessonCompletion(lessonId, System.currentTimeMillis(), score, profileId)
        dao.applyRewardOnce(
            RewardEventEntity(
                eventId = RewardEventIds.lessonCompletion(profileId, lessonId),
                profileId = profileId,
                source = "LESSON",
                sourceId = lessonId,
                xpDelta = 25,
                coinDelta = 5,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveStudySession(lessonId: String, startedAt: Long, durationMillis: Long, profileId: String = "primary") {
        if (durationMillis <= 0L) return
        dao.insertStudySession(
            StudySessionEntity(
                sessionId = UUID.randomUUID().toString(),
                profileId = profileId,
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
