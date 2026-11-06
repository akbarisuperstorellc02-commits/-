package com.example.data

import android.util.Log
import com.example.model.ChatMessage
import com.google.firebase.functions.FirebaseFunctions
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val TAG = "OnlineAITutorService"

sealed interface OnlineAiResult {
    data class Success(val message: ChatMessage) : OnlineAiResult
    data class Error(val errorMessage: String) : OnlineAiResult
}

/**
 * Online-only tutor client. Gemini credentials remain in Firebase Secret Manager;
 * the Android app can invoke only the App-Check-protected callable function.
 */
class OnlineAITutorService {
    private val functions by lazy { FirebaseFunctions.getInstance("us-central1") }

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
            promptBuilder.append(
                "Personalization: Address the learner by the exact name '$learnerName'. " +
                    "Never call the learner Zahra or use another person's name. " +
                    "Keep the lesson continuous with the learner's level and recent conversation.\n\n"
            )

            conversationHistory.takeLast(4).forEach { msg ->
                val role = if (msg.isFromTutor) "Tutor" else "Learner"
                promptBuilder.append("$role: ${msg.englishText}\n")
            }
            promptBuilder.append("\nLearner's new message:\n$userMessage\n\n")
            promptBuilder.append("Respond as Master Gohari and guide $learnerName.")

            val task = functions
                .getHttpsCallable("generateTutorResponse")
                .call(mapOf("prompt" to promptBuilder.toString()))

            val result = Tasks.await(task, 90, TimeUnit.SECONDS)
            val data = result.data as? Map<*, *>
            val responseText = data?.get("text") as? String

            if (responseText.isNullOrBlank()) {
                OnlineAiResult.Error("پاسخ خالی از هوش مصنوعی آنلاین دریافت شد؛ پاسخ آماده‌ای نمایش داده نمی‌شود.")
            } else {
                val now = System.currentTimeMillis()
                OnlineAiResult.Success(
                    ChatMessage(
                        id = "online_$now",
                        englishText = responseText.trim(),
                        isFromTutor = true,
                        timestampMillis = now
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: TimeoutException) {
            Log.e(TAG, "Online Gemini request timed out", e)
            OnlineAiResult.Error("زمان پاسخگویی هوش مصنوعی آنلاین به پایان رسید. لطفاً دوباره تلاش کنید.")
        } catch (e: Exception) {
            val details = generateSequence(e as Throwable?) { it.cause }
                .mapNotNull { it.message }
                .distinct()
                .joinToString(" | ")
            Log.e(TAG, "Secure online AI request failed: $details", e)
            val normalized = details.lowercase()
            val message = when {
                "app check" in normalized || "failed_precondition" in normalized ||
                    "permission_denied" in normalized || "403" in normalized ->
                    "دسترسی امن به سرور هوش مصنوعی رد شد. ثبت بودن توکن App Check نسخهٔ Debug و فعال بودن App Check را بررسی کنید."
                "resource-exhausted" in normalized || "429" in normalized || "quota" in normalized ->
                    "محدودیت یا سهمیهٔ درخواست‌های Gemini پر شده است. سهمیهٔ API را بررسی کنید."
                "unauthenticated" in normalized || "401" in normalized ->
                    "احراز هویت درخواست آنلاین موفق نشد. تنظیمات Firebase را بررسی کنید."
                "unavailable" in normalized || "network" in normalized ||
                    "unable to resolve host" in normalized ->
                    "سرور یا اینترنت در دسترس نیست. اتصال اینترنت را بررسی و دوباره تلاش کنید."
                else ->
                    "پاسخ آنلاین دریافت نشد. جزئیات فنی: ${details.ifBlank { e.localizedMessage ?: e.javaClass.simpleName }.take(300)}"
            }
            OnlineAiResult.Error(message)
        }
    }
}
