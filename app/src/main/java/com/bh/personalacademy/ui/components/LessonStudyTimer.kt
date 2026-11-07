package com.bh.personalacademy.ui.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.academy.core.AsciiDigits
import com.example.academy.domain.model.AcademyLocale
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun LessonStudyTimer(
    lessonId: String,
    locale: AcademyLocale,
    modifier: Modifier = Modifier,
    onSaveSession: (lessonId: String, startedAtMillis: Long, durationMillis: Long) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var active by remember(lessonId) { mutableStateOf(false) }
    var segmentStartedAt by remember(lessonId) { mutableStateOf<Long?>(null) }
    var sessionStartedAt by remember(lessonId) { mutableStateOf<Long?>(null) }
    var accumulatedMillis by remember(lessonId) { mutableLongStateOf(0L) }
    var displayedMillis by remember(lessonId) { mutableLongStateOf(0L) }

    val activeNow by rememberUpdatedState(active)
    val segmentNow by rememberUpdatedState(segmentStartedAt)
    val sessionNow by rememberUpdatedState(sessionStartedAt)
    val accumulatedNow by rememberUpdatedState(accumulatedMillis)
    val saveNow by rememberUpdatedState(onSaveSession)

    DisposableEffect(lifecycleOwner, lessonId) {
        val observer = LifecycleEventObserver { _, event ->
            val now = System.currentTimeMillis()
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    val segment = segmentNow
                    if (activeNow && segment != null) {
                        accumulatedMillis = accumulatedNow + (now - segment).coerceAtLeast(0L)
                        segmentStartedAt = null
                    }
                }
                Lifecycle.Event.ON_START -> {
                    if (activeNow && segmentNow == null) segmentStartedAt = now
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (activeNow) {
                val now = System.currentTimeMillis()
                val total = accumulatedNow + (segmentNow?.let { (now - it).coerceAtLeast(0L) } ?: 0L)
                val started = sessionNow ?: now
                if (total >= 1_000L) saveNow(lessonId, started, total)
            }
        }
    }

    LaunchedEffect(active, segmentStartedAt, accumulatedMillis) {
        while (active) {
            val now = System.currentTimeMillis()
            displayedMillis = accumulatedMillis +
                (segmentStartedAt?.let { (now - it).coerceAtLeast(0L) } ?: 0L)
            delay(1_000L)
        }
    }

    val labels = when (locale) {
        AcademyLocale.ENGLISH_US -> Triple("Start study timer", "Finish session", "Active study")
        AcademyLocale.DARI_AF -> Triple("آغاز زمان مطالعه", "پایان جلسه", "زمان مطالعه")
        AcademyLocale.PASHTO_AF -> Triple("د مطالعې پیل", "ناسته پای ته رسول", "د مطالعې وخت")
        AcademyLocale.ARABIC -> Triple("بدء المؤقت", "إنهاء الجلسة", "وقت الدراسة")
    }
    val minutes = displayedMillis / 60_000L
    val seconds = (displayedMillis / 1_000L) % 60L
    val clock = AsciiDigits.normalize(String.format(Locale.US, "%02d:%02d", minutes, seconds))

    Text("${labels.third}: $clock")
    OutlinedButton(
        modifier = modifier,
        onClick = {
            val now = System.currentTimeMillis()
            if (!active) {
                active = true
                sessionStartedAt = now
                segmentStartedAt = now
                accumulatedMillis = 0L
                displayedMillis = 0L
            } else {
                val total = accumulatedMillis +
                    (segmentStartedAt?.let { (now - it).coerceAtLeast(0L) } ?: 0L)
                val started = sessionStartedAt ?: now
                if (total >= 1_000L) onSaveSession(lessonId, started, total)
                active = false
                segmentStartedAt = null
                sessionStartedAt = null
                accumulatedMillis = 0L
                displayedMillis = 0L
            }
        }
    ) {
        Text(if (active) labels.second else labels.first)
    }
}
