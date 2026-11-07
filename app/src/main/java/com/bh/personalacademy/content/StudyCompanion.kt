package com.bh.personalacademy.content

/** Selects the calendar companion from the learner's saved study time. */
object StudyCompanion {
    fun emojiForMinutes(minutes: Long): String = when {
        minutes >= 60L -> "🐢"
        minutes >= 30L -> "🦊"
        minutes >= 15L -> "🐝"
        minutes > 0L -> "🐣"
        else -> "🌱"
    }
}
