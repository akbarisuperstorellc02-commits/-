package com.bh.personalacademy.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.academy.domain.model.AcademyFontId
import com.example.academy.domain.model.AcademyLocale
import com.example.academy.domain.model.AcademyThemeId
import com.example.academy.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.academyDataStore by preferencesDataStore(name = "bh_academy_profile")

/**
 * Local multi-profile preferences. Each profile has its own namespaced settings,
 * while the original unscoped keys remain readable for backwards compatibility.
 */
class AcademyPreferences(private val context: Context) {
    private object Keys {
        val activeProfile = stringPreferencesKey("active_profile_id")
        val profileIds = stringSetPreferencesKey("profile_ids")
        val displayName = stringPreferencesKey("display_name")
        val username = stringPreferencesKey("username")
        val locale = stringPreferencesKey("locale")
        val theme = stringPreferencesKey("theme")
        val font = stringPreferencesKey("font")
        val dailyGoal = intPreferencesKey("daily_goal_minutes")
        val avatarUri = stringPreferencesKey("avatar_uri")
        val dailyLessons = intPreferencesKey("daily_goal_lessons")
        val dailyWords = intPreferencesKey("daily_goal_words")
        val dailyQuizzes = intPreferencesKey("daily_goal_quizzes")
    }

    val profiles: Flow<List<UserProfile>> = context.academyDataStore.data.map { data ->
        val ids = (data[Keys.profileIds] ?: emptySet()) + "primary"
        ids.map { readProfile(data, it) }.sortedWith(compareBy<UserProfile> { it.profileId != "primary" }.thenBy { it.createdAtEpochMillis })
    }

    val profile: Flow<UserProfile> = context.academyDataStore.data.map { data ->
        val id = data[Keys.activeProfile] ?: "primary"
        readProfile(data, id)
    }

    private fun readProfile(data: Preferences, id: String): UserProfile {
        fun string(name: String, legacy: Preferences.Key<String>, fallback: String): String =
            data[stringPreferencesKey("${id}_${name}")] ?: if (id == "primary") data[legacy] ?: fallback else fallback
        fun int(name: String, legacy: Preferences.Key<Int>, fallback: Int): Int =
            data[intPreferencesKey("${id}_${name}")] ?: if (id == "primary") data[legacy] ?: fallback else fallback

        return UserProfile(
            profileId = id,
            displayName = string("display_name", Keys.displayName, if (id == "primary") "زهرا" else "Learner"),
            username = string("username", Keys.username, if (id == "primary") "zahra" else "learner"),
            locale = runCatching { AcademyLocale.valueOf(string("locale", Keys.locale, "DARI_AF")) }.getOrDefault(AcademyLocale.DARI_AF),
            theme = runCatching { AcademyThemeId.valueOf(string("theme", Keys.theme, "SOFT_PINK")) }.getOrDefault(AcademyThemeId.SOFT_PINK),
            font = runCatching { AcademyFontId.valueOf(string("font", Keys.font, "SANS_SERIF")) }.getOrDefault(AcademyFontId.SANS_SERIF),
            dailyStudyGoalMinutes = int("daily_goal_minutes", Keys.dailyGoal, 20),
            dailyGoalLessons = int("daily_goal_lessons", Keys.dailyLessons, 1),
            dailyGoalWords = int("daily_goal_words", Keys.dailyWords, 5),
            dailyGoalQuizzes = int("daily_goal_quizzes", Keys.dailyQuizzes, 1),
            avatarUri = string("avatar_uri", Keys.avatarUri, "").ifBlank { null },
            createdAtEpochMillis = data[longKey(id, "created_at")] ?: 0L
        )
    }

    private fun longKey(id: String, name: String) = androidx.datastore.preferences.core.longPreferencesKey("${id}_${name}")

