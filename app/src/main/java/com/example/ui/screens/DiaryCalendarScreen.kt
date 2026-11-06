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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserProgressManager
import com.example.model.DiaryNote
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import com.example.ui.components.PuffyCard
import com.example.ui.components.PuffyIconButton
import com.example.ui.theme.LocalZahraTheme
import java.util.UUID

@Composable
fun DiaryCalendarScreen(
    userProgressManager: UserProgressManager,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val learnerName = LocalZahraTheme.current.learnerName
    val diaryNotes by userProgressManager.diaryNotes.collectAsState()
    val todayMinutes by userProgressManager.todayStudyMinutes.collectAsState()
    val totalHours by userProgressManager.totalStudyHours.collectAsState()
    val streak by userProgressManager.streakDays.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedCalendarDay by remember { mutableIntStateOf(6) }

    if (showAddNoteDialog) {
        AddDiaryNoteDialog(
            dayNumber = selectedCalendarDay,
            onDismiss = { showAddNoteDialog = false },
            onSave = { newNote ->
                userProgressManager.addDiaryNote(newNote)
                showAddNoteDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .padding(14.dp)
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
            PuffyBadge(text = "تقویم مطالعه و خاطرات خودسازی", emoji = "📅")
            PuffyIconButton(
                onClick = { showAddNoteDialog = true },
                icon = Icons.Default.Add,
                size = 42.dp,
                backgroundColor = theme.primaryColor,
                testTag = "add_diary_note_button"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Study Analytics Summary Card (گزارش پیشرفت)
        PuffyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥 استمرار", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                    Text(text = "$streak روز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF333333)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⏱️ امروز", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                    Text(text = "$todayMinutes دقیقه", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF333333)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📚 کل ساعات", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                    Text(text = "$totalHours ساعت", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Study Calendar Widget
        PuffyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            elevation = 6.dp
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
                    Text(
                        text = "👑 آبان‌ماه ۱۴۰۵ • تقویم خودسازی $learnerName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    PuffyBadge(text = "${diaryNotes.size} یادداشت ثبت‌شده", emoji = "📝")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { dayTitle ->
                        Text(
                            text = dayTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val daysList = (1..14).toList()
                val chunkedDays = daysList.chunked(7)
                chunkedDays.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        week.forEach { day ->
                            val isSelected = selectedCalendarDay == day
                            val hasNoteOnDay = diaryNotes.any { it.dateIso.endsWith(String.format("%02d", day)) }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) theme.primaryColor else Color(0xFF1E1E26))
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color.White else if (hasNoteOnDay) theme.primaryColor else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedCalendarDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$day",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                    if (hasNoteOnDay) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.Black else theme.primaryColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "یادداشت‌ها و تجربیات یادگیری $learnerName:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            PuffyButton(
                onClick = { showAddNoteDialog = true },
                text = "ثبت خاطره جدید",
                emoji = "✍️",
                height = 36.dp,
                fontSize = 12,
                modifier = Modifier.width(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(diaryNotes, key = { it.id }) { note ->
                DiaryNoteCard(note = note)
            }
        }
    }
}

@Composable
fun DiaryNoteCard(note: DiaryNote) {
    val theme = LocalZahraTheme.current.currentTheme

    PuffyCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF17171E),
        borderColor = theme.primaryColor.copy(alpha = 0.4f),
        cornerRadius = 20.dp
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
                    Text(text = note.moodEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = note.titleFa,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${note.dateDisplayFa} • ${note.moodTitleFa}",
                            fontSize = 11.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.attachedStickers.forEach { sticker ->
                        Text(text = sticker, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.content,
                fontSize = 13.sp,
                color = Color(0xFFDDDDDD),
                lineHeight = 20.sp
            )

            if (note.englishSentenceLearned.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242013))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جمله انگلیسی آموخته‌شده: ${note.englishSentenceLearned}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddDiaryNoteDialog(
    dayNumber: Int,
    onDismiss: () -> Unit,
    onSave: (DiaryNote) -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var englishSentence by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Pair("👑", "مصمم و هدفمند")) }
    val attachedStickers = remember { mutableStateListOf("👑", "🥇", "✨") }

    val moods = listOf(
        Pair("👑", "مصمم و باوقار"),
        Pair("🔥", "پرتلاش و باانگیزه"),
        Pair("💡", "یادگیری عمیق"),
        Pair("🏆", "پیروز و سرافراز"),
        Pair("💎", "آرامش در خودسازی")
    )

    Dialog(onDismissRequest = onDismiss) {
        PuffyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            cornerRadius = 24.dp,
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PuffyBadge(text = "ثبت در تقویم خودسازی $learnerName", emoji = "👑")
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان یادداشت خودسازی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = theme.primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("متن دستاورد، تمرین یا خاطره امروز...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = theme.primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = englishSentence,
                    onValueChange = { englishSentence = it },
                    label = { Text("جمله انگلیسی مهم امروز") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = theme.primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "حس و حال و وضعیت ذهنی $learnerName:", fontSize = 12.sp, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(moods) { mood ->
                        val isSelected = selectedMood == mood
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) theme.primaryColor else Color(0xFF1E1E24))
                                .clickable { selectedMood = mood }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mood.first, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mood.second,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PuffyButton(
                    onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            onSave(
                                DiaryNote(
                                    id = UUID.randomUUID().toString(),
                                    dateIso = "2026-11-${String.format("%02d", dayNumber)}",
                                    dateDisplayFa = "$dayNumber آبان‌ماه",
                                    moodEmoji = selectedMood.first,
                                    moodTitleFa = selectedMood.second,
                                    titleFa = title.ifBlank { "پیشرفت روزانه $learnerName" },
                                    content = content.ifBlank { "امروز گامی محکم در یادگیری زبان و خودسازی برداشتم." },
                                    englishSentenceLearned = englishSentence,
                                    attachedStickers = attachedStickers.toList(),
                                    colorTagHex = 0xFFD4AF37
                                )
                            )
                        }
                    },
                    text = "ثبت در تقویم (+۱۰ سکه طلا 🥇)",
                    emoji = "👑",
                    height = 50.dp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_diary_note_button"
                )
            }
        }
    }
}
