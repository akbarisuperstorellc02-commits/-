package com.bh.personalacademy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bh.personalacademy.data.local.StudySessionEntity
import com.example.academy.core.AsciiDigits
import com.example.academy.domain.model.AcademyLocale
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StudyCalendarCard(
    locale: AcademyLocale,
    sessions: List<StudySessionEntity>,
    modifier: Modifier = Modifier
) {
    var monthOffset by remember { mutableIntStateOf(0) }
    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val localeTag = locale.languageTag
    val formattingLocale = remember(localeTag) { Locale.forLanguageTag(localeTag) }
    val monthTitle = remember(monthOffset, localeTag) {
        SimpleDateFormat("MMMM yyyy", formattingLocale).format(calendar.time)
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY).mod(7)
    val weekCount = (firstDayOffset + daysInMonth + 6) / 7
    val monthStart = remember(monthOffset) { (calendar.clone() as Calendar).timeInMillis }
    val monthEnd = remember(monthOffset) {
        (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
    }
    val minutesByDay = remember(sessions, monthOffset) {
        sessions.asSequence()
            .filter { it.startedAt >= monthStart && it.startedAt < monthEnd }
            .groupBy { session ->
                Calendar.getInstance().apply { timeInMillis = session.startedAt }
                    .get(Calendar.DAY_OF_MONTH)
            }
            .mapValues { (_, daySessions) -> daySessions.sumOf { it.durationMillis } / 60_000L }
    }
    val weekdayNames = remember(localeTag) {
        val names = DateFormatSymbols(formattingLocale).shortWeekdays
        listOf( Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY).map { names[it] }
    }
    val totalMonthMinutes = minutesByDay.values.sum()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (locale) {
                        AcademyLocale.ENGLISH_US -> "Study calendar"
                        AcademyLocale.DARI_AF -> "تقویم مطالعه"
                        AcademyLocale.PASHTO_AF -> "د مطالعې کلیزه"
                        AcademyLocale.ARABIC -> "تقويم الدراسة"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { monthOffset -= 1 }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                IconButton(onClick = { monthOffset += 1 }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }
            Text(monthTitle, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekdayNames.forEach { name ->
                    Text(
                        name,
                        modifier = Modifier.weight(1f),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            repeat(weekCount) { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { weekday ->
                        val day = week * 7 + weekday - firstDayOffset + 1
                        if (day in 1..daysInMonth) {
                            val minutes = minutesByDay[day] ?: 0L
                            val studied = minutes > 0L
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (studied) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    AsciiDigits.normalize(day.toString()),
                                    fontWeight = if (studied) FontWeight.Bold else FontWeight.Normal,
                                    color = if (studied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                                if (studied) {
                                    Text(
                                        AsciiDigits.normalize(minutes.toString()) + "m",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                } else {
                                    Spacer(Modifier.size(10.dp))
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f).aspectRatio(0.9f))
                        }
                    }
                }
            }
            Text(
                when (locale) {
                    AcademyLocale.ENGLISH_US -> "${AsciiDigits.normalize(minutesByDay.count { it.value > 0 }.toString())} study days · ${AsciiDigits.normalize(totalMonthMinutes.toString())} minutes"
                    AcademyLocale.DARI_AF -> "${AsciiDigits.normalize(minutesByDay.count { it.value > 0 }.toString())} روز مطالعه · ${AsciiDigits.normalize(totalMonthMinutes.toString())} دقیقه"
                    AcademyLocale.PASHTO_AF -> "${AsciiDigits.normalize(minutesByDay.count { it.value > 0 }.toString())} د مطالعې ورځې · ${AsciiDigits.normalize(totalMonthMinutes.toString())} دقیقې"
                    AcademyLocale.ARABIC -> "${AsciiDigits.normalize(minutesByDay.count { it.value > 0 }.toString())} أيام دراسة · ${AsciiDigits.normalize(totalMonthMinutes.toString())} دقيقة"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
