package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ChatMessage
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
import java.util.UUID
import java.util.Calendar
import java.util.Locale

data class ScoreReward(
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

class UserProgressManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("zahra_self_dev_prefs", Context.MODE_PRIVATE)

    private val _levelsState = MutableStateFlow<List<Level>>(CurriculumData.levels)
    val levelsState: StateFlow<List<Level>> = _levelsState.asStateFlow()

    private val _goldCoins = MutableStateFlow(prefs.getInt("gold_coins", 180))
    val goldCoins: StateFlow<Int> = _goldCoins.asStateFlow()

    private val _totalStars = MutableStateFlow(prefs.getInt("total_stars", 8))
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    private val _streakDays = MutableStateFlow(prefs.getInt("streak_days", 7))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _todayStudySeconds = MutableStateFlow(prefs.getInt("today_study_seconds", 0))
    val todayStudySeconds: StateFlow<Int> = _todayStudySeconds.asStateFlow()

    private val _todayStudyMinutes = MutableStateFlow(_todayStudySeconds.value / 60)
    val todayStudyMinutes: StateFlow<Int> = _todayStudyMinutes.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 30).coerceIn(5, 600))
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

    private val _learnerGender = MutableStateFlow(prefs.getString("learner_gender", "female") ?: "female")
    val learnerGender: StateFlow<String> = _learnerGender.asStateFlow()

    private val _learnerBio = MutableStateFlow(prefs.getString("learner_bio", "در مسیر یادگیری و رشد هر روز بهتر می‌شوم ✨") ?: "در مسیر یادگیری و رشد هر روز بهتر می‌شوم ✨")
    val learnerBio: StateFlow<String> = _learnerBio.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(loadSavedMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _diaryNotes = MutableStateFlow<List<DiaryNote>>(loadSavedDiaryNotes())
    val diaryNotes: StateFlow<List<DiaryNote>> = _diaryNotes.asStateFlow()

    private val _wordStages = MutableStateFlow<List<GameStage>>(
        CurriculumData.wordGameStages.map { stage ->
            stage.copy(isCompleted = prefs.getBoolean("word_stage_${stage.stageNumber}_completed", false))
        }
    )
    val wordStages: StateFlow<List<GameStage>> = _wordStages.asStateFlow()

    private val _medals = MutableStateFlow<List<GoldenMedal>>(CurriculumData.goldenMedals)
    val medals: StateFlow<List<GoldenMedal>> = _medals.asStateFlow()

    init {
        // Keep daily counters accurate across app restarts and calendar-day changes.
        refreshDailyCounters()

        // Personalize the entire curriculum and achievement copy for the active learner.
        _levelsState.value = CurriculumData.levels.map { personalizeLevel(it, _learnerName.value) }
        _medals.value = CurriculumData.goldenMedals.map { medal ->
            medal.copy(
                titleFa = personalizeText(medal.titleFa, _learnerName.value),
                descriptionFa = personalizeText(medal.descriptionFa, _learnerName.value)
            )
        }
        _diaryNotes.value = _diaryNotes.value.map { note ->
            note.copy(
                titleFa = personalizeText(note.titleFa, _learnerName.value),
                content = personalizeText(note.content, _learnerName.value),
                englishSentenceLearned = personalizeText(note.englishSentenceLearned, _learnerName.value)
            )
        }

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

            val xpEarned = 70 + (stars * 20)
            addScore(xpEarned, "قبولی در آزمون سطح $levelId")
        }

        checkMedalsUpdate()
        return stars
    }

    fun updateLearnerName(newName: String) {
        val trimmed = newName.trim().take(40).ifBlank { "کاربر جدید" }
        _learnerName.value = trimmed
        prefs.edit().putString("learner_name", trimmed).apply()

        // Refresh every name-dependent lesson, example, quiz and level title immediately.
        val savedProgress = _levelsState.value.associateBy { it.id }
        _levelsState.value = CurriculumData.levels.map { baseLevel ->
            val saved = savedProgress[baseLevel.id]
            personalizeLevel(
                baseLevel.copy(
                    isUnlocked = saved?.isUnlocked ?: baseLevel.isUnlocked,
                    bestScore = saved?.bestScore ?: baseLevel.bestScore,
                    starsEarned = saved?.starsEarned ?: baseLevel.starsEarned,
                    isMastered = saved?.isMastered ?: baseLevel.isMastered
                ),
                trimmed
            )
        }
        _medals.value = CurriculumData.goldenMedals.map { medal ->
            medal.copy(
                titleFa = personalizeText(medal.titleFa, trimmed),
                descriptionFa = personalizeText(medal.descriptionFa, trimmed),
                isUnlocked = _medals.value.firstOrNull { it.id == medal.id }?.isUnlocked ?: medal.isUnlocked
            )
        }
        _diaryNotes.value = _diaryNotes.value.map { note ->
            note.copy(
                titleFa = personalizeText(note.titleFa, trimmed),
                content = personalizeText(note.content, trimmed),
                englishSentenceLearned = personalizeText(note.englishSentenceLearned, trimmed)
            )
        }
        saveDiaryNotes(_diaryNotes.value)
    }

    private fun personalizeText(source: String, name: String): String {
        return source
            .replace("Zahra", name, ignoreCase = true)
            .replace("زهرای عزیزم", "$name عزیزم")
            .replace("زهرای عزیز", "$name عزیز")
            .replace("زهرای من", "$name عزیز")
            .replace("زهرای", name)
            .replace("زهرا", name)
    }

    private fun personalizeLevel(level: Level, name: String): Level {
        return level.copy(
            titleFa = personalizeText(level.titleFa, name),
            titleEn = personalizeText(level.titleEn, name),
            descriptionFa = personalizeText(level.descriptionFa, name),
            items = level.items.map { item ->
                item.copy(
                    englishText = personalizeText(item.englishText, name),
                    // The original phonetic transcription was specific to "Zahra";
                    // keep the learner-name slot out of IPA rather than showing a false pronunciation.
                    phonetic = if (item.id == "a1_1") "/həˈloʊ aɪ æm/" else item.phonetic,
                    persianTranslation = personalizeText(item.persianTranslation, name),
                    exampleSentenceEn = personalizeText(item.exampleSentenceEn, name),
                    exampleSentenceFa = personalizeText(item.exampleSentenceFa, name),
                    grammarRuleFa = item.grammarRuleFa?.let { personalizeText(it, name) }
                )
            },
            examQuestions = level.examQuestions.map { question ->
                question.copy(
                    promptEn = personalizeText(question.promptEn, name),
                    promptFa = personalizeText(question.promptFa, name),
                    options = question.options.map { personalizeText(it, name) },
                    explanationFa = personalizeText(question.explanationFa, name),
                    targetSpeechText = question.targetSpeechText?.let { personalizeText(it, name) }
                )
            }
        )
    }

    fun setLearnerGender(gender: String) {
        val normalized = if (gender == "male") "male" else "female"
        _learnerGender.value = normalized
        val defaultAvatar = if (normalized == "male") "🧑🏻‍🎓" else "👸🏻"
        _learnerAvatar.value = defaultAvatar
        prefs.edit().putString("learner_gender", normalized).putString("learner_avatar", defaultAvatar).apply()
    }

    fun setAvatar(avatar: String) {
        _learnerAvatar.value = avatar
        prefs.edit().putString("learner_avatar", avatar).apply()
    }

    fun updateLearnerBio(bio: String) {
        val trimmed = bio.trim().take(120)
        _learnerBio.value = trimmed
        prefs.edit().putString("learner_bio", trimmed).apply()
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
            score >= 2500 -> "استاد افسانه‌ای Z9 👑" to "نهایت تسلط و پیشرفت"
            score >= 1500 -> "استاد سخنوری ✨" to "تسلط بر مکالمات پیشرفته"
            score >= 800 -> "ستاره درخشان زبان 🌟" to "گسترش دایره لغات و گرامر"
            score >= 400 -> "قهرمان خودسازی 🌸" to "پیشرفت چشمگیر در اصطلاحات"
            else -> "یادگیرنده پرتلاش 💖" to "شروع باانگیزه از پایه"
        }
    }

    fun recordSpeakingPractice(scorePercent: Int) {
        val points = when {
            scorePercent >= 85 -> 40
            scorePercent >= 60 -> 25
            else -> 10
        }
        addScore(points, "تمرین گویش صوتی ($scorePercent%)")

        addGoldCoins(10)
    }

    /**
     * Adds only measured foreground learning time supplied by the monotonic session timer.
     */
    fun recordStudySeconds(seconds: Int) {
        if (seconds <= 0) return
        refreshDailyCounters()

        val newTodaySeconds = _todayStudySeconds.value + seconds
        _todayStudySeconds.value = newTodaySeconds
        _todayStudyMinutes.value = newTodaySeconds / 60

        val previousTotalSeconds = prefs.getInt(
            "total_study_seconds_v2",
            prefs.getInt("total_study_minutes_v2", _totalStudyHours.value * 60) * 60
        )
        val newTotalSeconds = previousTotalSeconds + seconds
        val newTotalMinutes = newTotalSeconds / 60
        val newTotalHours = newTotalMinutes / 60
        _totalStudyHours.value = newTotalHours

        prefs.edit()
            .putInt("today_study_seconds", newTodaySeconds)
            .putInt("today_study_minutes", _todayStudyMinutes.value)
            .putInt("total_study_seconds_v2", newTotalSeconds)
            .putInt("total_study_minutes_v2", newTotalMinutes)
            .putInt("total_study_hours", newTotalHours)
            .apply()

        updateStudyStreak()
    }

    private fun refreshDailyCounters() {
        val todayCalendar = Calendar.getInstance()
        val today = dateKey(todayCalendar)
        val yesterdayCalendar = (todayCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val storedDate = prefs.getString("study_day", null)

        // Remove the former demo/default daily value once; all future progress is timed.
        if (!prefs.getBoolean("real_study_tracking_v1", false)) {
            _todayStudySeconds.value = 0
            _todayStudyMinutes.value = 0
            prefs.edit()
                .putInt("today_study_seconds", 0)
                .putInt("today_study_minutes", 0)
                .putString("study_day", today)
                .putBoolean("real_study_tracking_v1", true)
                .apply()
            return
        }

        if (storedDate == null) {
            _todayStudySeconds.value = 0
            _todayStudyMinutes.value = 0
            prefs.edit()
                .putInt("today_study_seconds", 0)
                .putInt("today_study_minutes", 0)
                .putString("study_day", today)
                .apply()
            return
        }
        if (storedDate == today) return

        val keepStreak = storedDate == dateKey(yesterdayCalendar)
        _todayStudySeconds.value = 0
        _todayStudyMinutes.value = 0
        if (!keepStreak) _streakDays.value = 0

        prefs.edit()
            .putInt("today_study_seconds", 0)
            .putInt("today_study_minutes", 0)
            .putInt("streak_days", _streakDays.value)
            .putString("study_day", today)
            .apply()
    }

    private fun dateKey(calendar: Calendar): String =
        String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

    private fun updateStudyStreak() {
        val todayCalendar = Calendar.getInstance()
        val todayKey = dateKey(todayCalendar)
        val yesterdayCalendar = (todayCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastActivity = prefs.getString("last_study_activity_day", null)
        if (lastActivity == todayKey) return

        val nextStreak = if (lastActivity == dateKey(yesterdayCalendar)) {
            _streakDays.value + 1
        } else if (lastActivity == null && prefs.getString("study_day", null) == todayKey) {
            maxOf(1, _streakDays.value)
        } else {
            1
        }

        _streakDays.value = nextStreak
        prefs.edit()
            .putInt("streak_days", nextStreak)
            .putString("last_study_activity_day", todayKey)
            .apply()
    }

    fun setDailyGoal(minutes: Int) {
        val safeMinutes = minutes.coerceIn(5, 600)
        _dailyGoalMinutes.value = safeMinutes
        prefs.edit().putInt("daily_goal_minutes", safeMinutes).apply()
    }

    fun spendGoldCoin(amount: Int = 1): Boolean {
        val cost = amount.coerceAtLeast(1)
        val current = _goldCoins.value
        // Never deduct partially: the full hint price must be available.
        if (current < cost) return false
        val updated = current - cost
        _goldCoins.value = updated
        prefs.edit().putInt("gold_coins", updated).apply()
        return true
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

    fun addCustomChatMessage(message: ChatMessage) {
        val currentList = _chatMessages.value.toMutableList().apply { add(message) }
        _chatMessages.value = currentList.toList()
        saveMessages(currentList)
    }

    fun sendUserMessage(englishOrFa: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromTutor = false,
            englishText = englishOrFa,
            persianTranslation = null
        )
        val currentList = _chatMessages.value.toMutableList().apply { add(userMsg) }
        _chatMessages.value = currentList

        // Online-only chat: persist the learner message; only OnlineAITutorService may add tutor replies.
        _chatMessages.value = currentList.toList()
        saveMessages(currentList)
    }

    fun addDiaryNote(note: DiaryNote) {
        val updated = _diaryNotes.value.toMutableList().apply { add(0, note) }
        _diaryNotes.value = updated
        saveDiaryNotes(updated)
        addGoldCoins(10)

        addScore(20, "ثبت خاطره در تقویم روزانه")
    }

    fun completeWordStage(stageNumber: Int) {
        val stage = _wordStages.value.firstOrNull { it.stageNumber == stageNumber } ?: return
        // Award rewards only once; completed stages survive app restarts.
        if (stage.isCompleted) return

        _wordStages.value = _wordStages.value.map {
            if (it.stageNumber == stageNumber) it.copy(isCompleted = true) else it
        }
        prefs.edit().putBoolean("word_stage_${stageNumber}_completed", true).apply()

        addGoldCoins(25)
        addStars(1)

        addScore(30, "فتح مرحله بازی کلمات")
    }

    private fun saveMessages(messages: List<ChatMessage>) {
        val arr = JSONArray()
        val trimmed = if (messages.size > 40) messages.takeLast(40) else messages
        for (m in trimmed) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("isTutor", m.isFromTutor)
                put("en", m.englishText)
                put("fa", m.persianTranslation ?: "")
                put("tip", m.grammarTipFa ?: "")
                put("weak", m.weaknessFlagged ?: "")
            }
            arr.put(obj)
        }
        prefs.edit().putString("saved_chat_json", arr.toString()).apply()
    }

    private fun loadSavedMessages(): List<ChatMessage> {
        val jsonStr = prefs.getString("saved_chat_json", null)
        if (jsonStr.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        isFromTutor = obj.getBoolean("isTutor"),
                        englishText = obj.getString("en"),
                        persianTranslation = obj.optString("fa").ifEmpty { null },
                        grammarTipFa = obj.optString("tip").ifEmpty { null },
                        weaknessFlagged = obj.optString("weak").ifEmpty { null }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
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
                    titleFa = "آغاز رسمی مسیر خودسازی",
                    content = "امروز رسماً آکادمی اختصاصی BH (BH Exclusive Academy) را با شعار «از صفر شروع کن، تا تسلط کامل ادامه بده!» آغاز کردم.",
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
