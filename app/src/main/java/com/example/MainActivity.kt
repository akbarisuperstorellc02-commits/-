package com.bh.personalacademy

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bh.personalacademy.content.AcademyGameCatalog
import com.bh.personalacademy.content.AcademyGrammarCatalog
import com.bh.personalacademy.content.AcademyLessonCatalog
import com.bh.personalacademy.content.AcademyStoryCatalog
import com.bh.personalacademy.content.LocalizedText
import com.bh.personalacademy.ui.components.LessonStudyTimer
import com.bh.personalacademy.content.GameMode
import com.bh.personalacademy.content.GameStyle
import com.bh.personalacademy.data.local.*
import com.bh.personalacademy.data.backup.AcademyBackupManager
import com.bh.personalacademy.data.AcademyRepository
import com.bh.personalacademy.presentation.AcademyViewModel
import com.bh.personalacademy.presentation.AcademyViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bh.personalacademy.data.preferences.AcademyPreferences
import com.bh.personalacademy.reminders.ReminderScheduler
import com.example.academy.core.AsciiDigits
import com.example.academy.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.UUID

private enum class AcademyTab { HOME, LEARN, GRAMMAR, QUIZ, GAMES, PROGRESS, PROFILE, TOOLS, VOCABULARY, CALENDAR, SPEAKING, STORIES }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AcademyPreferences(applicationContext)
        val database = AcademyDatabase.getInstance(applicationContext)
        val repository = AcademyRepository(preferences, database.academyDao())
        setContent {
            val academyViewModel: AcademyViewModel = viewModel(
                factory = AcademyViewModelFactory(repository)
            )
            BHAcademyApp(academyViewModel, preferences, database.academyDao())
        }
    }
}

@Composable
private fun BHAcademyApp(viewModel: AcademyViewModel, preferences: AcademyPreferences, dao: AcademyDao) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val lessons by viewModel.lessonProgress.collectAsStateWithLifecycle()
    val attempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val gameAttempts by viewModel.gameAttempts.collectAsStateWithLifecycle()
    val todayRange = remember { val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }; val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }; start.timeInMillis to end.timeInMillis }
    val todayStudyMillis by dao.observeStudyMillis(todayRange.first, todayRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0L)
    val todayLessonCount by dao.observeCompletedLessonCount(todayRange.first, todayRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0)
    val todayQuizCount by dao.observeQuizCount(todayRange.first, todayRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0)
    val todayWordCount by dao.observeWordsPractisedCount(todayRange.first, todayRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0)
    val weekRange = remember {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
        start.timeInMillis to end.timeInMillis
    }
    val monthRange = remember {
        val start = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        start.timeInMillis to end.timeInMillis
    }
    val weekStudyMillis by dao.observeStudyMillis(weekRange.first, weekRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0L)
    val monthStudyMillis by dao.observeStudyMillis(monthRange.first, monthRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = 0L)
    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val weekSessionStarts by dao.observeStudySessionStarts(weekRange.first, weekRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val monthSessionStarts by dao.observeStudySessionStarts(monthRange.first, monthRange.second, profile.profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val weekStudyDays = remember(weekSessionStarts) { weekSessionStarts.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it)) }.toSet().size }
    val monthStudyDays = remember(monthSessionStarts) { monthSessionStarts.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it)) }.toSet().size }
    val totalStudyMillis by dao.observeTotalStudyMillis(profile.profileId).collectAsStateWithLifecycle(initialValue = 0L)
    var tab by remember { mutableStateOf(AcademyTab.HOME) }
    var band by remember { mutableStateOf<LearningBand?>(null) }
    var speakingLessonPrompt by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(profile.profileId) { viewModel.initialize() }
    LaunchedEffect(profile.profileId, todayIso, profile.dailyStudyGoalMinutes, profile.dailyGoalLessons, profile.dailyGoalWords, profile.dailyGoalQuizzes) {
        dao.saveDailyGoal(
            DailyGoalEntity(
                profileId = profile.profileId,
                dateIso = todayIso,
                targetMinutes = profile.dailyStudyGoalMinutes,
                targetLessons = profile.dailyGoalLessons,
                targetWords = profile.dailyGoalWords,
                targetQuizzes = profile.dailyGoalQuizzes
            )
        )
    }
    LaunchedEffect(profile.profileId, todayStudyMillis, todayLessonCount, todayWordCount, todayQuizCount,
        profile.dailyStudyGoalMinutes, profile.dailyGoalLessons, profile.dailyGoalWords, profile.dailyGoalQuizzes) {
        val goalsMet = todayStudyMillis >= profile.dailyStudyGoalMinutes * 60_000L &&
            todayLessonCount >= profile.dailyGoalLessons &&
            todayWordCount >= profile.dailyGoalWords &&
            todayQuizCount >= profile.dailyGoalQuizzes
        if (goalsMet) {
            dao.applyRewardOnce(
                RewardEventEntity(
                    eventId = "daily_goal_${profile.profileId}_$todayIso", profileId = profile.profileId, source = "DAILY_GOAL", sourceId = todayIso,
                    xpDelta = 25, coinDelta = 10, createdAt = System.currentTimeMillis()
                )
            )
        }
    }
    LaunchedEffect(profile.profileId, weekStudyDays, monthStudyDays) {
        val now = System.currentTimeMillis()
        if (weekStudyDays >= 5) {
            val key = SimpleDateFormat("YYYY-'W'ww", Locale.US).format(Date(now))
            dao.applyRewardOnce(RewardEventEntity(
                eventId = "weekly_challenge_${profile.profileId}_$key", profileId = profile.profileId, source = "CHALLENGE", sourceId = "weekly_$key",
                xpDelta = 50, coinDelta = 20, createdAt = now
            ))
        }
        if (monthStudyDays >= 20) {
            val key = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
            dao.applyRewardOnce(RewardEventEntity(
                eventId = "monthly_challenge_${profile.profileId}_$key", profileId = profile.profileId, source = "CHALLENGE", sourceId = "monthly_$key",
                xpDelta = 150, coinDelta = 50, createdAt = now
            ))
        }
    }

    val palette = paletteFor(profile.theme)
    CompositionLocalProvider(LocalLayoutDirection provides if (profile.locale.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        val appColorScheme = if (profile.theme in setOf(AcademyThemeId.BLACK_GOLD, AcademyThemeId.DARK, AcademyThemeId.NIGHT, AcademyThemeId.GREEN_BLACK)) {
            darkColorScheme(primary = palette.primary, onPrimary = Color(0xFF1D1B16), secondary = palette.secondary,
                background = palette.background, surface = palette.surface, onSurface = Color.White,
                surfaceVariant = palette.variant, onSurfaceVariant = Color.White)
        } else {
            lightColorScheme(primary = palette.primary, onPrimary = Color.White, secondary = palette.secondary,
                background = palette.background, surface = palette.surface, surfaceVariant = palette.variant)
        }
        MaterialTheme(
            colorScheme = appColorScheme,
            typography = Typography(bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFor(profile.font)),
                bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFor(profile.font)),
                titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = fontFor(profile.font), fontWeight = FontWeight.Bold)),
            shapes = Shapes(
                small = RoundedCornerShape(26.dp),
                medium = RoundedCornerShape(32.dp),
                large = RoundedCornerShape(40.dp)
            )
        ) {
            Scaffold(containerColor = palette.background, bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.surface,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    shadowElevation = 18.dp,
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.16f))
                ) {
                NavigationBar(containerColor = palette.surface, tonalElevation = 6.dp) {
                    val items = mutableListOf(
                        Triple(AcademyTab.HOME, Icons.Default.Home, tr(profile.locale, "home")),
                        Triple(AcademyTab.LEARN, Icons.Default.MenuBook, tr(profile.locale, "learn")),
                        Triple(AcademyTab.GAMES, Icons.Default.Extension, tr(profile.locale, "games")),
                        Triple(AcademyTab.PROGRESS, Icons.Default.Insights, tr(profile.locale, "progress")),
                        Triple(AcademyTab.PROFILE, Icons.Default.Person, tr(profile.locale, "profile")),
                        Triple(AcademyTab.SPEAKING, Icons.Default.Mic, when (profile.locale) {
                            AcademyLocale.ENGLISH_US -> "Speak"
                            AcademyLocale.DARI_AF -> "گفتار"
                            AcademyLocale.PASHTO_AF -> "خبرې"
                            AcademyLocale.ARABIC -> "تحدث"
                        })
                    )
                    items.add(Triple(AcademyTab.STORIES, Icons.Default.MenuBook, when (profile.locale) {
                        AcademyLocale.ENGLISH_US -> "Stories"
                        AcademyLocale.DARI_AF -> "داستان‌ها"
                        AcademyLocale.PASHTO_AF -> "کیسې"
                        AcademyLocale.ARABIC -> "قصص"
                    }))
                    items.forEach { (destination, icon, label) ->
                        NavigationBarItem(selected = tab == destination, onClick = { tab = destination; band = null },
                            icon = { Icon(icon, contentDescription = label) }, label = { Text(label, maxLines = 1, fontSize = 10.sp) })
                    }
                }
                }
            }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        AcademyTab.HOME -> HomePage(profile, wallet, lessons.size, attempts.size, (todayStudyMillis / 60000L).toInt(),
                            todayLessonCount, todayWordCount, todayQuizCount, weekStudyDays, monthStudyDays,
                            { tab = AcademyTab.LEARN }, { tab = AcademyTab.GAMES }, { tab = AcademyTab.PROFILE }, { tab = AcademyTab.TOOLS }, { tab = AcademyTab.CALENDAR })
                        AcademyTab.LEARN -> LearnPage(profile.locale, band, lessons.map { it.lessonId }.toSet(),
                            { band = it },
                            { tab = AcademyTab.VOCABULARY },
                            { tab = AcademyTab.GRAMMAR },
                            { prompt -> speakingLessonPrompt = prompt; tab = AcademyTab.SPEAKING },
                            { id ->
                                viewModel.completeLesson(id, 100, profile.profileId)
                                val activeBand = band
                                if (activeBand != null) {
                                    val completedBefore = lessons.map { it.lessonId }.toSet()
                                    val bandFinished = AcademyLessonCatalog.forBand(activeBand).all { it.id == id || it.id in completedBefore }
                                    if (bandFinished) {
                                        viewModel.rewardOnce(
                                            RewardEventEntity(
                                                eventId = "level_${profile.profileId}_${activeBand.name}",
                                                profileId = profile.profileId,
                                                source = "LEVEL_COMPLETION",
                                                sourceId = activeBand.name,
                                                xpDelta = 100,
                                                coinDelta = 20,
                                                createdAt = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            },
                            { lessonId, startedAt, duration ->
                                viewModel.saveStudySession(lessonId, startedAt, duration, profile.profileId)
                            },
                            { tab = AcademyTab.QUIZ })
                        AcademyTab.GRAMMAR -> GrammarPage(profile.locale) { tab = AcademyTab.LEARN }
                        AcademyTab.QUIZ -> QuizPage(profile.locale, profile.profileId, dao) { tab = AcademyTab.PROGRESS }
                        AcademyTab.GAMES -> GamePage(profile.locale, profile.profileId, wallet?.coins ?: 0, dao)
                        AcademyTab.PROGRESS -> ProgressPage(profile.locale, lessons.size, attempts, gameAttempts.size, todayStudyMillis, weekStudyMillis, monthStudyMillis, totalStudyMillis, wallet?.xp ?: 0, wallet?.coins ?: 0)
                        AcademyTab.PROFILE -> ProfilePage(profile, profiles, preferences, dao)
                        AcademyTab.TOOLS -> ToolsPage(profile.locale, profile.profileId, dao) { tab = AcademyTab.HOME }
                        AcademyTab.CALENDAR -> CalendarPage(profile.locale, profile.profileId, dao) { tab = AcademyTab.HOME }
                        AcademyTab.VOCABULARY -> VocabularyPage(profile.locale, profile.profileId, dao)
                        AcademyTab.SPEAKING -> SpeakingPage(profile.locale, profile.profileId, dao, speakingLessonPrompt)
                        AcademyTab.STORIES -> StoriesPage(profile.locale) { prompt -> speakingLessonPrompt = prompt; tab = AcademyTab.SPEAKING }
                    }
                }
            }
        }
    }
}

private data class Palette(val primary: Color, val secondary: Color, val background: Color, val surface: Color, val variant: Color)
private fun paletteFor(theme: AcademyThemeId): Palette = when (theme) {
    AcademyThemeId.SOFT_PINK -> Palette(Color(0xFFD96F9E), Color(0xFFF2AFCB), Color(0xFFFFF5F8), Color.White, Color(0xFFFBE3ED))
    AcademyThemeId.PINK_PURPLE -> Palette(Color(0xFFB05DAB), Color(0xFFD8A6D5), Color(0xFFFBF3FC), Color.White, Color(0xFFF1E1F2))
    AcademyThemeId.LILAC_WHITE -> Palette(Color(0xFF9576B5), Color(0xFFC6B2D8), Color(0xFFF7F3FA), Color.White, Color(0xFFEDE6F3))
    AcademyThemeId.PASTEL -> Palette(Color(0xFF70AFA2), Color(0xFFF0B8A2), Color(0xFFFFF8F0), Color.White, Color(0xFFEAF3EE))
    AcademyThemeId.FLORAL -> Palette(Color(0xFFB66E83), Color(0xFF9EBA9A), Color(0xFFFFF7F4), Color.White, Color(0xFFF3E8E2))
    AcademyThemeId.NAVY_BLUE -> Palette(Color(0xFF315B9A), Color(0xFF82A9D8), Color(0xFFF1F5FB), Color.White, Color(0xFFE5EDF8))
    AcademyThemeId.BLUE_GRAY -> Palette(Color(0xFF536779), Color(0xFF9BAFBD), Color(0xFFF2F5F7), Color.White, Color(0xFFE4EAEE))
    AcademyThemeId.GREEN_BLACK -> Palette(Color(0xFF64B99A), Color(0xFF24745D), Color(0xFF14231F), Color(0xFF20342D), Color(0xFF30483F))
    AcademyThemeId.SPORT -> Palette(Color(0xFF1D65C1), Color(0xFFF08A35), Color(0xFFF1F6FD), Color.White, Color(0xFFE1ECFA))
    AcademyThemeId.SPACE -> Palette(Color(0xFF5368B5), Color(0xFF9B8BD4), Color(0xFFEEF0FA), Color.White, Color(0xFFE1E4F4))
    AcademyThemeId.BLACK_GOLD -> Palette(Color(0xFFD6B96E), Color(0xFF9A762A), Color(0xFF171717), Color(0xFF242321), Color(0xFF39352C))
    AcademyThemeId.WHITE_GOLD -> Palette(Color(0xFF9A762A), Color(0xFFD6B96E), Color(0xFFFAF8F1), Color.White, Color(0xFFF0EBDD))
    AcademyThemeId.ROYAL_PURPLE -> Palette(Color(0xFF68429A), Color(0xFFB99BD3), Color(0xFFF5F0FB), Color.White, Color(0xFFE9E0F5))
    AcademyThemeId.NIGHT -> Palette(Color(0xFF9B8AE0), Color(0xFF5B6FB4), Color(0xFF171B2A), Color(0xFF232A3D), Color(0xFF343D55))
    AcademyThemeId.LIGHT -> Palette(Color(0xFF6E8CB8), Color(0xFFADC5E2), Color(0xFFFAFCFF), Color.White, Color(0xFFEAF0F7))
    AcademyThemeId.DARK -> Palette(Color(0xFF8E78C7), Color(0xFFB7A8DE), Color(0xFF191820), Color(0xFF282631), Color(0xFF393644))
}
private fun fontFor(font: AcademyFontId) = when(font) {
    AcademyFontId.SANS_SERIF -> FontFamily.SansSerif
    AcademyFontId.SERIF -> FontFamily.Serif
    AcademyFontId.CURSIVE -> FontFamily.Cursive
    AcademyFontId.VAZIRMATN -> FontFamily(Font(R.font.vazirmatn_regular))
    AcademyFontId.SAHEL -> FontFamily(Font(R.font.sahel_regular))
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = content) }
    }
}
@Composable
private fun PuffyAvatar(emoji: String, size: Int) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)),
        shadowElevation = 12.dp,
        tonalElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = (size * 0.48f).sp) }
    }
}

