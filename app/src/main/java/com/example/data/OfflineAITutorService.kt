package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ChatMessage
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "OfflineAITutor"

sealed interface OfflineAiResult {
    data class Success(val message: ChatMessage) : OfflineAiResult
    data class Error(val errorMessage: String) : OfflineAiResult
}

/** Local-only tutor: no Firebase, API key, HTTP request, or canned fallback. */
class OfflineAITutorService(context: Context) {
    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "master-gohari-lm").apply { isDaemon = true }
    }
    private val dispatcher = executor.asCoroutineDispatcher()
    private val mutex = Mutex()
    private val closed = AtomicBoolean(false)
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    suspend fun initialize(): String? = withContext(dispatcher) {
        mutex.withLock {
            if (closed.get()) return@withLock "سرویس هوش مصنوعی بسته شده است."
            if (conversation != null) return@withLock null
            try {
                val modelFile = GemmaModelInstaller.ensureInstalled(appContext)
                val cachePath = appContext.cacheDir.absolutePath
                val loadedEngine = try {
                    Engine(EngineConfig(modelPath = modelFile.absolutePath, backend = Backend.GPU(), cacheDir = cachePath))
                        .also { it.initialize() }
                } catch (gpuError: Exception) {
                    Log.w(TAG, "GPU initialization failed; retrying on CPU", gpuError)
                    Engine(EngineConfig(modelPath = modelFile.absolutePath, backend = Backend.CPU(), cacheDir = cachePath))
                        .also { it.initialize() }
                }
                engine = loadedEngine
                conversation = loadedEngine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(SYSTEM_INSTRUCTION),
                        samplerConfig = SamplerConfig(topK = 40, topP = 0.9, temperature = 0.7)
                    )
                )
                null
            } catch (error: Exception) {
                Log.e(TAG, "Offline model initialization failed", error)
                "مدل آفلاین آماده نشد: " + (error.localizedMessage ?: error.javaClass.simpleName)
            }
        }
    }

    suspend fun sendPracticeMessage(
        userMessage: String,
        learnerName: String,
        currentLevelCode: String,
        conversationHistory: List<ChatMessage>
    ): OfflineAiResult {
        val cleanMessage = userMessage.trim()
        if (cleanMessage.isBlank()) return OfflineAiResult.Error("لطفاً پیام خود را بنویسید.")
        val initError = initialize()
        if (initError != null) return OfflineAiResult.Error(initError)

        return withContext(dispatcher) {
            try {
                val prompt = """
                    نام زبان‌آموز: $learnerName
                    سطح فعلی انگلیسی: $currentLevelCode
                    پیام تازه زبان‌آموز:
                    $cleanMessage
                    پاسخ را با توجه به گفت‌وگوی جاری و سطح زبان‌آموز تولید کن.
                """.trimIndent()
                val output = StringBuilder()
                checkNotNull(conversation).sendMessageAsync(prompt).collect { chunk ->
                    output.append(chunk.toString())
                }
                val generated = output.toString().trim()
                if (generated.isBlank()) {
                    OfflineAiResult.Error("مدل آفلاین پاسخ متنی تولید نکرد. دوباره تلاش کنید.")
                } else {
                    OfflineAiResult.Success(parseTutorMessage(generated))
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "Local inference failed", error)
                OfflineAiResult.Error("تولید پاسخ روی گوشی با خطا روبه‌رو شد: " +
                    (error.localizedMessage ?: error.javaClass.simpleName))
            }
        }
    }

    private fun parseTutorMessage(raw: String): ChatMessage {
        fun section(name: String, next: String): String? {
            val regex = Regex("(?is)\\[$name\\]\\s*(.*?)(?=\\[(?:$next)\\]|$)")
            return regex.find(raw)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
        val english = section("ENGLISH", "EXPLANATION|TIP|PRACTICE")
        val explanation = section("EXPLANATION", "TIP|PRACTICE")
        val tip = section("TIP", "PRACTICE")
        val practice = section("PRACTICE", "NEVER_MATCH_THIS_TAG")
        return ChatMessage(
            id = "offline_" + UUID.randomUUID(),
            isFromTutor = true,
            englishText = english ?: raw,
            persianTranslation = explanation,
            grammarTipFa = tip,
            quickReplies = practice?.let { listOf(it) } ?: emptyList()
        )
    }

    fun closeAsync() {
        if (!closed.compareAndSet(false, true)) return
        executor.execute {
            runCatching { conversation?.close() }
            conversation = null
            runCatching { engine?.close() }
            engine = null
            dispatcher.close()
        }
    }

    private companion object {
        val SYSTEM_INSTRUCTION = """
            You are استاد گوهری (Master Gohari), a capable, patient and professional private
            English tutor and self-development mentor for آکادمی اختصاصی BH.
            Generate a fresh, thoughtful answer to every message. Never use canned replies,
            never claim to be online, and never invent progress or achievements.
            Address the learner using the exact current learner name supplied in the prompt.
            Adapt explanations to the supplied CEFR level and teach step by step.
            The app supports Afghan Dari, Pashto, Arabic and English. Detect the learner's
            language and write the explanation in that same language. For English lessons,
            include a natural English example and a clear explanation in the learner's language.
            Correct mistakes kindly, explain the rule, and give one short practice task.
            For vocabulary include meaning, pronunciation guidance and an example.
            For self-development give realistic, safe, actionable advice.
            Format every answer using exactly these four section markers:
            [ENGLISH]
            A concise English response or example.
            [EXPLANATION]
            The useful explanation in the learner's detected language.
            [TIP]
            One concise grammar, vocabulary or practical learning tip.
            [PRACTICE]
            One short question or exercise the learner can answer next.
            Do not add markdown code fences. Do not fabricate facts. Be warm, clear and useful.
        """.trimIndent()
    }
}
