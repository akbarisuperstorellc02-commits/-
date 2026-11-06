package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Level
import com.example.model.QuestionType
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.components.SpeakingEvaluationDialog
import com.example.ui.theme.LocalZahraTheme
import com.example.util.TextToSpeechHelper

@Composable
fun LevelExamScreen(
    level: Level,
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    onExamFinished: () -> Unit,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val learnerName = LocalZahraTheme.current.learnerName
    val questions = level.examQuestions

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var isExamCompleted by remember { mutableStateOf(false) }
    var showSpeakingDialog by remember { mutableStateOf(false) }
    var speakingScoreForQuestion by remember { mutableStateOf<Int?>(null) }

    val currentQ = questions.getOrNull(currentQuestionIndex)

    if (showSpeakingDialog && currentQ != null) {
        val target = currentQ.targetSpeechText ?: currentQ.promptEn
        SpeakingEvaluationDialog(
            targetSentenceEn = target,
            targetSentenceFa = currentQ.promptFa,
            ttsHelper = ttsHelper,
            onDismiss = { showSpeakingDialog = false },
            onScoreResult = { score ->
                speakingScoreForQuestion = score
                hasAnsweredCurrent = true
                if (score >= 60) {
                    correctAnswersCount++
                }
                showSpeakingDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
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
                    contentDescription = "انصراف",
                    tint = theme.primaryColor
                )
            }
            PuffyBadge(text = "آزمون مرحله‌ای سطح ${level.levelCode}", emoji = "👑")
            Text(
                text = "سوال ${currentQuestionIndex + 1} از ${questions.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = theme.primaryColor,
            trackColor = theme.backgroundColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isExamCompleted) {
            // Level Completion Celebration Modal (جشن تکمیل سطح)
            val totalQuestions = questions.size
            val scorePercent = ((correctAnswersCount.toDouble() / totalQuestions.toDouble()) * 100).toInt()
            val starsEarned = userProgressManager.submitExamResult(level.id, scorePercent)
            val isPassed = scorePercent >= 60

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp,
                    elevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(58.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isPassed) "🎉 جشن تسلط بر سطح ${level.levelCode}!" else "🌸 تلاش ارزشمند $learnerName!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPassed) theme.buttonBevel else Color.DarkGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Big Stars
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { starIdx ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (starIdx < starsEarned) theme.primaryColor else Color(0xFF333333),
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "$scorePercent%",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = "پاسخ‌های صحیح: $correctAnswersCount از $totalQuestions",
                            fontSize = 14.sp,
                            color = Color(0xFFAAAAAA)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1D16))
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = if (isPassed)
                                    "پیام معلم خصوصی هوش مصنوعی: تبریک $learnerName عزیز! با یادگیری واقعی و تسلط کامل، سطح بعدی برای شما گشوده شد و پاداش طلایی دریافت کردی 🥇👑"
                                else
                                    "پیام معلم خصوصی: یادگیری واقعی نیاز به شتاب ندارد. مباحث را دوباره با صبوری مرور می‌کنیم تا به نمره ۱۰۰ برسی 💖",
                                fontSize = 12.sp,
                                color = theme.primaryColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        PuffyButton(
                            onClick = onExamFinished,
                            text = "ادامه مسیر خودسازی",
                            emoji = "👑",
                            height = 52.dp,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "finish_exam_button"
                        )
                    }
                }
            }
        } else if (currentQ != null) {
            // Active Question Display
            PuffyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 26.dp,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PuffyBadge(
                            text = when (currentQ.type) {
                                QuestionType.SPEAKING -> "آزمون گفتار و تلفظ"
                                QuestionType.LISTENING -> "آزمون شنیداری"
                                QuestionType.TRANSLATION -> "آزمون درک مطلب"
                                QuestionType.GRAMMAR_FILL -> "آزمون گرامر"
                                QuestionType.MULTIPLE_CHOICE -> "چهارگزینه‌ای"
                            },
                            emoji = "👑"
                        )

                        if (currentQ.type == QuestionType.LISTENING) {
                            PuffyIconButton(
                                onClick = { ttsHelper?.speak(currentQ.promptEn) },
                                icon = Icons.Default.VolumeUp,
                                size = 42.dp,
                                backgroundColor = theme.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentQ.promptEn,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentQ.promptFa,
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (currentQ.type == QuestionType.SPEAKING) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PuffyIconButton(
                                    onClick = { showSpeakingDialog = true },
                                    icon = Icons.Default.Mic,
                                    size = 72.dp,
                                    backgroundColor = theme.primaryColor,
                                    testTag = "exam_mic_button"
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (speakingScoreForQuestion != null)
                                        "نمره گفتار $learnerName: ${speakingScoreForQuestion}% ${if (speakingScoreForQuestion!! >= 60) "✅" else "❌"}"
                                    else
                                        "روی میکروفون بزنید و با صدای رسا بیان کنید 🎙️",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentQ.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = selectedOptionIndex == optIndex
                                val isCorrect = optIndex == currentQ.correctIndex

                                val optionBg = when {
                                    hasAnsweredCurrent && isCorrect -> Color(0xFF16301A)
                                    hasAnsweredCurrent && isSelected && !isCorrect -> Color(0xFF331616)
                                    isSelected -> Color(0xFF262013)
                                    else -> Color(0xFF1B1B22)
                                }
                                val optionBorder = when {
                                    hasAnsweredCurrent && isCorrect -> Color(0xFF4CAF50)
                                    hasAnsweredCurrent && isSelected && !isCorrect -> Color(0xFFE57373)
                                    isSelected -> theme.primaryColor
                                    else -> Color(0xFF33333E)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(optionBg)
                                        .border(1.5.dp, optionBorder, RoundedCornerShape(18.dp))
                                        .clickable(enabled = !hasAnsweredCurrent) {
                                            selectedOptionIndex = optIndex
                                            hasAnsweredCurrent = true
                                            if (optIndex == currentQ.correctIndex) {
                                                correctAnswersCount++
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (hasAnsweredCurrent && isCorrect) Color(0xFF4CAF50)
                                                    else if (hasAnsweredCurrent && isSelected) Color(0xFFE57373)
                                                    else Color(0xFF2A281E)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${optIndex + 1}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasAnsweredCurrent && (isCorrect || isSelected)) Color.White else theme.primaryColor
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = optionText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (hasAnsweredCurrent && isCorrect) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "صحیح", tint = Color(0xFF4CAF50))
                                        } else if (hasAnsweredCurrent && isSelected && !isCorrect) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "غلط", tint = Color(0xFFE57373))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (hasAnsweredCurrent) {
                        Text(
                            text = currentQ.explanationFa,
                            fontSize = 12.sp,
                            color = theme.primaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    PuffyButton(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOptionIndex = null
                                hasAnsweredCurrent = false
                                speakingScoreForQuestion = null
                            } else {
                                isExamCompleted = true
                            }
                        },
                        text = if (currentQuestionIndex < questions.size - 1) "سوال بعدی" else "دیدن کارنامه آزمون",
                        emoji = "✨",
                        height = 50.dp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasAnsweredCurrent,
                        testTag = "next_exam_question_button"
                    )
                }
            }
        }
    }
}
