package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressManager
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.theme.LocalZahraTheme

@Composable
fun PuffyProfileScreen(
    userProgressManager: UserProgressManager,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val goldCoins by userProgressManager.goldCoins.collectAsState()
    val stars by userProgressManager.totalStars.collectAsState()
    val streak by userProgressManager.streakDays.collectAsState()
    val totalHours by userProgressManager.totalStudyHours.collectAsState()
    val todayMinutes by userProgressManager.todayStudyMinutes.collectAsState()
    val medals by userProgressManager.medals.collectAsState()
    val levels by userProgressManager.levelsState.collectAsState()
    val totalScore by userProgressManager.totalScore.collectAsState()
    val learnerName by userProgressManager.learnerName.collectAsState()
    val learnerAvatar by userProgressManager.learnerAvatar.collectAsState()
    val learnerGender by userProgressManager.learnerGender.collectAsState()
    val learnerBio by userProgressManager.learnerBio.collectAsState()
    val avatarChoices = if (learnerGender == "male") listOf("🧑🏻‍🎓", "👨🏻‍🎓", "🧑🏻‍🚀", "🦸🏻‍♂️", "🥷🏻", "🧑🏻‍💻", "⚽", "🏆") else listOf("👸🏻", "👩🏻‍🎓", "🧚🏻‍♀️", "🦸🏻‍♀️", "👩🏻‍🚀", "👩🏻‍💻", "🌸", "👑")

    var showEditNameDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var editedNameText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(learnerName) }
    var editedBioText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(learnerBio) }

    val completedLevelsCount = levels.count { it.isMastered }
    val currentActiveLevel = levels.firstOrNull { it.isUnlocked && !it.isMastered } ?: levels.firstOrNull()
    val (rankTitle, rankDesc) = userProgressManager.getLearnerRank(totalScore)

    if (showEditNameDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showEditNameDialog = false }) {
            PuffyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 24.dp,
                elevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ویرایش پروفایل ✨",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.buttonBevel
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "نام و معرفی کوتاه پروفایل خود را ویرایش کنید:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = editedNameText,
                        onValueChange = { editedNameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("نام نمایشی") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = editedBioText,
                        onValueChange = { editedBioText = it.take(120) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        placeholder = { Text("معرفی کوتاه من...") }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PuffyButton(
                            onClick = {
                                userProgressManager.updateLearnerName(editedNameText)
                                userProgressManager.updateLearnerBio(editedBioText)
                                showEditNameDialog = false
                            },
                            text = "ذخیره نام ✨",
                            modifier = Modifier.weight(1f)
                        )
                        PuffyButton(
                            onClick = { showEditNameDialog = false },
                            text = "انصراف",
                            backgroundColor = Color(0xFFEEEEEE),
                            bevelColor = Color(0xFFCCCCCC),
                            textColor = Color.DarkGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = theme.buttonBevel
                )
            }
            PuffyBadge(text = "پروفایل ${if (learnerGender == "male") "پسرانه" else "دخترانه"}", emoji = if (learnerGender == "male") "🛡️" else "👑")
            PuffyIconButton(
                onClick = onNavigateToSettings,
                icon = Icons.Default.Palette,
                size = 40.dp,
                backgroundColor = theme.primaryColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Main Puffy Avatar Card
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF171923),
                    cornerRadius = 30.dp,
                    elevation = 12.dp,
                    borderColor = theme.accentGold.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF24283A), Color(0xFF171923), Color(0xFF30243A))
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BH EXCLUSIVE ACADEMY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = theme.accentGold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "پروفایل اختصاصی من",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Premium avatar portrait with a theme-aware glowing ring
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .shadow(16.dp, CircleShape, ambientColor = theme.accentGold.copy(alpha = 0.35f))
                                .background(
                                    Brush.linearGradient(listOf(theme.primaryColor, theme.secondaryColor, theme.accentGold)),
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .border(
                                    3.5.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White,
                                            theme.primaryColor,
                                            theme.secondaryColor
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = learnerAvatar, fontSize = 66.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("نوع پروفایل را انتخاب کن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8EF))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("female" to "دخترانه 👸🏻", "male" to "پسرانه 🧑🏻‍🎓").forEach { (gender, label) ->
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if (learnerGender == gender) theme.primaryColor else Color(0xFF292D3B)).border(1.5.dp, if (learnerGender == gender) theme.accentGold else Color(0xFF45495A), RoundedCornerShape(16.dp)).clickable { userProgressManager.setLearnerGender(gender) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("آواتار دلخواهت را انتخاب کن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8EF))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(avatarChoices) { avatar ->
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (learnerAvatar == avatar) theme.primaryColor else Color(0xFF292D3B)).border(2.dp, if (learnerAvatar == avatar) theme.accentGold else Color(0xFF45495A), CircleShape).clickable { userProgressManager.setAvatar(avatar) }, contentAlignment = Alignment.Center) {
                                    Text(avatar, fontSize = 25.sp)
                                }
                            }
                        }
                        Text(learnerBio, fontSize = 12.sp, color = Color(0xFFD6D8E2), textAlign = TextAlign.Center, lineHeight = 18.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 10.dp))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Editable Learner Name Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = learnerName.ifBlank { "کاربر جدید" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PuffyIconButton(
                                onClick = {
                                    editedNameText = learnerName
                                    editedBioText = learnerBio
                                    showEditNameDialog = true
                                },
                                icon = Icons.Default.Edit,
                                size = 32.dp,
                                backgroundColor = theme.backgroundColor,
                                tint = theme.primaryColor,
                                testTag = "edit_name_button"
                            )
                        }

                        Text(
                            text = rankTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentGold,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Level Badge & Stars
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item {
                                PuffyBadge(
                                    text = "سطح فعلی: ${currentActiveLevel?.levelCode ?: "A1"}",
                                    emoji = "🏆",
                                    backgroundColor = Color(0xFF292D3B),
                                    textColor = Color.White
                                )
                            }
                            item {
                                PuffyBadge(
                                    text = "$completedLevelsCount سطح مسلط شده",
                                    emoji = "✨",
                                    backgroundColor = Color(0xFF3A3020),
                                    textColor = theme.accentGold
                                )
                            }
                        }
                    }
                }
            }

            // Active Scoring & Ranking Card
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    elevation = 6.dp,
                    borderColor = Color(0xFFFFD54F)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⭐️", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "سیستم امتیازدهی و رتبه فعال",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.buttonBevel
                                    )
                                    Text(
                                        text = rankDesc,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            PuffyBadge(
                                text = "$totalScore XP",
                                backgroundColor = Color(0xFFF3E5F5),
                                textColor = Color(0xFF8E24AA)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ways to earn XP explanation
                        Text(
                            text = "روش‌های کسب امتیاز در آکادمی:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    "آزمون: ۱۰۰+ XP" to Color(0xFF2E7D32),
                                    "گویش: ۴۰+ XP" to Color(0xFF0277BD),
                                    "بازی: ۳۰+ XP" to Color(0xFFEF6C00),
                                    "هوش مصنوعی: ۱۰+ XP" to Color(0xFFC2185B)
                                )
                            ) { reward ->
                                PuffyBadge(
                                    text = reward.first,
                                    backgroundColor = reward.second.copy(alpha = 0.12f),
                                    textColor = reward.second
                                )
                            }
                        }
                    }
                }
            }

            // Puffy Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            title = "استمرار مطالعه",
                            value = "$streak روز متوالی",
                            emoji = "🔥",
                            color = Color(0xFFFF9E00),
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            title = "ستاره‌های پفکی",
                            value = "$stars ⭐",
                            emoji = "✨",
                            color = theme.primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            title = "سکه و پاداش",
                            value = "$goldCoins 🥇",
                            emoji = "👑",
                            color = Color(0xFFF57F17),
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            title = "کل زمان مطالعه",
                            value = "$totalHours ساعت • امروز $todayMinutes دقیقه",
                            emoji = "⏱️",
                            color = Color(0xFF7B2CBF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Puffy Medals Shelf
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
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
                                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = theme.primaryColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ویترین دستاوردهای تو:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.buttonBevel
                                )
                            }
                            PuffyBadge(text = "${medals.count { it.isUnlocked }} از ${medals.size}", emoji = "🎖️")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(medals) { medal ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (medal.isUnlocked) theme.backgroundColor else Color(0xFFF5F5F5))
                                        .border(
                                            1.5.dp,
                                            if (medal.isUnlocked) theme.primaryColor else Color.LightGray,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = medal.iconEmoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = medal.titleFa,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (medal.isUnlocked) theme.buttonBevel else Color.Gray
                                            )
                                            Text(
                                                text = if (medal.isUnlocked) "آنلاک شده ✓" else "${medal.requiredStars} ستاره نیاز است",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Slogan & Royal Commitment
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    backgroundColor = theme.backgroundColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "👑", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "عهدنامه خودسازی BH",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.buttonBevel
                        )
                        Text(
                            text = "«از صفر شروع کن، تا تسلط کامل ادامه بده!»",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.primaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "من متعهد می‌شوم هر روز حتی با قدم‌های کوچک به سمت تسلط بر سطوح A1 تا Z9 حرکت کنم.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    PuffyCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
