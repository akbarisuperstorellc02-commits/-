package com.bh.personalacademy.data.backup

import android.content.Context
import android.net.Uri
import com.bh.personalacademy.data.local.*
import com.bh.personalacademy.data.preferences.AcademyPreferences
import com.bh.personalacademy.reminders.ReminderScheduler
import com.example.academy.domain.model.AcademyFontId
import com.example.academy.domain.model.AcademyLocale
import com.example.academy.domain.model.AcademyThemeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Versioned, offline JSON backup. Restore merges rows by their stable primary keys,
 * so importing a backup does not silently erase newer local records.
 */
object AcademyBackupManager {
    private const val FORMAT = "BH_PERSONAL_ACADEMY"
    private const val VERSION = 1

    suspend fun export(context: Context, uri: Uri, preferences: AcademyPreferences, dao: AcademyDao, profileId: String = "primary") =
        withContext(Dispatchers.IO) {
            val profile = preferences.profile.first()
            val root = JSONObject()
                .put("format", FORMAT)
                .put("schemaVersion", VERSION)
                .put("createdAt", System.currentTimeMillis())
                .put("profile", JSONObject()
                    .put("displayName", profile.displayName)
                    .put("username", profile.username)
                    .put("avatarUri", profile.avatarUri ?: JSONObject.NULL)
                    .put("locale", profile.locale.name)
                    .put("theme", profile.theme.name)
                    .put("font", profile.font.name)
                    .put("dailyGoalMinutes", profile.dailyStudyGoalMinutes)
                    .put("dailyGoalLessons", profile.dailyGoalLessons)
                    .put("dailyGoalWords", profile.dailyGoalWords)
                    .put("dailyGoalQuizzes", profile.dailyGoalQuizzes))

            root.put("lessonProgress", JSONArray().apply {
                dao.observeLessonProgress(profileId).first().forEach { put(JSONObject()
                    .put("profileId", it.profileId).put("lessonId", it.lessonId)
                    .put("completedAt", it.completedAt ?: JSONObject.NULL)
                    .put("bestScore", it.bestScore).put("attempts", it.attempts)) }
            })
            root.put("quizAttempts", JSONArray().apply {
                dao.observeQuizAttempts(profileId).first().forEach { put(JSONObject()
                    .put("attemptId", it.attemptId).put("profileId", it.profileId).put("quizId", it.quizId)
                    .put("score", it.score).put("totalQuestions", it.totalQuestions).put("completedAt", it.completedAt)
                    .put("incorrectQuestionIds", it.incorrectQuestionIds)) }
            })
            root.put("gameAttempts", JSONArray().apply {
                dao.observeGameAttempts(profileId).first().forEach { put(JSONObject()
                    .put("attemptId", it.attemptId).put("profileId", it.profileId).put("gameId", it.gameId)
                    .put("score", it.score).put("totalQuestions", it.totalQuestions).put("completedAt", it.completedAt)) }
            })
            root.put("studySessions", JSONArray().apply {
                dao.observeStudySessions(profileId).first().forEach { put(JSONObject()
                    .put("sessionId", it.sessionId).put("profileId", it.profileId).put("lessonId", it.lessonId)
                    .put("startedAt", it.startedAt).put("durationMillis", it.durationMillis)
                    .put("completedAt", it.completedAt ?: JSONObject.NULL)) }
            })
            root.put("wallet", dao.observeWallet(profileId).first()?.let {
                JSONObject().put("profileId", it.profileId).put("xp", it.xp).put("coins", it.coins).put("level", it.level)
            } ?: JSONObject.NULL)
            root.put("rewardEvents", JSONArray().apply {
                dao.observeRewardEvents(profileId).first().forEach { put(JSONObject()
                    .put("eventId", it.eventId).put("profileId", it.profileId).put("source", it.source)
                    .put("sourceId", it.sourceId).put("xpDelta", it.xpDelta).put("coinDelta", it.coinDelta)
                    .put("createdAt", it.createdAt)) }
            })
            root.put("vocabulary", JSONArray().apply {
                dao.observeVocabulary(profileId).first().forEach { put(JSONObject()
                    .put("wordId", it.wordId).put("profileId", it.profileId).put("english", it.english)
                    .put("dariMeaning", it.dariMeaning).put("pronunciation", it.pronunciation)
                    .put("exampleEnglish", it.exampleEnglish).put("exampleDari", it.exampleDari)
                    .put("categoryId", it.categoryId).put("favourite", it.favourite).put("mastery", it.mastery)
                    .put("lastPractisedAt", it.lastPractisedAt)) }
            })
            root.put("dailyGoals", JSONArray().apply {
                dao.observeDailyGoals(profileId).first().forEach { put(JSONObject()
                    .put("profileId", it.profileId).put("dateIso", it.dateIso)
                    .put("targetMinutes", it.targetMinutes).put("targetLessons", it.targetLessons)
                    .put("targetWords", it.targetWords).put("targetQuizzes", it.targetQuizzes)) }
            })
            root.put("reminders", JSONArray().apply {
                dao.observeReminders(profileId).first().forEach { put(JSONObject()
                    .put("reminderId", it.reminderId).put("profileId", it.profileId).put("title", it.title)
                    .put("hour", it.hour).put("minute", it.minute).put("repeatDaysMask", it.repeatDaysMask)
                    .put("enabled", it.enabled)) }
            })

            val output = context.contentResolver.openOutputStream(uri)
                ?: throw IOException("Cannot open the selected backup destination.")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(root.toString(2)) }
        }

    suspend fun restore(context: Context, uri: Uri, preferences: AcademyPreferences, dao: AcademyDao, profileId: String = "primary") =
        withContext(Dispatchers.IO) {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open the selected backup file.")
            val json = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(json)
            require(root.optString("format") == FORMAT) { "This file is not a BH Personal Academy backup." }
            require(root.optInt("schemaVersion") == VERSION) { "This backup version is not supported by this app." }

            val profile = root.getJSONObject("profile")
            preferences.updateDisplayName(profile.optString("displayName", "زهرا"))
            preferences.updateUsername(profile.optString("username", "zahra"))
            preferences.updateLocale(enumValue(profile.optString("locale"), AcademyLocale.DARI_AF))
            preferences.updateTheme(enumValue(profile.optString("theme"), AcademyThemeId.SOFT_PINK))
            preferences.updateFont(enumValue(profile.optString("font"), AcademyFontId.SANS_SERIF))
            preferences.updateDailyGoal(profile.optInt("dailyGoalMinutes", 20).coerceIn(5, 240))
            preferences.updateDailyTargets(
                profile.optInt("dailyGoalLessons", 1).coerceIn(1, 20),
                profile.optInt("dailyGoalWords", 5).coerceIn(1, 100),
                profile.optInt("dailyGoalQuizzes", 1).coerceIn(1, 10)
            )
            preferences.updateAvatar(if (profile.isNull("avatarUri")) null else profile.optString("avatarUri"))

            root.array("lessonProgress").forEachObject { o ->
                dao.mergeLessonProgress(LessonProgressEntity(
                    profileId = profileId,
                    lessonId = o.getString("lessonId"),
                    completedAt = o.longOrNull("completedAt"),
                    bestScore = o.optInt("bestScore"),
                    attempts = o.optInt("attempts")
                ))
            }
            root.array("quizAttempts").forEachObject { o ->
                dao.restoreQuizAttempt(QuizAttemptEntity(
                    attemptId = scopedId(profileId, o.getString("attemptId")), profileId = profileId,
                    quizId = o.getString("quizId"), score = o.optInt("score"), totalQuestions = o.optInt("totalQuestions"),
                    completedAt = o.optLong("completedAt"), incorrectQuestionIds = o.optString("incorrectQuestionIds")
                ))
            }
            root.array("gameAttempts").forEachObject { o ->
                dao.restoreGameAttempt(GameAttemptEntity(
                    attemptId = scopedId(profileId, o.getString("attemptId")), profileId = profileId,
                    gameId = o.getString("gameId"), score = o.optInt("score"),
                    totalQuestions = o.optInt("totalQuestions"), completedAt = o.optLong("completedAt")
                ))
            }
            root.array("studySessions").forEachObject { o ->
                dao.mergeStudySession(StudySessionEntity(
                    sessionId = scopedId(profileId, o.getString("sessionId")), profileId = profileId,
                    lessonId = o.getString("lessonId"), startedAt = o.optLong("startedAt"),
                    durationMillis = o.optLong("durationMillis"), completedAt = o.longOrNull("completedAt")
                ))
            }
            if (!root.isNull("wallet")) {
                val o = root.getJSONObject("wallet")
                dao.mergeWallet(WalletEntity(profileId, o.optLong("xp"), o.optLong("coins"), o.optInt("level", 1)))
            }
            root.array("rewardEvents").forEachObject { o ->
                dao.restoreRewardEvent(RewardEventEntity(
                    eventId = scopedId(profileId, o.getString("eventId")), profileId = profileId,
                    source = o.getString("source"), sourceId = o.getString("sourceId"),
                    xpDelta = o.optInt("xpDelta"), coinDelta = o.optInt("coinDelta"), createdAt = o.optLong("createdAt")
                ))
            }
            root.array("vocabulary").forEachObject { o ->
                dao.mergeVocabulary(VocabularyEntity(
                    wordId = o.getString("wordId"), profileId = profileId,
                    english = o.getString("english"), dariMeaning = o.optString("dariMeaning"),
                    pronunciation = o.optString("pronunciation"), exampleEnglish = o.optString("exampleEnglish"),
                    exampleDari = o.optString("exampleDari"), categoryId = o.optString("categoryId"),
                    favourite = o.optBoolean("favourite"), mastery = o.optInt("mastery"),
                    lastPractisedAt = o.optLong("lastPractisedAt")
                ))
            }
            root.array("dailyGoals").forEachObject { o ->
                dao.saveDailyGoal(DailyGoalEntity(
                    profileId = profileId, dateIso = o.getString("dateIso"),
                    targetMinutes = o.optInt("targetMinutes"), targetLessons = o.optInt("targetLessons"),
                    targetWords = o.optInt("targetWords"), targetQuizzes = o.optInt("targetQuizzes")
                ))
            }
            root.array("reminders").forEachObject { o ->
                val reminder = ReminderEntity(
                    reminderId = scopedId(profileId, o.getString("reminderId")), profileId = profileId,
                    title = o.getString("title"), hour = o.optInt("hour"), minute = o.optInt("minute"),
                    repeatDaysMask = o.optInt("repeatDaysMask"), enabled = o.optBoolean("enabled")
                )
                dao.saveReminder(reminder)
                if (reminder.enabled) {
                    ReminderScheduler.schedule(context, reminder.reminderId, reminder.title, reminder.hour, reminder.minute, reminder.repeatDaysMask)
                }
            }
        }

    private inline fun JSONArray.forEachObject(action: (JSONObject) -> Unit) {
        for (index in 0 until length()) action(getJSONObject(index))
    }

    private fun scopedId(profileId: String, sourceId: String): String =
        if (sourceId.startsWith("$profileId:")) sourceId else "$profileId:$sourceId"

    private fun JSONObject.array(name: String): JSONArray = optJSONArray(name) ?: JSONArray()
    private fun JSONObject.longOrNull(name: String): Long? =
        if (!has(name) || isNull(name)) null else optLong(name)

    private inline fun <reified T : Enum<T>> enumValue(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
