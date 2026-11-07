package com.bh.personalacademy.content

/**
 * Selects a calm, friendly calendar companion from the learner's saved study time.
 * Thresholds are kept in one place so the UI and tests use identical rules.
 */
object StudyCompanion {
    fun emojiForMinutes(minutes: Long): String = when {
        minutes >= 60L -> "🐢"
        minutes >= 30L -> "🦊"
        minutes >= 15L -> "🐝"
        minutes > 0L -> "🐣"
        else -> "🌱"
    }
}
