package com.bh.personalacademy.speaking

import java.util.Locale

/** Word-level edit-distance score for speech-to-text feedback. Not phoneme/accent scoring. */
object SpeechScoring {
    fun accuracy(target: String, spoken: String): Int {
        val a = normalize(target)
        val b = normalize(spoken)
        if (a.isEmpty()) return 0
        val previous = IntArray(b.size + 1) { it }
        for (i in 1..a.size) {
            val current = IntArray(b.size + 1)
            current[0] = i
            for (j in 1..b.size) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
            }
            current.copyInto(previous)
        }
        return ((1.0 - previous[b.size].toDouble() / maxOf(a.size, b.size, 1)) * 100.0)
            .toInt().coerceIn(0, 100)
    }

    private fun normalize(value: String): List<String> =
        value.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
}
