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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressManager
import com.example.model.ClassroomType
import com.example.model.LessonItem
import com.example.model.Level
import com.example.model.TierGroup
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.components.SpeakingEvaluationDialog
import com.example.ui.theme.LocalZahraTheme
import com.example.util.TextToSpeechHelper

@Composable
fun LearningLevelsScreen(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    initialClassroomFilter: ClassroomType? = null,
    onOpenExam: (Int) -> Unit,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val levels by userProgressManager.levelsState.collectAsState()

    var selectedLevelForDetail by remember { mutableStateOf<Level?>(null) }
    var selectedTierFilter by remember { mutableStateOf<TierGroup?>(null) }
    var selectedClassroomFilter by remember { mutableStateOf<ClassroomType?>(initialClassroomFilter) }
    var speakingItem by remember { mutableStateOf<LessonItem?>(null) }

    if (speakingItem != null) {
        SpeakingEvaluationDialog(
            targetSentenceEn = speakingItem!!.englishText,
            targetSentenceFa = speakingItem!!.persianTranslation,
            ttsHelper = ttsHelper,
            onDismiss = { speakingItem = null },
            onScoreResult = { score ->
                userProgressManager.addGoldCoins(5)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
    ) {
        // Top Header
        PuffyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            cornerRadius = 24.dp,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (selectedLevelForDetail != null) {
                        selectedLevelForDetail = null
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = theme.buttonBevel
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = selectedLevelForDetail?.titleFa ?: "نقشه جامع سطوح A1 تا Z24 زهرا",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.buttonBevel
                    )
                    Text(
                        text = "آموزش‌های گسترده و پیوسته همراه با آزمون پایان سطح 🌸",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Tier Filter Bar (A1-Z24)
        if (selectedLevelForDetail == null) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedTierFilter == null) theme.primaryColor else Color.White)
                            .border(1.dp, if (selectedTierFilter == null) theme.buttonBevel else Color(0xFFFFD1DC), RoundedCornerShape(16.dp))
                            .clickable { selectedTierFilter = null }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "همه ۴۵ سطح (A1 تا Z24)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTierFilter == null) Color.White else theme.buttonBevel
                        )
                    }
                }
                items(TierGroup.values()) { tier ->
                    val isSelected = selectedTierFilter == tier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) theme.primaryColor else Color.White)
                            .border(1.dp, if (isSelected) theme.buttonBevel else Color(0xFFFFD1DC), RoundedCornerShape(16.dp))
                            .clickable { selectedTierFilter = tier }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "${tier.emoji} ${tier.titleFa}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else theme.buttonBevel
                        )
                    }
                }
            }
        }

        if (selectedLevelForDetail == null) {
            val filteredLevels = if (selectedTierFilter != null) {
                levels.filter { it.tierGroup == selectedTierFilter }
            } else {
                levels
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(filteredLevels, key = { it.id }) { level ->
                    LevelCardItem(
                        level = level,
                        onLevelClick = {
                            if (level.isUnlocked) {
                                selectedLevelForDetail = level
                            }
                        },
                        onStartExam = {
                            if (level.isUnlocked) {
                                onOpenExam(level.id)
                            }
                        }
                    )
                }
            }
        } else {
            val currentLevel = selectedLevelForDetail!!
            val filteredItems = if (selectedClassroomFilter != null) {
                currentLevel.items.filter { it.classroomType == selectedClassroomFilter }
            } else {
                currentLevel.items
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                // Exam Callout Card
                PuffyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = theme.backgroundColor,
                    borderColor = theme.primaryColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "آزمون سنجش سطح ${currentLevel.levelCode}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.buttonBevel
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PuffyBadge(text = if (currentLevel.isMastered) "تسلط کامل ✓" else "آزمون مرحله‌ای", emoji = "📝")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLevel.bestScore > 0) "بهترین نمره زهرا: ${currentLevel.bestScore}% ⭐" else "برای باز کردن سطح بعدی، در امتحان شرکت کنید",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }

                        PuffyButton(
                            onClick = { onOpenExam(currentLevel.id) },
                            text = "شروع آزمون",
                            emoji = "🎯",
                            height = 46.dp,
                            modifier = Modifier.width(135.dp),
                            testTag = "start_level_exam_button"
                        )
                    }
                }

                Text(
                    text = "آموزش‌های این سطح (${filteredItems.size} مبحث):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.buttonBevel,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        LessonVocabularyItem(
                            item = item,
                            onPlayTts = { ttsHelper?.speak(item.englishText) },
                            onPracticeSpeaking = { speakingItem = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCardItem(
    level: Level,
    onLevelClick: () -> Unit,
    onStartExam: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme

    PuffyCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("level_card_${level.id}"),
        backgroundColor = if (level.isUnlocked) Color.White else Color(0xFFFBFBFB),
        borderColor = if (level.isUnlocked) theme.primaryColor.copy(alpha = 0.45f) else Color.LightGray
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = level.isUnlocked, onClick = onLevelClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Code Box (e.g. A1, B2, Z9)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (level.isUnlocked) theme.backgroundColor else Color(0xFFEEEEEE)
                        )
                        .border(
                            1.5.dp,
                            if (level.isUnlocked) theme.primaryColor else Color.Gray,
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (level.isUnlocked) {
                        Text(text = level.iconEmoji, fontSize = 26.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "قفل",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = level.titleFa,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (level.isUnlocked) theme.buttonBevel else Color.Gray
                        )
                        if (level.bestScore > 0) {
                            PuffyBadge(text = "${level.bestScore}%", emoji = "⭐")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${level.titleEn} • ${level.descriptionFa}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { starIndex ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIndex < level.starsEarned) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${level.items.size} مبحث آموزشی",
                            fontSize = 11.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (level.isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PuffyButton(
                        onClick = onLevelClick,
                        text = "مشاهده آموزش",
                        emoji = "📖",
                        height = 42.dp,
                        fontSize = 12,
                        modifier = Modifier.weight(1f),
                        backgroundColor = theme.secondaryColor
                    )
                    PuffyButton(
                        onClick = onStartExam,
                        text = "آزمون سطح",
                        emoji = "📝",
                        height = 42.dp,
                        fontSize = 12,
                        modifier = Modifier.weight(1f),
                        backgroundColor = theme.primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun LessonVocabularyItem(
    item: LessonItem,
    onPlayTts: () -> Unit,
    onPracticeSpeaking: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme

    PuffyCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.englishText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.buttonBevel
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.phonetic,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = item.persianTranslation,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryColor
                    )
                }

                PuffyIconButton(
                    onClick = onPlayTts,
                    icon = Icons.Default.VolumeUp,
                    size = 38.dp,
                    backgroundColor = theme.secondaryColor
                )

                Spacer(modifier = Modifier.width(6.dp))

                PuffyIconButton(
                    onClick = onPracticeSpeaking,
                    icon = Icons.Default.Mic,
                    size = 38.dp,
                    backgroundColor = theme.primaryColor
                )
            }

            if (item.grammarRuleFa != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "💡 نکته آموزشی: ${item.grammarRuleFa}",
                        fontSize = 11.sp,
                        color = theme.buttonBevel
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFAFAFA))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "💬 \"${item.exampleSentenceEn}\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = item.exampleSentenceFa,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
