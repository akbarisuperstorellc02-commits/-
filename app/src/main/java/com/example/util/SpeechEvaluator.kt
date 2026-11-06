package com.example.util

import com.example.model.SpeechEvaluationResult
import kotlin.math.max
import kotlin.math.min

object SpeechEvaluator {

    fun evaluate(spoken: String, target: String): SpeechEvaluationResult {
        val cleanSpoken = normalize(spoken)
        val cleanTarget = normalize(target)

        val targetTokens = cleanTarget.split(" ").filter { it.isNotBlank() }
        val spokenTokens = cleanSpoken.split(" ").filter { it.isNotBlank() }

        if (targetTokens.isEmpty()) {
            return SpeechEvaluationResult(
                spokenText = spoken,
                targetText = target,
                scorePercent = 100,
                feedbackFa = "عالی و بی‌نقص! 💖",
                stars = 3,
                matchedTokens = targetTokens,
                missedTokens = emptyList()
            )
        }

        val matched = mutableListOf<String>()
        val missed = mutableListOf<String>()

        var matchCount = 0
        for (targetToken in targetTokens) {
            val isFound = spokenTokens.any { spokenToken ->
                levenshteinDistance(targetToken, spokenToken) <= max(1, targetToken.length / 3)
            }
            if (isFound) {
                matchCount++
                matched.add(targetToken)
            } else {
                missed.add(targetToken)
            }
        }

        // String similarity calculation
        val levSim = 1.0 - (levenshteinDistance(cleanSpoken, cleanTarget).toDouble() / max(1, max(cleanSpoken.length, cleanTarget.length)))
        val tokenSim = matchCount.toDouble() / targetTokens.size.toDouble()

        val finalScore = ((tokenSim * 0.7 + levSim * 0.3) * 100).toInt().coerceIn(0, 100)

        val (feedback, stars) = when {
            finalScore >= 85 -> Pair("فوق‌العاده و بی‌نقص! تلفظت مثل یک بومی‌زبان زیباست! 💖✨", 3)
            finalScore >= 65 -> Pair("خیلی عالی بود عزیزم! فقط کمی دقت بیشتر لازمه 🌸", 2)
            finalScore >= 40 -> Pair("تلاش خوبی بود! بازم تمرین کن تا به اوج برسی 🎀", 1)
            else -> Pair("اشکالی نداره عزیزم، یک‌بار دیگه با صدای رسا تکرار کن! 🌷", 0)
        }

        return SpeechEvaluationResult(
            spokenText = spoken.ifBlank { "صدایی شنیده نشد" },
            targetText = target,
            scorePercent = finalScore,
            feedbackFa = feedback,
            stars = stars,
            matchedTokens = matched,
            missedTokens = missed
        )
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