    suspend fun createProfile(displayName: String, username: String): String {
        val name = displayName.trim().take(40)
        val user = username.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(32)
        require(name.isNotEmpty()) { "Profile name cannot be empty." }
        require(user.isNotEmpty()) { "Username cannot be empty." }
        val id = UUID.randomUUID().toString()
        context.academyDataStore.edit { data ->
            val ids = (data[Keys.profileIds] ?: emptySet()) + "primary"
            require(ids.none { existing ->
                val existingUser = data[stringPreferencesKey("${existing}_username")]
                    ?: if (existing == "primary") data[Keys.username] ?: "zahra" else "learner"
                existingUser.equals(user, ignoreCase = true)
            }) { "This username is already in use." }
            data[Keys.profileIds] = ids + id
            data[stringPreferencesKey("${id}_display_name")] = name
            data[stringPreferencesKey("${id}_username")] = user
            data[stringPreferencesKey("${id}_locale")] = "DARI_AF"
            data[stringPreferencesKey("${id}_theme")] = "SOFT_PINK"
            data[stringPreferencesKey("${id}_font")] = "SANS_SERIF"
            data[intPreferencesKey("${id}_daily_goal_minutes")] = 20
            data[intPreferencesKey("${id}_daily_goal_lessons")] = 1
            data[intPreferencesKey("${id}_daily_goal_words")] = 5
            data[intPreferencesKey("${id}_daily_goal_quizzes")] = 1
            data[longKey(id, "created_at")] = System.currentTimeMillis()
        }
        return id
    }

    suspend fun switchProfile(profileId: String) {
        context.academyDataStore.edit { data ->
            val ids = (data[Keys.profileIds] ?: emptySet()) + "primary"
            require(profileId in ids) { "Profile does not exist." }
            data[Keys.activeProfile] = profileId
        }
    }

    suspend fun updateDisplayName(value: String) = updateString("display_name", value.trim().take(40), Keys.displayName)
    suspend fun updateUsername(value: String) {
        val username = value.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(32)
        if (username.isNotEmpty()) updateString("username", username, Keys.username)
    }
    suspend fun updateLocale(value: AcademyLocale) = updateString("locale", value.name, Keys.locale)
    suspend fun updateTheme(value: AcademyThemeId) = updateString("theme", value.name, Keys.theme)
    suspend fun updateFont(value: AcademyFontId) = updateString("font", value.name, Keys.font)
    suspend fun updateDailyGoal(minutes: Int) = updateInt("daily_goal_minutes", minutes.coerceIn(5, 240), Keys.dailyGoal)
    suspend fun updateDailyTargets(lessons: Int, words: Int, quizzes: Int) {
        context.academyDataStore.edit { data ->
            val id = data[Keys.activeProfile] ?: "primary"
            data[intPreferencesKey("${id}_daily_goal_lessons")] = lessons.coerceIn(1, 20)
            data[intPreferencesKey("${id}_daily_goal_words")] = words.coerceIn(1, 100)
            data[intPreferencesKey("${id}_daily_goal_quizzes")] = quizzes.coerceIn(1, 10)
        }
    }
    suspend fun updateAvatar(uri: String?) {
        context.academyDataStore.edit { data ->
            val id = data[Keys.activeProfile] ?: "primary"
            val key = stringPreferencesKey("${id}_avatar_uri")
            if (uri == null) data.remove(key) else data[key] = uri
        }
    }

    private suspend fun updateString(name: String, value: String, legacy: Preferences.Key<String>) {
        context.academyDataStore.edit { data ->
            val id = data[Keys.activeProfile] ?: "primary"
            data[stringPreferencesKey("${id}_${name}")] = value
            if (id == "primary") data[legacy] = value
        }
    }

    private suspend fun updateInt(name: String, value: Int, legacy: Preferences.Key<Int>) {
        context.academyDataStore.edit { data ->
            val id = data[Keys.activeProfile] ?: "primary"
            data[intPreferencesKey("${id}_${name}")] = value
            if (id == "primary") data[legacy] = value
        }
    }

    suspend fun resetProfile() {
        context.academyDataStore.edit { data ->
            val id = data[Keys.activeProfile] ?: "primary"
            data.asMap().keys.filter { it.name.startsWith("${id}_") }.forEach { data.remove(it) }
            if (id == "primary") {
                data.remove(Keys.displayName); data.remove(Keys.username); data.remove(Keys.locale)
                data.remove(Keys.theme); data.remove(Keys.font); data.remove(Keys.dailyGoal)
                data.remove(Keys.avatarUri); data.remove(Keys.dailyLessons); data.remove(Keys.dailyWords)
                data.remove(Keys.dailyQuizzes)
            }
            data[Keys.activeProfile] = id
        }
    }
}
