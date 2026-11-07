package com.example.academy.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.academy.domain.model.AcademyFontId
import com.example.academy.domain.model.AcademyLocale
import com.example.academy.domain.model.AcademyThemeId
import com.example.academy.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.academyDataStore: DataStore<Preferences> by preferencesDataStore(name = "bh_academy_preferences")

class AcademyPreferencesRepository(private val context: Context) {
    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val USERNAME = stringPreferencesKey("username")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val LOCALE = stringPreferencesKey("locale_tag")
        val THEME = stringPreferencesKey("theme_id")
        val FONT = stringPreferencesKey("font_id")
        val DAILY_GOAL = intPreferencesKey("daily_goal_minutes")
    }

    val profile: Flow<UserProfile> = context.academyDataStore.data.map { prefs ->
        UserProfile(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "زهرا",
            username = prefs[Keys.USERNAME] ?: "zahra",
            avatarUri = prefs[Keys.AVATAR_URI],
            locale = AcademyLocale.entries.firstOrNull { it.languageTag == prefs[Keys.LOCALE] } ?: AcademyLocale.DARI_AF,
            theme = runCatching { AcademyThemeId.valueOf(prefs[Keys.THEME] ?: "SOFT_PINK") }.getOrDefault(AcademyThemeId.SOFT_PINK),
            font = runCatching { AcademyFontId.valueOf(prefs[Keys.FONT] ?: "SANS_SERIF") }.getOrDefault(AcademyFontId.SANS_SERIF),
            dailyStudyGoalMinutes = (prefs[Keys.DAILY_GOAL] ?: 20).coerceIn(5, 600)
        )
    }

    suspend fun updateDisplayName(name: String) {
        val clean = name.trim().take(32).ifBlank { "زهرا" }
        context.academyDataStore.edit { it[Keys.DISPLAY_NAME] = clean }
    }

    suspend fun updateUsername(username: String) {
        val clean = username.trim().filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(24)
        if (clean.isNotBlank()) context.academyDataStore.edit { it[Keys.USERNAME] = clean }
    }

    suspend fun updateAvatar(uri: String?) {
        context.academyDataStore.edit { prefs ->
            if (uri == null) prefs.remove(Keys.AVATAR_URI) else prefs[Keys.AVATAR_URI] = uri
        }
    }

    suspend fun updateLocale(locale: AcademyLocale) {
        context.academyDataStore.edit { it[Keys.LOCALE] = locale.languageTag }
    }

    suspend fun updateTheme(theme: AcademyThemeId) {
        context.academyDataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun updateFont(font: AcademyFontId) {
        context.academyDataStore.edit { it[Keys.FONT] = font.name }
    }

    suspend fun updateDailyGoal(minutes: Int) {
        context.academyDataStore.edit { it[Keys.DAILY_GOAL] = minutes.coerceIn(5, 600) }
    }
}
