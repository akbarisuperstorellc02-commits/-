package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.example.util.TextToSpeechHelper

@Composable
fun WordGameScreen(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val stages by userProgressManager.wordStages.collectAsState()
    val goldCoins by userProgressManager.goldCoins.collectAsState()
    val stars by userProgressManager.totalStars.collectAsState()

    var activeStageIndex by remember { mutableIntStateOf(0) }
    val currentStage = stages.getOrNull(activeStageIndex) ?: stages.first()

    val availableLetters = remember(currentStage.stageNumber) {
        mutableStateListOf<Pair<Int, Char>>().apply {
            currentStage.jumbledLetters.forEachIndexed { i, c -> add(Pair(i, c)) }
        }
    }
    val selectedLetters = remember(currentStage.stageNumber) {
        mutableStateListOf<Pair<Int, Char>>()
    }

    var isStageSolved by remember(currentStage.stageNumber) {
        mutableStateOf(currentStage.isCompleted)
    }
    var hintCount by remember(currentStage.stageNumber) { mutableIntStateOf(0) }
    var hintMessage by remember(currentStage.stageNumber) { mutableStateOf("") }

    fun checkWord() {
        val formed = selectedLetters.map { it.second }.joinToString("")
        if (formed.equals(currentStage.targetWordEn, ignoreCase = true)) {
            isStageSolved = true
            ttsHelper?.speak(currentStage.targetWordEn)
            userProgressManager.completeWordStage(currentStage.stageNumber)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = theme.primaryColor
                )
            }
            PuffyBadge(text = "چالش واژگان آکادمی BH", emoji = "👑")
            Row(verticalAlignment = Alignment.CenterVertically) {
                PuffyBadge(text = "$goldCoins 🥇", backgroundColor = Color(0xFF241F10), textColor = theme.primaryColor)
                Spacer(modifier = Modifier.width(6.dp))
                PuffyBadge(text = "$stars ⭐", backgroundColor = Color(0xFF2E2612), textColor = Color(0xFFFFDF73))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stage selector bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stages) { stage ->
                val isCurrent = stage.stageNumber == currentStage.stageNumber
                val isDone = stage.isCompleted
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> theme.primaryColor
                                isDone -> Color(0xFF4CAF50)
                                else -> Color(0xFF1E1E26)
                            }
                        )
                        .border(
                            1.5.dp,
                            if (isCurrent) Color.White else Color.Transparent,
                            CircleShape
                        )
                        .clickable {
                            activeStageIndex = stage.stageNumber - 1
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDone) "✓" else "${stage.stageNumber}",
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent || isDone) Color.Black else Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Word Game Card
        PuffyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "مرحله ${currentStage.stageNumber} از ${stages.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentStage.emoji,
                    fontSize = 62.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentStage.hintFa,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Paid, progressive letter hint: one coin reveals one next letter.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PuffyButton(
                        onClick = {
                            if (hintCount >= currentStage.targetWordEn.length) {
                                hintMessage = "تمام حروف کلمه آشکار شده است."
                            } else if (userProgressManager.spendGoldCoin(6)) {
                                hintCount += 1
                                hintMessage = "حرف راهنما: ${currentStage.targetWordEn[hintCount - 1]}  •  ۶ سکه مصرف شد"
                            } else {
                                hintMessage = "برای دریافت راهنمایی، ۶ سکه لازم داری."
                            }
                        },
                        text = "راهنمایی  (۶ سکه)",
                        emoji = "💡",
                        height = 42.dp,
                        fontSize = 13,
                        backgroundColor = Color(0xFFB85C9E),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isStageSolved,
                        textColor = if (theme == com.example.ui.theme.ZahraThemeMode.OBSIDIAN_BLACK_GOLD) Color(0xFF17130A) else Color.White
                    )
                }
                if (hintCount > 0 || hintMessage.isNotBlank()) {
                    Text(
                        text = if (hintCount > 0) "راهنمای کلمه: " + currentStage.targetWordEn.take(hintCount) +
                            "•".repeat((currentStage.targetWordEn.length - hintCount).coerceAtLeast(0)) +
                            if (hintMessage.startsWith("برای") || hintMessage.startsWith("تمام")) "\n$hintMessage" else "" else hintMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slots for Selected Letters
                val targetLen = currentStage.targetWordEn.length
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(targetLen) { i ->
                        val item = selectedLetters.getOrNull(i)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (item != null) Color(0xFF262013) else Color(0xFF1B1B22))
                                .border(
                                    2.dp,
                                    if (item != null) theme.primaryColor else Color(0xFF33333E),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = item != null && !isStageSolved) {
                                    if (item != null) {
                                        selectedLetters.remove(item)
                                        availableLetters.add(item)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item?.second?.toString() ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                if (isStageSolved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1B2E1C))
                            .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎉 عالی حدس زدی! ${currentStage.targetWordEn}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PuffyIconButton(
                                    onClick = { ttsHelper?.speak(currentStage.targetWordEn) },
                                    icon = Icons.Default.VolumeUp,
                                    size = 36.dp,
                                    backgroundColor = theme.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "پاداش خودسازی: +۲۵ سکه طلا 🥇 و ۱ ستاره ⭐",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PuffyButton(
                        onClick = {
                            if (activeStageIndex < stages.size - 1) {
                                activeStageIndex++
                            }
                        },
                        text = if (activeStageIndex < stages.size - 1) "مرحله بعد" else "آفرین قهرمان! بازی تکمیل شد!",
                        emoji = "👑",
                        height = 52.dp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "next_stage_button"
                    )
                } else {
                    Text(
                        text = "حروف را به ترتیب لمس کنید تا کلمه ساخته شود:",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(availableLetters) { letterPair ->
                            PuffyButton(
                                onClick = {
                                    availableLetters.remove(letterPair)
                                    selectedLetters.add(letterPair)
                                    checkWord()
                                },
                                text = letterPair.second.toString(),
                                height = 50.dp,
                                modifier = Modifier.width(52.dp),
                                fontSize = 20,
                                backgroundColor = theme.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PuffyButton(
                            onClick = {
                                selectedLetters.clear()
                                availableLetters.clear()
                                currentStage.jumbledLetters.forEachIndexed { i, c ->
                                    availableLetters.add(Pair(i, c))
                                }
                            },
                            text = "چیدن دوباره حروف",
                            icon = Icons.Default.Refresh,
                            height = 42.dp,
                            fontSize = 12,
                            backgroundColor = Color(0xFF2B2B33),
                            textColor = Color.White,
                            bevelColor = Color(0xFF1E1E24),
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
        }
    }
}
