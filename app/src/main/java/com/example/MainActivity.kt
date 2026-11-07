package com.bh.personalacademy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bh.personalacademy.data.local.*
import com.bh.personalacademy.content.AcademyLessonCatalog
import com.bh.personalacademy.data.preferences.AcademyPreferences
import com.example.academy.core.AsciiDigits
import com.example.academy.domain.model.*
import kotlinx.coroutines.launch
import java.util.UUID

private enum class AcademyTab { HOME, LEARN, QUIZ, GAMES, PROGRESS, PROFILE }

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
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(AcademyTab.HOME) }
    var band by remember { mutableStateOf<LearningBand?>(null) }
    LaunchedEffect(Unit) { dao.insertWallet(WalletEntity(profileId = "primary", coins = 30)) }

    val palette = paletteFor(profile.theme)
    CompositionLocalProvider(LocalLayoutDirection provides if (profile.locale.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        MaterialTheme(
            colorScheme = lightColorScheme(primary = palette.primary, onPrimary = Color.White, secondary = palette.secondary,
                background = palette.background, surface = palette.surface, surfaceVariant = palette.variant),
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
                        AcademyTab.HOME -> HomePage(profile, wallet, lessons.size, attempts.size,
                            { tab = AcademyTab.LEARN }, { tab = AcademyTab.GAMES }, { tab = AcademyTab.PROFILE })
                        AcademyTab.LEARN -> LearnPage(profile.locale, band, lessons.size,
                            { band = it },
                            { id -> scope.launch {
                                dao.saveLessonProgress(LessonProgressEntity(lessonId = id, completedAt = System.currentTimeMillis(), bestScore = 100, attempts = 1))
                                dao.applyRewardOnce(RewardEventEntity(UUID.randomUUID().toString(), source = "LESSON", sourceId = id,
                                    xpDelta = 25, coinDelta = 5, createdAt = System.currentTimeMillis()))
                            } }, { tab = AcademyTab.QUIZ })
                        AcademyTab.QUIZ -> QuizPage(profile.locale, dao) { tab = AcademyTab.PROGRESS }
                        AcademyTab.GAMES -> GamePage(profile.locale, wallet?.coins ?: 0, dao)
                        AcademyTab.PROGRESS -> ProgressPage(profile.locale, lessons.size, attempts, wallet?.xp ?: 0, wallet?.coins ?: 0)
                        AcademyTab.PROFILE -> ProfilePage(profile, preferences)
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
private fun HomePage(p: UserProfile, w: WalletEntity?, done: Int, quizzes: Int, learn: () -> Unit, games: () -> Unit, profile: () -> Unit) {
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
            LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
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
private fun LearnPage(locale: AcademyLocale, selected: LearningBand?, completed: Int, select: (LearningBand?) -> Unit, complete: (String) -> Unit, quiz: () -> Unit) {
    val names = listOf("Pre-A1", "A1", "A2", "B1", "B2", "C1")
    val lessons = selected?.let { AcademyLessonCatalog.forBand(it) }.orEmpty()
    Page {
        Text(tr(locale, "learningPath"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    Text("${index + 1}. ${lesson.title}", style = MaterialTheme.typography.titleMedium)
                    Text(lesson.objective, fontSize = 13.sp)
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) tr(locale, "hideDetails") else tr(locale, "lessonDetails"))
                    }
                    if (expanded) {
                        Text(lesson.explanation)
                        Text(tr(locale, "newWords"), fontWeight = FontWeight.Bold)
                        lesson.words.forEach { word ->
                            Text("${word.english} — ${word.dari}  (${word.pronunciation})", fontSize = 13.sp)
                        }
                        Text(lesson.example, fontWeight = FontWeight.SemiBold)
                        Text(lesson.translation)
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
private fun GamePage(locale: AcademyLocale, coins: Long, dao: AcademyDao) {
    val scope = rememberCoroutineScope()
    var hint by remember { mutableStateOf(false) }
    var solved by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }
    Page {
        Text(tr(locale, "games"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text(tr(locale, "wordChallenge"), style = MaterialTheme.typography.titleLarge)
            Text("APPLE", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Text(tr(locale, "chooseMeaning"))
            listOf("سیب", "کتاب", "آب").forEach { option ->
                OutlinedButton(onClick = {
                    if (option == "سیب") {
                        solved = true; feedback = tr(locale, "correct")
                        scope.launch { dao.applyRewardOnce(RewardEventEntity(UUID.randomUUID().toString(), source = "GAME", sourceId = "apple_round_01", xpDelta = 15, coinDelta = 3, createdAt = System.currentTimeMillis())) }
                    } else feedback = tr(locale, "wrong")
                }, enabled = !solved, modifier = Modifier.fillMaxWidth()) { Text(option) }
            }
            if (hint) Text(tr(locale, "hintText"))
            if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = {
                if (!hint && !solved) scope.launch {
                    if (dao.spendHintCoins() > 0) { hint = true; feedback = tr(locale, "hintPaid") }
                    else feedback = tr(locale, "needCoins")
                }
            }, enabled = !hint && !solved, modifier = Modifier.fillMaxWidth()) { Text(tr(locale, "hintButton")) }
            Text(tr(locale, "coinBalance", AsciiDigits.normalize("$coins")))
            TextButton(onClick = { hint = false; solved = false; feedback = "" }) { Text(tr(locale, "newRound")) }
        }
        Text(tr(locale, "gameRoadmap"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
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
private fun ProgressPage(locale: AcademyLocale, lessons: Int, attempts: List<QuizAttemptEntity>, xp: Long, coins: Long) {
    Page {
        Text(tr(locale, "progress"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text(tr(locale, "summary"), style = MaterialTheme.typography.titleLarge)
            Line(tr(locale, "lessons"), "$lessons")
            Line(tr(locale, "quizzes"), "${attempts.size}")
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
private fun ProfilePage(profile: UserProfile, prefs: AcademyPreferences) {
    val scope = rememberCoroutineScope()
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var username by remember(profile.username) { mutableStateOf(profile.username) }
    Page {
        Text(tr(profile.locale, "profile"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AcademyCard {
            Text("🌷", fontSize = 42.sp)
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
            AcademyThemeId.values().chunked(2).forEach { pair ->
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
        "myProfile" to "My profile", "learningPath" to "English learning path", "chooseLevel" to "Choose your current level",
        "bandSub" to "Structured learning at $arg", "viewLessons" to "View lessons", "allLevels" to "All levels",
        "lessonExplain" to "Learn the topic, practise vocabulary and check your understanding.", "lessonDetails" to "Open lesson", "hideDetails" to "Hide details", "newWords" to "New vocabulary",
        "completeLesson" to "Complete lesson · earn XP", "quizReview" to "Review quizzes", "wordChallenge" to "Word meaning challenge",
        "chooseMeaning" to "Choose the Dari meaning of APPLE", "correct" to "Excellent! Correct answer.", "wrong" to "Not quite. Try again.",
        "hintText" to "Hint: It is a fruit.", "hintPaid" to "Hint unlocked. 6 coins spent.", "needCoins" to "You need at least 6 coins.",
        "hintButton" to "Get a hint · 6 coins", "coinBalance" to "Your coins: $arg", "newRound" to "New round",
        "gameRoadmap" to "More vocabulary, spelling and memory games are planned for the next content phase.",
        "summary" to "Learning summary", "average" to "Average quiz score", "quizHistory" to "Quiz history", "noHistory" to "No quiz attempts yet.", "quizTitle" to "Quick mixed quiz", "quizComplete" to "Quiz completed!", "quizSaved" to "Your result has been saved to your learning history.", "seeProgress" to "View progress", "questionProgress" to "Question $arg",
        "displayName" to "Display name", "username" to "Username", "save" to "Save profile", "language" to "Language", "theme" to "Theme",
        "font" to "Font", "minutes" to "minutes", "DARI_AF" to "Afghan Dari", "PASHTO_AF" to "Afghan Pashto", "ARABIC" to "Arabic", "ENGLISH_US" to "English",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive"
    )
    val fa = mapOf(
        "home" to "خانه", "learn" to "آموزش", "games" to "بازی‌ها", "progress" to "پیشرفت", "profile" to "پروفایل",
        "greeting" to "سلام $arg جان!", "welcome" to "هر روز یک قدم کوچک، یک موفقیت بزرگ.",
        "journey" to "مسیر یادگیری تو", "journeySub" to "هر واژه تازه تو را به هدفت نزدیک‌تر می‌کند.",
        "level" to "سطح", "coins" to "سکه", "dailyGoal" to "هدف روزانه مطالعه", "goalMins" to "$arg دقیقه",
        "goalHint" to "برای ثبت زمان واقعی، یک درس را آغاز کن.", "lessons" to "درس‌های تکمیل‌شده", "quizzes" to "آزمون‌های انجام‌شده",
        "continue" to "برای قدم بعدی آماده‌ای؟", "chooseLesson" to "یک سطح را انتخاب کن و یادگیری را ادامه بده.", "openLessons" to "رفتن به درس‌ها",
        "myProfile" to "پروفایل من", "learningPath" to "مسیر آموزش انگلیسی", "chooseLevel" to "سطح فعلی خود را انتخاب کن",
        "bandSub" to "درس‌های مرحله‌بندی‌شده $arg", "viewLessons" to "دیدن درس‌ها", "allLevels" to "همه سطح‌ها",
        "lessonExplain" to "موضوع را یاد بگیر، واژه‌های تازه را تمرین کن و دانسته‌هایت را بسنج.", "lessonDetails" to "بازکردن درس", "hideDetails" to "بستن جزئیات", "newWords" to "واژگان تازه",
        "completeLesson" to "تکمیل درس · دریافت امتیاز", "quizReview" to "رفتن به آزمون‌ها", "wordChallenge" to "چالش معنی واژه",
        "chooseMeaning" to "معنی دری کلمه APPLE را انتخاب کن", "correct" to "آفرین! پاسخ درست است.", "wrong" to "این پاسخ درست نیست؛ دوباره تلاش کن.",
        "hintText" to "راهنما: این یک میوه است.", "hintPaid" to "راهنما فعال شد؛ 6 سکه مصرف شد.", "needCoins" to "حداقل 6 سکه نیاز داری.",
        "hintButton" to "دریافت راهنما · 6 سکه", "coinBalance" to "سکه‌های تو: $arg", "newRound" to "دور تازه",
        "gameRoadmap" to "بازی‌های واژگان، املا و حافظه در مرحله بعدی محتوا اضافه می‌شوند.",
        "summary" to "خلاصه یادگیری تو", "average" to "میانگین نمره آزمون", "quizHistory" to "تاریخچه آزمون", "noHistory" to "هنوز آزمونی انجام نشده است.", "quizTitle" to "آزمون ترکیبی کوتاه", "quizComplete" to "آزمون تکمیل شد!", "quizSaved" to "نتیجه در تاریخچه یادگیری ذخیره شد.", "seeProgress" to "دیدن پیشرفت", "questionProgress" to "پرسش $arg",
        "displayName" to "نام نمایشی", "username" to "نام کاربری", "save" to "ذخیره پروفایل", "language" to "زبان", "theme" to "تم",
        "font" to "فونت", "minutes" to "دقیقه", "DARI_AF" to "دری افغانستان", "PASHTO_AF" to "پشتو افغانستان", "ARABIC" to "عربی", "ENGLISH_US" to "انگلیسی",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "دست‌نویس"
    )
    val ps = mapOf("home" to "کور", "learn" to "زده کړه", "games" to "لوبې", "progress" to "پرمختګ", "profile" to "پروفایل",
        "greeting" to "سلام، $arg!", "welcome" to "هره ورځ لږ پرمختګ، لویه لاسته راوړنه.", "journey" to "ستاسو د زده کړې سفر",
        "level" to "کچه", "coins" to "سکې", "dailyGoal" to "د مطالعې ورځنی هدف", "lessons" to "بشپړ شوي درسونه", "quizzes" to "ازموینې",
        "learningPath" to "د انګلیسي زده کړې لاره", "chooseLevel" to "خپله کچه وټاکئ", "viewLessons" to "درسونه وګورئ", "allLevels" to "ټولې کچې",
        "correct" to "ډېر ښه! ځواب سم دی.", "quizTitle" to "لنډه ګډه ازموینه", "quizComplete" to "ازموینه بشپړه شوه!", "quizSaved" to "ستاسو پایله د زده کړې په تاریخ کې خوندي شوه.", "seeProgress" to "پرمختګ وګورئ", "wrong" to "بیا هڅه وکړئ.", "hintButton" to "لارښوونه · 6 سکې", "hintPaid" to "لارښوونه فعاله شوه؛ 6 سکې ولګېدې.",
        "needCoins" to "لږ تر لږه 6 سکې پکار دي.", "displayName" to "ښکاره نوم", "username" to "کارن نوم", "save" to "پروفایل خوندي کړئ",
        "language" to "ژبه", "theme" to "بڼه", "font" to "لیکبڼه", "minutes" to "دقیقې",
        "DARI_AF" to "افغان دري", "PASHTO_AF" to "افغان پښتو", "ARABIC" to "عربي", "ENGLISH_US" to "انګلیسي",
        "SANS_SERIF" to "سن‌سریف", "SERIF" to "سریف", "CURSIVE" to "لاس لیک")
    val ar = mapOf("home" to "الرئيسية", "learn" to "التعلم", "games" to "الألعاب", "progress" to "التقدم", "profile" to "الملف الشخصي",
        "greeting" to "مرحباً، $arg!", "welcome" to "خطوة صغيرة كل يوم تصنع إنجازاً كبيراً.", "journey" to "رحلة التعلم",
        "level" to "المستوى", "coins" to "العملات", "dailyGoal" to "هدف الدراسة اليومي", "lessons" to "الدروس المكتملة", "quizzes" to "الاختبارات",
        "learningPath" to "مسار تعلم الإنجليزية", "chooseLevel" to "اختر مستواك", "viewLessons" to "عرض الدروس", "allLevels" to "كل المستويات",
        "correct" to "ممتاز! إجابة صحيحة.", "quizTitle" to "اختبار قصير متنوع", "quizComplete" to "اكتمل الاختبار!", "quizSaved" to "تم حفظ النتيجة في سجل التعلم.", "seeProgress" to "عرض التقدم", "wrong" to "حاول مرة أخرى.", "hintButton" to "تلميح · 6 عملات", "hintPaid" to "تم تفعيل التلميح؛ خُصمت 6 عملات.",
        "needCoins" to "تحتاج إلى 6 عملات على الأقل.", "displayName" to "الاسم الظاهر", "username" to "اسم المستخدم", "save" to "حفظ الملف",
        "language" to "اللغة", "theme" to "السمة", "font" to "الخط", "minutes" to "دقيقة",
        "DARI_AF" to "الداري الأفغانية", "PASHTO_AF" to "البشتوية الأفغانية", "ARABIC" to "العربية", "ENGLISH_US" to "الإنجليزية",
        "SANS_SERIF" to "Sans Serif", "SERIF" to "Serif", "CURSIVE" to "Cursive")
    val selected = when(locale) { AcademyLocale.ENGLISH_US -> en; AcademyLocale.DARI_AF -> fa; AcademyLocale.PASHTO_AF -> ps; AcademyLocale.ARABIC -> ar }
    return selected[key] ?: en[key] ?: key
}
