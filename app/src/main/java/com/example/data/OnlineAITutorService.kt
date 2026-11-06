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
            promptBuilder.append("Current Academy Level: $currentLevelCode\n")
            promptBuilder.append("Personalization rule: Address the learner by the exact name '$learnerName' naturally throughout the lesson, examples, exercises, encouragement and follow-up questions. Never call the learner Zahra or use another person's name. Keep the lesson continuous with this learner's current level and recent conversation.\n\n")

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
            val diagnosticDetails = generateSequence(e as Throwable?) { it.cause }
                .mapNotNull { it.message }
                .distinct()
                .joinToString(" | ")
            Log.e(TAG, "Online AI generation error: $diagnosticDetails", e)

            val normalized = diagnosticDetails.lowercase()
            val userFacingError = when {
                "app check" in normalized || "permission_denied" in normalized ||
                    "403" in normalized || "attestation" in normalized ->
                    "دسترسی Firebase AI Logic رد شد (403). در Firebase Console بخش AI Logic را فعال کنید و توکن App Check مخصوص نسخهٔ Debug را از Logcat در App Check ثبت کنید. اگر App Check اجباری است، توکن Debug ثبت‌نشده باعث همین خطا می‌شود."
                "429" in normalized || "resource_exhausted" in normalized ||
                    "quota" in normalized || "rate limit" in normalized ->
                    "سهمیه یا محدودیت درخواست‌های Gemini پر شده است (429). وضعیت سهمیه و فعال بودن پرداخت/محدودیت‌های پروژه Firebase را بررسی کنید."
                "404" in normalized || "not_found" in normalized ||
                    "model" in normalized && "not" in normalized ->
                    "مدل یا سرویس Gemini در پروژه در دسترس نیست (404). فعال بودن Firebase AI Logic و نام مدل انتخاب‌شده را بررسی کنید."
                "api key" in normalized || "api_key" in normalized ||
                    "unauthenticated" in normalized || "401" in normalized ->
                    "احراز هویت درخواست Gemini موفق نشد (401). تنظیمات کلید API مربوط به Firebase و محدودیت‌های آن را بررسی کنید؛ کلید را داخل چت یا مخزن عمومی منتشر نکنید."
                "unable to resolve host" in normalized || "unknownhost" in normalized ||
                    "sockettimeout" in normalized || "network" in normalized ->
                    "اتصال اینترنت برقرار نیست یا دسترسی به سرویس Gemini مسدود شده است. اینترنت و دسترسی شبکه را بررسی کنید."
                else ->
                    "پاسخ آنلاین دریافت نشد. جزئیات فنی: ${diagnosticDetails.ifBlank { e.localizedMessage ?: e.javaClass.simpleName }.take(350)}"
            }
            OnlineAiResult.Error(errorMessage = userFacingError)
        }
    }
}
