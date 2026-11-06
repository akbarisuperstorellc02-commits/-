package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressManager
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.components.SpeakingEvaluationDialog
import com.example.ui.theme.LocalZahraTheme
import com.example.util.TextToSpeechHelper

data class SpeakingDrillItem(
    val english: String,
    val persian: String,
    val levelCode: String,
    val emoji: String
)

@Composable
fun SpeakingPracticeScreen(
    userProgressManager: UserProgressManager,
    ttsHelper: TextToSpeechHelper?,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val learnerName by userProgressManager.learnerName.collectAsState()

    val drillList = listOf(
        SpeakingDrillItem("Start from zero, continue until absolute mastery!", "از صفر شروع کن، تا تسلط کامل ادامه بده!", "Motto", "👑"),
        SpeakingDrillItem("I am $learnerName, and I am learning English with passion.", "من $learnerName هستم و با اشتیاق انگلیسی یاد می‌گیرم.", "A1", "✨"),
        SpeakingDrillItem("Could you please explain that from the beginning?", "می‌شود لطفاً آن را از ابتدا توضیح دهید؟", "A2", "🗣️"),
        SpeakingDrillItem("Think positive and shine like pure gold every day.", "مثبت فکر کن و هر روز مثل طلای ناب بدرخش.", "Phonics", "💡"),
        SpeakingDrillItem("If you persist with dedication, you will succeed.", "اگر با تعهد پایداری کنی، موفق خواهی شد.", "B1", "🏆"),
        SpeakingDrillItem("Self-discipline is paramount for achieving excellence.", "خودانضباطی برای دستیابی به تعالی بالاترین اهمیت را دارد.", "B2", "🌟"),
        SpeakingDrillItem("Her speech was marked by eloquence and articulacy.", "کلام او با فصاحت و شیوایی تمام همراه بود.", "C1/C2", "💎")
    )

    var activeDrill by remember { mutableStateOf<SpeakingDrillItem?>(null) }

    if (activeDrill != null) {
        SpeakingEvaluationDialog(
            targetSentenceEn = activeDrill!!.english,
            targetSentenceFa = activeDrill!!.persian,
            ttsHelper = ttsHelper,
            onDismiss = { activeDrill = null },
            onScoreResult = { score ->
                userProgressManager.recordSpeakingPractice(score)
            }
        )
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
                    tint = theme.primaryColor
                )
            }
            PuffyBadge(text = "آزمایشگاه مکالمه و تلفظ طلایی", emoji = "🎙️")
            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info Card
        PuffyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "👑 سنجش مکالمه صوتی و تسلط کلام:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor
                )
                Text(
                    text = "روی میکروفون بزنید و جملات را با لهجه رسا ادا کنید. تلفظ شما به صورت کلمه‌به‌کلمه تحلیل و نمره‌گذاری خواهد شد.",
                    fontSize = 12.sp,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(drillList) { drill ->
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = drill.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = drill.english,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = drill.persian,
                                fontSize = 12.sp,
                                color = theme.primaryColor
                            )
                        }

                        // Play Audio
                        PuffyIconButton(
                            onClick = { ttsHelper?.speak(drill.english) },
                            icon = Icons.Default.VolumeUp,
                            size = 36.dp,
                            backgroundColor = theme.primaryColor
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Speak & evaluate
                        PuffyIconButton(
                            onClick = { activeDrill = drill },
                            icon = Icons.Default.Mic,
                            size = 40.dp,
                            backgroundColor = Color(0xFF262013),
                            tint = theme.primaryColor
                        )
                    }
                }
            }
        }
    }
}
