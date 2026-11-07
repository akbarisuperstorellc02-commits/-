package com.bh.personalacademy

import android.os.Bundle
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.bh.personalacademy.data.local.*
import com.bh.personalacademy.content.AcademyLessonCatalog
import com.bh.personalacademy.content.AcademyGameCatalog
import com.bh.personalacademy.content.GameMode
import com.bh.personalacademy.data.preferences.AcademyPreferences
import com.example.academy.core.AsciiDigits
import com.example.academy.domain.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.bh.personalacademy.reminders.ReminderScheduler
import java.util.UUID
import java.util.Calendar

private enum class AcademyTab { HOME, LEARN, QUIZ, GAMES, PROGRESS, PROFILE, TOOLS, VOCABULARY }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AcademyPreferences(applicationContext)
        val database = AcademyDatabase.getInstance(applicationContext)
        setContent { BHAcademyApp(preferences, database.academyDao()) }
    }
}

@Composable
private fun BHAcademyApp(preferences: AcademyPreferences, dao: AcademyDao) {
    val profile by preferences.profile.collectAsStateWithLifecycle(initialValue = UserProfile())
    val wallet by dao.observeWallet().collectAsStateWithLifecycle(initialValue = null)
    val lessons by dao.observeLessonProgress().collectAsStateWithLifecycle(initialValue = emptyList())
    val attempts by dao.observeQuizAttempts().collectAsStateWithLifecycle(initialValue = emptyList())
    val gameAttempts by dao.observeGameAttempts().collectAsStateWithLifecycle(initialValue = emptyList())
    val todayRange = remember { val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }; val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }; start.timeInMillis to end.timeInMillis }
    val todayStudyMillis by dao.observeStudyMillis(todayRange.first, todayRange.second).collectAsStateWithLifecycle(initialValue = 0L)
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(AcademyTab.HOME) }
    var band by remember { mutableStateOf<LearningBand?>(null) }
    LaunchedEffect(Unit) { dao.insertWallet(WalletEntity(profileId = "primary", coins = 30)) }

    val palette = paletteFor(profile.theme)
    CompositionLocalProvider(LocalLayoutDirection provides if (profile.locale.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        val appColorScheme = if (profile.theme == AcademyThemeId.BLACK_GOLD || profile.theme == AcademyThemeId.DARK) {
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
                titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = fontFor(profile.font), fontWeight = FontWeight.Bold))
        ) {
            Scaffold(containerColor = palette.background, bottomBar = {
                NavigationBar(containerColor = palette.surface) {
                    val items = listOf(
                        Triple(AcademyTab.HOME, Icons.Default.Home, tr(profile.locale, "home")),
                        Triple(AcademyTab.LEARN, Icons.Default.MenuBook, tr(profile.locale, "learn")),
                        Triple(AcademyTab.GAMES, Icons.Default.Extension, tr(profile.locale, "games")),
                        Triple(AcademyTab.PROGRESS, Icons.Default.Insights, tr(profile.locale, "progress")),
                        Triple(AcademyTab.PROFILE, Icons.Default.Person, tr(profile.locale, "profile"))
                    )
                    items.forEach { (destination, icon, label) ->
                        NavigationBarItem(selected = tab == destination, onClick = { tab = destination; band = null },
                            icon = { Icon(icon, contentDescription = label) }, label = { Text(label, maxLines = 1, fontSize = 10.sp) })
                    }
                }
            }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        AcademyTab.HOME -> HomePage(profile, wallet, lessons.size, attempts.size, (todayStudyMillis / 60000L).toInt(),
                            { tab = AcademyTab.LEARN }, { tab = AcademyTab.GAMES }, { tab = AcademyTab.PROFILE }, { tab = AcademyTab.TOOLS })
                        AcademyTab.LEARN -> LearnPage(profile.locale, band, lessons.size,
                            { band = it },
                            { tab = AcademyTab.VOCABULARY },
                            { id -> scope.launch {
                                dao.recordLessonCompletion(lessonId = id, completedAt = System.currentTimeMillis(), score = 100)
                                dao.applyRewardOnce(RewardEventEntity(UUID.randomUUID().toString(), source = "LESSON", sourceId = id,
                                    xpDelta = 25, coinDelta = 5, createdAt = System.currentTimeMillis()))
                            } }, { tab = AcademyTab.QUIZ })
                        AcademyTab.QUIZ -> QuizPage(profile.locale, dao) { tab = AcademyTab.PROGRESS }
                        AcademyTab.GAMES -> GamePage(profile.locale, wallet?.coins ?: 0, dao)
                        AcademyTab.PROGRESS -> ProgressPage(profile.locale, lessons.size, attempts, gameAttempts.size, wallet?.xp ?: 0, wallet?.coins ?: 0)
                        AcademyTab.PROFILE -> ProfilePage(profile, preferences)
                        AcademyTab.TOOLS -> ToolsPage(profile.locale, dao) { tab = AcademyTab.HOME }
                        AcademyTab.VOCABULARY -> VocabularyPage(profile.locale, dao)
                    }
                }
            }
        }
    }
}

