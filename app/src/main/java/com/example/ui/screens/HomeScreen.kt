package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserProgressManager
import com.example.model.ClassroomType
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.theme.LocalZahraTheme
import com.example.util.TextToSpeechHelper

@Composable
fun HomeScreen(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    onNavigateToLevels: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSpeakingDrill: () -> Unit,
    onNavigateToWordGame: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectClassroom: (ClassroomType) -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val goldCoins by userProgressManager.goldCoins.collectAsState()
    val stars by userProgressManager.totalStars.collectAsState()
    val streak by userProgressManager.streakDays.collectAsState()
    val todaySeconds by userProgressManager.todayStudySeconds.collectAsState()
    val dailyGoal by userProgressManager.dailyGoalMinutes.collectAsState()
    val levels by userProgressManager.levelsState.collectAsState()
    val totalScore by userProgressManager.totalScore.collectAsState()
    val learnerName by userProgressManager.learnerName.collectAsState()
    val learnerAvatar by userProgressManager.learnerAvatar.collectAsState()
    val recentReward by userProgressManager.recentReward.collectAsState()

    val unlockedCount = levels.count { it.isUnlocked }
    val totalLevels = levels.size
    val (rankTitle, _) = userProgressManager.getLearnerRank(totalScore)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Celebratory Reward Banner if points were just earned
        if (recentReward != null) {
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 8.dp,
                    borderColor = Color(0xFFFFD54F)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎉", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "+${recentReward!!.points} امتیاز کسب شد! ✨",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFC2185B)
                                )
                                Text(
                                    text = recentReward!!.reason,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        PuffyButton(
                            onClick = { userProgressManager.clearRecentReward() },
                            text = "عالیه 💖",
                            height = 36.dp,
                            fontSize = 11
                        )
                    }
                }
            }
        }

        // Top Puffy App Bar Header
        item {
            PuffyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onNavigateToProfile)
                    ) {
                        // 3D Puffy Profile Avatar Thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(Color.White, theme.primaryColor)),
                                    CircleShape
                                )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = learnerAvatar, fontSize = 30.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "خودسازی $learnerName",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.buttonBevel
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "👑", fontSize = 14.sp)
                            }
                            Text(
                                text = rankTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                        }
                    }

                    // Score XP, Gold Coins & Profile Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PuffyBadge(
                            text = "$totalScore XP ✨",
                            backgroundColor = Color(0xFFF3E5F5),
                            textColor = Color(0xFF8E24AA)
                        )
                        PuffyBadge(text = "$goldCoins 🥇", backgroundColor = Color(0xFFFFF9C4), textColor = Color(0xFFE65100))

                        PuffyIconButton(
                            onClick = onNavigateToProfile,
                            icon = Icons.Default.AccountCircle,
                            size = 38.dp,
                            backgroundColor = theme.primaryColor,
                            testTag = "open_profile_button"
                        )
                    }
                }
            }
        }

        // Hero Card: Zahra Puffy Profile & Roadmap from A1 to Z9
        item {
            PuffyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevation = 8.dp,
                borderColor = theme.primaryColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        PuffyBadge(
                            text = "مسیر طلایی A1 تا Z9",
                            emoji = "👑",
                            backgroundColor = theme.backgroundColor,
                            textColor = theme.primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "مسیر یادگیری $learnerName در آکادمی BH",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.buttonBevel
                        )
                        Text(
                            text = "آموزش‌های گسترده و پیوسته از الفبای A1 تا قله تسلط مطلق Z9 با معلم هوشمند صبور",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PuffyButton(
                                onClick = onNavigateToLevels,
                                text = "آموزش‌های $learnerName",
                                emoji = "🌸",
                                height = 44.dp,
                                fontSize = 12,
                                modifier = Modifier.weight(1f),
                                testTag = "open_levels_hero"
                            )
                            PuffyButton(
                                onClick = onNavigateToProfile,
                                text = "پروفایل من",
                                emoji = "👸",
                                height = 44.dp,
                                fontSize = 12,
                                modifier = Modifier.weight(1f),
                                backgroundColor = theme.secondaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Puffy Avatar Image in Hero
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, theme.primaryColor, CircleShape)
                            .clickable(onClick = onNavigateToProfile)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = learnerAvatar, fontSize = 54.sp)
                        }
                    }
                }
            }
        }

        // Daily Motivation & Study Goal
        item {
            PuffyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هدف روزانه مطالعه",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.buttonBevel
                            )
                        }
                        Text(
                            text = if (todaySeconds == 0) {
                                "0 از $dailyGoal دقیقه"
                            } else if (todaySeconds < 60) {
                                "$todaySeconds ثانیه از $dailyGoal دقیقه"
                            } else {
                                "${todaySeconds / 60} دقیقه و ${todaySeconds % 60} ثانیه از $dailyGoal دقیقه"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progressRatio = (todaySeconds.toFloat() / (dailyGoal * 60f)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = theme.primaryColor,
                        trackColor = theme.backgroundColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (todaySeconds >= dailyGoal * 60) {
                            "آفرین! هدف مطالعه امروزت تکمیل شد 🎉"
                        } else {
                            val remainingSeconds = (dailyGoal * 60 - todaySeconds).coerceAtLeast(0)
                            "برای تکمیل هدف امروز، ${remainingSeconds / 60} دقیقه و ${remainingSeconds % 60} ثانیه باقی مانده است."
                        },
                        fontSize = 12.sp,
                        color = theme.buttonBevel,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Primary Feature Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeFeatureCard(
                        title = "سطوح A1 تا Z9",
                        subtitle = "$unlockedCount از $totalLevels سطح فعال",
                        emoji = "👑",
                        badge = "۳۰ سطح جامع",
                        color = theme.primaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLevels
                    )

                    HomeFeatureCard(
                        title = "بازی لغات پفکی",
                        subtitle = "۶۰ مرحله ادامه‌دار با پاداش",
                        emoji = "🎮",
                        badge = "مرحله‌ای",
                        color = theme.accentGold,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWordGame
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeFeatureCard(
                        title = "معلم هوش مصنوعی",
                        subtitle = "صبور و مهربان از سطح صفر",
                        emoji = "🎙️",
                        badge = "اختصاصی",
                        color = theme.secondaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToChat
                    )

                    HomeFeatureCard(
                        title = "تقویم و خاطرات",
                        subtitle = "ثبت روزانه و برنامه مطالعه",
                        emoji = "📅",
                        badge = "رنگی و شاد",
                        color = Color(0xFF9D4EDD),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCalendar
                    )

                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    HomeFeatureCard(
                        title = "زنگ یادآور",
                        subtitle = "تنظیم زنگ روزانه",
                        emoji = "⏰",
                        badge = "یادآوری واقعی",
                        color = Color(0xFFE85D9E),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAlarms
                    )
                }
            }
        }

        // Specialized Classrooms
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کلاس‌های تخصصی آکادمی BH:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.buttonBevel
                    )
                    PuffyBadge(text = "۷ مهارت طلایی", emoji = "🌸")
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(ClassroomType.values()) { classroom ->
                        ClassroomPillCard(
                            classroom = classroom,
                            onClick = { onSelectClassroom(classroom) }
                        )
                    }
                }
            }
        }

        // Daily Golden Wisdom Quote
        item {
            PuffyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پیام انگیزشی اختصاصی $learnerName:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.buttonBevel
                            )
                        }

                        PuffyIconButton(
                            onClick = { ttsHelper?.speak("Start from zero, continue until absolute mastery!") },
                            icon = Icons.Default.VolumeUp,
                            size = 36.dp,
                            backgroundColor = theme.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"Start from zero, continue until absolute mastery!\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )

                    Text(
                        text = "«از صفر شروع کن، تا تسلط کامل ادامه بده!»",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassroomPillCard(
    classroom: ClassroomType,
    onClick: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme

    PuffyCard(
        modifier = Modifier
            .clickable(onClick = onClick),
        cornerRadius = 18.dp,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = classroom.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = classroom.titleFa,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.buttonBevel
                )
                Text(
                    text = classroom.titleEn,
                    fontSize = 10.sp,
                    color = theme.primaryColor
                )
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    title: String,
    subtitle: String,
    emoji: String,
    badge: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    PuffyCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 20.dp,
        elevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 28.sp)
                PuffyBadge(text = badge, backgroundColor = color.copy(alpha = 0.15f), textColor = color)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.buttonBevel
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2,
                lineHeight = 15.sp
            )
        }
    }
}
