package com.example.data

import android.util.Log
import com.example.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "OnlineAITutorService"

sealed interface OnlineAiResult {
    data class Success(val message: ChatMessage) : OnlineAiResult
    data class Error(val errorMessage: String) : OnlineAiResult
}

class OnlineAITutorService {

    // Lazy initialization per Firebase AI Logic guidelines to avoid startup overhead
    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = com.google.firebase.ai.type.content {
                text(
                    """
                    You are Master Gohari (استاد گوهری), the private AI English tutor for "BH Self Development" (خودسازی / BH Exclusive Academy).
                    The motto of the academy is: "از صفر شروع کن، تا تسلط کامل ادامه بده!" (Start from zero, continue until absolute mastery!).
                    
                    Persona & Guidelines:
                    1. Patient, kind, warm, respectful, and highly professional.
                    2. Use friendly and supportive tone with cute emojis (🌸, ✨, 👑, 💖).
                    3. If the learner asks to explain from scratch or speaks in Persian, explain gently in Persian with clear English examples and pronunciation hints.
                    4. Always check the learner's English grammar and vocabulary gently. If there is an error, praise their effort first, then explain the correction respectfully.
                    5. For every learning response, use a clear bilingual structure:
                       - A short, natural English answer.
                       - A faithful Dari (Afghan Persian) translation or explanation.
                       - One concise grammar or vocabulary tip when relevant.
                       - One short practice question matched to the learner's level.
                       Keep headings readable and do not claim a translation was provided when it was not.
                    6. If the learner asks about self-development, habits, goals, motivation, or time management,
                       provide practical, age-appropriate guidance and a small actionable exercise.
                    7. Never be harsh, dismissive, or overly academic. Adapt to levels A1 through Z9.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun sendPracticeMessage(
        userMessage: String,
        learnerName: String,
        currentLevelCode: String,
        conversationHistory: List<ChatMessage>
    ): OnlineAiResult = withContext(Dispatchers.IO) {
        try {
            val promptBuilder = StringBuilder()
            promptBuilder.append("Learner Name: $learnerName\n")
            promptBuilder.append("Current Academy Level: $currentLevelCode\n\n")

            // Append last 4 messages for context
            val recentHistory = conversationHistory.takeLast(4)
            if (recentHistory.isNotEmpty()) {
                promptBuilder.append("Recent conversation context:\n")
                for (msg in recentHistory) {
                    val role = if (msg.isFromTutor) "Tutor" else "Learner"
                    promptBuilder.append("$role: ${msg.englishText}\n")
                }
                promptBuilder.append("\n")
            }

            promptBuilder.append("Learner's new message:\n$userMessage\n\n")
            promptBuilder.append("Please respond as Master Gohari to guide and practice with $learnerName.")

            val response = withTimeout(60_000L) {
                generativeModel.generateContent(promptBuilder.toString())
            }

            val responseText = response.text
            if (!responseText.isNullOrBlank()) {
                val cleanedText = responseText.trim()
                val replyMessage = ChatMessage(
                    id = "online_${System.currentTimeMillis()}",
                    englishText = cleanedText,
                    // The model returns the bilingual lesson in its main response body.
                    // Do not put a status label in the translation field.
                    isFromTutor = true,
                    timestampMillis = System.currentTimeMillis()
                )
                OnlineAiResult.Success(replyMessage)
            } else {
                OnlineAiResult.Error(
                    errorMessage = "پاسخ خالی از مدل هوش مصنوعی دریافت شد؛ هیچ پاسخ آماده‌ای نمایش داده نشد."
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Online AI generation timed out", e)
            OnlineAiResult.Error(
                errorMessage = "زمان پاسخگویی هوش مصنوعی آنلاین به پایان رسید. لطفاً دوباره تلاش کنید."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Online AI generation error", e)
            OnlineAiResult.Error(
                errorMessage = e.localizedMessage ?: "ارتباط با هوش مصنوعی آنلاین برقرار نشد. لطفاً اتصال و تنظیمات Firebase را بررسی کنید."
            )
        }
    }
}