private data class Palette(val primary: Color, val secondary: Color, val background: Color, val surface: Color, val variant: Color)
private fun paletteFor(theme: AcademyThemeId): Palette = when (theme) {
    AcademyThemeId.NAVY_BLUE, AcademyThemeId.BLUE_GRAY, AcademyThemeId.SPORT -> Palette(Color(0xFF315B9A), Color(0xFF82A9D8), Color(0xFFF1F5FB), Color.White, Color(0xFFE5EDF8))
    AcademyThemeId.GREEN_BLACK -> Palette(Color(0xFF24745D), Color(0xFF83B9A4), Color(0xFFF0F7F3), Color.White, Color(0xFFE0EEE7))
    AcademyThemeId.BLACK_GOLD -> Palette(Color(0xFFD6B96E), Color(0xFF9A762A), Color(0xFF171717), Color(0xFF242321), Color(0xFF39352C))
    AcademyThemeId.WHITE_GOLD -> Palette(Color(0xFF9A762A), Color(0xFFD6B96E), Color(0xFFFAF8F1), Color.White, Color(0xFFF0EBDD))
    AcademyThemeId.ROYAL_PURPLE, AcademyThemeId.PINK_PURPLE -> Palette(Color(0xFF8051A8), Color(0xFFB99BD3), Color(0xFFF7F1FB), Color.White, Color(0xFFEDE2F5))
    AcademyThemeId.LILAC_WHITE, AcademyThemeId.NIGHT -> Palette(Color(0xFF9576B5), Color(0xFFC6B2D8), Color(0xFFF7F3FA), Color.White, Color(0xFFEDE6F3))
    AcademyThemeId.SPACE -> Palette(Color(0xFF5368B5), Color(0xFF9B8BD4), Color(0xFFEEF0FA), Color.White, Color(0xFFE1E4F4))
    AcademyThemeId.LIGHT -> Palette(Color(0xFF6E8CB8), Color(0xFFADC5E2), Color(0xFFFAFCFF), Color.White, Color(0xFFEAF0F7))
    AcademyThemeId.DARK -> Palette(Color(0xFF8E78C7), Color(0xFFB7A8DE), Color(0xFF191820), Color(0xFF282631), Color(0xFF393644))
    else -> Palette(Color(0xFFD96F9E), Color(0xFFF2AFCB), Color(0xFFFFF5F8), Color.White, Color(0xFFFBE3ED))
}
private fun fontFor(font: AcademyFontId) = when(font) {
    AcademyFontId.SANS_SERIF -> FontFamily.SansSerif
    AcademyFontId.SERIF -> FontFamily.Serif
    AcademyFontId.CURSIVE -> FontFamily.Cursive
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) }
    }
}
@Composable
private fun AcademyCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
@Composable
private fun HomePage(p: UserProfile, w: WalletEntity?, done: Int, quizzes: Int, studyMinutes: Int, learn: () -> Unit, games: () -> Unit, profile: () -> Unit, tools: () -> Unit) {
    Page {
        Text(tr(p.locale, "greeting", p.displayName), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(tr(p.locale, "welcome"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
        AcademyCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr(p.locale, "journey"), style = MaterialTheme.typography.titleLarge)
                    Text(tr(p.locale, "journeySub"))
                }
                Text("🌸", fontSize = 42.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(tr(p.locale, "level"), "${w?.level ?: 1}")
                Stat("XP", "${w?.xp ?: 0}")
                Stat(tr(p.locale, "coins"), "${w?.coins ?: 0}")
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
            OutlinedButton(onClick = games, modifier = Modifier.weight(1f)) { Text(tr(p.locale, "games")) }
            OutlinedButton(onClick = profile, modifier = Modifier.weight(1f)) { Text(tr(p.locale, "myProfile")) }
        }
        OutlinedButton(onClick = tools, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Alarm, null); Spacer(Modifier.width(8.dp)); Text(tr(p.locale, "studyTools")) }
    }
}
@Composable private fun Stat(label: String, value: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(AsciiDigits.normalize(value), fontWeight = FontWeight.Bold); Text(label, fontSize = 11.sp) }
    }
}
@Composable private fun MiniStat(label: String, value: String, modifier: Modifier) {
    AcademyCard { Text(AsciiDigits.normalize(value), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary); Text(label, fontSize = 12.sp) }
}

@Composable
private fun LearnPage(locale: AcademyLocale, selected: LearningBand?, completed: Int, select: (LearningBand?) -> Unit, vocabulary: () -> Unit, complete: (String) -> Unit, quiz: () -> Unit) {
    val names = listOf("Pre-A1", "A1", "A2", "B1", "B2", "C1")
    val lessons = selected?.let { AcademyLessonCatalog.forBand(it) }.orEmpty()
    Page {
        Text(tr(locale, "learningPath"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = vocabulary, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Bookmarks, null); Spacer(Modifier.width(8.dp)); Text(tr(locale, "vocabulary")) }
        if (selected == null) {
            Text(tr(locale, "chooseLevel"))
            LearningBand.values().forEachIndexed { i, level ->
                AcademyCard {
                    Text(names[i], style = MaterialTheme.typography.titleLarge)
                    Text(tr(locale, "bandSub", names[i]))
                    Button(onClick = { select(level) }, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "viewLessons")) }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selected.name.replace("_", "-"), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = { select(null) }) { Text(tr(locale, "allLevels")) }
            }
            lessons.forEachIndexed { index, lesson ->
                var expanded by remember(lesson.id) { mutableStateOf(false) }
                AcademyCard {
                    Text(AsciiDigits.normalize("${index + 1}. ${lesson.localizedTitle.forLocale(locale)}"), style = MaterialTheme.typography.titleMedium)
                    Text(AsciiDigits.normalize(lesson.localizedObjective.forLocale(locale)), fontSize = 13.sp)
                    TextButton(onClick = { expanded = !expanded }) {
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
                    }
                    Button(onClick = { complete(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(6.dp)); Text(tr(locale, "completeLesson"))
                    }
                }
            }
            OutlinedButton(onClick = quiz, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "quizReview")) }
        }
    }
}

