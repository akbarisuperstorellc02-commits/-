package com.bh.personalacademy.data.preferences
import android.content.Context
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
private val Context.academyDataStore by preferencesDataStore(name = "bh_academy_profile")
class AcademyPreferences(private val context: Context) {
 private object Keys {
  val displayName=stringPreferencesKey("display_name"); val username=stringPreferencesKey("username")
  val locale=stringPreferencesKey("locale"); val theme=stringPreferencesKey("theme")
  val font=stringPreferencesKey("font"); val dailyGoal=intPreferencesKey("daily_goal_minutes")
  val avatarUri=stringPreferencesKey("avatar_uri")
  val dailyLessons=intPreferencesKey("daily_goal_lessons"); val dailyWords=intPreferencesKey("daily_goal_words"); val dailyQuizzes=intPreferencesKey("daily_goal_quizzes")
 }
 val profile: Flow<UserProfile> = context.academyDataStore.data.map { v ->
  UserProfile(displayName=v[Keys.displayName] ?: "زهرا", username=v[Keys.username] ?: "zahra",
   locale=runCatching { AcademyLocale.valueOf(v[Keys.locale] ?: "DARI_AF") }.getOrDefault(AcademyLocale.DARI_AF),
   theme=runCatching { AcademyThemeId.valueOf(v[Keys.theme] ?: "SOFT_PINK") }.getOrDefault(AcademyThemeId.SOFT_PINK),
   font=runCatching { AcademyFontId.valueOf(v[Keys.font] ?: "SANS_SERIF") }.getOrDefault(AcademyFontId.SANS_SERIF),
   dailyStudyGoalMinutes=v[Keys.dailyGoal] ?: 20, dailyGoalLessons=v[Keys.dailyLessons] ?: 1,
   dailyGoalWords=v[Keys.dailyWords] ?: 5, dailyGoalQuizzes=v[Keys.dailyQuizzes] ?: 1, avatarUri=v[Keys.avatarUri])
 }
 suspend fun updateDisplayName(value:String){val s=value.trim().take(40);if(s.isNotEmpty())context.academyDataStore.edit{it[Keys.displayName]=s}}
 suspend fun updateUsername(value:String){val s=value.trim().lowercase().filter{it.isLetterOrDigit()||it=='_'}.take(32);if(s.isNotEmpty())context.academyDataStore.edit{it[Keys.username]=s}}
 suspend fun updateLocale(value:AcademyLocale){context.academyDataStore.edit{it[Keys.locale]=value.name}}
 suspend fun updateTheme(value:AcademyThemeId){context.academyDataStore.edit{it[Keys.theme]=value.name}}
 suspend fun updateFont(value:AcademyFontId){context.academyDataStore.edit{it[Keys.font]=value.name}}
 suspend fun updateDailyGoal(minutes:Int){context.academyDataStore.edit{it[Keys.dailyGoal]=minutes.coerceIn(5,240)}}
 suspend fun updateDailyTargets(lessons:Int,words:Int,quizzes:Int){context.academyDataStore.edit{it[Keys.dailyLessons]=lessons.coerceIn(1,20);it[Keys.dailyWords]=words.coerceIn(1,100);it[Keys.dailyQuizzes]=quizzes.coerceIn(1,10)}}
 suspend fun updateAvatar(uri:String?){context.academyDataStore.edit{if(uri==null)it.remove(Keys.avatarUri) else it[Keys.avatarUri]=uri}}
 suspend fun resetProfile(){context.academyDataStore.edit{it.clear()}}
}