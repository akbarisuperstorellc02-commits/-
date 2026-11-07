package com.example.model

/**
 * Standard or Progressive Tier Code (A1 to Z9)
 */
enum class TierGroup(val titleFa: String, val emoji: String) {
    TIER_A("مقدمات و پایه‌ها (A1 - A3)", "🌱"),
    TIER_B("مکالمه روزمره و زمان‌ها (B1 - B3)", "🌸"),
    TIER_C("گرامر و اصطلاحات کاربردی (C1 - C3)", "🎀"),
    TIER_D("فن بیان، لهجه و سخنوری (D1 - D3)", "🎙️"),
    TIER_E("انگلیسی پیشرفته و شغلی (E1 - E3)", "🏛️"),
    TIER_F("اصطلاحات بومی و اسلنگ (F1 - F3)", "💎"),
    TIER_G("خودسازی و روانشناسی (G1 - G3)", "📖"),
    TIER_Z("تسلط مطلق و تاج استادی (Z1 - Z9)", "👑")
}

enum class ClassroomType(val titleFa: String, val titleEn: String, val emoji: String) {
    PHONICS("کلاس حروف و فونیکس", "Phonics & Sounds", "🔤"),
    GRAMMAR("کلاس جامع گرامر", "Grammar Mastery", "📖"),
    READING_WRITING("کلاس خواندن و نوشتن", "Reading & Writing", "✍️"),
    CONVERSATION("کلاس مکالمه پفکی", "Puffy Conversation", "🎙️"),
    LISTENING("کلاس شنیداری و لیسنینگ", "Listening Lab", "🎧"),
    PRONUNCIATION("کلاس تلفظ و لهجه", "Pronunciation & Accent", "🗣️"),
    VOCABULARY("کلاس واژگان طلایی", "Vocabulary Builder", "📚")
}

data class LessonItem(
    val id: String,
    val englishText: String,
    val phonetic: String,
    val persianTranslation: String,
    val exampleSentenceEn: String,
    val exampleSentenceFa: String,
    val emoji: String,
    val classroomType: ClassroomType,
    val levelCode: String,
    val grammarRuleFa: String? = null
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRANSLATION,
    LISTENING,
    SPEAKING,
    GRAMMAR_FILL
}

data class QuizQuestion(
    val id: String,
    val type: QuestionType,
    val promptEn: String,
    val promptFa: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationFa: String,
    val targetSpeechText: String? = null
)

data class Level(
    val id: Int,
    val levelCode: String, // e.g. "A1", "A2", ..., "Z9"
    val tierGroup: TierGroup,
    val titleFa: String,
    val titleEn: String,
    val iconEmoji: String,
    val descriptionFa: String,
    val accentColorHex: Long,
    val items: List<LessonItem>,
    val examQuestions: List<QuizQuestion>,
    var isUnlocked: Boolean = false,
    var bestScore: Int = 0,
    var starsEarned: Int = 0,
    var isMastered: Boolean = false
)

data class GoldenMedal(
    val id: String,
    val titleFa: String,
    val descriptionFa: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val requiredStars: Int = 0
)

data class DiaryNote(
    val id: String,
    val dateIso: String,
    val dateDisplayFa: String,
    val moodEmoji: String,
    val moodTitleFa: String,
    val titleFa: String,
    val content: String,
    val englishSentenceLearned: String = "",
    val attachedStickers: List<String> = emptyList(),
    val colorTagHex: Long = 0xFFFF85A1
)

data class GameStage(
    val stageNumber: Int,
    val targetWordEn: String,
    val hintFa: String,
    val emoji: String,
    val jumbledLetters: List<Char>,
    val bonusGoldReward: Int = 25,
    val isCompleted: Boolean = false
)

data class SpeechEvaluationResult(
    val spokenText: String,
    val targetText: String,
    val scorePercent: Int,
    val feedbackFa: String,
    val stars: Int,
    val matchedTokens: List<String>,
    val missedTokens: List<String>
)