@Composable
private fun VocabularyPage(locale: AcademyLocale, dao: AcademyDao) {
    val words by dao.observeVocabulary().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        AcademyLessonCatalog.all.forEach { lesson ->
            lesson.words.forEach { word ->
                dao.insertVocabularyIfMissing(VocabularyEntity(
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
            }
        }
    }
}

@Composable
private fun GamePage(locale: AcademyLocale, coins: Long, dao: AcademyDao) {
    val scope = rememberCoroutineScope()
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var hintVisible by remember { mutableStateOf(false) }
    var hintPending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }
    var finished by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(30) }
    var resultSaved by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMode?.id, finished) {
        if (selectedMode?.timed == true && !finished) {
            while (secondsLeft > 0 && !finished) {
                delay(1_000L)
                secondsLeft -= 1
            }
            if (secondsLeft <= 1) finished = true
        }
    }

    LaunchedEffect(finished, selectedMode?.id) {
        val mode = selectedMode
        if (finished && mode != null && !resultSaved) {
            resultSaved = true
            dao.insertGameAttempt(
                GameAttemptEntity(
                    attemptId = UUID.randomUUID().toString(),
                    gameId = mode.id,
                    score = score,
                    totalQuestions = 5,
                    completedAt = System.currentTimeMillis()
                )
            )
            if (score >= 3) {
                dao.applyRewardOnce(
                    RewardEventEntity(
                        eventId = "game_reward_${mode.id}",
                        source = "GAME",
                        sourceId = "${mode.id}_first_completion",
                        xpDelta = 30,
                        coinDelta = 8,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun resetRound(mode: GameMode? = selectedMode) {
        selectedMode = mode
        round = 0
        score = 0
        selectedAnswer = -1
        hintVisible = false
        hintPending = false
        feedback = ""
        finished = false
        secondsLeft = 30
        resultSaved = false
    }

    val labels = when (locale) {
        AcademyLocale.ENGLISH_US -> listOf("Games", "Back to games", "Question", "Next", "Your result", "Correct", "Try again", "Time left", "Hint · 6 coins", "6 coins were spent.", "Not enough coins.", "Play again")
        AcademyLocale.DARI_AF -> listOf("بازی‌های آموزشی", "بازگشت به بازی‌ها", "پرسش", "بعدی", "نتیجه تو", "درست", "دوباره تلاش کن", "زمان باقی‌مانده", "راهنما · 6 سکه", "6 سکه مصرف شد.", "سکه کافی نداری.", "بازی دوباره")
        AcademyLocale.PASHTO_AF -> listOf("زده‌کړیزې لوبې", "لوبو ته بېرته", "پوښتنه", "بل", "ستاسو پایله", "سم", "بیا هڅه", "پاتې وخت", "لارښوونه · 6 سکې", "6 سکې ولګېدې.", "کافي سکې نشته.", "بیا لوبه")
        AcademyLocale.ARABIC -> listOf("الألعاب التعليمية", "العودة إلى الألعاب", "السؤال", "التالي", "نتيجتك", "صحيح", "حاول مجدداً", "الوقت المتبقي", "تلميح · 6 عملات", "تم خصم 6 عملات.", "لا توجد عملات كافية.", "العب مرة أخرى")
    }

    Page {
        if (selectedMode == null) {
            Text(labels[0], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(tr(locale, "gameLibrary"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
            AcademyGameCatalog.modes.forEachIndexed { index, mode ->
                AcademyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(AsciiDigits.normalize((index + 1).toString()), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(mode.title.forLocale(locale), style = MaterialTheme.typography.titleMedium)
                            Text(mode.description.forLocale(locale), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
                        }
                    }
                    Button(onClick = { resetRound(mode) }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr(locale, "playGame"))
                    }
                }
            }
        } else {
            val mode = selectedMode!!
            val question = AcademyGameCatalog.questionFor(mode, round)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mode.title.forLocale(locale), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { resetRound(null) }) { Text(labels[1]) }
            }
            AcademyCard {
                Text("${labels[2]} ${AsciiDigits.normalize((round + 1).toString())}/5", fontSize = 12.sp)
                LinearProgressIndicator(progress = { (round + 1) / 5f }, modifier = Modifier.fillMaxWidth())
                if (mode.timed) Text("${labels[7]}: ${AsciiDigits.normalize(secondsLeft.toString())}s", color = MaterialTheme.colorScheme.error)
                Text(question.prompt.forLocale(locale), style = MaterialTheme.typography.titleLarge)
                question.options.forEachIndexed { optionIndex, option ->
                    OutlinedButton(
                        onClick = {
                            if (selectedAnswer == -1 && !finished) {
                                selectedAnswer = optionIndex
                                if (optionIndex == question.correctIndex) {
                                    score += 1
                                    feedback = labels[5]
                                } else {
                                    feedback = labels[6]
                                }
                            }
                        },
                        enabled = selectedAnswer == -1 && !finished,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option.forLocale(locale))
                    }
                }
                if (hintVisible) Text(question.hint.forLocale(locale), color = MaterialTheme.colorScheme.primary)
                if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (selectedAnswer != -1) {
                    Text(question.explanation.forLocale(locale), fontSize = 13.sp)
                    Button(onClick = {
                        if (round == 4) finished = true
                        else {
                            round += 1
                            selectedAnswer = -1
                            hintVisible = false
                            hintPending = false
                            feedback = ""
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (round == 4) labels[4] else labels[3]) }
                } else {
                    OutlinedButton(
                        onClick = {
                            if (!hintVisible && !hintPending) {
                                hintPending = true
                                scope.launch {
                                    if (dao.spendHintCoins() > 0) {
                                        hintVisible = true
                                        feedback = labels[8]
                                    } else {
                                        feedback = labels[10]
                                    }
                                    hintPending = false
                                }
                            }
                        },
                        enabled = !hintVisible && !hintPending,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(labels[8]) }
                }
                Text(tr(locale, "coinBalance", AsciiDigits.normalize(coins.toString())), fontSize = 12.sp)
            }
            if (finished) {
                AcademyCard {
                    Text(labels[4], style = MaterialTheme.typography.titleLarge)
                    Text("${AsciiDigits.normalize(score.toString())}/5", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text(if (score >= 3) tr(locale, "gamePassed") else tr(locale, "gameTryAgain"))
                    Button(onClick = { resetRound(mode) }, modifier = Modifier.fillMaxWidth()) { Text(labels[11]) }
                    OutlinedButton(onClick = { resetRound(null) }, modifier = Modifier.fillMaxWidth()) { Text(labels[1]) }
                }
            }
        }
    }
}

@Composable
private fun QuizPage(locale: AcademyLocale, dao: AcademyDao, onFinished: () -> Unit) {
    data class Question(val prompt: String, val options: List<String>, val correct: Int, val explanation: String)
    val questions = listOf(
        Question("What does APPLE mean?", listOf("سیب", "کتاب", "آب"), 0, "APPLE = سیب"),
        Question("Choose the correct greeting for the morning.", listOf("Good night", "Good morning", "Goodbye"), 1, "Good morning = صبح بخیر"),
        Question("I ___ a student.", listOf("is", "are", "am"), 2, "With I, use AM.")
    )
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(finished) {
        if (finished) {
            val attemptId = UUID.randomUUID().toString()
            dao.insertQuizAttempt(QuizAttemptEntity(
                attemptId = attemptId, quizId = "starter_mixed_quiz",
                score = score, totalQuestions = questions.size,
                completedAt = System.currentTimeMillis()
            ))
            if (score >= 2) {
                dao.applyRewardOnce(RewardEventEntity(
                    eventId = UUID.randomUUID().toString(), source = "QUIZ",
                    sourceId = "starter_mixed_quiz_first_reward", xpDelta = 30, coinDelta = 6,
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
                Text("${AsciiDigits.normalize(score.toString())}/${questions.size}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Text(tr(locale, "quizSaved"))
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "seeProgress")) }
            } else {
                val question = questions[index]
                Text(tr(locale, "questionProgress", "${index + 1}/${questions.size}"))
                LinearProgressIndicator(progress = { (index + 1).toFloat() / questions.size }, modifier = Modifier.fillMaxWidth())
                Text(question.prompt, style = MaterialTheme.typography.titleLarge)
                question.options.forEachIndexed { optionIndex, option ->
                    OutlinedButton(onClick = {
                        val correct = optionIndex == question.correct
                        if (correct) score += 1
                        explanation = question.explanation
                        if (index == questions.lastIndex) finished = true else index += 1
                    }, modifier = Modifier.fillMaxWidth()) { Text(option) }
                }
                if (explanation.isNotEmpty()) Text(explanation)
            }
        }
    }
}

@Composable
private fun ProgressPage(locale: AcademyLocale, lessons: Int, attempts: List<QuizAttemptEntity>, games: Int, xp: Long, coins: Long) {
    Page {
        Text(tr(locale, "progress"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text(tr(locale, "summary"), style = MaterialTheme.typography.titleLarge)
            Line(tr(locale, "lessons"), "$lessons")
            Line(tr(locale, "quizzes"), "${attempts.size}")
            Line(tr(locale, "games"), "${games}")
            Line("XP", "$xp")
            Line(tr(locale, "coins"), "$coins")
            val average = if (attempts.isEmpty()) 0 else attempts.map { if (it.totalQuestions == 0) 0 else it.score * 100 / it.totalQuestions }.average().toInt()
            Line(tr(locale, "average"), "$average%")
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
private fun ToolsPage(locale: AcademyLocale, dao: AcademyDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reminders by dao.observeReminders().collectAsStateWithLifecycle(initialValue = emptyList())
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    var sessionStart by remember { mutableLongStateOf(0L) }
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("19:00") }
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
                    sessionId = sessionId, lessonId = "independent-study",
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
                OutlinedButton(onClick = { running = false }, enabled = running, modifier = Modifier.weight(1f)) { Text(tr(locale, "pauseTimer")) }
            }
            Button(onClick = {
                running = false
                if (elapsedSeconds > 0L && sessionStart > 0L) {
                    scope.launch {
                        dao.insertStudySession(StudySessionEntity(
                            sessionId = sessionId, lessonId = "independent-study",
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
            Text(tr(locale, "dailyReminder"), fontSize = 12.sp)
            Button(onClick = {
                val parts = reminderTime.trim().split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull()
                val minute = parts.getOrNull(1)?.toIntOrNull()
                if (reminderTitle.isBlank() || hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                    feedback = tr(locale, "invalidTime")
                } else {
                    val id = UUID.randomUUID().toString()
                    val title = reminderTitle.trim()
                    val schedule = {
                        scope.launch {
                            dao.saveReminder(ReminderEntity(reminderId = id, title = title, hour = hour!!, minute = minute!!, repeatDaysMask = 127, enabled = true))
                        }
                        ReminderScheduler.schedule(context, id, title, hour!!, minute!!, 127)
                        feedback = tr(locale, "reminderSaved")
                        reminderTitle = ""
                    }
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        feedback = tr(locale, "permissionNeeded")
                    }
                    schedule()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "scheduleReminder")) }
            if (reminders.isEmpty()) Text(tr(locale, "noReminders"))
            reminders.forEach { reminder ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(reminder.title, fontWeight = FontWeight.SemiBold)
                        Text(AsciiDigits.normalize("%02d:%02d".format(reminder.hour, reminder.minute)), fontSize = 12.sp)
                    }
                    IconButton(onClick = {
                        scope.launch { dao.deleteReminder(reminder.reminderId) }
                        ReminderScheduler.cancel(context, reminder.reminderId)
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete reminder") }
                }
            }
            if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProfilePage(profile: UserProfile, prefs: AcademyPreferences) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var username by remember(profile.username) { mutableStateOf(profile.username) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            scope.launch { prefs.updateAvatar(uri.toString()) }
        }
    }
    Page {
        Text(tr(profile.locale, "profile"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (profile.avatarUri != null) {
                    AsyncImage(model = profile.avatarUri, contentDescription = tr(profile.locale, "profile"),
                        modifier = Modifier.size(76.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.size(76.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Text("🌷", fontSize = 36.sp)
                    }
                }
                OutlinedButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }) { Text(tr(profile.locale, "changeAvatar")) }
            }
            Text(profile.displayName, style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(displayName, { displayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(profile.locale, "displayName")) }, singleLine = true)
            OutlinedTextField(username, { username = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr(profile.locale, "username")) }, singleLine = true)
            Button(onClick = { scope.launch { prefs.updateDisplayName(displayName); prefs.updateUsername(username) } }, modifier = Modifier.fillMaxWidth()) { Text(tr(profile.locale, "save")) }
        }
        AcademyCard {
            Text(tr(profile.locale, "language"), style = MaterialTheme.typography.titleMedium)
            AcademyLocale.values().forEach { option ->
                OutlinedButton(onClick = { scope.launch { prefs.updateLocale(option) } }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (profile.locale == option) "✓  ${tr(profile.locale, option.name)}" else tr(profile.locale, option.name))
                }
            }
        }
        AcademyCard {
            Text(tr(profile.locale, "theme"), style = MaterialTheme.typography.titleMedium)
            AcademyThemeId.values().toList().chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pair.forEach { option ->
                        OutlinedButton(onClick = { scope.launch { prefs.updateTheme(option) } }, modifier = Modifier.weight(1f)) {
                            Text(tr(profile.locale, option.name), fontSize = 9.sp, maxLines = 1)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        AcademyCard {
            Text(tr(profile.locale, "font"), style = MaterialTheme.typography.titleMedium)
            AcademyFontId.values().forEach { option ->
                OutlinedButton(onClick = { scope.launch { prefs.updateFont(option) } }, modifier = Modifier.fillMaxWidth()) { Text(tr(profile.locale, option.name)) }
            }
            Text(tr(profile.locale, "dailyGoal"), style = MaterialTheme.typography.titleMedium)
            var goal by remember(profile.dailyStudyGoalMinutes) { mutableFloatStateOf(profile.dailyStudyGoalMinutes.toFloat()) }
            Text("${AsciiDigits.normalize(goal.toInt().toString())} ${tr(profile.locale, "minutes")}")
            Slider(value = goal, onValueChange = { goal = it }, valueRange = 5f..120f, steps = 22,
                onValueChangeFinished = { scope.launch { prefs.updateDailyGoal(goal.toInt()) } })
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
        "myProfile" to "My profile", "learningPath" to "English learning path", "vocabulary" to "Vocabulary notebook", "searchWords" to "Search words", "wordCount" to "$arg words", "chooseLevel" to "Choose your current level",
        "bandSub" to "Structured learning at $arg", "viewLessons" to "View lessons", "allLevels" to "All levels",
        "lessonExplain" to "Learn the topic, practise vocabulary and check your understanding.", "lessonDetails" to "Open lesson", "hideDetails" to "Hide details", "newWords" to "New vocabulary",
        "completeLesson" to "Complete lesson · earn XP", "quizReview" to "Review quizzes", "wordChallenge" to "Word meaning challenge",
        "chooseMeaning" to "Choose the Dari meaning of APPLE", "correct" to "Excellent! Correct answer.", "wrong" to "Not quite. Try again.",
        "hintText" to "Hint: It is a fruit.", "hintPaid" to "Hint unlocked. 6 coins spent.", "needCoins" to "You need at least 6 coins.",
        "hintButton" to "Get a hint · 6 coins", "coinBalance" to "Your coins: $arg", "newRound" to "New round",
        "gameRoadmap" to "More vocabulary, spelling and memory games are planned for the next content phase.",
        "summary" to "Learning summary", "average" to "Average quiz score", "quizHistory" to "Quiz history", "noHistory" to "No quiz attempts yet.", "quizTitle" to "Quick mixed quiz", "quizComplete" to "Quiz completed!", "quizSaved" to "Your result has been saved to your learning history.", "seeProgress" to "View progress", "questionProgress" to "Question $arg",
        "displayName" to "Display name", "changeAvatar" to "Change photo", "username" to "Username", "save" to "Save profile", "language" to "Language", "theme" to "Theme",
        "font" to "Font", "minutes" to "minutes", "studyTools" to "Study tools", "timer" to "Study timer", "startTimer" to "Start", "pauseTimer" to "Pause", "finishTimer" to "Finish session", "timerSaved" to "Study session saved.", "reminders" to "Reminders and alarms", "reminderTitle" to "Reminder title", "timeHHMM" to "Time (HH:MM)", "dailyReminder" to "Repeat every day", "scheduleReminder" to "Schedule reminder", "reminderSaved" to "Reminder scheduled.", "invalidTime" to "Enter a title and valid time such as 19:30.", "permissionNeeded" to "Allow notifications in Android settings to receive reminders.", "noReminders" to "No reminders scheduled yet.", "DARI_AF" to "Afghan Dari", "PASHTO_AF" to "Afghan Pashto", "ARABIC" to "Arabic", "ENGLISH_US" to "English",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive"
    )
    val fa = mapOf(
        "home" to "خانه", "learn" to "آموزش", "games" to "بازی‌ها", "progress" to "پیشرفت", "profile" to "پروفایل",
        "greeting" to "سلام $arg جان!", "welcome" to "هر روز یک قدم کوچک، یک موفقیت بزرگ.",
        "journey" to "مسیر یادگیری تو", "journeySub" to "هر واژه تازه تو را به هدفت نزدیک‌تر می‌کند.",
        "level" to "سطح", "coins" to "سکه", "dailyGoal" to "هدف روزانه مطالعه", "goalMins" to "$arg دقیقه",
        "goalHint" to "برای ثبت زمان واقعی، یک درس را آغاز کن.", "lessons" to "درس‌های تکمیل‌شده", "quizzes" to "آزمون‌های انجام‌شده",
        "continue" to "برای قدم بعدی آماده‌ای؟", "chooseLesson" to "یک سطح را انتخاب کن و یادگیری را ادامه بده.", "openLessons" to "رفتن به درس‌ها",
        "myProfile" to "پروفایل من", "learningPath" to "مسیر آموزش انگلیسی", "vocabulary" to "دفترچه واژگان", "searchWords" to "جست‌وجوی واژه", "wordCount" to "$arg واژه", "chooseLevel" to "سطح فعلی خود را انتخاب کن",
        "bandSub" to "درس‌های مرحله‌بندی‌شده $arg", "viewLessons" to "دیدن درس‌ها", "allLevels" to "همه سطح‌ها",
        "lessonExplain" to "موضوع را یاد بگیر، واژه‌های تازه را تمرین کن و دانسته‌هایت را بسنج.", "lessonDetails" to "بازکردن درس", "hideDetails" to "بستن جزئیات", "newWords" to "واژگان تازه",
        "completeLesson" to "تکمیل درس · دریافت امتیاز", "quizReview" to "رفتن به آزمون‌ها", "wordChallenge" to "چالش معنی واژه",
        "chooseMeaning" to "معنی دری کلمه APPLE را انتخاب کن", "correct" to "آفرین! پاسخ درست است.", "wrong" to "این پاسخ درست نیست؛ دوباره تلاش کن.",
        "hintText" to "راهنما: این یک میوه است.", "hintPaid" to "راهنما فعال شد؛ 6 سکه مصرف شد.", "needCoins" to "حداقل 6 سکه نیاز داری.",
        "hintButton" to "دریافت راهنما · 6 سکه", "coinBalance" to "سکه‌های تو: $arg", "newRound" to "دور تازه",
        "gameRoadmap" to "بازی‌های واژگان، املا و حافظه در مرحله بعدی محتوا اضافه می‌شوند.",
        "summary" to "خلاصه یادگیری تو", "average" to "میانگین نمره آزمون", "quizHistory" to "تاریخچه آزمون", "noHistory" to "هنوز آزمونی انجام نشده است.", "quizTitle" to "آزمون ترکیبی کوتاه", "quizComplete" to "آزمون تکمیل شد!", "quizSaved" to "نتیجه در تاریخچه یادگیری ذخیره شد.", "seeProgress" to "دیدن پیشرفت", "questionProgress" to "پرسش $arg",
        "displayName" to "نام نمایشی", "changeAvatar" to "تغییر عکس", "username" to "نام کاربری", "save" to "ذخیره پروفایل", "language" to "زبان", "theme" to "تم",
        "font" to "فونت", "minutes" to "دقیقه", "studyTools" to "ابزارهای مطالعه", "timer" to "تایمر مطالعه", "startTimer" to "شروع", "pauseTimer" to "مکث", "finishTimer" to "پایان جلسه", "timerSaved" to "جلسه مطالعه ذخیره شد.", "reminders" to "یادآوری و زنگ", "reminderTitle" to "عنوان یادآوری", "timeHHMM" to "زمان (ساعت:دقیقه)", "dailyReminder" to "تکرار هر روز", "scheduleReminder" to "تنظیم یادآوری", "reminderSaved" to "یادآوری تنظیم شد.", "invalidTime" to "عنوان و زمان درست مانند 19:30 وارد کن.", "permissionNeeded" to "برای دریافت یادآوری، اجازه اعلان‌ها را در اندروید فعال کن.", "noReminders" to "هنوز یادآوری تنظیم نشده است.", "DARI_AF" to "دری افغانستان", "PASHTO_AF" to "پشتو افغانستان", "ARABIC" to "عربی", "ENGLISH_US" to "انگلیسی",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "دست‌نویس"
    )
    val ps = mapOf("home" to "کور", "learn" to "زده کړه", "games" to "لوبې", "progress" to "پرمختګ", "profile" to "پروفایل",
        "greeting" to "سلام، $arg!", "welcome" to "هره ورځ لږ پرمختګ، لویه لاسته راوړنه.", "journey" to "ستاسو د زده کړې سفر",
        "level" to "کچه", "coins" to "سکې", "dailyGoal" to "د مطالعې ورځنی هدف", "lessons" to "بشپړ شوي درسونه", "quizzes" to "ازموینې",
        "learningPath" to "د انګلیسي زده کړې لاره", "vocabulary" to "د لغتونو کتابچه", "searchWords" to "د لغت لټون", "wordCount" to "$arg لغتونه", "chooseLevel" to "خپله کچه وټاکئ", "viewLessons" to "درسونه وګورئ", "allLevels" to "ټولې کچې",
        "correct" to "ډېر ښه! ځواب سم دی.", "quizTitle" to "لنډه ګډه ازموینه", "quizComplete" to "ازموینه بشپړه شوه!", "quizSaved" to "ستاسو پایله د زده کړې په تاریخ کې خوندي شوه.", "seeProgress" to "پرمختګ وګورئ", "wrong" to "بیا هڅه وکړئ.", "hintButton" to "لارښوونه · 6 سکې", "hintPaid" to "لارښوونه فعاله شوه؛ 6 سکې ولګېدې.",
        "needCoins" to "لږ تر لږه 6 سکې پکار دي.", "displayName" to "ښکاره نوم", "changeAvatar" to "انځور بدل کړئ", "username" to "کارن نوم", "save" to "پروفایل خوندي کړئ",
        "language" to "ژبه", "theme" to "بڼه", "font" to "لیکبڼه", "minutes" to "دقیقې", "studyTools" to "د مطالعې وسایل", "timer" to "د مطالعې ټایمر", "startTimer" to "پیل", "pauseTimer" to "تم", "finishTimer" to "ناسته پای ته ورسوه", "timerSaved" to "د مطالعې ناسته خوندي شوه.", "reminders" to "یادونې او الارم", "reminderTitle" to "د یادونې سرلیک", "timeHHMM" to "وخت (HH:MM)", "dailyReminder" to "هره ورځ تکرار", "scheduleReminder" to "یادونه تنظیم کړه", "reminderSaved" to "یادونه تنظیم شوه.", "invalidTime" to "سرلیک او سم وخت ولیکئ، لکه 19:30.", "permissionNeeded" to "د یادونو ترلاسه کولو لپاره د Android خبرتیا اجازه ورکړئ.", "noReminders" to "تر اوسه یادونه نشته.",
        "DARI_AF" to "افغان دري", "PASHTO_AF" to "افغان پښتو", "ARABIC" to "عربي", "ENGLISH_US" to "انګلیسي",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "لاس لیک")
    val ar = mapOf("home" to "الرئيسية", "learn" to "التعلم", "games" to "الألعاب", "progress" to "التقدم", "profile" to "الملف الشخصي",
        "greeting" to "مرحباً، $arg!", "welcome" to "خطوة صغيرة كل يوم تصنع إنجازاً كبيراً.", "journey" to "رحلة التعلم",
        "level" to "المستوى", "coins" to "العملات", "dailyGoal" to "هدف الدراسة اليومي", "lessons" to "الدروس المكتملة", "quizzes" to "الاختبارات",
        "learningPath" to "مسار تعلم الإنجليزية", "vocabulary" to "دفتر المفردات", "searchWords" to "ابحث عن كلمة", "wordCount" to "$arg كلمة", "chooseLevel" to "اختر مستواك", "viewLessons" to "عرض الدروس", "allLevels" to "كل المستويات",
        "correct" to "ممتاز! إجابة صحيحة.", "quizTitle" to "اختبار قصير متنوع", "quizComplete" to "اكتمل الاختبار!", "quizSaved" to "تم حفظ النتيجة في سجل التعلم.", "seeProgress" to "عرض التقدم", "wrong" to "حاول مرة أخرى.", "hintButton" to "تلميح · 6 عملات", "hintPaid" to "تم تفعيل التلميح؛ خُصمت 6 عملات.",
        "needCoins" to "تحتاج إلى 6 عملات على الأقل.", "displayName" to "الاسم الظاهر", "changeAvatar" to "تغيير الصورة", "username" to "اسم المستخدم", "save" to "حفظ الملف",
        "language" to "اللغة", "theme" to "السمة", "font" to "الخط", "minutes" to "دقيقة", "studyTools" to "أدوات الدراسة", "timer" to "مؤقت الدراسة", "startTimer" to "ابدأ", "pauseTimer" to "إيقاف مؤقت", "finishTimer" to "إنهاء الجلسة", "timerSaved" to "تم حفظ جلسة الدراسة.", "reminders" to "التذكيرات والمنبهات", "reminderTitle" to "عنوان التذكير", "timeHHMM" to "الوقت (HH:MM)", "dailyReminder" to "التكرار يومياً", "scheduleReminder" to "جدولة التذكير", "reminderSaved" to "تمت جدولة التذكير.", "invalidTime" to "أدخل عنواناً ووقتاً صحيحاً مثل 19:30.", "permissionNeeded" to "اسمح بالإشعارات من إعدادات Android لاستلام التذكيرات.", "noReminders" to "لا توجد تذكيرات مجدولة بعد.",
        "DARI_AF" to "الداري الأفغانية", "PASHTO_AF" to "البشتوية الأفغانية", "ARABIC" to "العربية", "ENGLISH_US" to "الإنجليزية",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive")
    val psComplete = ps + mapOf(
        "journeySub" to "هره نوې کلمه مو موخې ته نږدې کوي.", "goalMins" to "$arg دقیقې", "goalHint" to "د مطالعې وخت ثبتولو لپاره درس پیل کړئ.",
        "continue" to "راتلونکي ګام ته چمتو یاست؟", "chooseLesson" to "کچه وټاکئ او زده کړه روانه وساتئ.", "openLessons" to "درسونه پرانیزئ",
        "myProfile" to "زما پروفایل", "bandSub" to "د $arg لپاره منظم درسونه", "lessonDetails" to "درس پرانیزئ", "hideDetails" to "جزئیات پټ کړئ",
        "newWords" to "نوې کلمې", "completeLesson" to "درس بشپړ کړئ · XP ترلاسه کړئ", "quizReview" to "ازموینې بیا وګورئ",
        "wordChallenge" to "د کلمې د مانا لوبه", "chooseMeaning" to "د APPLE پښتو/دري مانا وټاکئ", "hintText" to "لارښوونه: دا یوه مېوه ده.",
        "coinBalance" to "ستاسې سکې: $arg", "newRound" to "نوی پړاو", "gameRoadmap" to "نورې د لغت، املا او حافظې لوبې به وروسته ورزیاتې شي.",
        "summary" to "د زده کړې لنډیز", "average" to "د ازموینو منځنۍ نمره", "quizHistory" to "د ازموینو تاریخچه", "noHistory" to "تر اوسه ازموینه نه ده شوې.",
        "questionProgress" to "پوښتنه $arg"
    )
    val arComplete = ar + mapOf(
        "journeySub" to "كل كلمة جديدة تقرّبك من هدفك.", "goalMins" to "$arg دقيقة", "goalHint" to "ابدأ درساً لتسجيل وقت الدراسة الفعلي.",
        "continue" to "هل أنت مستعد للخطوة التالية؟", "chooseLesson" to "اختر مستوى وتابع التعلم.", "openLessons" to "افتح الدروس",
        "myProfile" to "ملفي الشخصي", "bandSub" to "دروس منظمة للمستوى $arg", "lessonDetails" to "افتح الدرس", "hideDetails" to "إخفاء التفاصيل",
        "newWords" to "مفردات جديدة", "completeLesson" to "أكمل الدرس · اكسب XP", "quizReview" to "مراجعة الاختبارات",
        "wordChallenge" to "تحدي معنى الكلمة", "chooseMeaning" to "اختر معنى APPLE باللغة الدارية", "hintText" to "تلميح: إنها فاكهة.",
        "coinBalance" to "عملاتك: $arg", "newRound" to "جولة جديدة", "gameRoadmap" to "ستضاف ألعاب المفردات والإملاء والذاكرة في مرحلة المحتوى التالية.",
        "summary" to "ملخص التعلم", "average" to "متوسط نتيجة الاختبارات", "quizHistory" to "سجل الاختبارات", "noHistory" to "لم تُجرَ أي اختبارات بعد.",
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
