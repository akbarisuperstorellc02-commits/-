package com.example.data

import android.content.Context
import android.content.SharedPreferences
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

internal data class StudyTimeProgress(
    val totalHours: Int,
    val remainingMinutes: Int
)

internal fun calculateStudyTimeProgress(
    existingHours: Int,
    existingRemainderMinutes: Int,
    addedMinutes: Int
): StudyTimeProgress {
    val safeHours = existingHours.coerceAtLeast(0)
    val safeRemainder = existingRemainderMinutes.coerceIn(0, 59)
    val safeAdded = addedMinutes.coerceAtLeast(0)
    val accumulatedMinutes = safeRemainder + safeAdded
    return StudyTimeProgress(
        totalHours = safeHours + accumulatedMinutes / 60,
        remainingMinutes = accumulatedMinutes % 60
    )
}

data class ScoreReward(
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

class UserProgressManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("zahra_self_dev_prefs", Context.MODE_PRIVATE)

    private val _levelsState = MutableStateFlow<List<Level>>(expandLearningCurriculum(CurriculumData.levels))
    val levelsState: StateFlow<List<Level>> = _levelsState.asStateFlow()

    private val _goldCoins = MutableStateFlow(prefs.getInt("gold_coins", 180))
    val goldCoins: StateFlow<Int> = _goldCoins.asStateFlow()

    private val _totalStars = MutableStateFlow(prefs.getInt("total_stars", 8))
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    private val _streakDays = MutableStateFlow(prefs.getInt("streak_days", 7))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _todayStudyMinutes = MutableStateFlow(prefs.getInt("today_study_minutes", 25))
    val todayStudyMinutes: StateFlow<Int> = _todayStudyMinutes.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 30))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _totalStudyHours = MutableStateFlow(prefs.getInt("total_study_hours", 14))
    val totalStudyHours: StateFlow<Int> = _totalStudyHours.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        try {
            ZahraThemeMode.valueOf(prefs.getString("current_theme", ZahraThemeMode.SWEET_SAKURA_PUFFY.name) ?: ZahraThemeMode.SWEET_SAKURA_PUFFY.name)
        } catch (e: Exception) {
            ZahraThemeMode.SWEET_SAKURA_PUFFY
        }
    )
    val currentTheme: StateFlow<ZahraThemeMode> = _currentTheme.asStateFlow()

    private val _fontStyle = MutableStateFlow(
        try {
            ZahraFontStyle.valueOf(prefs.getString("font_style", ZahraFontStyle.FANTASY_ROUND.name) ?: ZahraFontStyle.FANTASY_ROUND.name)
        } catch (e: Exception) {
            ZahraFontStyle.FANTASY_ROUND
        }
    )
    val fontStyle: StateFlow<ZahraFontStyle> = _fontStyle.asStateFlow()

    private val _learnerName = MutableStateFlow(prefs.getString("learner_name", "زهرا") ?: "زهرا")
    val learnerName: StateFlow<String> = _learnerName.asStateFlow()

    private val _totalScore = MutableStateFlow(prefs.getInt("total_score_xp", 280))
    val totalScore: StateFlow<Int> = _totalScore.asStateFlow()

    private val _recentReward = MutableStateFlow<ScoreReward?>(null)
    val recentReward: StateFlow<ScoreReward?> = _recentReward.asStateFlow()

    private val _learnerAvatar = MutableStateFlow(prefs.getString("learner_avatar", "👑") ?: "👑")
    val learnerAvatar: StateFlow<String> = _learnerAvatar.asStateFlow()


    private val _diaryNotes = MutableStateFlow<List<DiaryNote>>(loadSavedDiaryNotes())
    val diaryNotes: StateFlow<List<DiaryNote>> = _diaryNotes.asStateFlow()

    private val _wordStages = MutableStateFlow<List<GameStage>>(CurriculumData.wordGameStages)
    val wordStages: StateFlow<List<GameStage>> = _wordStages.asStateFlow()

    private val _medals = MutableStateFlow<List<GoldenMedal>>(CurriculumData.goldenMedals)
    val medals: StateFlow<List<GoldenMedal>> = _medals.asStateFlow()

    init {
        // Load level unlocks from preferences
        val currentLevels = _levelsState.value.mapIndexed { index, level ->
            val unlocked = prefs.getBoolean("level_${level.id}_unlocked", index == 0)
            val best = prefs.getInt("level_${level.id}_best", 0)
            val stars = prefs.getInt("level_${level.id}_stars", 0)
            val mastered = prefs.getBoolean("level_${level.id}_mastered", false)
            level.copy(isUnlocked = unlocked, bestScore = best, starsEarned = stars, isMastered = mastered)
        }
        _levelsState.value = currentLevels
        checkMedalsUpdate()
    }

    fun submitExamResult(levelId: Int, scorePercent: Int): Int {
        val stars = when {
            scorePercent >= 85 -> 3
            scorePercent >= 70 -> 2
            scorePercent >= 50 -> 1
            else -> 0
        }

        val isPassed = scorePercent >= 60

        val updatedLevels = _levelsState.value.map { level ->
            if (level.id == levelId) {
                val newBest = maxOf(level.bestScore, scorePercent)
                val newStars = maxOf(level.starsEarned, stars)
                prefs.edit()
                    .putInt("level_${level.id}_best", newBest)
                    .putInt("level_${level.id}_stars", newStars)
                    .putBoolean("level_${level.id}_mastered", isPassed)
                    .apply()
                level.copy(bestScore = newBest, starsEarned = newStars, isMastered = isPassed)
            } else if (level.id == levelId + 1 && isPassed) {
                // Unlock next level ONLY when real mastery is achieved!
                prefs.edit().putBoolean("level_${level.id}_unlocked", true).apply()
                level.copy(isUnlocked = true)
            } else {
                level
            }
        }
        _levelsState.value = updatedLevels

        if (isPassed) {
            val bonusGold = stars * 30
            addGoldCoins(bonusGold)
            addStars(stars)
            addStudyMinutes(15)
            val xpEarned = 70 + (stars * 20)
            addScore(xpEarned, "قبولی در آزمون سطح $levelId")
        }

        checkMedalsUpdate()
        return stars
    }

    fun updateLearnerName(newName: String) {
        val trimmed = newName.trim().ifBlank { "زهرا" }
        _learnerName.value = trimmed
        prefs.edit().putString("learner_name", trimmed).apply()
    }

    fun addScore(points: Int, reason: String) {
        val newScore = _totalScore.value + points
        _totalScore.value = newScore
        prefs.edit().putInt("total_score_xp", newScore).apply()
        _recentReward.value = ScoreReward(points, reason)
    }

    fun clearRecentReward() {
        _recentReward.value = null
    }

    fun getLearnerRank(score: Int): Pair<String, String> {
        return when {
            score >= 2500 -> "پرنسس افسانه‌ای Z9 👑" to "نهایت تسلط طلایی و دیپلماتیک"
            score >= 1500 -> "استاد سخنوری دخترانه ✨" to "تسلط بر مکالمات پیشرفته"
            score >= 800 -> "ستاره درخشان زبان 🌟" to "گسترش دایره لغات و گرامر"
            score >= 400 -> "قهرمان خودسازی 🌸" to "پیشرفت چشمگیر در اصطلاحات"
            else -> "پرنسس نوآموز 💖" to "شروع باانگیزه از صفر مطلق"
        }
    }

    fun recordSpeakingPractice(scorePercent: Int) {
        val points = when {
            scorePercent >= 85 -> 40
            scorePercent >= 60 -> 25
            else -> 10
        }
        addScore(points, "تمرین گویش صوتی ($scorePercent%)")
        addStudyMinutes(3)
        addGoldCoins(10)
    }

    fun addStudyMinutes(minutes: Int) {
        if (minutes <= 0) return

        val newToday = _todayStudyMinutes.value + minutes
        _todayStudyMinutes.value = newToday

        val progress = calculateStudyTimeProgress(
            existingHours = _totalStudyHours.value,
            existingRemainderMinutes = prefs.getInt("total_study_minutes_remainder", 0),
            addedMinutes = minutes
        )
        _totalStudyHours.value = progress.totalHours

        prefs.edit()
            .putInt("today_study_minutes", newToday)
            .putInt("total_study_hours", progress.totalHours)
            .putInt("total_study_minutes_remainder", progress.remainingMinutes)
            .apply()
    }

    fun setDailyGoal(minutes: Int) {
        _dailyGoalMinutes.value = minutes
        prefs.edit().putInt("daily_goal_minutes", minutes).apply()
    }

    fun addGoldCoins(amount: Int) {
        val newGold = _goldCoins.value + amount
        _goldCoins.value = newGold
        prefs.edit().putInt("gold_coins", newGold).apply()
    }

    fun addStars(amount: Int) {
        val newStars = _totalStars.value + amount
        _totalStars.value = newStars
        prefs.edit().putInt("total_stars", newStars).apply()
        checkMedalsUpdate()
    }

    private fun checkMedalsUpdate() {
        val stars = _totalStars.value
        val updated = _medals.value.map { medal ->
            if (stars >= medal.requiredStars) medal.copy(isUnlocked = true) else medal
        }
        _medals.value = updated
    }

    fun setTheme(theme: ZahraThemeMode) {
        _currentTheme.value = theme
        prefs.edit().putString("current_theme", theme.name).apply()
    }

    fun setFontStyle(style: ZahraFontStyle) {
        _fontStyle.value = style
        prefs.edit().putString("font_style", style.name).apply()
    }

    fun setAvatar(avatar: String) {
        _learnerAvatar.value = avatar
        prefs.edit().putString("learner_avatar", avatar).apply()
    }

    fun addDiaryNote(note: DiaryNote) {
        val updated = _diaryNotes.value.toMutableList().apply { add(0, note) }
        _diaryNotes.value = updated
        saveDiaryNotes(updated)
        addGoldCoins(10)
        addStudyMinutes(5)
        addScore(20, "ثبت خاطره در تقویم روزانه")
    }

    fun completeWordStage(stageNumber: Int) {
        val updated = _wordStages.value.map {
            if (it.stageNumber == stageNumber) it.copy(isCompleted = true) else it
        }
        _wordStages.value = updated
        addGoldCoins(25)
        addStars(1)
        addStudyMinutes(5)
        addScore(30, "فتح مرحله بازی کلمات")
    }

    private fun saveDiaryNotes(notes: List<DiaryNote>) {
        val arr = JSONArray()
        for (n in notes) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("dateIso", n.dateIso)
                put("dateDisplayFa", n.dateDisplayFa)
                put("moodEmoji", n.moodEmoji)
                put("moodTitleFa", n.moodTitleFa)
                put("titleFa", n.titleFa)
                put("content", n.content)
                put("english", n.englishSentenceLearned)
                put("color", n.colorTagHex)
                put("stickers", JSONArray(n.attachedStickers))
            }
            arr.put(obj)
        }
        prefs.edit().putString("saved_diary_json", arr.toString()).apply()
    }

    private fun loadSavedDiaryNotes(): List<DiaryNote> {
        val jsonStr = prefs.getString("saved_diary_json", null)
        if (jsonStr.isNullOrBlank()) {
            return listOf(
                DiaryNote(
                    id = "init_zahra_1",
                    dateIso = "2026-11-06",
                    dateDisplayFa = "۱۶ آبان - جمعه طلایی",
                    moodEmoji = "👑",
                    moodTitleFa = "باانگیزه و مصمم",
                    titleFa = "آغاز رسمی مسیر خودسازی زهرا",
                    content = "امروز رسماً آکادمی زبان انگلیسی زهرا (Zahra English Academy) را با شعار «از صفر شروع کن، تا تسلط کامل ادامه بده!» آغاز کردم.",
                    englishSentenceLearned = "Start from zero, continue until absolute mastery! 👑",
                    attachedStickers = listOf("👑", "🥇", "✨"),
                    colorTagHex = 0xFFD4AF37
                )
            )
        }
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<DiaryNote>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val stickArr = obj.optJSONArray("stickers")
                val sticks = mutableListOf<String>()
                if (stickArr != null) {
                    for (j in 0 until stickArr.length()) sticks.add(stickArr.getString(j))
                }
                list.add(
                    DiaryNote(
                        id = obj.getString("id"),
                        dateIso = obj.getString("dateIso"),
                        dateDisplayFa = obj.getString("dateDisplayFa"),
                        moodEmoji = obj.getString("moodEmoji"),
                        moodTitleFa = obj.getString("moodTitleFa"),
                        titleFa = obj.getString("titleFa"),
                        content = obj.getString("content"),
                        englishSentenceLearned = obj.optString("english", ""),
                        attachedStickers = sticks,
                        colorTagHex = obj.optLong("color", 0xFFD4AF37)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
