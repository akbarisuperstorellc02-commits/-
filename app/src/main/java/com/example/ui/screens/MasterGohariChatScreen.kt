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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserProgressManager
import com.example.model.ChatMessage
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.components.SpeakingEvaluationDialog
import com.example.ui.theme.LocalZahraTheme
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.launch

@Composable
fun MasterGohariChatScreen(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val messages by userProgressManager.chatMessages.collectAsState()
    val learnerName by userProgressManager.learnerName.collectAsState()
    val levels by userProgressManager.levelsState.collectAsState()
    val listState = rememberLazyListState()

    val onlineAiService = remember { com.example.data.OnlineAITutorService() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var isAiGenerating by remember { mutableStateOf(false) }
    var aiErrorMessage by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    var speakingPracticeSentence by remember { mutableStateOf<Pair<String, String>?>(null) }

    val currentActiveLevelCode = levels.firstOrNull { it.isUnlocked && !it.isMastered }?.levelCode ?: "A1"

    fun handleSendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank() || isAiGenerating) return
        inputText = ""

        run {
            val userMsg = ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                isFromTutor = false,
                englishText = trimmed,
                persianTranslation = null
            )
            userProgressManager.addCustomChatMessage(userMsg)
            isAiGenerating = true
            aiErrorMessage = null

            coroutineScope.launch {
                val result = onlineAiService.sendPracticeMessage(
                    userMessage = trimmed,
                    learnerName = learnerName,
                    currentLevelCode = currentActiveLevelCode,
                    conversationHistory = messages
                )
                isAiGenerating = false
                when (result) {
                    is com.example.data.OnlineAiResult.Success -> {
                        userProgressManager.addCustomChatMessage(result.message)
                        userProgressManager.addScore(15, "مکالمه با هوش مصنوعی آنلاین Gemini")

                    }
                    is com.example.data.OnlineAiResult.Error -> {
                        aiErrorMessage = result.errorMessage
                        // Keep the learner's message in the input so it can be retried after fixing connectivity.
                        inputText = trimmed
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (speakingPracticeSentence != null) {
        SpeakingEvaluationDialog(
            targetSentenceEn = speakingPracticeSentence!!.first,
            targetSentenceFa = speakingPracticeSentence!!.second,
            ttsHelper = ttsHelper,
            onDismiss = { speakingPracticeSentence = null },
            onScoreResult = { score ->
                userProgressManager.recordSpeakingPractice(score)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .imePadding()
    ) {
        // Top App Bar with the academy AI tutor profile
        PuffyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            cornerRadius = 24.dp,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = theme.primaryColor
                    )
                }

                // AI Tutor Avatar Portrait
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, theme.primaryColor, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zahra_ai_tutor_1793948695153),
                        contentDescription = "معلم خصوصی هوش مصنوعی",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "هوش مصنوعی آنلاین Gemini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "پاسخ زنده از مدل هوش مصنوعی آنلاین",
                        fontSize = 10.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    learnerName = learnerName,
                    onPlayAudio = { ttsHelper?.speak(msg.englishText) },
                    onPracticeSpeaking = {
                        speakingPracticeSentence = Pair(
                            msg.englishText,
                            msg.persianTranslation ?: "تمرین تلفظ جمله اختصاصی"
                        )
                    },
                    onQuickReplyClick = { reply ->
                        handleSendMessage(reply)
                    }
                )
            }

            if (isAiGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(text = "✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معلم آنلاین Gemini در حال تحلیل و پاسخ برای $learnerName است... 🌸",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }

        if (aiErrorMessage != null) {
            Text(
                text = "خطای اتصال هوش مصنوعی آنلاین: $aiErrorMessage",
                color = Color(0xFFFF8A80),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            )
        }

        // Bottom Input Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141418))
                .border(1.dp, Color(0xFF2B2B33), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Practice voice input
                IconButton(
                    onClick = {
                        speakingPracticeSentence = Pair(
                            if (inputText.isNotBlank()) inputText else "Start from zero, continue until absolute mastery!",
                            "تمرین گفتار با معلم هوش مصنوعی"
                        )
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "مکالمه صوتی",
                        tint = theme.primaryColor
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "پیام خود را برای معلم آنلاین بنویسید...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1D1D24),
                        unfocusedContainerColor = Color(0xFF1D1D24),
                        focusedIndicatorColor = theme.primaryColor,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                PuffyIconButton(
                    onClick = {
                        handleSendMessage(inputText)
                    },
                    icon = Icons.AutoMirrored.Filled.Send,
                    size = 46.dp,
                    backgroundColor = theme.primaryColor,
                    testTag = "send_chat_button"
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    learnerName: String,
    onPlayAudio: () -> Unit,
    onPracticeSpeaking: () -> Unit,
    onQuickReplyClick: (String) -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val isTutor = message.isFromTutor

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isTutor) Alignment.Start else Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isTutor) Arrangement.Start else Arrangement.End
        ) {
            if (isTutor) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, theme.primaryColor, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zahra_ai_tutor_1793948695153),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Bubble container
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isTutor) 4.dp else 20.dp,
                            bottomEnd = if (isTutor) 20.dp else 4.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isTutor) 4.dp else 20.dp,
                            bottomEnd = if (isTutor) 20.dp else 4.dp
                        )
                    )
                    .background(if (isTutor) Color(0xFF1B1B22) else theme.primaryColor)
                    .border(
                        1.dp,
                        if (isTutor) theme.primaryColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.englishText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isTutor) Color(0xFFEEEEEE) else Color(0xFF0D0D0F)
                    )

                    if (!message.persianTranslation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.persianTranslation,
                            fontSize = 13.sp,
                            color = if (isTutor) theme.primaryColor else Color(0xFF222222),
                            lineHeight = 19.sp
                        )
                    }

                    if (!message.grammarTipFa.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF262013))
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "💡 ${message.grammarTipFa}",
                                fontSize = 11.sp,
                                color = theme.primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!message.weaknessFlagged.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        PuffyBadge(
                            text = "نکته مهم برای مرور $learnerName: ${message.weaknessFlagged}",
                            emoji = "🎯",
                            backgroundColor = Color(0xFF332014),
                            textColor = Color(0xFFFF9E00)
                        )
                    }

                    if (isTutor) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PuffyIconButton(
                                onClick = onPlayAudio,
                                icon = Icons.Default.VolumeUp,
                                size = 32.dp,
                                backgroundColor = theme.primaryColor
                            )
                            PuffyButton(
                                onClick = onPracticeSpeaking,
                                text = "تمرین تلفظ این جمله",
                                emoji = "🎙️",
                                height = 32.dp,
                                fontSize = 11,
                                backgroundColor = Color(0xFF2D2A20),
                                textColor = theme.primaryColor
                            )
                        }
                    }
                }
            }
        }

        // Quick suggestions
        if (message.quickReplies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(message.quickReplies) { reply ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E26))
                            .border(1.dp, theme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clickable { onQuickReplyClick(reply) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = reply,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }
    }
}
