package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressManager
import com.example.model.ClassroomType
import com.example.model.Level
import com.example.ui.screens.DiaryCalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningLevelsScreen
import com.example.ui.screens.LevelExamScreen
import com.example.ui.screens.PuffyProfileScreen
import com.example.ui.screens.SettingsAndThemeScreen
import com.example.ui.screens.SpeakingPracticeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WordGameScreen
import com.example.ui.theme.LocalZahraTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZahraThemeState
import com.example.util.TextToSpeechHelper

enum class AppScreen { SPLASH, HOME, LEVELS, LEVEL_EXAM, SPEAKING, GAME, CALENDAR, SETTINGS, PROFILE }

class MainActivity : ComponentActivity() {
    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ttsHelper = TextToSpeechHelper(this)
        val progress = UserProgressManager(this)
        setContent {
            val theme by progress.currentTheme.collectAsState()
            val font by progress.fontStyle.collectAsState()
            val avatar by progress.learnerAvatar.collectAsState()
            val name by progress.learnerName.collectAsState()
            val goal by progress.dailyGoalMinutes.collectAsState()
            val academyTheme = ZahraThemeState(
                currentTheme = theme, fontStyle = font, learnerName = name,
                learnerAvatar = avatar, dailyGoalMinutes = goal
            )
            CompositionLocalProvider(LocalZahraTheme provides academyTheme) {
                MyApplicationTheme(dynamicColor = false) {
                    MainAppContent(progress, ttsHelper)
                }
            }
        }
    }

    override fun onDestroy() {
        ttsHelper?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun MainAppContent(userProgressManager: UserProgressManager, ttsHelper: TextToSpeechHelper?) {
    var screen by remember { mutableStateOf(AppScreen.SPLASH) }
    var examLevel by remember { mutableStateOf<Level?>(null) }
    var classroom by remember { mutableStateOf<ClassroomType?>(null) }
    val levels by userProgressManager.levelsState.collectAsState()

    if (screen != AppScreen.HOME && screen != AppScreen.SPLASH) {
        BackHandler {
            screen = if (screen == AppScreen.LEVEL_EXAM) AppScreen.LEVELS else AppScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (screen != AppScreen.SPLASH && screen != AppScreen.LEVEL_EXAM) {
                PuffyBottomNavBar(screen) { screen = it }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "academy_screen_transition"
            ) { target ->
                when (target) {
                    AppScreen.SPLASH -> SplashScreen { screen = AppScreen.HOME }
                    AppScreen.HOME -> HomeScreen(
                        userProgressManager = userProgressManager, ttsHelper = ttsHelper,
                        onNavigateToLevels = { classroom = null; screen = AppScreen.LEVELS },
                        onNavigateToSpeakingDrill = { screen = AppScreen.SPEAKING },
                        onNavigateToWordGame = { screen = AppScreen.GAME },
                        onNavigateToCalendar = { screen = AppScreen.CALENDAR },
                        onNavigateToSettings = { screen = AppScreen.SETTINGS },
                        onNavigateToProfile = { screen = AppScreen.PROFILE },
                        onSelectClassroom = { classroom = it; screen = AppScreen.LEVELS }
                    )
                    AppScreen.PROFILE -> PuffyProfileScreen(userProgressManager, { screen = AppScreen.SETTINGS }) { screen = AppScreen.HOME }
                    AppScreen.LEVELS -> LearningLevelsScreen(
                        userProgressManager, ttsHelper, classroom,
                        onOpenExam = { id -> examLevel = levels.firstOrNull { it.id == id }; screen = AppScreen.LEVEL_EXAM },
                        onBack = { screen = AppScreen.HOME }
                    )
                    AppScreen.LEVEL_EXAM -> LevelExamScreen(
                        level = examLevel ?: levels.first(), userProgressManager = userProgressManager,
                        ttsHelper = ttsHelper, onExamFinished = { screen = AppScreen.LEVELS },
                        onBack = { screen = AppScreen.LEVELS }
                    )
                    AppScreen.SPEAKING -> SpeakingPracticeScreen(userProgressManager, ttsHelper) { screen = AppScreen.HOME }
                    AppScreen.GAME -> WordGameScreen(userProgressManager, ttsHelper) { screen = AppScreen.HOME }
                    AppScreen.CALENDAR -> DiaryCalendarScreen(userProgressManager) { screen = AppScreen.HOME }
                    AppScreen.SETTINGS -> SettingsAndThemeScreen(userProgressManager) { screen = AppScreen.HOME }
                }
            }
        }
    }
}

@Composable
fun PuffyBottomNavBar(currentScreen: AppScreen, onTabSelected: (AppScreen) -> Unit) {
    val theme = LocalZahraTheme.current.currentTheme
    Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = theme.primaryColor.copy(alpha = .22f))
            .clip(RoundedCornerShape(26.dp)).background(Color.White)
            .border(1.5.dp, theme.primaryColor.copy(alpha = .28f), RoundedCornerShape(26.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            NavBarItem("خانه", Icons.Default.Home, currentScreen == AppScreen.HOME, { onTabSelected(AppScreen.HOME) }, "nav_home")
            NavBarItem("آموزش", Icons.Default.School, currentScreen == AppScreen.LEVELS, { onTabSelected(AppScreen.LEVELS) }, "nav_learning")
            NavBarItem("بازی‌ها", Icons.Default.Gamepad, currentScreen == AppScreen.GAME, { onTabSelected(AppScreen.GAME) }, "nav_games")
            NavBarItem("تقویم", Icons.Default.CalendarMonth, currentScreen == AppScreen.CALENDAR, { onTabSelected(AppScreen.CALENDAR) }, "nav_calendar")
            NavBarItem("پروفایل", Icons.Default.AccountCircle, currentScreen == AppScreen.PROFILE, { onTabSelected(AppScreen.PROFILE) }, "nav_profile")
        }
    }
}

@Composable
fun NavBarItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, testTag: String) {
    val theme = LocalZahraTheme.current.currentTheme
    Box(
        modifier = Modifier.testTag(testTag).clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) theme.primaryColor else Color.Transparent)
            .clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(19.dp))
            if (isSelected) Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