@Composable
private fun AcademyCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp, pressedElevation = 7.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 21.dp, vertical = 21.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}
@Composable
private fun HomePage(p: UserProfile, w: WalletEntity?, done: Int, quizzes: Int, studyMinutes: Int, todayLessons: Int, todayWords: Int, todayQuizzes: Int, weeklyStudyDays: Int, monthlyStudyDays: Int, learn: () -> Unit, games: () -> Unit, profile: () -> Unit, tools: () -> Unit, calendar: () -> Unit) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }
    val dateTimeLabel = remember(currentTimeMillis, p.locale) {
        AsciiDigits.normalize(
            SimpleDateFormat("EEEE, d MMMM · HH:mm", Locale.forLanguageTag(p.locale.languageTag))
                .format(Date(currentTimeMillis))
        )
    }
    Page {
        Text(tr(p.locale, "greeting", p.displayName), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(dateTimeLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(tr(p.locale, "welcome"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(38.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
            shadowElevation = 18.dp,
            tonalElevation = 8.dp
        ) {
            Column(
                Modifier.background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("✨  ${tr(p.locale, "journey")}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text(tr(p.locale, "journeySub"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
                        Text(tr(p.locale, "greeting", p.displayName), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    if (p.avatarUri?.startsWith("emoji:") == true) {
                        PuffyAvatar(emoji = p.avatarUri.removePrefix("emoji:"), size = 76)
                    } else if (p.avatarUri != null) {
                        AsyncImage(
                            model = p.avatarUri,
                            contentDescription = tr(p.locale, "profile"),
                            modifier = Modifier.size(76.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        PuffyAvatar(emoji = "🌸", size = 76)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat(tr(p.locale, "level"), "${w?.level ?: 1}")
                    Stat("XP", "${w?.xp ?: 0}")
                    Stat(tr(p.locale, "coins"), "${w?.coins ?: 0}")
                }
            }
        }
        AcademyCard {
            Text(tr(p.locale, "dailyGoal"), style = MaterialTheme.typography.titleLarge)
            Text(tr(p.locale, "goalMins", p.dailyStudyGoalMinutes.toString()))
            val goalProgress = (studyMinutes.toFloat() / p.dailyStudyGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
            LinearProgressIndicator(progress = { goalProgress }, modifier = Modifier.fillMaxWidth())
            Text("${AsciiDigits.normalize(studyMinutes.toString())} / ${AsciiDigits.normalize(p.dailyStudyGoalMinutes.toString())} ${tr(p.locale, "minutes")}", fontSize = 12.sp)
            Text(tr(p.locale, "goalHint"), fontSize = 12.sp)
        }
        AcademyCard {
            Text(when (p.locale) {
                AcademyLocale.ENGLISH_US -> "Study challenges"
                AcademyLocale.DARI_AF -> "چالش‌های مطالعه"
                AcademyLocale.PASHTO_AF -> "د مطالعې ننګونې"
                AcademyLocale.ARABIC -> "تحديات الدراسة"
            }, style = MaterialTheme.typography.titleMedium)
            val challengeRows = listOf(
                Triple(
                    when (p.locale) { AcademyLocale.ENGLISH_US -> "Study 5 days this week" ; AcademyLocale.DARI_AF -> "5 روز این هفته مطالعه کن" ; AcademyLocale.PASHTO_AF -> "په دې اونۍ کې 5 ورځې مطالعه وکړئ" ; AcademyLocale.ARABIC -> "ادرس 5 أيام هذا الأسبوع" },
                    weeklyStudyDays, 5
                ),
                Triple(
                    when (p.locale) { AcademyLocale.ENGLISH_US -> "Study 20 days this month" ; AcademyLocale.DARI_AF -> "20 روز این ماه مطالعه کن" ; AcademyLocale.PASHTO_AF -> "په دې میاشت کې 20 ورځې مطالعه وکړئ" ; AcademyLocale.ARABIC -> "ادرس 20 يوماً هذا الشهر" },
                    monthlyStudyDays, 20
                )
            )
            challengeRows.forEach { (label, current, target) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("$label · ${AsciiDigits.normalize(current.toString())}/$target", fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { (current.toFloat() / target).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        AcademyCard {
            Text(
                when (p.locale) {
                    AcademyLocale.ENGLISH_US -> "Today's learning targets"
                    AcademyLocale.DARI_AF -> "هدف‌های یادگیری امروز"
                    AcademyLocale.PASHTO_AF -> "د نن ورځې د زده کړې موخې"
                    AcademyLocale.ARABIC -> "أهداف التعلم اليوم"
                },
                style = MaterialTheme.typography.titleMedium
            )
            val targets = listOf(
                Triple(
                    when (p.locale) {
                        AcademyLocale.ENGLISH_US -> "Lessons"
                        AcademyLocale.DARI_AF -> "درس‌ها"
                        AcademyLocale.PASHTO_AF -> "درسونه"
                        AcademyLocale.ARABIC -> "الدروس"
                    },
                    todayLessons,
                    p.dailyGoalLessons
                ),
                Triple(
                    when (p.locale) {
                        AcademyLocale.ENGLISH_US -> "Words practised"
                        AcademyLocale.DARI_AF -> "واژه‌های تمرین‌شده"
                        AcademyLocale.PASHTO_AF -> "تمرین شوې کلمې"
                        AcademyLocale.ARABIC -> "الكلمات المتدرّب عليها"
                    },
                    todayWords,
                    p.dailyGoalWords
                ),
                Triple(
                    when (p.locale) {
                        AcademyLocale.ENGLISH_US -> "Quizzes"
                        AcademyLocale.DARI_AF -> "آزمون‌ها"
                        AcademyLocale.PASHTO_AF -> "ازموینې"
                        AcademyLocale.ARABIC -> "الاختبارات"
                    },
                    todayQuizzes,
                    p.dailyGoalQuizzes
                )
            )
            targets.forEach { (label, current, target) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$label · ${AsciiDigits.normalize(current.toString())}/${AsciiDigits.normalize(target.toString())}",
                        fontSize = 12.sp
                    )
                    LinearProgressIndicator(
                        progress = { (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat(tr(p.locale, "lessons"), "${done}", Modifier.weight(1f))
            MiniStat(tr(p.locale, "quizzes"), "${quizzes}", Modifier.weight(1f))
        }
        AcademyCard {
            Text(tr(p.locale, "continue"), style = MaterialTheme.typography.titleLarge)
            Text(tr(p.locale, "chooseLesson"))
            Button(onClick = learn, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.MenuBook, null); Spacer(Modifier.width(8.dp)); Text(tr(p.locale, "openLessons")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = games, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(p.locale, "games")) }
            OutlinedButton(onClick = profile, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(p.locale, "myProfile")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = tools, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Icon(Icons.Default.Alarm, null); Spacer(Modifier.width(6.dp)); Text(tr(p.locale, "studyTools")) }
            OutlinedButton(onClick = calendar, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(6.dp))
                Text(when (p.locale) {
                    AcademyLocale.ENGLISH_US -> "Calendar"
                    AcademyLocale.DARI_AF -> "تقویم"
                    AcademyLocale.PASHTO_AF -> "کلیزه"
                    AcademyLocale.ARABIC -> "التقويم"
                })
            }
        }
    }
}
@Composable private fun Stat(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        shadowElevation = 5.dp,
        tonalElevation = 3.dp
    ) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(AsciiDigits.normalize(value), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
@Composable private fun MiniStat(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(AsciiDigits.normalize(value), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LearnPage(locale: AcademyLocale, selected: LearningBand?, completedLessonIds: Set<String>, select: (LearningBand?) -> Unit, vocabulary: () -> Unit, grammar: () -> Unit, openSpeaking: (String) -> Unit, complete: (String) -> Unit, saveStudySession: (String, Long, Long) -> Unit, quiz: () -> Unit) {
    val names = when (locale) {
        AcademyLocale.ENGLISH_US -> listOf("Pre-A1", "A1", "A2", "B1", "B2", "C1", "C2", "Specialized")
        AcademyLocale.DARI_AF -> listOf("پیش از A1", "A1", "A2", "B1", "B2", "C1", "C2", "تخصصی")
        AcademyLocale.PASHTO_AF -> listOf("له A1 مخکې", "A1", "A2", "B1", "B2", "C1", "C2", "ځانګړې زده‌کړه")
        AcademyLocale.ARABIC -> listOf("ما قبل A1", "A1", "A2", "B1", "B2", "C1", "C2", "تخصصي")
    }
    val lessons = selected?.let { AcademyLessonCatalog.forBand(it) }.orEmpty()
    Page {
        Text(tr(locale, "learningPath"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = grammar, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Icon(Icons.Default.Spellcheck, null); Spacer(Modifier.width(8.dp)); Text(tr(locale, "grammarAcademy")) }
        OutlinedButton(onClick = vocabulary, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Icon(Icons.Default.Bookmarks, null); Spacer(Modifier.width(8.dp)); Text(tr(locale, "vocabulary")) }
        if (selected == null) {
            Text(tr(locale, "chooseLevel"))
            LearningBand.values().forEachIndexed { i, level ->
                val unlocked = AcademyLessonCatalog.isBandUnlocked(level, completedLessonIds)
                AcademyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(names[i], style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        if (!unlocked) Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
                    }
                    Text(tr(locale, "bandSub", names[i]))
                    if (!unlocked) Text(tr(locale, "levelLocked"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                    Button(onClick = { select(level) }, enabled = unlocked, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "viewLessons")) }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selected.name.replace("_", "-"), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = { select(null) }) { Text(tr(locale, "allLevels")) }
            }
            lessons.forEachIndexed { index, lesson ->
                val isCompleted = lesson.id in completedLessonIds
                val lessonUnlocked = AcademyLessonCatalog.isLessonUnlocked(selected, index, completedLessonIds)
                var expanded by remember(lesson.id) { mutableStateOf(false) }
                AcademyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(AsciiDigits.normalize("${index + 1}. ${lesson.localizedTitle.forLocale(locale)}"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (isCompleted) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        else if (!lessonUnlocked) Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(AsciiDigits.normalize(lesson.localizedObjective.forLocale(locale)), fontSize = 13.sp)
                    if (isCompleted) Text(tr(locale, "lessonDone"), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    if (!lessonUnlocked) Text(tr(locale, "lessonLocked"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    TextButton(onClick = { expanded = !expanded }, enabled = lessonUnlocked || isCompleted) {
                        Text(if (expanded) tr(locale, "hideDetails") else tr(locale, "lessonDetails"))
                    }
                    if (expanded) {
                        Text(AsciiDigits.normalize(lesson.explanation))
                        Text(tr(locale, "newWords"), fontWeight = FontWeight.Bold)
                        lesson.words.forEach { word ->
                            Text("${word.english} — ${word.dari}  (${word.pronunciation})", fontSize = 13.sp)
                        }
                        Text(AsciiDigits.normalize(lesson.example), fontWeight = FontWeight.SemiBold)
                        Text(AsciiDigits.normalize(lesson.translation))
                        Text(AsciiDigits.normalize(lesson.grammarTip))
                        AcademyCard {
                            Text(tr(locale, "speakingLesson"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(AsciiDigits.normalize(lesson.example), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(AsciiDigits.normalize(lesson.translation), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { openSpeaking(lesson.example) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                                Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text(tr(locale, "practiceSpeaking"))
                            }
                        }
                        LessonStudyTimer(
                            lessonId = lesson.id,
                            locale = locale,
                            modifier = Modifier.fillMaxWidth(),
                            onSaveSession = saveStudySession
                        )
                    }
                    Button(
                        onClick = { complete(lesson.id) },
                        enabled = lessonUnlocked && !isCompleted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(6.dp))
                        Text(tr(locale, if (isCompleted) "lessonDone" else "completeLesson"))
                    }
                }
            }
            OutlinedButton(onClick = quiz, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(locale, "quizReview")) }
        }
    }
}

@Composable
private fun GrammarPage(locale: AcademyLocale, onBack: () -> Unit) {
    var selectedBand by remember { mutableStateOf<LearningBand?>(null) }
    Page {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp)); Text(tr(locale, "backToLessons")) }
        Text(tr(locale, "grammarAcademy"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(tr(locale, "grammarIntro"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { selectedBand = null }, shape = RoundedCornerShape(22.dp)) { Text(tr(locale, "allLevels")) }
            LearningBand.values().forEach { level ->
                OutlinedButton(onClick = { selectedBand = level }, shape = RoundedCornerShape(22.dp)) { Text(level.name.replace("_", "-")) }
            }
        }
        (selectedBand?.let { AcademyGrammarCatalog.forBand(it) } ?: AcademyGrammarCatalog.all).forEach { lesson ->
            var answer by remember(lesson.id) { mutableIntStateOf(-1) }
            AcademyCard {
                Text(lesson.band.name.replace("_", "-"), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(lesson.title.forLocale(locale), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(lesson.explanation.forLocale(locale))
                Text(lesson.example, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(lesson.translation.forLocale(locale), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Divider()
                Text(lesson.question.forLocale(locale), fontWeight = FontWeight.Bold)
                lesson.options.forEachIndexed { index, option ->
                    OutlinedButton(onClick = { answer = index }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(if (answer == index) 2.dp else 1.dp, if (answer == index && answer == lesson.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                        Text(option.forLocale(locale))
                    }
                }
                if (answer >= 0) Text(if (answer == lesson.correctIndex) tr(locale, "grammarCorrect") else tr(locale, "grammarTryAgain"),
                    color = if (answer == lesson.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                if (answer >= 0) TextButton(onClick = { answer = -1 }) { Text(tr(locale, "grammarRetry")) }
            }
        }
    }
}

@Composable
private fun VocabularyPage(locale: AcademyLocale, profileId: String, dao: AcademyDao) {
    val words by dao.observeVocabulary(profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(profileId) {
        AcademyLessonCatalog.all.forEach { lesson ->
            lesson.words.forEach { word ->
                dao.insertVocabularyIfMissing(VocabularyEntity(
                    profileId = profileId,
                    wordId = "${lesson.id}_${word.english.lowercase().replace(" ", "_")}",
                    english = word.english,
                    dariMeaning = word.dari,
                    pronunciation = word.pronunciation,
                    exampleEnglish = lesson.example,
                    exampleDari = lesson.translation,
                    categoryId = lesson.band.name
                ))
            }
        }
    }
    val filtered = words.filter { it.english.contains(search, ignoreCase = true) || it.dariMeaning.contains(search, ignoreCase = true) }
    Page {
        Text(tr(locale, "vocabulary"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(locale, "searchWords")) }, singleLine = true)
        Text(tr(locale, "wordCount", AsciiDigits.normalize("${filtered.size}")), fontSize = 12.sp)
        filtered.forEach { word ->
            AcademyCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(word.english, style = MaterialTheme.typography.titleMedium)
                        Text(word.dariMeaning)
                        Text(word.pronunciation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
                    }
                    IconButton(onClick = {
                        scope.launch { dao.saveVocabulary(word.copy(favourite = !word.favourite)) }
                    }) { Icon(if (word.favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null) }
                }
                Text(word.exampleEnglish, fontWeight = FontWeight.SemiBold)
                Text(word.exampleDari, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (locale) {
                            AcademyLocale.ENGLISH_US -> "Mastery: ${word.mastery}/5"
                            AcademyLocale.DARI_AF -> "میزان یادگیری: ${word.mastery}/5"
                            AcademyLocale.PASHTO_AF -> "د زده کړې کچه: ${word.mastery}/5"
                            AcademyLocale.ARABIC -> "مستوى الإتقان: ${word.mastery}/5"
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )
                    Button(onClick = {
                        scope.launch {
                            dao.saveVocabulary(word.copy(
                                mastery = (word.mastery + 1).coerceAtMost(5),
                                lastPractisedAt = System.currentTimeMillis()
                            ))
                        }
                    }) {
                        Text(when (locale) {
                            AcademyLocale.ENGLISH_US -> "Practised"
                            AcademyLocale.DARI_AF -> "تمرین شد"
                            AcademyLocale.PASHTO_AF -> "تمرین شو"
                            AcademyLocale.ARABIC -> "تم التدريب"
                        })
                    }
                }
            }
        }
    }
}

private fun gameEmoji(style: GameStyle): String = when (style) {
    GameStyle.MEANING -> "🧠"
    GameStyle.SPELLING -> "✍️"
    GameStyle.PICTURE -> "🖼️"
    GameStyle.MISSING_WORD -> "🧩"
    GameStyle.SENTENCE -> "📝"
    GameStyle.ODD_ONE_OUT -> "🔍"
    GameStyle.TRANSLATION -> "🌍"
    GameStyle.WORD_ORDER -> "🔠"
    GameStyle.MATCHING -> "🧩"
    GameStyle.CLUE -> "🔎"
    GameStyle.MEMORY -> "🧠"
    GameStyle.SPEED -> "⚡"
    GameStyle.CROSSWORD -> "🔤"
    GameStyle.WORD_BUILD -> "🔡"
    GameStyle.MISTAKE_REVIEW -> "🔁"
    GameStyle.SPELLING_CHECK -> "✅"
    GameStyle.VOCAB_RACE -> "🏁"
    GameStyle.LISTENING -> "🎧"
}

@Composable
private fun GameArtBadge(emoji: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size * 0.48f).sp)
    }
}

@Composable
private fun GamePage(locale: AcademyLocale, profileId: String, coins: Long, dao: AcademyDao) {
    val scope = rememberCoroutineScope()
    var selectedGameId by remember { mutableStateOf<String?>(null) }
    val selectedMode = AcademyGameCatalog.modes.firstOrNull { it.id == selectedGameId }
    var round by remember(selectedGameId) { mutableIntStateOf(0) }
    var score by remember(selectedGameId) { mutableIntStateOf(0) }
    var answered by remember(selectedGameId, round) { mutableStateOf(false) }
    var hintCount by remember(selectedGameId, round) { mutableIntStateOf(0) }
    var hintPending by remember(selectedGameId, round) { mutableStateOf(false) }
    var feedback by remember(selectedGameId, round) { mutableStateOf("") }
    var gameFinished by remember(selectedGameId) { mutableStateOf(false) }
    var secondsLeft by remember(selectedGameId, round) { mutableIntStateOf(20) }
    var typedAnswer by remember(selectedGameId, round) { mutableStateOf("") }
    val question = selectedMode?.let { AcademyGameCatalog.questionFor(it, round) }

    LaunchedEffect(selectedGameId, round, answered) {
        if (selectedMode?.timed == true && !answered && !gameFinished) {
            while (secondsLeft > 0) {
                delay(1_000L)
                secondsLeft -= 1
            }
            if (!answered && secondsLeft == 0) {
                answered = true
                feedback = when (locale) {
                    AcademyLocale.ENGLISH_US -> "Time is up. Review the answer and continue."
                    AcademyLocale.DARI_AF -> "زمان تمام شد. پاسخ را مرور کن و ادامه بده."
                    AcademyLocale.PASHTO_AF -> "وخت پای ته ورسېد. ځواب وګورئ او دوام ورکړئ."
                    AcademyLocale.ARABIC -> "انتهى الوقت. راجع الإجابة ثم تابع."
                }
            }
        }
    }

    LaunchedEffect(gameFinished) {
        val mode = selectedMode
        if (gameFinished && mode != null) {
            dao.insertGameAttempt(
                GameAttemptEntity(
                    attemptId = UUID.randomUUID().toString(),
                    profileId = profileId,
                    gameId = mode.id,
                    score = score,
                    totalQuestions = 5,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    Page {
        Text(tr(locale, "games"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (selectedMode == null) {
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "Choose a mini-game to practise English."
                    AcademyLocale.DARI_AF -> "یک بازی را انتخاب کن و انگلیسی را با سرگرمی تمرین کن."
                    AcademyLocale.PASHTO_AF -> "د انګلیسي تمرین لپاره یوه لوبه وټاکئ."
                    AcademyLocale.ARABIC -> "اختر لعبة قصيرة لممارسة الإنجليزية."
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f)
            )
            AcademyGameCatalog.modes.forEach { mode ->
                AcademyCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GameArtBadge(gameEmoji(mode.style), 76)
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(mode.title.forLocale(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(mode.description.forLocale(locale), fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (mode.timed) when (locale) {
                                        AcademyLocale.ENGLISH_US -> "Timed · 20 sec"
                                        AcademyLocale.DARI_AF -> "زمان‌دار · 20 ثانیه"
                                        AcademyLocale.PASHTO_AF -> "وخت لرونکی · 20 ثانیې"
                                        AcademyLocale.ARABIC -> "مؤقت · 20 ثانية"
                                    } else when (locale) {
                                        AcademyLocale.ENGLISH_US -> "5 questions"
                                        AcademyLocale.DARI_AF -> "5 پرسش"
                                        AcademyLocale.PASHTO_AF -> "5 پوښتنې"
                                        AcademyLocale.ARABIC -> "5 أسئلة"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(onClick = { selectedGameId = mode.id }) {
                                    Text(
                                        when (locale) {
                                            AcademyLocale.ENGLISH_US -> "Play"
                                            AcademyLocale.DARI_AF -> "بازی"
                                            AcademyLocale.PASHTO_AF -> "لوبه"
                                            AcademyLocale.ARABIC -> "ابدأ"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (gameFinished) {
            AcademyCard {
                Text(selectedMode.title.forLocale(locale), style = MaterialTheme.typography.titleLarge)
                Text(
                    when (locale) {
                        AcademyLocale.ENGLISH_US -> "Game complete"
                        AcademyLocale.DARI_AF -> "بازی تکمیل شد"
                        AcademyLocale.PASHTO_AF -> "لوبه بشپړه شوه"
                        AcademyLocale.ARABIC -> "اكتملت اللعبة"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    AsciiDigits.normalize("$score / 5"),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (locale) {
                        AcademyLocale.ENGLISH_US -> "Your result has been saved."
                        AcademyLocale.DARI_AF -> "نتیجه بازی در تاریخچه ذخیره شد."
                        AcademyLocale.PASHTO_AF -> "د لوبې پایله خوندي شوه."
                        AcademyLocale.ARABIC -> "تم حفظ نتيجة اللعبة."
                    }
                )
                Button(onClick = { selectedGameId = null }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        when (locale) {
                            AcademyLocale.ENGLISH_US -> "Back to games"
                            AcademyLocale.DARI_AF -> "بازگشت به بازی‌ها"
                            AcademyLocale.PASHTO_AF -> "لوبو ته بېرته"
                            AcademyLocale.ARABIC -> "العودة إلى الألعاب"
                        }
                    )
                }
            }
        } else if (question != null) {
            AcademyCard {
                Text(selectedMode.title.forLocale(locale), style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(AsciiDigits.normalize("Question ${round + 1} / 5"), modifier = Modifier.weight(1f), fontSize = 12.sp)
                    if (selectedMode.timed) {
                        Text(
                            AsciiDigits.normalize("${secondsLeft}s"),
                            color = if (secondsLeft <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { (round + 1) / 5f },
                    modifier = Modifier.fillMaxWidth()
                )
                GameArtBadge(question.visualEmoji, 96)
                Text(question.prompt.forLocale(locale), style = MaterialTheme.typography.titleMedium)
                if (selectedMode.style == GameStyle.WORD_BUILD) {
                    val scrambled = question.hint.forLocale(locale)
                    Text(scrambled, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        when (locale) {
                            AcademyLocale.ENGLISH_US -> "Use the letter clue and choose the correct spelling below."
                            AcademyLocale.DARI_AF -> "با استفاده از راهنمای حرف، املای درست را از گزینه‌ها انتخاب کن."
                            AcademyLocale.PASHTO_AF -> "د توري له نښې ګټه واخلئ او سمه املا وټاکئ."
                            AcademyLocale.ARABIC -> "استخدم تلميح الحرف واختر التهجئة الصحيحة."
                        },
                        fontSize = 12.sp
                    )
                }
                if (selectedMode.style != GameStyle.WORD_BUILD) {
                question.options.forEachIndexed { optionIndex, option ->
                    OutlinedButton(
                        onClick = {
                            if (!answered) {
                                answered = true
                                val isCorrect = optionIndex == question.correctIndex
                                if (isCorrect) {
                                    score += 1
                                    feedback = question.explanation.forLocale(locale)
                                    scope.launch {
                                        dao.applyRewardOnce(
                                            RewardEventEntity(
                                                eventId = "game_${profileId}_${selectedMode.id}_$round",
                                                profileId = profileId,
                                                source = "GAME",
                                                sourceId = "${selectedMode.id}_round_${round}",
                                                xpDelta = 10,
                                                coinDelta = 2,
                                                createdAt = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                } else {
                                    feedback = when (locale) {
                                        AcademyLocale.ENGLISH_US -> "Not quite. "
                                        AcademyLocale.DARI_AF -> "این پاسخ درست نیست. "
                                        AcademyLocale.PASHTO_AF -> "دا ځواب سم نه دی. "
                                        AcademyLocale.ARABIC -> "ليست هذه الإجابة الصحيحة. "
                                    } + question.explanation.forLocale(locale)
                                }
                            }
                        },
                        enabled = !answered,
                        modifier = Modifier.fillMaxWidth()
                    , shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                        Text(option.forLocale(locale))
                    }
                }
                }
                if (selectedMode.style == GameStyle.WORD_BUILD) {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { typedAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(when (locale) {
                            AcademyLocale.ENGLISH_US -> "Type the word"
                            AcademyLocale.DARI_AF -> "واژه را بنویس"
                            AcademyLocale.PASHTO_AF -> "کلمه ولیکئ"
                            AcademyLocale.ARABIC -> "اكتب الكلمة"
                        }) }
                    )
                    Button(
                        onClick = {
                            if (!answered && typedAnswer.isNotBlank()) {
                                answered = true
                                val isCorrect = typedAnswer.trim().equals(question.answer, ignoreCase = true)
                                if (isCorrect) {
                                    score += 1
                                    feedback = question.explanation.forLocale(locale)
                                    scope.launch {
                                        dao.applyRewardOnce(RewardEventEntity(
                                            eventId = "game_${profileId}_${selectedMode.id}_$round",
                                            profileId = profileId,
                                            source = "GAME",
                                            sourceId = selectedMode.id + "_round_" + round,
                                            xpDelta = 10,
                                            coinDelta = 2,
                                            createdAt = System.currentTimeMillis()
                                        ))
                                    }
                                } else {
                                    feedback = when (locale) {
                                        AcademyLocale.ENGLISH_US -> "Not quite. Correct answer: " + question.answer + ". "
                                        AcademyLocale.DARI_AF -> "پاسخ درست نیست. جواب درست: " + question.answer + ". "
                                        AcademyLocale.PASHTO_AF -> "سم نه دی. سم ځواب: " + question.answer + ". "
                                        AcademyLocale.ARABIC -> "ليست صحيحة. الإجابة الصحيحة: " + question.answer + ". "
                                    } + question.explanation.forLocale(locale)
                                }
                            }
                        },
                        enabled = !answered && typedAnswer.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 7.dp, pressedElevation = 2.dp)
                    ) {
                        Text(when (locale) {
                            AcademyLocale.ENGLISH_US -> "Check spelling"
                            AcademyLocale.DARI_AF -> "بررسی واژه"
                            AcademyLocale.PASHTO_AF -> "کلمه وازمویئ"
                            AcademyLocale.ARABIC -> "تحقق من الكلمة"
                        })
                    }
                }
                if (hintCount > 0) {
                    if (selectedMode.style != GameStyle.WORD_BUILD) Text(question.hint.forLocale(locale), color = MaterialTheme.colorScheme.primary)
                    Text(
                        AsciiDigits.normalize(question.answer.take(hintCount).padEnd(question.answer.length, '_')),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                if (feedback.isNotEmpty()) Text(feedback, fontWeight = FontWeight.SemiBold)
                if (!answered) {
                    OutlinedButton(
                        onClick = {
                            if (hintCount < question.answer.length && !hintPending) {
                                hintPending = true
                                scope.launch {
                                    if (dao.spendHintCoins(profileId) > 0) {
                                        hintCount += 1
                                        feedback = when (locale) {
                                            AcademyLocale.ENGLISH_US -> "Hint unlocked. Exactly 6 coins spent."
                                            AcademyLocale.DARI_AF -> "راهنما فعال شد؛ دقیقاً 6 سکه مصرف شد."
                                            AcademyLocale.PASHTO_AF -> "لارښوونه فعاله شوه؛ 6 سکې ولګېدې."
                                            AcademyLocale.ARABIC -> "تم فتح التلميح؛ خُصمت 6 عملات."
                                        }
                                    } else {
                                        feedback = tr(locale, "needCoins")
                                    }
                                    hintPending = false
                                }
                            }
                        },
                        enabled = hintCount < question.answer.length && !hintPending,
                        modifier = Modifier.fillMaxWidth()
                    , shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                        Text(
                            when (locale) {
                                AcademyLocale.ENGLISH_US -> "Hint · 6 coins"
                                AcademyLocale.DARI_AF -> "راهنما · 6 سکه"
                                AcademyLocale.PASHTO_AF -> "لارښوونه · 6 سکې"
                                AcademyLocale.ARABIC -> "تلميح · 6 عملات"
                            }
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (round == 4) gameFinished = true else round += 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when (locale) {
                                AcademyLocale.ENGLISH_US -> if (round == 4) "Finish game" else "Next question"
                                AcademyLocale.DARI_AF -> if (round == 4) "پایان بازی" else "پرسش بعدی"
                                AcademyLocale.PASHTO_AF -> if (round == 4) "لوبه پای ته رسول" else "بله پوښتنه"
                                AcademyLocale.ARABIC -> if (round == 4) "إنهاء اللعبة" else "السؤال التالي"
                            }
                        )
                    }
                }
                Text(tr(locale, "coinBalance", AsciiDigits.normalize("$coins")), fontSize = 12.sp)
                TextButton(onClick = { selectedGameId = null }) {
                    Text(
                        when (locale) {
                            AcademyLocale.ENGLISH_US -> "Exit game"
                            AcademyLocale.DARI_AF -> "خروج از بازی"
                            AcademyLocale.PASHTO_AF -> "له لوبې وتل"
                            AcademyLocale.ARABIC -> "الخروج من اللعبة"
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun QuizPage(locale: AcademyLocale, profileId: String, dao: AcademyDao, onFinished: () -> Unit) {
    data class Question(
        val type: LocalizedText,
        val prompt: LocalizedText,
        val options: List<LocalizedText>,
        val correct: Int,
        val explanation: LocalizedText
    )
    fun text(en: String, fa: String, ps: String, ar: String) = LocalizedText(en, fa, ps, ar)
    val questions = listOf(
        Question(
            text("Vocabulary", "واژگان", "لغتونه", "المفردات"),
            text("What does APPLE mean?", "معنی APPLE چیست؟", "د APPLE مانا څه ده؟", "ما معنى APPLE؟"),
            listOf(text("Apple / سیب", "سیب", "مڼه", "تفاحة"), text("Book / کتاب", "کتاب", "کتاب", "كتاب"), text("Water / آب", "آب", "اوبه", "ماء")),
            0,
            text("APPLE means سیب.", "APPLE به معنی سیب است.", "APPLE د مڼې په مانا دی.", "APPLE تعني تفاحة.")
        ),
        Question(
            text("True or false", "درست یا نادرست", "سم یا ناسم", "صح أم خطأ"),
            text("Blue is a colour.", "Blue یک رنگ است.", "Blue یو رنګ دی.", "Blue لون."),
            listOf(text("True", "درست", "سم", "صحيح"), text("False", "نادرست", "ناسم", "خطأ")),
            0,
            text("Blue means آبی.", "Blue به معنی آبی است.", "Blue د آبي رنګ مانا لري.", "Blue تعني الأزرق.")
        ),
        Question(
            text("Fill in the blank", "جای خالی را پر کن", "تش ځای ډک کړئ", "أكمل الفراغ"),
            text("She ___ a student.", "She ___ a student.", "She ___ a student.", "She ___ a student."),
            listOf(text("am", "am", "am", "am"), text("is", "is", "is", "is"), text("are", "are", "are", "are")),
            1,
            text("Use IS with she, he and it.", "با she، he و it از IS استفاده می‌کنیم.", "له she، he او it سره IS کاروو.", "نستخدم IS مع she وhe وit.")
        ),
        Question(
            text("Translation", "ترجمه", "ژباړه", "الترجمة"),
            text("Choose the translation of: Good morning.", "ترجمه Good morning را انتخاب کن.", "د Good morning ژباړه وټاکئ.", "اختر ترجمة Good morning."),
            listOf(text("صبح بخیر", "صبح بخیر", "سهار مو پخیر", "صباح الخير"), text("شب بخیر", "شب بخیر", "شپه مو پخیر", "تصبح على خير"), text("خداحافظ", "خداحافظ", "خدای پامان", "مع السلامة")),
            0,
            text("Good morning is used in the morning.", "Good morning برای صبح استفاده می‌شود.", "Good morning د سهار لپاره کارېږي.", "تُستخدم Good morning في الصباح.")
        ),
        Question(
            text("Sentence building", "جمله‌سازی", "د جملې جوړول", "تركيب الجملة"),
            text("Choose the correct sentence: (I / tea / drink)", "جمله درست را انتخاب کن: (I / tea / drink)", "سمه جمله وټاکئ: (I / tea / drink)", "اختر الجملة الصحيحة: (I / tea / drink)"),
            listOf(text("I drink tea.", "I drink tea.", "I drink tea.", "I drink tea."), text("Tea I drink.", "Tea I drink.", "Tea I drink.", "Tea I drink."), text("Drink I tea.", "Drink I tea.", "Drink I tea.", "Drink I tea.")),
            0,
            text("The usual order is subject + verb + object.", "ترتیب معمول جمله فاعل + فعل + مفعول است.", "د جملې عادي ترتیب فاعل + فعل + مفعول دی.", "الترتيب المعتاد هو الفاعل ثم الفعل ثم المفعول.")
        )
    )
    val quizQuestions = remember(locale) {
        questions.mapIndexed { questionIndex, question ->
            val rotation = (questionIndex + 1) % question.options.size
            val rotatedOptions = question.options.drop(rotation) + question.options.take(rotation)
            question.copy(
                options = rotatedOptions,
                correct = (question.correct - rotation + question.options.size) % question.options.size
            )
        }
    }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var explanation by remember { mutableStateOf("") }
    val incorrectIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(finished) {
        if (finished) {
            val attemptId = UUID.randomUUID().toString()
            dao.insertQuizAttempt(QuizAttemptEntity(
                attemptId = attemptId,
                profileId = profileId,
                quizId = "starter_mixed_quiz",
                score = score,
                totalQuestions = quizQuestions.size,
                completedAt = System.currentTimeMillis(),
                incorrectQuestionIds = incorrectIds.joinToString(",")
            ))
            if (score >= 3) {
                val rewardDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                dao.applyRewardOnce(RewardEventEntity(
                    eventId = "quiz_${profileId}_starter_mixed_quiz_$rewardDate",
                    profileId = profileId,
                    source = "QUIZ",
                    sourceId = "starter_mixed_quiz_$rewardDate",
                    xpDelta = 30,
                    coinDelta = 6,
                    createdAt = System.currentTimeMillis()
                ))
            }
        }
    }
    Page {
        Text(tr(locale, "quizTitle"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            if (finished) {
                Text(tr(locale, "quizComplete"), style = MaterialTheme.typography.titleLarge)
                Text("${AsciiDigits.normalize(score.toString())}/${quizQuestions.size}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Text(tr(locale, "quizSaved"))
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "seeProgress")) }
            } else {
                val question = quizQuestions[index]
                Text(tr(locale, "questionProgress", "${index + 1}/${quizQuestions.size}"))
                LinearProgressIndicator(progress = { (index + 1).toFloat() / quizQuestions.size }, modifier = Modifier.fillMaxWidth())
                Text(question.type.forLocale(locale), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(question.prompt.forLocale(locale), style = MaterialTheme.typography.titleLarge)
                question.options.forEachIndexed { optionIndex, option ->
                    OutlinedButton(
                        onClick = {
                            if (selectedAnswer == -1) {
                                selectedAnswer = optionIndex
                                if (optionIndex == question.correct) score += 1 else incorrectIds.add("q_${index + 1}")
                                explanation = question.explanation.forLocale(locale)
                            }
                        },
                        enabled = selectedAnswer == -1,
                        modifier = Modifier.fillMaxWidth()
                    , shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(option.forLocale(locale)) }
                }
                if (explanation.isNotEmpty()) {
                    Text(explanation, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = {
                        if (index == quizQuestions.lastIndex) finished = true
                        else { index += 1; selectedAnswer = -1; explanation = "" }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (index == quizQuestions.lastIndex) tr(locale, "finishQuiz") else tr(locale, "nextQuestion"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressPage(locale: AcademyLocale, lessons: Int, attempts: List<QuizAttemptEntity>, games: Int, todayMillis: Long, weekMillis: Long, monthMillis: Long, totalStudyMillis: Long, xp: Long, coins: Long) {
    Page {
        Text(tr(locale, "progress"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text(tr(locale, "summary"), style = MaterialTheme.typography.titleLarge)
            Line(tr(locale, "lessons"), "$lessons")
            Line(tr(locale, "quizzes"), "${attempts.size}")
            Line(tr(locale, "games"), "${games}")
            val totalMinutes = totalStudyMillis / 60_000L
            Line(tr(locale, "studyToday"), "${todayMillis / 60000L} min")
            Line(tr(locale, "studyWeek"), "${weekMillis / 60000L} min")
            Line(tr(locale, "studyMonth"), "${monthMillis / 60000L} min")
            Line(tr(locale, "studyTotal"), "${totalMinutes / 60}h ${totalMinutes % 60}m")
            Line("XP", "$xp")
            Line(tr(locale, "coins"), "$coins")
            val average = if (attempts.isEmpty()) 0 else attempts.map { if (it.totalQuestions == 0) 0 else it.score * 100 / it.totalQuestions }.average().toInt()
            Line(tr(locale, "average"), "$average%")
        }
        AcademyCard {
            Text(tr(locale, "badges"), style = MaterialTheme.typography.titleLarge)
            val badges = listOf(
                Triple(lessons >= 1, "🌱", tr(locale, "badgeFirstLesson")),
                Triple(lessons >= 5, "📚", tr(locale, "badgeFiveLessons")),
                Triple(attempts.isNotEmpty(), "📝", tr(locale, "badgeFirstQuiz")),
                Triple(games >= 1, "🎮", tr(locale, "badgeFirstGame")),
                Triple(totalStudyMillis >= 3_600_000L, "⏳", tr(locale, "badgeHour"))
            )
            val earned = badges.filter { it.first }
            if (earned.isEmpty()) Text(tr(locale, "noBadges"))
            earned.forEach { (_, icon, title) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(icon, fontSize = 24.sp)
                    Text(title, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        AcademyCard {
            Text(tr(locale, "quizHistory"), style = MaterialTheme.typography.titleMedium)
            if (attempts.isEmpty()) Text(tr(locale, "noHistory")) else attempts.take(15).forEach { Text("${it.quizId}: ${it.score}/${it.totalQuestions}") }
        }
    }
}
@Composable private fun Line(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(AsciiDigits.normalize(value), fontWeight = FontWeight.Bold) }
}


@Composable
private fun CalendarPage(locale: AcademyLocale, profileId: String, dao: AcademyDao, onBack: () -> Unit) {
    val sessions by dao.observeStudySessions(profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val reminders by dao.observeReminders(profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    var monthOffset by remember { mutableIntStateOf(0) }
    val month = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val studiedDates = remember(sessions) {
        sessions.map { dateFormat.format(Date(it.startedAt)) }.toSet()
    }
    var selectedDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var lastMonthOffset by remember { mutableIntStateOf(0) }
    LaunchedEffect(monthOffset) {
        if (monthOffset != lastMonthOffset) {
            selectedDate = dateFormat.format(month.time)
            lastMonthOffset = monthOffset
        }
    }
    val firstOffset = (month.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthTitle = AsciiDigits.normalize(
        SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag(locale.languageTag)).format(month.time)
    )
    val dayHeaders = when (locale) {
        AcademyLocale.ENGLISH_US -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        AcademyLocale.DARI_AF -> listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        AcademyLocale.PASHTO_AF -> listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        AcademyLocale.ARABIC -> listOf("الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
    }
    val selectedSessions = sessions.filter { dateFormat.format(Date(it.startedAt)) == selectedDate }
    val selectedMinutes = selectedSessions.sumOf { it.durationMillis } / 60_000L
    val title = when (locale) {
        AcademyLocale.ENGLISH_US -> "Study calendar"
        AcademyLocale.DARI_AF -> "تقویم مطالعه"
        AcademyLocale.PASHTO_AF -> "د مطالعې کلیزه"
        AcademyLocale.ARABIC -> "تقويم الدراسة"
    }
    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text(tr(locale, "home")) }
        }
        AcademyCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthOffset -= 1 }) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
                Text(monthTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick = { monthOffset += 1 }) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                dayHeaders.forEach { day ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(day, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
            val weekCount = (firstOffset + daysInMonth + 6) / 7
            repeat(weekCount) { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(7) { column ->
                        val day = week * 7 + column - firstOffset + 1
                        if (day in 1..daysInMonth) {
                            val date = Calendar.getInstance().apply {
                                set(Calendar.YEAR, month.get(Calendar.YEAR))
                                set(Calendar.MONTH, month.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            val iso = dateFormat.format(date.time)
                            val studied = iso in studiedDates
                            val selected = iso == selectedDate
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            selected -> MaterialTheme.colorScheme.primary
                                            studied -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable { selectedDate = iso },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(
                                        AsciiDigits.normalize(day.toString()),
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (studied) Text("•", color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "Marked days show saved study sessions."
                    AcademyLocale.DARI_AF -> "روزهای نشان‌شده دارای جلسه مطالعه ثبت‌شده هستند."
                    AcademyLocale.PASHTO_AF -> "نښه شوې ورځې د مطالعې ثبت شوې ناستې لري."
                    AcademyLocale.ARABIC -> "الأيام المميزة تحتوي على جلسات دراسة محفوظة."
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AcademyCard {
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "Activity on $selectedDate"
                    AcademyLocale.DARI_AF -> "فعالیت در تاریخ $selectedDate"
                    AcademyLocale.PASHTO_AF -> "په $selectedDate کې فعالیت"
                    AcademyLocale.ARABIC -> "النشاط في $selectedDate"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "Study time: $selectedMinutes minutes"
                    AcademyLocale.DARI_AF -> "زمان مطالعه: $selectedMinutes دقیقه"
                    AcademyLocale.PASHTO_AF -> "د مطالعې وخت: $selectedMinutes دقیقې"
                    AcademyLocale.ARABIC -> "وقت الدراسة: $selectedMinutes دقيقة"
                }
            )
            if (selectedSessions.isEmpty()) {
                Text(
                    when (locale) {
                        AcademyLocale.ENGLISH_US -> "No study sessions saved for this day."
                        AcademyLocale.DARI_AF -> "برای این روز جلسه مطالعه ثبت نشده است."
                        AcademyLocale.PASHTO_AF -> "په دې ورځ د مطالعې ناسته نه ده ثبت شوې."
                        AcademyLocale.ARABIC -> "لا توجد جلسات دراسة محفوظة لهذا اليوم."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                selectedSessions.forEach { session ->
                    Text(
                        "${session.lessonId} · ${AsciiDigits.normalize((session.durationMillis / 60_000L).toString())} min",
                        fontSize = 12.sp
                    )
                }
            }
        }
        AcademyCard {
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "Scheduled reminders"
                    AcademyLocale.DARI_AF -> "یادآوری‌های تنظیم‌شده"
                    AcademyLocale.PASHTO_AF -> "ټاکل شوې یادونې"
                    AcademyLocale.ARABIC -> "التذكيرات المجدولة"
                },
                style = MaterialTheme.typography.titleMedium
            )
            if (reminders.isEmpty()) {
                Text(
                    when (locale) {
                        AcademyLocale.ENGLISH_US -> "No reminders yet."
                        AcademyLocale.DARI_AF -> "هنوز یادآوری تنظیم نشده است."
                        AcademyLocale.PASHTO_AF -> "تر اوسه یادونې نشته."
                        AcademyLocale.ARABIC -> "لا توجد تذكيرات بعد."
                    }
                )
            } else reminders.forEach { reminder ->
                Text("${reminder.title} · ${AsciiDigits.normalize("%02d:%02d".format(reminder.hour, reminder.minute))}")
            }
        }
    }
}

@Composable
private fun ToolsPage(locale: AcademyLocale, profileId: String, dao: AcademyDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reminders by dao.observeReminders(profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    var sessionStart by remember { mutableLongStateOf(0L) }
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("19:00") }
    var editingReminderId by remember { mutableStateOf<String?>(null) }
    var repeatDaysMask by remember { mutableIntStateOf(127) }
    var feedback by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) running = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            running = false
        }
    }

    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            elapsedSeconds += 1
            if (elapsedSeconds % 15L == 0L && sessionStart > 0L) {
                dao.insertStudySession(StudySessionEntity(
                    sessionId = sessionId, profileId = profileId, lessonId = "independent-study",
                    startedAt = sessionStart, durationMillis = elapsedSeconds * 1000L
                ))
            }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, tr(locale, "permissionNeeded"), Toast.LENGTH_LONG).show()
    }

    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr(locale, "studyTools"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text(tr(locale, "home")) }
        }
        AcademyCard {
            Text(tr(locale, "timer"), style = MaterialTheme.typography.titleLarge)
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            val time = listOf(hours, minutes, seconds).joinToString(":") { it.toString().padStart(2, '0') }
            Text(AsciiDigits.normalize(time), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (!running) {
                        if (sessionStart == 0L) sessionStart = System.currentTimeMillis()
                        running = true
                    }
                }, enabled = !running, modifier = Modifier.weight(1f)) { Text(tr(locale, "startTimer")) }
                OutlinedButton(onClick = { running = false }, enabled = running, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(locale, "pauseTimer")) }
            }
            Button(onClick = {
                running = false
                if (elapsedSeconds > 0L && sessionStart > 0L) {
                    scope.launch {
                        dao.insertStudySession(StudySessionEntity(
                            sessionId = sessionId, profileId = profileId, lessonId = "independent-study",
                            startedAt = sessionStart, durationMillis = elapsedSeconds * 1000L,
                            completedAt = System.currentTimeMillis()
                        ))
                    }
                    feedback = tr(locale, "timerSaved")
                }
                elapsedSeconds = 0L
                sessionStart = 0L
                sessionId = UUID.randomUUID().toString()
            }, enabled = elapsedSeconds > 0L, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "finishTimer")) }
            if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.primary)
        }
        AcademyCard {
            Text(tr(locale, "reminders"), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(reminderTitle, { reminderTitle = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(locale, "reminderTitle")) }, singleLine = true)
            OutlinedTextField(reminderTime, { reminderTime = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(locale, "timeHHMM")) }, singleLine = true)
            Text(tr(locale, "repeatDays"), fontSize = 12.sp)
            val dayLabels = when (locale) {
                AcademyLocale.ENGLISH_US -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                AcademyLocale.DARI_AF -> listOf("یک", "دو", "سه", "چهار", "پنج", "جمعه", "شنبه")
                AcademyLocale.PASHTO_AF -> listOf("یو", "دو", "درې", "څلور", "پنځه", "جمعه", "شنبه")
                AcademyLocale.ARABIC -> listOf("أحد", "إثن", "ثلا", "أرب", "خمي", "جمع", "سبت")
            }
            dayLabels.chunked(4).forEachIndexed { rowIndex, labels ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    labels.forEachIndexed { localIndex, label ->
                        val dayIndex = rowIndex * 4 + localIndex
                        FilterChip(
                            selected = (repeatDaysMask and (1 shl dayIndex)) != 0,
                            onClick = { repeatDaysMask = repeatDaysMask xor (1 shl dayIndex) },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (labels.size < 4) repeat(4 - labels.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            if (repeatDaysMask == 0) Text(tr(locale, "oneTimeReminder"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                val parts = reminderTime.trim().split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull()
                val minute = parts.getOrNull(1)?.toIntOrNull()
                if (reminderTitle.isBlank() || hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                    feedback = tr(locale, "invalidTime")
                } else {
                    val id = editingReminderId ?: UUID.randomUUID().toString()
                    val title = reminderTitle.trim()
                    val schedule = {
                        scope.launch {
                            dao.saveReminder(ReminderEntity(reminderId = id, profileId = profileId, title = title, hour = hour!!, minute = minute!!, repeatDaysMask = repeatDaysMask, enabled = true))
                        }
                        ReminderScheduler.cancel(context, id)
                        ReminderScheduler.schedule(context, id, title, hour!!, minute!!, repeatDaysMask)
                        feedback = tr(locale, if (editingReminderId == null) "reminderSaved" else "reminderUpdated")
                        reminderTitle = ""
                        reminderTime = "19:00"
                        repeatDaysMask = 127
                        editingReminderId = null
                    }
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        feedback = tr(locale, "permissionNeeded")
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            feedback = tr(locale, "exactAlarmPermission")
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    }
                    schedule()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(tr(locale, if (editingReminderId == null) "scheduleReminder" else "updateReminder"))
            }
            if (editingReminderId != null) {
                TextButton(onClick = {
                    editingReminderId = null
                    reminderTitle = ""
                    reminderTime = "19:00"
                    repeatDaysMask = 127
                }) { Text(tr(locale, "cancel")) }
            }
            if (reminders.isEmpty()) Text(tr(locale, "noReminders"))
            reminders.forEach { reminder ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(reminder.title, fontWeight = FontWeight.SemiBold)
                        Text(AsciiDigits.normalize("%02d:%02d".format(reminder.hour, reminder.minute)), fontSize = 12.sp)
                    }
                    Switch(
                        checked = reminder.enabled,
                        onCheckedChange = { enabled ->
                            scope.launch { dao.saveReminder(reminder.copy(enabled = enabled)) }
                            if (enabled) {
                                ReminderScheduler.schedule(context, reminder.reminderId, reminder.title, reminder.hour, reminder.minute, reminder.repeatDaysMask)
                            } else {
                                ReminderScheduler.cancel(context, reminder.reminderId)
                            }
                        }
                    )
                    IconButton(onClick = {
                        editingReminderId = reminder.reminderId
                        reminderTitle = reminder.title
                        reminderTime = "%02d:%02d".format(reminder.hour, reminder.minute)
                        repeatDaysMask = reminder.repeatDaysMask
                    }) { Icon(Icons.Default.Edit, contentDescription = "Edit reminder") }
                    IconButton(onClick = {
                        scope.launch { dao.deleteReminder(reminder.reminderId) }
                        ReminderScheduler.cancel(context, reminder.reminderId)
                        if (editingReminderId == reminder.reminderId) {
                            editingReminderId = null
                            reminderTitle = ""
                            reminderTime = "19:00"
                            repeatDaysMask = 127
                        }
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete reminder") }
                }
            }
            if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProfilePage(profile: UserProfile, profiles: List<UserProfile>, prefs: AcademyPreferences, dao: AcademyDao) {
    val scope = rememberCoroutineScope()
    var showCreateProfile by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileUsername by remember { mutableStateOf("") }
    var profileError by remember { mutableStateOf("") }
    val context = LocalContext.current
    val wallet by dao.observeWallet(profile.profileId).collectAsStateWithLifecycle(initialValue = null)
    val completedLessons by dao.observeLessonProgress(profile.profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val quizAttempts by dao.observeQuizAttempts(profile.profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val gameAttempts by dao.observeGameAttempts(profile.profileId).collectAsStateWithLifecycle(initialValue = emptyList())
    val totalStudyMillis by dao.observeTotalStudyMillis(profile.profileId).collectAsStateWithLifecycle(initialValue = 0L)
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var username by remember(profile.username) { mutableStateOf(profile.username) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            scope.launch { prefs.updateAvatar(uri.toString()) }
        }
    }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var resetStep by remember { mutableIntStateOf(0) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching { AcademyBackupManager.export(context, uri, prefs, dao, profile.profileId) }
                .onSuccess { Toast.makeText(context, tr(profile.locale, "backupExported"), Toast.LENGTH_LONG).show() }
                .onFailure { Toast.makeText(context, tr(profile.locale, "backupFailed"), Toast.LENGTH_LONG).show() }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImportUri = uri
    }
    Page {
        Text(tr(profile.locale, "profile"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Profiles"
                AcademyLocale.DARI_AF -> "پروفایل‌ها"
                AcademyLocale.PASHTO_AF -> "پروفایلونه"
                AcademyLocale.ARABIC -> "الملفات الشخصية"
            }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            profiles.forEach { item ->
                OutlinedButton(
                    onClick = { scope.launch { prefs.switchProfile(item.profileId) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(if (item.profileId == profile.profileId) 2.dp else 1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = if (item.profileId == profile.profileId) 0.8f else 0.3f))
                ) {
                    Text((if (item.profileId == profile.profileId) "✓  " else "") + item.displayName + "  @" + item.username)
                }
            }
            Button(onClick = { showCreateProfile = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(8.dp))
                Text(when (profile.locale) {
                    AcademyLocale.ENGLISH_US -> "Add another profile"
                    AcademyLocale.DARI_AF -> "افزودن پروفایل جدید"
                    AcademyLocale.PASHTO_AF -> "نوی پروفایل زیاتول"
                    AcademyLocale.ARABIC -> "إضافة ملف شخصي"
                })
            }
        }
        AcademyCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (profile.avatarUri?.startsWith("emoji:") == true) {
                    PuffyAvatar(emoji = profile.avatarUri.removePrefix("emoji:"), size = 82)
                } else if (profile.avatarUri != null) {
                    AsyncImage(model = profile.avatarUri, contentDescription = tr(profile.locale, "profile"),
                        modifier = Modifier.size(82.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    PuffyAvatar(emoji = "🌸", size = 82)
                }
                OutlinedButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }, shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(profile.locale, "changeAvatar")) }
            }
            Text(profile.displayName, style = MaterialTheme.typography.titleLarge)
            Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Choose your academy avatar"
                AcademyLocale.DARI_AF -> "آواتار دلخواهت را انتخاب کن"
                AcademyLocale.PASHTO_AF -> "خپل خوښ اوتار وټاکئ"
                AcademyLocale.ARABIC -> "اختر الصورة الرمزية"
            }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            val avatarChoices = listOf("👸", "🌸", "🧚‍♀️", "👩‍🎓", "🦋", "👑", "🧑‍🚀", "🧑‍🎓", "🦸", "⚽", "🚀", "🐯", "🐼", "🦊", "🎮", "🏆")
            avatarChoices.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { emoji ->
                        val selected = profile.avatarUri == "emoji:$emoji"
                        Surface(
                            modifier = Modifier.weight(1f).clickable { scope.launch { prefs.updateAvatar("emoji:$emoji") } },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .18f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            shadowElevation = if (selected) 5.dp else 1.dp
                        ) {
                            Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 28.sp)
                            }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            OutlinedTextField(displayName, { displayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(profile.locale, "displayName")) }, singleLine = true)
            OutlinedTextField(username, { username = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(profile.locale, "username")) }, singleLine = true)
            Button(onClick = {
                scope.launch {
                    prefs.updateDisplayName(displayName)
                    val saved = prefs.updateUsername(username)
                    if (!saved) {
                        val message = when (profile.locale) {
                            AcademyLocale.ENGLISH_US -> "Username is invalid or already in use."
                            AcademyLocale.DARI_AF -> "نام کاربری نادرست است یا قبلاً استفاده شده است."
                            AcademyLocale.PASHTO_AF -> "کارنوم ناسم دی یا مخکې کارول شوی دی."
                            AcademyLocale.ARABIC -> "اسم المستخدم غير صالح أو مستخدم بالفعل."
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(tr(profile.locale, "save")) }
        }
        AcademyCard {
            Text(
                when (profile.locale) {
                    AcademyLocale.ENGLISH_US -> "Learning statistics"
                    AcademyLocale.DARI_AF -> "آمار یادگیری"
                    AcademyLocale.PASHTO_AF -> "د زده کړې احصایې"
                    AcademyLocale.ARABIC -> "إحصاءات التعلم"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(tr(profile.locale, "level"), AsciiDigits.normalize((wallet?.level ?: 1).toString()))
                Stat("XP", AsciiDigits.normalize((wallet?.xp ?: 0L).toString()))
                Stat(tr(profile.locale, "coins"), AsciiDigits.normalize((wallet?.coins ?: 0L).toString()))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(tr(profile.locale, "lessons"), AsciiDigits.normalize(completedLessons.size.toString()), Modifier.weight(1f))
                MiniStat(tr(profile.locale, "quizzes"), AsciiDigits.normalize(quizAttempts.size.toString()), Modifier.weight(1f))
                MiniStat(tr(profile.locale, "games"), AsciiDigits.normalize(gameAttempts.size.toString()), Modifier.weight(1f))
            }
            val totalMinutes = totalStudyMillis / 60_000L
            Line(
                when (profile.locale) {
                    AcademyLocale.ENGLISH_US -> "Total study time"
                    AcademyLocale.DARI_AF -> "مجموع زمان مطالعه"
                    AcademyLocale.PASHTO_AF -> "د مطالعې ټول وخت"
                    AcademyLocale.ARABIC -> "إجمالي وقت الدراسة"
                },
                AsciiDigits.normalize("${totalMinutes / 60}h ${totalMinutes % 60}m")
            )
        }
        AcademyCard {
            Text(tr(profile.locale, "language"), style = MaterialTheme.typography.titleMedium)
            AcademyLocale.values().forEach { option ->
                OutlinedButton(onClick = { scope.launch { prefs.updateLocale(option) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                    Text(if (profile.locale == option) "✓  ${tr(profile.locale, option.name)}" else tr(profile.locale, option.name))
                }
            }
        }
        AcademyCard {
            Text(tr(profile.locale, "theme"), style = MaterialTheme.typography.titleMedium)
            AcademyThemeId.values().toList().chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pair.forEach { option ->
                        OutlinedButton(onClick = { scope.launch { prefs.updateTheme(option) } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                            Text(tr(profile.locale, option.name), fontSize = 9.sp, maxLines = 1)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        AcademyCard {
            Text(tr(profile.locale, "backupRestore"), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { exportLauncher.launch("bh-personal-academy-backup.json") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                Icon(Icons.Default.CloudDownload, null); Spacer(Modifier.width(8.dp)); Text(tr(profile.locale, "createBackup"))
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/json")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text(tr(profile.locale, "restoreBackup"))
            }
            Text(tr(profile.locale, "backupMergeNote"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
            OutlinedButton(onClick = { resetStep = 1 }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) {
                Icon(Icons.Default.DeleteForever, null); Spacer(Modifier.width(8.dp)); Text(tr(profile.locale, "resetData"))
            }
        }
        AcademyCard {
            Text(tr(profile.locale, "font"), style = MaterialTheme.typography.titleMedium)
            AcademyFontId.values().forEach { option ->
                OutlinedButton(onClick = { scope.launch { prefs.updateFont(option) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f))) { Text(tr(profile.locale, option.name)) }
            }
            Text(tr(profile.locale, "dailyGoal"), style = MaterialTheme.typography.titleMedium)
            var goal by remember(profile.dailyStudyGoalMinutes) { mutableFloatStateOf(profile.dailyStudyGoalMinutes.toFloat()) }
            Text("${AsciiDigits.normalize(goal.toInt().toString())} ${tr(profile.locale, "minutes")}")
            Slider(value = goal, onValueChange = { goal = it }, valueRange = 5f..120f, steps = 22,
                onValueChangeFinished = { scope.launch { prefs.updateDailyGoal(goal.toInt()) } })
            var lessonTarget by remember(profile.dailyGoalLessons) { mutableFloatStateOf(profile.dailyGoalLessons.toFloat()) }
            var wordTarget by remember(profile.dailyGoalWords) { mutableFloatStateOf(profile.dailyGoalWords.toFloat()) }
            var quizTarget by remember(profile.dailyGoalQuizzes) { mutableFloatStateOf(profile.dailyGoalQuizzes.toFloat()) }
            Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Daily lessons: ${lessonTarget.toInt()}"
                AcademyLocale.DARI_AF -> "درس روزانه: ${lessonTarget.toInt()}"
                AcademyLocale.PASHTO_AF -> "ورځني درسونه: ${lessonTarget.toInt()}"
                AcademyLocale.ARABIC -> "الدروس اليومية: ${lessonTarget.toInt()}"
            })
            Slider(value = lessonTarget, onValueChange = { lessonTarget = it }, valueRange = 1f..10f, steps = 8,
                onValueChangeFinished = { scope.launch { prefs.updateDailyTargets(lessonTarget.toInt(), wordTarget.toInt(), quizTarget.toInt()) } })
            Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Daily words: ${wordTarget.toInt()}"
                AcademyLocale.DARI_AF -> "واژه روزانه: ${wordTarget.toInt()}"
                AcademyLocale.PASHTO_AF -> "ورځنۍ کلمې: ${wordTarget.toInt()}"
                AcademyLocale.ARABIC -> "الكلمات اليومية: ${wordTarget.toInt()}"
            })
            Slider(value = wordTarget, onValueChange = { wordTarget = it }, valueRange = 1f..50f, steps = 48,
                onValueChangeFinished = { scope.launch { prefs.updateDailyTargets(lessonTarget.toInt(), wordTarget.toInt(), quizTarget.toInt()) } })
            Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Daily quizzes: ${quizTarget.toInt()}"
                AcademyLocale.DARI_AF -> "آزمون روزانه: ${quizTarget.toInt()}"
                AcademyLocale.PASHTO_AF -> "ورځنۍ ازموینې: ${quizTarget.toInt()}"
                AcademyLocale.ARABIC -> "الاختبارات اليومية: ${quizTarget.toInt()}"
            })
            Slider(value = quizTarget, onValueChange = { quizTarget = it }, valueRange = 1f..5f, steps = 3,
                onValueChangeFinished = { scope.launch { prefs.updateDailyTargets(lessonTarget.toInt(), wordTarget.toInt(), quizTarget.toInt()) } })
        }
    }
    if (showCreateProfile) {
        AlertDialog(
            onDismissRequest = { showCreateProfile = false; profileError = "" },
            title = { Text(when (profile.locale) {
                AcademyLocale.ENGLISH_US -> "Create profile"
                AcademyLocale.DARI_AF -> "ساخت پروفایل"
                AcademyLocale.PASHTO_AF -> "پروفایل جوړول"
                AcademyLocale.ARABIC -> "إنشاء ملف شخصي"
            }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(newProfileName, { newProfileName = it }, label = { Text(tr(profile.locale, "displayName")) }, singleLine = true)
                    OutlinedTextField(newProfileUsername, { newProfileUsername = it }, label = { Text(tr(profile.locale, "username")) }, singleLine = true)
                    if (profileError.isNotBlank()) Text(profileError, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        runCatching { prefs.createProfile(newProfileName, newProfileUsername) }
                            .onSuccess { id ->
                                prefs.switchProfile(id)
                                newProfileName = ""; newProfileUsername = ""; profileError = ""; showCreateProfile = false
                            }
                            .onFailure { profileError = it.message ?: "Could not create profile." }
                    }
                }, enabled = newProfileName.isNotBlank() && newProfileUsername.isNotBlank()) { Text(tr(profile.locale, "save")) }
            },
            dismissButton = { TextButton(onClick = { showCreateProfile = false; profileError = "" }) { Text(tr(profile.locale, "cancel")) } }
        )
    }
    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text(tr(profile.locale, "confirmRestore")) },
            text = { Text(tr(profile.locale, "restoreWarning")) },
            confirmButton = {
                Button(onClick = {
                    val uri = pendingImportUri
                    pendingImportUri = null
                    if (uri != null) scope.launch {
                        runCatching { AcademyBackupManager.restore(context, uri, prefs, dao, profile.profileId) }
                            .onSuccess { Toast.makeText(context, tr(profile.locale, "restoreSuccess"), Toast.LENGTH_LONG).show() }
                            .onFailure { Toast.makeText(context, tr(profile.locale, "backupFailed"), Toast.LENGTH_LONG).show() }
                    }
                }) { Text(tr(profile.locale, "confirmRestore")) }
            },
            dismissButton = { TextButton(onClick = { pendingImportUri = null }) { Text(tr(profile.locale, "cancel")) } }
        )
    }
    if (resetStep == 1) {
        AlertDialog(
            onDismissRequest = { resetStep = 0 },
            title = { Text(tr(profile.locale, "resetData")) },
            text = { Text(tr(profile.locale, "resetWarning")) },
            confirmButton = { Button(onClick = { resetStep = 2 }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(tr(profile.locale, "continueReset")) } },
            dismissButton = { TextButton(onClick = { resetStep = 0 }) { Text(tr(profile.locale, "cancel")) } }
        )
    }
    if (resetStep == 2) {
        AlertDialog(
            onDismissRequest = { resetStep = 0 },
            title = { Text(tr(profile.locale, "finalResetTitle")) },
            text = { Text(tr(profile.locale, "finalResetWarning")) },
            confirmButton = {
                Button(onClick = {
                    resetStep = 0
                    scope.launch {
                        runCatching {
                            dao.observeReminders(profile.profileId).first().forEach { ReminderScheduler.cancel(context, it.reminderId) }
                            dao.clearProfileLearningData(profile.profileId)
                            prefs.resetProfile()
                            dao.insertWallet(WalletEntity(profileId = profile.profileId, coins = 30))
                        }.onSuccess {
                            Toast.makeText(context, tr(profile.locale, "resetSuccess"), Toast.LENGTH_LONG).show()
                        }.onFailure {
                            Toast.makeText(context, tr(profile.locale, "backupFailed"), Toast.LENGTH_LONG).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(tr(profile.locale, "finalResetButton")) }
            },
            dismissButton = { TextButton(onClick = { resetStep = 0 }) { Text(tr(profile.locale, "cancel")) } }
        )
    }
}


@Composable
private fun StoriesPage(locale: AcademyLocale, openSpeaking: (String) -> Unit) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showTranslation by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var completed by remember { mutableStateOf(setOf<String>()) }
    val stories = AcademyStoryCatalog.all
    val selected = stories.firstOrNull { it.id == selectedId }
    val levelLabel: (LearningBand) -> String = { storyBand ->
        val group = when (storyBand) {
            LearningBand.PRE_A1, LearningBand.A1 -> 0
            LearningBand.A2, LearningBand.B1 -> 1
            LearningBand.B2, LearningBand.C1, LearningBand.C2 -> 2
            LearningBand.SPECIALIZED -> 3
        }
        when (locale) {
            AcademyLocale.ENGLISH_US -> listOf("Beginner", "Intermediate", "Advanced", "Specialized")[group]
            AcademyLocale.DARI_AF -> listOf("مبتدی", "متوسط", "پیشرفته", "تخصصی")[group]
            AcademyLocale.PASHTO_AF -> listOf("پیل", "منځنۍ", "پرمختللې", "ځانګړې")[group]
            AcademyLocale.ARABIC -> listOf("مبتدئ", "متوسط", "متقدم", "متخصص")[group]
        }
    }
    Page {
        Text(when(locale) { AcademyLocale.ENGLISH_US -> "Story library"; AcademyLocale.DARI_AF -> "کتابخانه داستان‌ها"; AcademyLocale.PASHTO_AF -> "د کیسو کتابتون"; AcademyLocale.ARABIC -> "مكتبة القصص" },
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(when(locale) { AcademyLocale.ENGLISH_US -> "Learn English through short stories, useful words and comprehension checks."; AcademyLocale.DARI_AF -> "با داستان‌های کوتاه، واژگان کاربردی و آزمون درک مطلب انگلیسی یاد بگیر."; AcademyLocale.PASHTO_AF -> "د لنډو کیسو، ګټورو کلمو او د پوهې ازموینې له لارې انګلیسي زده کړئ."; AcademyLocale.ARABIC -> "تعلّم الإنجليزية من خلال القصص القصيرة والمفردات المفيدة واختبارات الفهم." },
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoStories, null, tint = MaterialTheme.colorScheme.primary)
            Text(when(locale) { AcademyLocale.ENGLISH_US -> "${completed.size} of ${stories.size} completed"; AcademyLocale.DARI_AF -> "${completed.size} از ${stories.size} داستان تکمیل‌شده"; AcademyLocale.PASHTO_AF -> "${completed.size} له ${stories.size} کیسو بشپړې شوې"; AcademyLocale.ARABIC -> "${completed.size} من ${stories.size} قصص مكتملة" },
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (selected == null) {
            stories.forEach { story ->
                AcademyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(story.emoji, fontSize = 30.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(story.title.forLocale(locale), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(levelLabel(story.band), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            Text(story.summary.forLocale(locale), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        if (story.id in completed) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { selectedId = story.id; showTranslation = false; selectedAnswer = null },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 7.dp)) {
                        Text(when(locale) { AcademyLocale.ENGLISH_US -> "Read story"; AcademyLocale.DARI_AF -> "خواندن داستان"; AcademyLocale.PASHTO_AF -> "کیسه ولولئ"; AcademyLocale.ARABIC -> "اقرأ القصة" })
                    }
                }
            }
        } else {
            TextButton(onClick = { selectedId = null; selectedAnswer = null }) {
                Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp))
                Text(when(locale) { AcademyLocale.ENGLISH_US -> "All stories"; AcademyLocale.DARI_AF -> "همه داستان‌ها"; AcademyLocale.PASHTO_AF -> "ټولې کیسې"; AcademyLocale.ARABIC -> "كل القصص" })
            }
            AcademyCard {
                Text(selected.title.forLocale(locale), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(levelLabel(selected.band), color = MaterialTheme.colorScheme.primary)
                Text(selected.englishText, style = MaterialTheme.typography.bodyLarge, lineHeight = 27.sp)
                OutlinedButton(onClick = { showTranslation = !showTranslation }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Icon(Icons.Default.Translate, null); Spacer(Modifier.width(8.dp))
                    Text(when(locale) { AcademyLocale.ENGLISH_US -> if(showTranslation) "Hide Dari translation" else "Show Dari translation"; AcademyLocale.DARI_AF -> if(showTranslation) "پنهان‌کردن ترجمه" else "نمایش ترجمه"; AcademyLocale.PASHTO_AF -> if(showTranslation) "ژباړه پټه کړه" else "ژباړه ښکاره کړه"; AcademyLocale.ARABIC -> if(showTranslation) "إخفاء الترجمة" else "عرض الترجمة" })
                }
                if (showTranslation) Text(if (locale == AcademyLocale.ENGLISH_US) selected.translation.fa else selected.translation.forLocale(locale), color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 25.sp)
                Divider()
                Text(when(locale) { AcademyLocale.ENGLISH_US -> "Useful words"; AcademyLocale.DARI_AF -> "واژگان مهم"; AcademyLocale.PASHTO_AF -> "مهمې کلمې"; AcademyLocale.ARABIC -> "كلمات مفيدة" }, fontWeight = FontWeight.Bold)
                selected.words.forEach { word ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(word.english, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(word.meaning.forLocale(locale), color = MaterialTheme.colorScheme.primary)
                    }
                }
                Divider()
                AcademyCard {
                    Text(when(locale) { AcademyLocale.ENGLISH_US -> "Speaking challenge"; AcademyLocale.DARI_AF -> "تمرین گفتار داستان"; AcademyLocale.PASHTO_AF -> "د کیسې د خبرو تمرین"; AcademyLocale.ARABIC -> "تحدي التحدث من القصة" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val storySpeakingLine = selected.englishText.substringBefore(".").trim().let { if (it.isNotBlank()) "$it." else selected.englishText }
                    Text(storySpeakingLine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Button(onClick = { openSpeaking(storySpeakingLine) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text(tr(locale, "practiceSpeaking"))
                    }
                }
                Divider()
                Text(when(locale) { AcademyLocale.ENGLISH_US -> "Comprehension check"; AcademyLocale.DARI_AF -> "آزمون درک مطلب"; AcademyLocale.PASHTO_AF -> "د پوهېدو ازموینه"; AcademyLocale.ARABIC -> "اختبار الفهم" }, fontWeight = FontWeight.Bold)
                Text(selected.question.forLocale(locale))
                selected.options.forEachIndexed { index, option ->
                    val isCorrect = index == selected.correctIndex
                    val chosen = selectedAnswer == index
                    OutlinedButton(onClick = { selectedAnswer = index }, modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(if(chosen) 2.dp else 1.dp, if(chosen && isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(22.dp)) { Text(option.forLocale(locale)) }
                }
                if (selectedAnswer != null) {
                    val correct = selectedAnswer == selected.correctIndex
                    Text(when(locale) {
                        AcademyLocale.ENGLISH_US -> if(correct) "Correct! Well done." else "Not quite. Read the story again."
                        AcademyLocale.DARI_AF -> if(correct) "آفرین! پاسخ درست است." else "پاسخ درست نیست؛ داستان را دوباره بخوان."
                        AcademyLocale.PASHTO_AF -> if(correct) "آفرین! سم ځواب دی." else "سم نه دی؛ کیسه بیا ولولئ."
                        AcademyLocale.ARABIC -> if(correct) "أحسنت! إجابة صحيحة." else "ليست الإجابة الصحيحة؛ اقرأ القصة مرة أخرى."
                    }, color = if(correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    if(correct) Button(onClick = { completed = completed + selected.id; selectedId = null; selectedAnswer = null },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Text(when(locale) { AcademyLocale.ENGLISH_US -> "Finish story"; AcademyLocale.DARI_AF -> "تکمیل داستان"; AcademyLocale.PASHTO_AF -> "کیسه بشپړه کړه"; AcademyLocale.ARABIC -> "إنهاء القصة" })
                    }
                }
            }
        }
    }
}

private fun tr(locale: AcademyLocale, key: String, arg: String = ""): String {
    val en = mapOf(
        "home" to "Home", "learn" to "Learn", "games" to "Games", "progress" to "Progress", "profile" to "Profile",
        "greeting" to "Hello, $arg!", "welcome" to "A little progress every day becomes a big achievement.",
        "journey" to "Your learning journey", "journeySub" to "Every new word brings you closer to your goal.",
        "level" to "Level", "coins" to "Coins", "dailyGoal" to "Daily study goal", "goalMins" to "$arg minutes",
        "goalHint" to "Start a lesson to track your real study time.", "lessons" to "Lessons completed", "quizzes" to "Quizzes taken",
        "continue" to "Ready for your next step?", "chooseLesson" to "Choose a level and continue learning.", "openLessons" to "Open lessons",
        "myProfile" to "My profile", "learningPath" to "English learning path", "grammarAcademy" to "Grammar academy", "grammarIntro" to "Study clear grammar rules and check your understanding across all levels.", "grammarCorrect" to "Correct! Excellent work.", "grammarTryAgain" to "Not quite. Review the rule and try again.", "grammarRetry" to "Try this question again", "speakingLesson" to "Speaking practice for this lesson", "practiceSpeaking" to "Practise speaking", "backToLessons" to "Back to lessons", "vocabulary" to "Vocabulary notebook", "searchWords" to "Search words", "wordCount" to "$arg words", "chooseLevel" to "Choose your current level",
        "bandSub" to "Structured learning at $arg", "viewLessons" to "View lessons", "levelLocked" to "Complete the previous level to unlock.", "allLevels" to "All levels",
        "lessonExplain" to "Learn the topic, practise vocabulary and check your understanding.", "lessonDetails" to "Open lesson", "hideDetails" to "Hide details", "newWords" to "New vocabulary",
        "completeLesson" to "Complete lesson · earn XP", "lessonDone" to "Lesson completed", "lessonLocked" to "Complete the previous lesson to unlock this one.", "quizReview" to "Review quizzes", "wordChallenge" to "Word meaning challenge",
        "chooseMeaning" to "Choose the Dari meaning of APPLE", "correct" to "Excellent! Correct answer.", "wrong" to "Not quite. Try again.",
        "hintText" to "Hint: It is a fruit.", "hintPaid" to "Hint unlocked. 6 coins spent.", "needCoins" to "You need at least 6 coins.",
        "hintButton" to "Get a hint · 6 coins", "coinBalance" to "Your coins: $arg", "newRound" to "New round",
        "gameRoadmap" to "More vocabulary, spelling and memory games are planned for the next content phase.",
        "summary" to "Learning summary", "badges" to "Achievements", "badgeFirstLesson" to "First lesson completed", "badgeFiveLessons" to "Five lessons completed", "badgeFirstQuiz" to "First quiz completed", "badgeFirstGame" to "First game played", "badgeHour" to "One hour of study", "noBadges" to "Complete lessons, quizzes and games to earn badges.", "studyToday" to "Today", "studyWeek" to "This week", "studyMonth" to "This month", "studyTotal" to "Total study time", "average" to "Average quiz score", "quizHistory" to "Quiz history", "noHistory" to "No quiz attempts yet.", "quizTitle" to "Quick mixed quiz", "nextQuestion" to "Next question", "finishQuiz" to "Finish quiz", "quizComplete" to "Quiz completed!", "quizSaved" to "Your result has been saved to your learning history.", "seeProgress" to "View progress", "questionProgress" to "Question $arg",
        "displayName" to "Display name", "changeAvatar" to "Change photo", "backupRestore" to "Backup and restore", "resetData" to "Reset all app data", "resetWarning" to "This removes your learning progress, quiz and game history, saved words, reminders and profile settings.", "continueReset" to "Continue", "finalResetTitle" to "Final confirmation", "finalResetWarning" to "This action cannot be undone. Create a backup first if you may need this data.", "finalResetButton" to "Delete all data", "resetSuccess" to "App data has been reset.", "createBackup" to "Create backup", "restoreBackup" to "Restore backup", "backupMergeNote" to "Restore merges saved records with this device; it does not erase local data.", "confirmRestore" to "Restore this backup?", "restoreWarning" to "The selected backup will update profile settings and merge saved learning records.", "cancel" to "Cancel", "backupExported" to "Backup saved successfully.", "restoreSuccess" to "Backup restored successfully.", "backupFailed" to "Backup operation failed. Check the selected file and try again.", "username" to "Username", "save" to "Save profile", "language" to "Language", "theme" to "Theme",
        "font" to "Font", "minutes" to "minutes", "studyTools" to "Study tools", "timer" to "Study timer", "startTimer" to "Start", "pauseTimer" to "Pause", "finishTimer" to "Finish session", "timerSaved" to "Study session saved.", "reminders" to "Reminders and alarms", "reminderTitle" to "Reminder title", "timeHHMM" to "Time (HH:MM)", "dailyReminder" to "Repeat every day", "repeatDays" to "Repeat on these days", "oneTimeReminder" to "No days selected: this will be a one-time reminder.", "scheduleReminder" to "Schedule reminder", "reminderSaved" to "Reminder scheduled.", "reminderUpdated" to "Reminder updated.", "updateReminder" to "Update reminder", "invalidTime" to "Enter a title and valid time such as 19:30.", "permissionNeeded" to "Allow notifications in Android settings to receive reminders.", "exactAlarmPermission" to "Allow exact alarms in Android settings; otherwise reminders may be delayed.", "noReminders" to "No reminders scheduled yet.", "DARI_AF" to "Afghan Dari", "PASHTO_AF" to "Afghan Pashto", "ARABIC" to "Arabic", "ENGLISH_US" to "English",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive", "VAZIRMATN" to "Vazirmatn", "SAHEL" to "Sahel"
    )
    val fa = mapOf(
        "home" to "خانه", "learn" to "آموزش", "games" to "بازی‌ها", "progress" to "پیشرفت", "profile" to "پروفایل",
        "greeting" to "سلام $arg جان!", "welcome" to "هر روز یک قدم کوچک، یک موفقیت بزرگ.",
        "journey" to "مسیر یادگیری تو", "journeySub" to "هر واژه تازه تو را به هدفت نزدیک‌تر می‌کند.",
        "level" to "سطح", "coins" to "سکه", "dailyGoal" to "هدف روزانه مطالعه", "goalMins" to "$arg دقیقه",
        "goalHint" to "برای ثبت زمان واقعی، یک درس را آغاز کن.", "lessons" to "درس‌های تکمیل‌شده", "quizzes" to "آزمون‌های انجام‌شده",
        "continue" to "برای قدم بعدی آماده‌ای؟", "chooseLesson" to "یک سطح را انتخاب کن و یادگیری را ادامه بده.", "openLessons" to "رفتن به درس‌ها",
        "myProfile" to "پروفایل من", "learningPath" to "مسیر آموزش انگلیسی", "grammarAcademy" to "آموزش گرامر", "grammarIntro" to "قواعد گرامر را یاد بگیر و در همه سطح‌ها دانشت را بیازما.", "grammarCorrect" to "آفرین! پاسخ درست است.", "grammarTryAgain" to "پاسخ درست نیست؛ قاعده را مرور کن و دوباره کوشش کن.", "grammarRetry" to "دوباره کوشش کن", "speakingLesson" to "تمرین گفتار این درس", "practiceSpeaking" to "تمرین گفتار", "backToLessons" to "بازگشت به درس‌ها", "vocabulary" to "دفترچه واژگان", "searchWords" to "جست‌وجوی واژه", "wordCount" to "$arg واژه", "chooseLevel" to "سطح فعلی خود را انتخاب کن",
        "bandSub" to "درس‌های مرحله‌بندی‌شده $arg", "viewLessons" to "دیدن درس‌ها", "levelLocked" to "برای بازشدن این سطح، سطح قبلی را تکمیل کن.", "allLevels" to "همه سطح‌ها",
        "lessonExplain" to "موضوع را یاد بگیر، واژه‌های تازه را تمرین کن و دانسته‌هایت را بسنج.", "lessonDetails" to "بازکردن درس", "hideDetails" to "بستن جزئیات", "newWords" to "واژگان تازه",
        "completeLesson" to "تکمیل درس · دریافت امتیاز", "lessonDone" to "درس تکمیل شده است", "lessonLocked" to "برای بازشدن این درس، درس قبلی را تکمیل کن.", "quizReview" to "رفتن به آزمون‌ها", "wordChallenge" to "چالش معنی واژه",
        "chooseMeaning" to "معنی دری کلمه APPLE را انتخاب کن", "correct" to "آفرین! پاسخ درست است.", "wrong" to "این پاسخ درست نیست؛ دوباره تلاش کن.",
        "hintText" to "راهنما: این یک میوه است.", "hintPaid" to "راهنما فعال شد؛ 6 سکه مصرف شد.", "needCoins" to "حداقل 6 سکه نیاز داری.",
        "hintButton" to "دریافت راهنما · 6 سکه", "coinBalance" to "سکه‌های تو: $arg", "newRound" to "دور تازه",
        "gameRoadmap" to "بازی‌های واژگان، املا و حافظه در مرحله بعدی محتوا اضافه می‌شوند.",
        "summary" to "خلاصه یادگیری تو", "badges" to "افتخارات", "badgeFirstLesson" to "نخستین درس تکمیل شد", "badgeFiveLessons" to "پنج درس تکمیل شد", "badgeFirstQuiz" to "نخستین آزمون انجام شد", "badgeFirstGame" to "نخستین بازی انجام شد", "badgeHour" to "یک ساعت مطالعه", "noBadges" to "برای دریافت مدال، درس‌ها و آزمون‌ها را تکمیل کن.", "studyToday" to "امروز", "studyWeek" to "این هفته", "studyMonth" to "این ماه", "studyTotal" to "مجموع زمان مطالعه", "average" to "میانگین نمره آزمون", "quizHistory" to "تاریخچه آزمون", "noHistory" to "هنوز آزمونی انجام نشده است.", "quizTitle" to "آزمون ترکیبی کوتاه", "nextQuestion" to "پرسش بعدی", "finishQuiz" to "پایان آزمون", "quizComplete" to "آزمون تکمیل شد!", "quizSaved" to "نتیجه در تاریخچه یادگیری ذخیره شد.", "seeProgress" to "دیدن پیشرفت", "questionProgress" to "پرسش $arg",
        "displayName" to "نام نمایشی", "changeAvatar" to "تغییر عکس", "backupRestore" to "پشتیبان‌گیری و بازیابی", "resetData" to "بازنشانی تمام اطلاعات برنامه", "resetWarning" to "پیشرفت آموزشی، تاریخچه آزمون و بازی، واژگان ذخیره‌شده، یادآوری‌ها و تنظیمات پروفایل حذف می‌شوند.", "continueReset" to "ادامه", "finalResetTitle" to "تأیید نهایی", "finalResetWarning" to "این کار برگشت‌پذیر نیست. اگر به اطلاعات نیاز داری، نخست نسخه پشتیبان بساز.", "finalResetButton" to "حذف تمام اطلاعات", "resetSuccess" to "اطلاعات برنامه بازنشانی شد.", "createBackup" to "ساخت نسخه پشتیبان", "restoreBackup" to "بازیابی نسخه پشتیبان", "backupMergeNote" to "بازیابی، اطلاعات را با داده‌های دستگاه یکجا می‌کند و اطلاعات محلی را پاک نمی‌کند.", "confirmRestore" to "این نسخه پشتیبان بازیابی شود؟", "restoreWarning" to "تنظیمات پروفایل و سوابق آموزشی از فایل انتخاب‌شده وارد می‌شوند.", "cancel" to "لغو", "backupExported" to "نسخه پشتیبان با موفقیت ذخیره شد.", "restoreSuccess" to "نسخه پشتیبان با موفقیت بازیابی شد.", "backupFailed" to "عملیات پشتیبان‌گیری ناکام شد؛ فایل را بررسی کن.", "username" to "نام کاربری", "save" to "ذخیره پروفایل", "language" to "زبان", "theme" to "تم",
        "font" to "فونت", "minutes" to "دقیقه", "studyTools" to "ابزارهای مطالعه", "timer" to "تایمر مطالعه", "startTimer" to "شروع", "pauseTimer" to "مکث", "finishTimer" to "پایان جلسه", "timerSaved" to "جلسه مطالعه ذخیره شد.", "reminders" to "یادآوری و زنگ", "reminderTitle" to "عنوان یادآوری", "timeHHMM" to "زمان (ساعت:دقیقه)", "dailyReminder" to "تکرار هر روز", "repeatDays" to "روزهای تکرار", "oneTimeReminder" to "اگر روزی انتخاب نشود، یادآوری فقط یک‌بار اجرا می‌شود.", "scheduleReminder" to "تنظیم یادآوری", "reminderSaved" to "یادآوری تنظیم شد.", "reminderUpdated" to "یادآوری به‌روزرسانی شد.", "updateReminder" to "به‌روزرسانی یادآوری", "invalidTime" to "عنوان و زمان درست مانند 19:30 وارد کن.", "permissionNeeded" to "برای دریافت یادآوری، اجازه اعلان‌ها را در اندروید فعال کن.", "exactAlarmPermission" to "برای زنگ دقیق، اجازه زنگ‌های دقیق را در تنظیمات اندروید فعال کن.", "noReminders" to "هنوز یادآوری تنظیم نشده است.", "DARI_AF" to "دری افغانستان", "PASHTO_AF" to "پشتو افغانستان", "ARABIC" to "عربی", "ENGLISH_US" to "انگلیسی",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "دست‌نویس", "VAZIRMATN" to "وزیرمتن", "SAHEL" to "ساحل"
    )
    val ps = mapOf("home" to "کور", "learn" to "زده کړه", "games" to "لوبې", "progress" to "پرمختګ", "profile" to "پروفایل",
        "greeting" to "سلام، $arg!", "welcome" to "هره ورځ لږ پرمختګ، لویه لاسته راوړنه.", "journey" to "ستاسو د زده کړې سفر",
        "level" to "کچه", "coins" to "سکې", "dailyGoal" to "د مطالعې ورځنی هدف", "lessons" to "بشپړ شوي درسونه", "quizzes" to "ازموینې",
        "learningPath" to "د انګلیسي زده کړې لاره", "vocabulary" to "د لغتونو کتابچه", "searchWords" to "د لغت لټون", "wordCount" to "$arg لغتونه", "chooseLevel" to "خپله کچه وټاکئ", "viewLessons" to "درسونه وګورئ", "levelLocked" to "د دې کچې د خلاصولو لپاره مخکینۍ کچه بشپړه کړئ.", "allLevels" to "ټولې کچې",
        "correct" to "ډېر ښه! ځواب سم دی.", "quizTitle" to "لنډه ګډه ازموینه", "nextQuestion" to "بله پوښتنه", "finishQuiz" to "ازموینه پای ته ورسوه", "quizComplete" to "ازموینه بشپړه شوه!", "quizSaved" to "ستاسو پایله د زده کړې په تاریخ کې خوندي شوه.", "seeProgress" to "پرمختګ وګورئ", "wrong" to "بیا هڅه وکړئ.", "hintButton" to "لارښوونه · 6 سکې", "hintPaid" to "لارښوونه فعاله شوه؛ 6 سکې ولګېدې.",
        "needCoins" to "لږ تر لږه 6 سکې پکار دي.", "displayName" to "ښکاره نوم", "changeAvatar" to "انځور بدل کړئ", "backupRestore" to "بیک اپ او بېرته راګرځول", "resetData" to "د اپ ټول معلومات بیا تنظیمول", "resetWarning" to "د زده کړې پرمختګ، د ازموینو او لوبو تاریخچه، خوندي لغتونه، یادونې او د پروفایل تنظیمات به ړنګ شي.", "continueReset" to "دوام", "finalResetTitle" to "وروستی تایید", "finalResetWarning" to "دا کار بېرته نه راګرځي. که معلوماتو ته اړتیا لرئ، لومړی بیک اپ جوړ کړئ.", "finalResetButton" to "ټول معلومات ړنګول", "resetSuccess" to "د اپ معلومات بیا تنظیم شول.", "createBackup" to "بیک اپ جوړول", "restoreBackup" to "بیک اپ بېرته راګرځول", "backupMergeNote" to "بېرته راګرځول معلومات یوځای کوي او د وسیلې موجود معلومات نه ړنګوي.", "confirmRestore" to "دا بیک اپ بېرته را وګرځوو؟", "restoreWarning" to "د پروفایل تنظیمات او د زده کړې ثبتونه به له ټاکل شوي فایل څخه ورزیات شي.", "cancel" to "لغوه", "backupExported" to "بیک اپ په بریالیتوب خوندي شو.", "restoreSuccess" to "بیک اپ په بریالیتوب بېرته را وګرځېد.", "backupFailed" to "د بیک اپ کار ناکام شو؛ فایل وګورئ او بیا هڅه وکړئ.", "username" to "کارن نوم", "save" to "پروفایل خوندي کړئ",
        "language" to "ژبه", "theme" to "بڼه", "font" to "لیکبڼه", "minutes" to "دقیقې", "studyTools" to "د مطالعې وسایل", "timer" to "د مطالعې ټایمر", "startTimer" to "پیل", "pauseTimer" to "تم", "finishTimer" to "ناسته پای ته ورسوه", "timerSaved" to "د مطالعې ناسته خوندي شوه.", "reminders" to "یادونې او الارم", "reminderTitle" to "د یادونې سرلیک", "timeHHMM" to "وخت (HH:MM)", "dailyReminder" to "هره ورځ تکرار", "repeatDays" to "د تکرار ورځې", "scheduleReminder" to "یادونه تنظیم کړه", "reminderSaved" to "یادونه تنظیم شوه.", "reminderUpdated" to "یادونه تازه شوه.", "updateReminder" to "یادونه تازه کول", "repeatDays" to "د تکرار ورځې", "oneTimeReminder" to "که کومه ورځ و نه ټاکل شي، یادونه یوازې یو ځل کېږي.", "invalidTime" to "سرلیک او سم وخت ولیکئ، لکه 19:30.", "permissionNeeded" to "د یادونو ترلاسه کولو لپاره د Android خبرتیا اجازه ورکړئ.", "exactAlarmPermission" to "دقیق الارم لپاره په Android تنظیماتو کې د دقیقو الارمونو اجازه ورکړئ.", "noReminders" to "تر اوسه یادونه نشته.",
        "DARI_AF" to "افغان دري", "PASHTO_AF" to "افغان پښتو", "ARABIC" to "عربي", "ENGLISH_US" to "انګلیسي",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "لاس لیک", "VAZIRMATN" to "وزیرمتن", "SAHEL" to "ساحل")
    val ar = mapOf("home" to "الرئيسية", "learn" to "التعلم", "games" to "الألعاب", "progress" to "التقدم", "profile" to "الملف الشخصي",
        "greeting" to "مرحباً، $arg!", "welcome" to "خطوة صغيرة كل يوم تصنع إنجازاً كبيراً.", "journey" to "رحلة التعلم",
        "level" to "المستوى", "coins" to "العملات", "dailyGoal" to "هدف الدراسة اليومي", "lessons" to "الدروس المكتملة", "quizzes" to "الاختبارات",
        "learningPath" to "مسار تعلم الإنجليزية", "vocabulary" to "دفتر المفردات", "searchWords" to "ابحث عن كلمة", "wordCount" to "$arg كلمة", "chooseLevel" to "اختر مستواك", "viewLessons" to "عرض الدروس", "levelLocked" to "أكمل المستوى السابق لفتح هذا المستوى.", "allLevels" to "كل المستويات",
        "correct" to "ممتاز! إجابة صحيحة.", "quizTitle" to "اختبار قصير متنوع", "nextQuestion" to "السؤال التالي", "finishQuiz" to "إنهاء الاختبار", "quizComplete" to "اكتمل الاختبار!", "quizSaved" to "تم حفظ النتيجة في سجل التعلم.", "seeProgress" to "عرض التقدم", "wrong" to "حاول مرة أخرى.", "hintButton" to "تلميح · 6 عملات", "hintPaid" to "تم تفعيل التلميح؛ خُصمت 6 عملات.",
        "needCoins" to "تحتاج إلى 6 عملات على الأقل.", "displayName" to "الاسم الظاهر", "changeAvatar" to "تغيير الصورة", "backupRestore" to "النسخ الاحتياطي والاستعادة", "resetData" to "إعادة ضبط جميع بيانات التطبيق", "resetWarning" to "سيؤدي ذلك إلى حذف التقدم وسجل الاختبارات والألعاب والمفردات المحفوظة والتذكيرات وإعدادات الملف.", "continueReset" to "متابعة", "finalResetTitle" to "التأكيد النهائي", "finalResetWarning" to "لا يمكن التراجع عن هذا الإجراء. أنشئ نسخة احتياطية أولاً إذا كنت قد تحتاج إلى هذه البيانات.", "finalResetButton" to "حذف جميع البيانات", "resetSuccess" to "تمت إعادة ضبط بيانات التطبيق.", "createBackup" to "إنشاء نسخة احتياطية", "restoreBackup" to "استعادة نسخة احتياطية", "backupMergeNote" to "تدمج الاستعادة السجلات مع بيانات الجهاز ولا تحذف البيانات المحلية.", "confirmRestore" to "هل تريد استعادة هذه النسخة؟", "restoreWarning" to "سيتم تحديث إعدادات الملف ودمج سجلات التعلم من الملف المحدد.", "cancel" to "إلغاء", "backupExported" to "تم حفظ النسخة الاحتياطية بنجاح.", "restoreSuccess" to "تمت استعادة النسخة الاحتياطية بنجاح.", "backupFailed" to "فشلت عملية النسخ الاحتياطي. تحقق من الملف وحاول مجدداً.", "username" to "اسم المستخدم", "save" to "حفظ الملف",
        "language" to "اللغة", "theme" to "السمة", "font" to "الخط", "minutes" to "دقيقة", "studyTools" to "أدوات الدراسة", "timer" to "مؤقت الدراسة", "startTimer" to "ابدأ", "pauseTimer" to "إيقاف مؤقت", "finishTimer" to "إنهاء الجلسة", "timerSaved" to "تم حفظ جلسة الدراسة.", "reminders" to "التذكيرات والمنبهات", "reminderTitle" to "عنوان التذكير", "timeHHMM" to "الوقت (HH:MM)", "dailyReminder" to "التكرار يومياً", "repeatDays" to "أيام التكرار", "oneTimeReminder" to "إذا لم تحدد يوماً، سيعمل التذكير مرة واحدة.", "scheduleReminder" to "جدولة التذكير", "reminderSaved" to "تمت جدولة التذكير.", "reminderUpdated" to "تم تحديث التذكير.", "updateReminder" to "تحديث التذكير", "invalidTime" to "أدخل عنواناً ووقتاً صحيحاً مثل 19:30.", "permissionNeeded" to "اسمح بالإشعارات من إعدادات Android لاستلام التذكيرات.", "exactAlarmPermission" to "اسمح بالمنبهات الدقيقة من إعدادات Android حتى لا تتأخر التذكيرات.", "noReminders" to "لا توجد تذكيرات مجدولة بعد.",
        "DARI_AF" to "الداري الأفغانية", "PASHTO_AF" to "البشتوية الأفغانية", "ARABIC" to "العربية", "ENGLISH_US" to "الإنجليزية",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive", "VAZIRMATN" to "Vazirmatn", "SAHEL" to "Sahel")
    val psComplete = ps + mapOf(
        "grammarAcademy" to "د ګرامر زده کړه", "grammarIntro" to "د ګرامر اصول زده کړئ او په ټولو کچو کې خپله پوهه وازمویئ.", "grammarCorrect" to "سم ځواب دی! ډېر ښه.", "grammarTryAgain" to "سم نه دی؛ قاعده بیا وګورئ او هڅه وکړئ.", "grammarRetry" to "بیا هڅه وکړئ", "speakingLesson" to "د دې درس د خبرو تمرین", "practiceSpeaking" to "د خبرو تمرین", "backToLessons" to "درسونو ته بېرته",
        "lessonExplain" to "موضوع زده کړئ، لغتونه تمرین کړئ او خپله پوهه وازمویئ.", "lessonDone" to "درس بشپړ شوی دی", "lessonLocked" to "د دې درس د خلاصولو لپاره مخکینی درس بشپړ کړئ.",
        "journeySub" to "هره نوې کلمه مو موخې ته نږدې کوي.", "goalMins" to "$arg دقیقې", "goalHint" to "د مطالعې وخت ثبتولو لپاره درس پیل کړئ.",
        "continue" to "راتلونکي ګام ته چمتو یاست؟", "chooseLesson" to "کچه وټاکئ او زده کړه روانه وساتئ.", "openLessons" to "درسونه پرانیزئ",
        "myProfile" to "زما پروفایل", "bandSub" to "د $arg لپاره منظم درسونه", "lessonDetails" to "درس پرانیزئ", "hideDetails" to "جزئیات پټ کړئ",
        "newWords" to "نوې کلمې", "completeLesson" to "درس بشپړ کړئ · XP ترلاسه کړئ", "lessonDone" to "درس بشپړ شوی دی", "quizReview" to "ازموینې بیا وګورئ",
        "wordChallenge" to "د کلمې د مانا لوبه", "chooseMeaning" to "د APPLE پښتو/دري مانا وټاکئ", "hintText" to "لارښوونه: دا یوه مېوه ده.",
        "coinBalance" to "ستاسې سکې: $arg", "newRound" to "نوی پړاو", "gameRoadmap" to "نورې د لغت، املا او حافظې لوبې به وروسته ورزیاتې شي.",
        "summary" to "د زده کړې لنډیز", "badges" to "ویاړونه", "badgeFirstLesson" to "لومړی درس بشپړ شو", "badgeFiveLessons" to "پنځه درسونه بشپړ شول", "badgeFirstQuiz" to "لومړۍ ازموینه بشپړه شوه", "badgeFirstGame" to "لومړۍ لوبه وشوه", "badgeHour" to "یو ساعت مطالعه", "noBadges" to "د نښانونو لپاره درسونه او ازموینې بشپړې کړئ.", "studyToday" to "نن", "studyWeek" to "دا اونۍ", "studyMonth" to "دا میاشت", "studyTotal" to "د مطالعې ټول وخت", "average" to "د ازموینو منځنۍ نمره", "quizHistory" to "د ازموینو تاریخچه", "noHistory" to "تر اوسه ازموینه نه ده شوې.",
        "questionProgress" to "پوښتنه $arg"
    )
    val arComplete = ar + mapOf(
        "grammarAcademy" to "أكاديمية القواعد", "grammarIntro" to "تعلّم قواعد واضحة واختبر فهمك في جميع المستويات.", "grammarCorrect" to "صحيح! أحسنت.", "grammarTryAgain" to "ليس تماماً. راجع القاعدة وحاول مجدداً.", "grammarRetry" to "أعد المحاولة", "speakingLesson" to "تدريب التحدث لهذا الدرس", "practiceSpeaking" to "تدريب التحدث", "backToLessons" to "العودة إلى الدروس",
        "lessonExplain" to "تعلّم الموضوع وتدرّب على المفردات واختبر فهمك.", "lessonDone" to "اكتمل الدرس", "lessonLocked" to "أكمل الدرس السابق لفتح هذا الدرس.",
        "journeySub" to "كل كلمة جديدة تقرّبك من هدفك.", "goalMins" to "$arg دقيقة", "goalHint" to "ابدأ درساً لتسجيل وقت الدراسة الفعلي.",
        "continue" to "هل أنت مستعد للخطوة التالية؟", "chooseLesson" to "اختر مستوى وتابع التعلم.", "openLessons" to "افتح الدروس",
        "myProfile" to "ملفي الشخصي", "bandSub" to "دروس منظمة للمستوى $arg", "lessonDetails" to "افتح الدرس", "hideDetails" to "إخفاء التفاصيل",
        "newWords" to "مفردات جديدة", "completeLesson" to "أكمل الدرس · اكسب XP", "lessonDone" to "اكتمل الدرس", "quizReview" to "مراجعة الاختبارات",
        "wordChallenge" to "تحدي معنى الكلمة", "chooseMeaning" to "اختر معنى APPLE باللغة الدارية", "hintText" to "تلميح: إنها فاكهة.",
        "coinBalance" to "عملاتك: $arg", "newRound" to "جولة جديدة", "gameRoadmap" to "ستضاف ألعاب المفردات والإملاء والذاكرة في مرحلة المحتوى التالية.",
        "summary" to "ملخص التعلم", "badges" to "الإنجازات", "badgeFirstLesson" to "أكملت الدرس الأول", "badgeFiveLessons" to "أكملت خمسة دروس", "badgeFirstQuiz" to "أكملت الاختبار الأول", "badgeFirstGame" to "لعبت اللعبة الأولى", "badgeHour" to "ساعة من الدراسة", "noBadges" to "أكمل الدروس والاختبارات والألعاب للحصول على الشارات.", "studyToday" to "اليوم", "studyWeek" to "هذا الأسبوع", "studyMonth" to "هذا الشهر", "studyTotal" to "إجمالي وقت الدراسة", "average" to "متوسط نتيجة الاختبارات", "quizHistory" to "سجل الاختبارات", "noHistory" to "لم تُجرَ أي اختبارات بعد.",
        "questionProgress" to "السؤال $arg"
    )
    val themeLabels = when (locale) {
        AcademyLocale.ENGLISH_US -> mapOf("SOFT_PINK" to "Soft pink", "PINK_PURPLE" to "Pink & purple", "LILAC_WHITE" to "Lilac & white", "PASTEL" to "Pastel", "FLORAL" to "Floral", "NAVY_BLUE" to "Navy blue", "BLUE_GRAY" to "Blue & grey", "GREEN_BLACK" to "Green & black", "SPORT" to "Sport", "SPACE" to "Space", "BLACK_GOLD" to "Black & gold", "WHITE_GOLD" to "White & gold", "ROYAL_PURPLE" to "Royal purple", "NIGHT" to "Night", "LIGHT" to "Light", "DARK" to "Dark")
        AcademyLocale.DARI_AF -> mapOf("SOFT_PINK" to "صورتی ملایم", "PINK_PURPLE" to "صورتی و بنفش", "LILAC_WHITE" to "یاسی و سفید", "PASTEL" to "پاستلی", "FLORAL" to "گل‌گلی", "NAVY_BLUE" to "آبی سرمه‌ای", "BLUE_GRAY" to "آبی و خاکستری", "GREEN_BLACK" to "سبز و سیاه", "SPORT" to "اسپرت", "SPACE" to "فضایی", "BLACK_GOLD" to "سیاه و طلایی", "WHITE_GOLD" to "سفید و طلایی", "ROYAL_PURPLE" to "بنفش سلطنتی", "NIGHT" to "شب", "LIGHT" to "روشن", "DARK" to "تاریک")
        AcademyLocale.PASHTO_AF -> mapOf("SOFT_PINK" to "نرمه ګلابي", "PINK_PURPLE" to "ګلابي او ارغواني", "LILAC_WHITE" to "یاسي او سپین", "PASTEL" to "پاستلي", "FLORAL" to "ګلدار", "NAVY_BLUE" to "تیاره آبي", "BLUE_GRAY" to "آبي او خړ", "GREEN_BLACK" to "شنه او تور", "SPORT" to "سپورتي", "SPACE" to "فضايي", "BLACK_GOLD" to "تور او زرین", "WHITE_GOLD" to "سپین او زرین", "ROYAL_PURPLE" to "شاهي ارغواني", "NIGHT" to "شپه", "LIGHT" to "روښانه", "DARK" to "تیاره")
        AcademyLocale.ARABIC -> mapOf("SOFT_PINK" to "وردي ناعم", "PINK_PURPLE" to "وردي وبنفسجي", "LILAC_WHITE" to "ليلكي وأبيض", "PASTEL" to "باستيل", "FLORAL" to "زهري", "NAVY_BLUE" to "أزرق داكن", "BLUE_GRAY" to "أزرق ورمادي", "GREEN_BLACK" to "أخضر وأسود", "SPORT" to "رياضي", "SPACE" to "فضائي", "BLACK_GOLD" to "أسود وذهبي", "WHITE_GOLD" to "أبيض وذهبي", "ROYAL_PURPLE" to "بنفسجي ملكي", "NIGHT" to "ليلي", "LIGHT" to "فاتح", "DARK" to "داكن")
    }
    val selected = when(locale) { AcademyLocale.ENGLISH_US -> en; AcademyLocale.DARI_AF -> fa; AcademyLocale.PASHTO_AF -> psComplete; AcademyLocale.ARABIC -> arComplete }
    return selected[key] ?: themeLabels[key] ?: en[key] ?: key
}


private data class SpeakingPrompt(val id: String, val text: String, val dari: String, val pashto: String, val arabic: String)
private val speakingPrompts = listOf(
    SpeakingPrompt("hello", "Hello, how are you today?", "سلام، امروز چطور هستی؟", "سلام، نن څنګه یې؟", "مرحباً، كيف حالك اليوم؟"),
    SpeakingPrompt("name", "My name is Zahra.", "نام من زهرا است.", "زما نوم زهرا دی.", "اسمي زهرا."),
    SpeakingPrompt("school", "I go to school every morning.", "من هر صبح به مکتب می‌روم.", "زه هر سهار ښوونځي ته ځم.", "أذهب إلى المدرسة كل صباح."),
    SpeakingPrompt("water", "I would like a glass of water, please.", "لطفاً یک گیلاس آب می‌خواهم.", "مهرباني وکړئ، یو ګیلاس اوبه غواړم.", "أريد كأساً من الماء من فضلك."),
    SpeakingPrompt("family", "My family is very important to me.", "خانواده‌ام برای من بسیار مهم است.", "زما کورنۍ راته ډېره مهمه ده.", "عائلتي مهمة جداً بالنسبة لي."),
    SpeakingPrompt("reading", "I enjoy reading books in my free time.", "من در وقت آزاد از کتاب‌خواندن لذت می‌برم.", "زه په خپل فارغ وخت کې د کتابونو لوستل خوښوم.", "أستمتع بقراءة الكتب في وقت فراغي."),
    SpeakingPrompt("practice", "Practice makes progress every day.", "تمرین هر روز باعث پیشرفت می‌شود.", "تمرین هره ورځ پرمختګ رامنځته کوي.", "الممارسة تحقق التقدم كل يوم."),
    SpeakingPrompt("future", "I am going to improve my English this year.", "من امسال می‌خواهم انگلیسی خود را بهتر بسازم.", "زه سږ کال غواړم خپله انګلیسي ښه کړم.", "سأحسن لغتي الإنجليزية هذا العام."),
    SpeakingPrompt("daily_routine", "I usually wake up early and plan my day.", "من معمولاً زود بیدار می‌شوم و روزم را برنامه‌ریزی می‌کنم.", "زه عموماً وختي پاڅېږم او خپله ورځ پلانوم.", "أستيقظ عادة مبكراً وأخطط ليومي."),
    SpeakingPrompt("directions", "Could you tell me how to get to the bus station?", "لطفاً می‌توانید راه رسیدن به ایستگاه بس را بگویید؟", "مهرباني وکړئ، د بس تمځای ته د تګ لار راته وښایئ.", "هل يمكنك أن تخبرني كيف أصل إلى محطة الحافلات؟"),
    SpeakingPrompt("restaurant", "I would like to order a meal without spicy sauce.", "می‌خواهم غذایی بدون ساس تند سفارش بدهم.", "زه غواړم بې له تندو ساسونو خواړه وغواړم.", "أود طلب وجبة من دون صلصة حارة."),
    SpeakingPrompt("doctor", "I have had a headache since yesterday.", "از دیروز به این‌سو سردرد دارم.", "له پرون راهیسې مې سر درد کوي.", "أعاني من صداع منذ أمس."),
    SpeakingPrompt("weather", "The weather is pleasant, so we can walk outside.", "هوا خوش است؛ پس می‌توانیم بیرون قدم بزنیم.", "هوا ښه ده، نو بهر ګرځېدای شو.", "الطقس لطيف، لذلك يمكننا المشي في الخارج."),
    SpeakingPrompt("opinion", "In my opinion, learning a language takes patience.", "به نظر من، یادگیری زبان به صبر نیاز دارد.", "زما په اند، د ژبې زده کړه زغم غواړي.", "في رأيي، تعلم اللغة يحتاج إلى الصبر."),
    SpeakingPrompt("past_event", "Last weekend, I visited my relatives.", "آخر هفته گذشته به دیدن خویشاوندانم رفتم.", "تېره اوونۍ مې خپلوان ولیدل.", "في عطلة نهاية الأسبوع الماضية زرت أقاربي."),
    SpeakingPrompt("plans", "If I finish my work early, I will visit my friend.", "اگر کارم را زود تمام کنم، به دیدن دوستم می‌روم.", "که خپل کار ژر خلاص کړم، ملګری به ووینم.", "إذا أنهيت عملي مبكراً فسأزور صديقي."),
    SpeakingPrompt("study", "I review new vocabulary before I go to sleep.", "پیش از خواب واژگان تازه را مرور می‌کنم.", "له خوب مخکې نوې کلمې تکراروم.", "أراجع المفردات الجديدة قبل النوم."),
    SpeakingPrompt("technology", "Technology is useful when we use it responsibly.", "تکنالوژی زمانی مفید است که مسئولانه از آن استفاده کنیم.", "ټکنالوژي هغه وخت ګټوره ده چې په مسؤلیت وکارول شي.", "تكون التكنولوجيا مفيدة عندما نستخدمها بمسؤولية."),
    SpeakingPrompt("work", "I would like to discuss the project schedule.", "می‌خواهم درباره زمان‌بندی پروژه گفت‌وگو کنم.", "زه غواړم د پروژې د مهالوېش په اړه خبرې وکړم.", "أود مناقشة الجدول الزمني للمشروع."),
    SpeakingPrompt("interview", "One of my strengths is solving problems calmly.", "یکی از توانایی‌های من حل آرامانه مشکلات است.", "زما یوه پیاوړتیا د ستونزو په ارامۍ حل کول دي.", "إحدى نقاط قوتي هي حل المشكلات بهدوء."),
    SpeakingPrompt("environment", "Small actions can help protect the environment.", "کارهای کوچک می‌توانند به حفاظت محیط زیست کمک کنند.", "کوچني کارونه د چاپېریال په ساتنه کې مرسته کوي.", "يمكن للأعمال الصغيرة أن تساعد في حماية البيئة."),
    SpeakingPrompt("storytelling", "Once, I helped a neighbour who needed support.", "روزی به همسایه‌ای که به کمک نیاز داشت، کمک کردم.", "یوه ورځ مې له هغه ګاونډي سره مرسته وکړه چې مرستې ته اړتیا یې لرله.", "ذات مرة ساعدت جاراً كان بحاجة إلى الدعم."),
    SpeakingPrompt("academic", "The evidence suggests that regular practice improves confidence.", "شواهد نشان می‌دهد که تمرین منظم اعتمادبه‌نفس را افزایش می‌دهد.", "شواهد ښيي چې منظم تمرین باور زیاتوي.", "تشير الأدلة إلى أن التدريب المنتظم يعزز الثقة."),
    SpeakingPrompt("negotiation", "Could we consider a different delivery date?", "آیا می‌توانیم تاریخ دیگری برای تحویل در نظر بگیریم؟", "ایا موږ د سپارلو بله نېټه په پام کې نیولی شو؟", "هل يمكننا النظر في موعد تسليم مختلف؟"),
    SpeakingPrompt("reflection", "I learn from mistakes and use feedback to improve.", "من از اشتباهات می‌آموزم و از بازخورد برای پیشرفت استفاده می‌کنم.", "زه له تېروتنو زده کړه کوم او د ښه کېدو لپاره نظرونه کاروم.", "أتعلم من الأخطاء وأستخدم الملاحظات للتحسن.")
)

private fun speechAccuracy(target: String, spoken: String): Int =
    com.bh.personalacademy.speaking.SpeechScoring.accuracy(target, spoken)

@Composable
private fun SpeakingPage(locale: AcademyLocale, profileId: String, dao: AcademyDao, initialPrompt: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var promptIndex by remember(initialPrompt) { mutableIntStateOf(0) }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(-1) }
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (!granted) status = "Microphone permission is needed to practise speaking."
    }
    val promptList = remember(initialPrompt) {
        if (initialPrompt.isNullOrBlank()) speakingPrompts else listOf(
            SpeakingPrompt("lesson_${initialPrompt.hashCode()}", initialPrompt, "جمله را با صدای بلند تمرین کن.", "دا جمله په لوړ غږ تمرین کړئ.", "تدرّب على نطق الجملة بصوت عالٍ.")
        ) + speakingPrompts
    }
    val prompt = promptList[promptIndex.coerceIn(0, promptList.lastIndex)]
    val currentPrompt by rememberUpdatedState(prompt)
    val recognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val speechEngine = remember {
        TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) { /* Ready for playback. */ }
        }
    }
    DisposableEffect(recognizer) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "" }
            override fun onBeginningOfSpeech() { listening = true }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { listening = false }
            override fun onError(error: Int) { listening = false; status = "Speech was not recognized. Try again in a quiet place." }
            override fun onResults(results: Bundle?) {
                listening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                transcript = spoken
                val evaluatedPrompt = currentPrompt
                val computedScore = speechAccuracy(evaluatedPrompt.text, spoken)
                score = computedScore
                status = "Score compares recognized words; it is not phoneme-level accent analysis."
                if (spoken.isNotBlank()) {
                    val earned = computedScore
                    scope.launch {
                        dao.insertGameAttempt(GameAttemptEntity(
                            attemptId = UUID.randomUUID().toString(),
                            profileId = profileId,
                            gameId = "speaking_" + evaluatedPrompt.id,
                            score = earned,
                            totalQuestions = 100,
                            completedAt = System.currentTimeMillis()
                        ))
                        if (earned >= 70) {
                            val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            dao.applyRewardOnce(RewardEventEntity(
                                eventId = "speaking_${profileId}_${evaluatedPrompt.id}_$day",
                                profileId = profileId,
                                source = "SPEAKING",
                                sourceId = evaluatedPrompt.id + "_" + day,
                                xpDelta = 10,
                                coinDelta = 2,
                                createdAt = System.currentTimeMillis()
                            ))
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose { recognizer?.destroy() }
    }
    DisposableEffect(speechEngine) {
        onDispose { speechEngine.stop(); speechEngine.shutdown() }
    }
    Page {
        Text(when (locale) {
            AcademyLocale.ENGLISH_US -> "Speaking practice"
            AcademyLocale.DARI_AF -> "تمرین صحبت‌کردن"
            AcademyLocale.PASHTO_AF -> "د خبرو تمرین"
            AcademyLocale.ARABIC -> "تدريب التحدث"
        }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        AcademyCard {
            Text(when (locale) {
                AcademyLocale.ENGLISH_US -> "Listen, speak and earn points"
                AcademyLocale.DARI_AF -> "گوش کن، صحبت کن و امتیاز بگیر"
                AcademyLocale.PASHTO_AF -> "واورئ، خبرې وکړئ او نمرې واخلئ"
                AcademyLocale.ARABIC -> "استمع وتحدث واكسب النقاط"
            }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text((promptIndex + 1).toString() + " / " + promptList.size, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(prompt.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(when (locale) {
                AcademyLocale.ENGLISH_US -> "Read, listen, then say the sentence aloud."
                AcademyLocale.DARI_AF -> prompt.dari
                AcademyLocale.PASHTO_AF -> prompt.pashto
                AcademyLocale.ARABIC -> prompt.arabic
            }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = {
                    speechEngine.language = Locale.US
                    speechEngine.speak(prompt.text, TextToSpeech.QUEUE_FLUSH, null, "prompt_" + prompt.id)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 7.dp, pressedElevation = 2.dp)
            ) { Icon(Icons.Default.VolumeUp, null); Spacer(Modifier.width(8.dp)); Text(when (locale) {
                    AcademyLocale.ENGLISH_US -> "Listen to pronunciation"
                    AcademyLocale.DARI_AF -> "شنیدن تلفظ"
                    AcademyLocale.PASHTO_AF -> "تلفظ واورئ"
                    AcademyLocale.ARABIC -> "استمع إلى النطق"
                }) }
            Button(
                onClick = {
                    if (!permissionGranted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    else if (recognizer == null) status = "Speech recognition is unavailable on this device."
                    else {
                        transcript = ""; score = -1
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        }
                        recognizer.startListening(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !listening,
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
            ) { Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text(when (locale) {
                    AcademyLocale.ENGLISH_US -> if (listening) "Listening…" else "Tap and speak"
                    AcademyLocale.DARI_AF -> if (listening) "در حال شنیدن…" else "بزن و صحبت کن"
                    AcademyLocale.PASHTO_AF -> if (listening) "اورېدل کېږي…" else "کلیک او خبرې وکړئ"
                    AcademyLocale.ARABIC -> if (listening) "جارٍ الاستماع…" else "اضغط وتحدث"
                }) }
            if (transcript.isNotBlank()) {
                Text(when (locale) {
                    AcademyLocale.ENGLISH_US -> "What the device heard:"
                    AcademyLocale.DARI_AF -> "چیزی که دستگاه شنید:"
                    AcademyLocale.PASHTO_AF -> "وسیله چې څه واورېدل:"
                    AcademyLocale.ARABIC -> "ما سمعه الجهاز:"
                }, fontWeight = FontWeight.Bold)
                Text(transcript)
                if (score >= 0) {
                    Text(score.toString() + " / 100", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    Text(when (locale) {
                        AcademyLocale.ENGLISH_US -> if (score >= 90) "Excellent!" else if (score >= 70) "Good! +10 XP, +2 coins" else "Keep practising"
                        AcademyLocale.DARI_AF -> if (score >= 90) "عالی!" else if (score >= 70) "آفرین! +10 XP و +2 سکه" else "دوباره تمرین کن"
                        AcademyLocale.PASHTO_AF -> if (score >= 90) "ډېر ښه!" else if (score >= 70) "آفرین! +10 XP او +2 سکې" else "بیا تمرین وکړئ"
                        AcademyLocale.ARABIC -> if (score >= 90) "ممتاز!" else if (score >= 70) "أحسنت! +10 XP و +2 عملة" else "واصل التدريب"
                    })
                }
            }
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.secondary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    recognizer?.cancel(); transcript = ""; score = -1; status = ""
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) { Text(when (locale) {
                    AcademyLocale.ENGLISH_US -> "Try again"
                    AcademyLocale.DARI_AF -> "دوباره کوشش"
                    AcademyLocale.PASHTO_AF -> "بیا هڅه"
                    AcademyLocale.ARABIC -> "حاول مجدداً"
                }) }
                Button(onClick = {
                    recognizer?.cancel()
                    promptIndex = (promptIndex + 1) % promptList.size
                    transcript = ""; score = -1; status = ""
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) { Text(when (locale) {
                    AcademyLocale.ENGLISH_US -> "Next"
                    AcademyLocale.DARI_AF -> "بعدی"
                    AcademyLocale.PASHTO_AF -> "بل"
                    AcademyLocale.ARABIC -> "التالي"
                }) }
            }
        }
        Text(
            "امتیاز بر اساس شباهت متن تشخیص‌شده با جمله اصلی محاسبه می‌شود؛ این ارزیابی کامل لهجه و آواشناسی نیست. کیفیت تشخیص به سرویس گفتار و بسته زبان گوشی بستگی دارد.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
