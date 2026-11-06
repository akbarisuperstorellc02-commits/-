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
import androidx.compose.material.icons.filled.ChatBubble
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
import com.example.ui.screens.MasterGohariChatScreen
import com.example.ui.screens.PuffyProfileScreen
import com.example.ui.screens.SettingsAndThemeScreen
import com.example.ui.screens.SpeakingPracticeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WordGameScreen
import com.example.ui.theme.LocalZahraTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZahraThemeState
import com.example.util.TextToSpeechHelper

enum class AppScreen {
    SPLASH,
    HOME,
    LEVELS,
    LEVEL_EXAM,
    CHAT,
    SPEAKING,
    GAME,
    CALENDAR,
    SETTINGS,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)

        enableEdgeToEdge()
        ttsHelper = TextToSpeechHelper(this)

        val userProgressManager = UserProgressManager(this)

        setContent {
            val currentTheme by userProgressManager.currentTheme.collectAsState()
            val currentFontStyle by userProgressManager.fontStyle.collectAsState()
            val currentAvatar by userProgressManager.learnerAvatar.collectAsState()
            val currentLearnerName by userProgressManager.learnerName.collectAsState()
            val dailyGoal by userProgressManager.dailyGoalMinutes.collectAsState()

            val zahraState = ZahraThemeState(
                currentTheme = currentTheme,
                fontStyle = currentFontStyle,
                learnerName = currentLearnerName,
                learnerAvatar = currentAvatar,
                dailyGoalMinutes = dailyGoal
            )

            CompositionLocalProvider(LocalZahraTheme provides zahraState) {
                MyApplicationTheme(dynamicColor = false) {
                    MainAppContent(
                        userProgressManager = userProgressManager,
                        ttsHelper = ttsHelper
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
    }
}

@Composable
fun MainAppContent(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var activeExamLevel by remember { mutableStateOf<Level?>(null) }
    var activeClassroomFilter by remember { mutableStateOf<ClassroomType?>(null) }
    val levels by userProgressManager.levelsState.collectAsState()

    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH) {
        BackHandler {
            if (currentScreen == AppScreen.LEVEL_EXAM) {
                currentScreen = AppScreen.LEVELS
            } else {
                currentScreen = AppScreen.HOME
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.LEVEL_EXAM && currentScreen != AppScreen.CHAT) {
                PuffyBottomNavBar(
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onEnterApp = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.HOME -> {
                        HomeScreen(
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            onNavigateToLevels = {
                                activeClassroomFilter = null
                                currentScreen = AppScreen.LEVELS
                            },
                            onNavigateToChat = { currentScreen = AppScreen.CHAT },
                            onNavigateToSpeakingDrill = { currentScreen = AppScreen.SPEAKING },
                            onNavigateToWordGame = { currentScreen = AppScreen.GAME },
                            onNavigateToCalendar = { currentScreen = AppScreen.CALENDAR },
                            onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                            onNavigateToProfile = { currentScreen = AppScreen.PROFILE },
                            onSelectClassroom = { classroom ->
                                activeClassroomFilter = classroom
                                currentScreen = AppScreen.LEVELS
                            }
                        )
                    }

                    AppScreen.PROFILE -> {
                        PuffyProfileScreen(
                            userProgressManager = userProgressManager,
                            onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.LEVELS -> {
                        LearningLevelsScreen(
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            initialClassroomFilter = activeClassroomFilter,
                            onOpenExam = { levelId ->
                                activeExamLevel = levels.firstOrNull { it.id == levelId }
                                currentScreen = AppScreen.LEVEL_EXAM
                            },
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.LEVEL_EXAM -> {
                        val level = activeExamLevel ?: levels.first()
                        LevelExamScreen(
                            level = level,
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            onExamFinished = { currentScreen = AppScreen.LEVELS },
                            onBack = { currentScreen = AppScreen.LEVELS }
                        )
                    }

                    AppScreen.CHAT -> {
                        MasterGohariChatScreen(
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.SPEAKING -> {
                        SpeakingPracticeScreen(
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.GAME -> {
                        WordGameScreen(
                            userProgressManager = userProgressManager,
                            ttsHelper = ttsHelper,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.CALENDAR -> {
                        DiaryCalendarScreen(
                            userProgressManager = userProgressManager,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsAndThemeScreen(
                            userProgressManager = userProgressManager,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PuffyBottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = theme.primaryColor.copy(alpha = 0.22f),
                spotColor = theme.buttonBevel.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(1.5.dp, theme.primaryColor.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "خانه",
                icon = Icons.Default.Home,
                isSelected = currentScreen == AppScreen.HOME,
                onClick = { onTabSelected(AppScreen.HOME) },
                testTag = "nav_home"
            )
            NavBarItem(
                label = "سطوح A1-Z9",
                icon = Icons.Default.School,
                isSelected = currentScreen == AppScreen.LEVELS,
                onClick = { onTabSelected(AppScreen.LEVELS) },
                testTag = "nav_levels"
            )
            NavBarItem(
                label = "معلم هوشمند",
                icon = Icons.Default.ChatBubble,
                isSelected = currentScreen == AppScreen.CHAT,
                onClick = { onTabSelected(AppScreen.CHAT) },
                testTag = "nav_chat"
            )
            NavBarItem(
                label = "بازی ۳۰ مرحله",
                icon = Icons.Default.Gamepad,
                isSelected = currentScreen == AppScreen.GAME,
                onClick = { onTabSelected(AppScreen.GAME) },
                testTag = "nav_game"
            )
            NavBarItem(
                label = "پروفایل",
                icon = Icons.Default.AccountCircle,
                isSelected = currentScreen == AppScreen.PROFILE,
                onClick = { onTabSelected(AppScreen.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = LocalZahraTheme.current.currentTheme

    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) theme.primaryColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(19.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
