package com.example.data

import android.content.Context
import com.example.academy.domain.model.AcademyLocale
import com.example.model.DiaryNote
import com.example.model.GameStage
import com.example.model.GoldenMedal
import com.example.model.Level
import com.example.ui.theme.ZahraFontStyle
import com.example.ui.theme.ZahraThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class ScoreReward(
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Transitional UI-facing progress facade. Structured learning records are being moved
 * to Room; profile and appearance preferences are kept separately from learning history.
 */
class UserProgressManager(context: Context) {
    private val prefs = context.getSharedPreferences("bh_personal_academy_v1", Context.MODE_PRIVATE)

    private val _levelsState = MutableStateFlow(
        CurriculumData.levels.mapIndexed { index, level ->
            level.copy(
                isUnlocked = prefs.getBoolean("level_${level.id}_unlocked", index == 0),
                bestScore = prefs.getInt("level_${level.id}_best", 0),
                starsEarned = prefs.getInt("level_${level.id}_stars", 0),
                isMastered = prefs.getBoolean("level_${level.id}_mastered", false)
            )
        }
    )
    val levelsState: StateFlow<List<Level>> = _levelsState.asStateFlow()

    private val _goldCoins = MutableStateFlow(prefs.getInt("gold_coins", 0))
    val goldCoins: StateFlow<Int> = _goldCoins.asStateFlow()
    private val _totalStars = MutableStateFlow(prefs.getInt("total_stars", 0))
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()
    private val _streakDays = MutableStateFlow(prefs.getInt("streak_days", 0))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val todayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    private val savedStudyDate = prefs.getString("study_date", todayKey)
    private val initialTodayMinutes = if (savedStudyDate == todayKey) prefs.getInt("today_study_minutes", 0) else 0
    private val _todayStudyMinutes = MutableStateFlow(initialTodayMinutes)
    val todayStudyMinutes: StateFlow<Int> = _todayStudyMinutes.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 20))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()
    private val _totalStudyHours = MutableStateFlow(prefs.getLong("total_study_minutes", 0L).div(60).toInt())
    val totalStudyHours: StateFlow<Int> = _totalStudyHours.asStateFlow()

    private val _currentTheme = MutableStateFlow(readEnum("current_theme", ZahraThemeMode.SWEET_SAKURA_PUFFY))
    val currentTheme: StateFlow<ZahraThemeMode> = _currentTheme.asStateFlow()
    private val _fontStyle = MutableStateFlow(readEnum("font_style", ZahraFontStyle.SYSTEM_SANS))
    val fontStyle: StateFlow<ZahraFontStyle> = _fontStyle.asStateFlow()
    private val _learnerName = MutableStateFlow(prefs.getString("learner_name", "زهرا") ?: "زهرا")
    val learnerName: StateFlow<String> = _learnerName.asStateFlow()
    private val _currentLocale = MutableStateFlow(
        AcademyLocale.entries.firstOrNull { it.languageTag == prefs.getString("locale_tag", "fa-AF") } ?: AcademyLocale.DARI_AF
    )
    val currentLocale: StateFlow<AcademyLocale> = _currentLocale.asStateFlow()
    private val _totalScore = MutableStateFlow(prefs.getInt("total_score_xp", 0))
    val totalScore: StateFlow<Int> = _totalScore.asStateFlow()
    private val _recentReward = MutableStateFlow<ScoreReward?>(null)
    val recentReward: StateFlow<ScoreReward?> = _recentReward.asStateFlow()
    private val _learnerAvatar = MutableStateFlow(prefs.getString("learner_avatar", "🌸") ?: "🌸")
    val learnerAvatar: StateFlow<String> = _learnerAvatar.asStateFlow()

    private val _diaryNotes = MutableStateFlow(loadDiaryNotes())
    val diaryNotes: StateFlow<List<DiaryNote>> = _diaryNotes.asStateFlow()
    private val _wordStages = MutableStateFlow(
        CurriculumData.wordGameStages.map { stage ->
            stage.copy(isCompleted = prefs.getBoolean("word_stage_${stage.stageNumber}_completed", false))
        }
    )
    val wordStages: StateFlow<List<GameStage>> = _wordStages.asStateFlow()
    private val _medals = MutableStateFlow(CurriculumData.goldenMedals.map { medal ->
        medal.copy(isUnlocked = prefs.getBoolean("medal_${medal.id}_unlocked", false))
    })
    val medals: StateFlow<List<GoldenMedal>> = _medals.asStateFlow()

    init {
        if (savedStudyDate != todayKey) {
            prefs.edit().putString("study_date", todayKey).putInt("today_study_minutes", 0).apply()
        }
        checkMedalsUpdate()
    }

    private inline fun <reified T : Enum<T>> readEnum(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(prefs.getString(key, fallback.name) ?: fallback.name) }.getOrDefault(fallback)

    fun updateLearnerName(newName: String) {
        val clean = newName.trim().take(32).ifBlank { "زهرا" }
        _learnerName.value = clean
        prefs.edit().putString("learner_name", clean).apply()
    }

    fun setLocale(locale: AcademyLocale) {
        _currentLocale.value = locale
        prefs.edit().putString("locale_tag", locale.languageTag).apply()
    }

    fun setAvatar(avatar: String) {
        _learnerAvatar.value = avatar
        prefs.edit().putString("learner_avatar", avatar).apply()
    }

    fun setTheme(theme: ZahraThemeMode) {
        _currentTheme.value = theme
        prefs.edit().putString("current_theme", theme.name).apply()
    }

    fun setFontStyle(style: ZahraFontStyle) {
        _fontStyle.value = style
        prefs.edit().putString("font_style", style.name).apply()
    }

    fun setDailyGoal(minutes: Int) {
        val safe = minutes.coerceIn(5, 600)
        _dailyGoalMinutes.value = safe
        prefs.edit().putInt("daily_goal_minutes", safe).apply()
    }

    fun addStudyMinutes(minutes: Int) {
        if (minutes <= 0) return
        val today = _todayStudyMinutes.value + minutes
        val total = prefs.getLong("total_study_minutes", 0L) + minutes
        _todayStudyMinutes.value = today
        _totalStudyHours.value = (total / 60L).toInt()
        prefs.edit()
            .putString("study_date", todayKey)
            .putInt("today_study_minutes", today)
            .putLong("total_study_minutes", total)
            .apply()
    }

    fun addScore(points: Int, reason: String) {
        if (points <= 0) return
        val total = (_totalScore.value + points).coerceAtMost(Int.MAX_VALUE)
        _totalScore.value = total
        prefs.edit().putInt("total_score_xp", total).apply()
        _recentReward.value = ScoreReward(points, reason)
    }

    fun clearRecentReward() { _recentReward.value = null }

    fun addGoldCoins(amount: Int) {
        if (amount <= 0) return
        val total = (_goldCoins.value + amount).coerceAtMost(Int.MAX_VALUE)
        _goldCoins.value = total
        prefs.edit().putInt("gold_coins", total).apply()
    }

    fun addStars(amount: Int) {
        if (amount <= 0) return
        val total = (_totalStars.value + amount).coerceAtMost(Int.MAX_VALUE)
        _totalStars.value = total
        prefs.edit().putInt("total_stars", total).apply()
        checkMedalsUpdate()
    }

    fun getLearnerRank(score: Int): Pair<String, String> = when {
        score >= 2500 -> "استاد درخشان 👑" to "تسلط پیشرفته"
        score >= 1500 -> "ستاره زبان 🌟" to "پیشرفت پیوسته"
        score >= 800 -> "قهرمان یادگیری ✨" to "گسترش واژگان و گرامر"
        score >= 400 -> "پژوهشگر پرتلاش 🌸" to "پایه‌های محکم"
        else -> "نوآموز پرتلاش 💖" to "آغاز مسیر یادگیری"
    }

    fun submitExamResult(levelId: Int, scorePercent: Int): Int {
        val score = scorePercent.coerceIn(0, 100)
        val stars = when { score >= 85 -> 3; score >= 70 -> 2; score >= 50 -> 1; else -> 0 }
        val passed = score >= 60
        val old = _levelsState.value.firstOrNull { it.id == levelId } ?: return 0
        val firstPass = passed && !old.isMastered
        val updated = _levelsState.value.map { level ->
            when {
                level.id == levelId -> {
                    val best = maxOf(level.bestScore, score)
                    val earned = maxOf(level.starsEarned, stars)
                    prefs.edit().putInt("level_${level.id}_best", best)
                        .putInt("level_${level.id}_stars", earned)
                        .putBoolean("level_${level.id}_mastered", level.isMastered || passed).apply()
                    level.copy(bestScore = best, starsEarned = earned, isMastered = level.isMastered || passed)
                }
                level.id == levelId + 1 && passed -> {
                    prefs.edit().putBoolean("level_${level.id}_unlocked", true).apply()
                    level.copy(isUnlocked = true)
                }
                else -> level
            }
        }
        _levelsState.value = updated
        if (firstPass) {
            addGoldCoins(stars * 30)
            addStars(stars)
            addScore(70 + stars * 20, "تکمیل آزمون سطح $levelId")
        }
        return stars
    }

    fun recordSpeakingPractice(scorePercent: Int) {
        val score = scorePercent.coerceIn(0, 100)
        addScore(if (score >= 85) 40 else if (score >= 60) 25 else 10, "تمرین تلفظ")
        addGoldCoins(10)
    }

    fun addDiaryNote(note: DiaryNote) {
        if (_diaryNotes.value.any { it.id == note.id }) return
        val updated = listOf(note) + _diaryNotes.value
        _diaryNotes.value = updated
        saveDiaryNotes(updated)
    }

    fun completeWordStage(stageNumber: Int) {
        val stage = _wordStages.value.firstOrNull { it.stageNumber == stageNumber } ?: return
        if (stage.isCompleted) return
        _wordStages.value = _wordStages.value.map {
            if (it.stageNumber == stageNumber) it.copy(isCompleted = true) else it
        }
        prefs.edit().putBoolean("word_stage_${stageNumber}_completed", true).apply()
        addGoldCoins(stage.bonusGoldReward)
        addStars(1)
        addScore(30, "تکمیل مرحله بازی کلمات")
    }

    private fun checkMedalsUpdate() {
        val stars = _totalStars.value
        _medals.value = _medals.value.map { medal ->
            val unlocked = stars >= medal.requiredStars && medal.requiredStars > 0
            if (unlocked && !medal.isUnlocked) prefs.edit().putBoolean("medal_${medal.id}_unlocked", true).apply()
            medal.copy(isUnlocked = medal.isUnlocked || unlocked)
        }
    }

    private fun saveDiaryNotes(notes: List<DiaryNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id", note.id); put("dateIso", note.dateIso); put("dateDisplayFa", note.dateDisplayFa)
                put("moodEmoji", note.moodEmoji); put("moodTitleFa", note.moodTitleFa)
                put("titleFa", note.titleFa); put("content", note.content)
                put("english", note.englishSentenceLearned); put("color", note.colorTagHex)
                put("stickers", JSONArray(note.attachedStickers))
            })
        }
        prefs.edit().putString("saved_diary_json", array.toString()).apply()
    }

    private fun loadDiaryNotes(): List<DiaryNote> {
        val raw = prefs.getString("saved_diary_json", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val stickers = obj.optJSONArray("stickers")
                DiaryNote(
                    id = obj.getString("id"), dateIso = obj.getString("dateIso"),
                    dateDisplayFa = obj.optString("dateDisplayFa"), moodEmoji = obj.optString("moodEmoji", "🌸"),
                    moodTitleFa = obj.optString("moodTitleFa"), titleFa = obj.optString("titleFa"),
                    content = obj.optString("content"), englishSentenceLearned = obj.optString("english"),
                    attachedStickers = if (stickers == null) emptyList() else (0 until stickers.length()).map { stickers.getString(it) },
                    colorTagHex = obj.optLong("color", 0xFFFF85A1)
                )
            }
        }.getOrDefault(emptyList())
    }
}
