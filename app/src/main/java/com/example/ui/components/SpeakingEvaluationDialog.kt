package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.model.SpeechEvaluationResult
import com.example.ui.theme.LocalZahraTheme
import com.example.util.SpeechEvaluator
import com.example.util.SpeechRecognizerHelper
import com.example.util.TextToSpeechHelper

@Composable
fun SpeakingEvaluationDialog(
    targetSentenceEn: String,
    targetSentenceFa: String,
    ttsHelper: TextToSpeechHelper?,
    onDismiss: () -> Unit,
    onScoreResult: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val theme = LocalZahraTheme.current.currentTheme

    var isListening by remember { mutableStateOf(false) }
    var recognizedSpokenText by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf<SpeechEvaluationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var customInputText by remember { mutableStateOf("") }

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { spoken ->
                recognizedSpokenText = spoken
                val result = SpeechEvaluator.evaluate(spoken, targetSentenceEn)
                evaluationResult = result
                onScoreResult?.invoke(result.scorePercent)
            },
            onError = { err ->
                errorMessage = err
            },
            onListeningStateChanged = { listening ->
                isListening = listening
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            errorMessage = null
            speechHelper.startListening()
        } else {
            errorMessage = "برای شنیدن صدای زیبایت، دسترسی میکروفون لازم است."
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        PuffyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            cornerRadius = 28.dp,
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header with close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PuffyBadge(text = "سنجش مکالمه و تلفظ", emoji = "🎙️")
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target phrase card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.backgroundColor)
                        .border(1.5.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = targetSentenceEn,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.buttonBevel,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PuffyIconButton(
                                onClick = { ttsHelper?.speak(targetSentenceEn) },
                                icon = Icons.Default.VolumeUp,
                                size = 38.dp,
                                backgroundColor = theme.primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = targetSentenceFa,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Puffy Microphone Button
                Box(contentAlignment = Alignment.Center) {
                    if (isListening) {
                        CircularProgressIndicator(
                            color = theme.primaryColor,
                            modifier = Modifier.size(92.dp),
                            strokeWidth = 4.dp
                        )
                    }
                    PuffyIconButton(
                        onClick = {
                            if (isListening) {
                                speechHelper.stopListening()
                            } else {
                                errorMessage = null
                                evaluationResult = null
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    speechHelper.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        icon = Icons.Default.Mic,
                        size = 76.dp,
                        backgroundColor = if (isListening) theme.accentGold else theme.primaryColor,
                        testTag = "record_speech_button"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isListening) "در حال شنیدن... با صدای رسا بگویید 👑" else "روی میکروفون بزنید و جمله را بگویید",
                    fontSize = 13.sp,
                    color = if (isListening) theme.accentGold else Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                }

                // Speech simulation or manual pronunciation typing fallback
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { customInputText = it },
                        placeholder = { Text("تست بدون میکروفون...", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = theme.primaryColor
                        )
                    )
                    PuffyButton(
                        onClick = {
                            val textToTest = customInputText.ifBlank { targetSentenceEn }
                            recognizedSpokenText = textToTest
                            val res = SpeechEvaluator.evaluate(textToTest, targetSentenceEn)
                            evaluationResult = res
                            onScoreResult?.invoke(res.scorePercent)
                        },
                        text = "نمره بده",
                        emoji = "✨",
                        height = 48.dp,
                        modifier = Modifier.width(115.dp)
                    )
                }

                // Score Result Card
                AnimatedVisibility(
                    visible = evaluationResult != null,
                    enter = fadeIn() + slideInVertically()
                ) {
                    val result = evaluationResult
                    if (result != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (result.scorePercent >= 70) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                )
                                .border(
                                    1.dp,
                                    if (result.scorePercent >= 70) Color(0xFF81C784) else Color(0xFFFFB74D),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${result.scorePercent}%",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (result.scorePercent >= 70) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Row {
                                        repeat(3) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < result.stars) Color(0xFFFFB300) else Color.LightGray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result.feedbackFa,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "شنیده شده: \"${result.spokenText}\"",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
