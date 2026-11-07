package com.example.academy.domain.model

/**
 * Domain types for BH Personal Academy.
 * UI strings must be resolved through the locale resources, never embedded in these models.
 */
enum class AcademyLocale(val languageTag: String, val isRtl: Boolean) {
    DARI_AF("fa-AF", true),
    PASHTO_AF("ps-AF", true),
    ARABIC("ar", true),
    ENGLISH_US("en-US", false)
}

enum class AcademyThemeId {
    SOFT_PINK, PINK_PURPLE, LILAC_WHITE, PASTEL, FLORAL,
    NAVY_BLUE, BLUE_GRAY, GREEN_BLACK, SPORT, SPACE,
    BLACK_GOLD, WHITE_GOLD, ROYAL_PURPLE, NIGHT, LIGHT, DARK
}

enum class AcademyFontId { SANS_SERIF, SERIF, CURSIVE }

enum class LearningBand { PRE_A1, A1, A2, B1, B2, C1, C2 }

data class UserProfile(
    val profileId: String = "primary",
    val displayName: String = "زهرا",
    val username: String = "zahra",
    val avatarUri: String? = null,
    val locale: AcademyLocale = AcademyLocale.DARI_AF,
    val theme: AcademyThemeId = AcademyThemeId.SOFT_PINK,
    val font: AcademyFontId = AcademyFontId.SANS_SERIF,
    val dailyStudyGoalMinutes: Int = 20,
    val dailyGoalLessons: Int = 1,
    val dailyGoalWords: Int = 5,
    val dailyGoalQuizzes: Int = 1,
    val createdAtEpochMillis: Long = 0L
) {
    fun withDisplayName(name: String): UserProfile =
        copy(displayName = name.trim().ifEmpty { displayName })
}

data class LearningLesson(
    val id: String,
    val band: LearningBand,
    val unitNumber: Int,
    val orderInUnit: Int,
    val titleKey: String,
    val objectiveKey: String,
    val contentVersion: Int = 1,
    val estimatedMinutes: Int = 8,
    val prerequisiteLessonIds: Set<String> = emptySet()
)

data class VocabularyEntry(
    val id: String,
    val english: String,
    val dariMeaning: String,
    val pronunciation: String,
    val exampleEnglish: String,
    val exampleDari: String,
    val categoryId: String,
    val isFavourite: Boolean = false,
    val mastery: Int = 0
)

data class QuizAttempt(
    val id: String,
    val profileId: String,
    val quizId: String,
    val score: Int,
    val totalQuestions: Int,
    val completedAtEpochMillis: Long,
    val incorrectQuestionIds: List<String>
) {
    val percentage: Int
        get() = if (totalQuestions <= 0) 0 else (score * 100 / totalQuestions).coerceIn(0, 100)
}

enum class RewardSource { LESSON, QUIZ, GAME, DAILY_GOAL, STREAK, LEVEL_COMPLETION, REVIEW }

data class RewardEvent(
    val eventId: String,
    val profileId: String,
    val source: RewardSource,
    val sourceId: String,
    val xpDelta: Int,
    val coinDelta: Int,
    val createdAtEpochMillis: Long
)

data class Wallet(
    val xp: Long = 0L,
    val coins: Long = 0L,
    val level: Int = 1
) {
    fun apply(event: RewardEvent): Wallet = copy(
        xp = (xp + event.xpDelta).coerceAtLeast(0L),
        coins = (coins + event.coinDelta).coerceAtLeast(0L),
        level = ((xp + event.xpDelta).coerceAtLeast(0L) / 500L + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    )

    fun spendHint(): Wallet? =
        if (coins >= HINT_COST_COINS) copy(coins = coins - HINT_COST_COINS) else null

    companion object {
        const val HINT_COST_COINS = 6L
    }
}

data class StudySession(
    val sessionId: String,
    val profileId: String,
    val lessonId: String,
    val startedAtEpochMillis: Long,
    val accumulatedMillis: Long = 0L,
    val isActive: Boolean = true
)

data class DailyGoal(
    val profileId: String,
    val dateIso: String,
    val targetMinutes: Int,
    val targetLessons: Int,
    val targetWords: Int,
    val targetQuizzes: Int
)
