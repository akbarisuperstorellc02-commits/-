package com.example.data

import android.content.Context
import android.os.StatFs
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/** Reassembles install-time AI-pack segments locally; no network access is used. */
internal object GemmaModelInstaller {
    private const val TAG = "GemmaModelInstaller"
    private const val MODEL_NAME = "gemma-4-E2B-it.litertlm"
    private val PART_NAMES = listOf("gemma4.part00", "gemma4.part01", "gemma4.part02")

    fun ensureInstalled(context: Context): File {
        val manifest = context.assets.open("gemma4.manifest").bufferedReader().use { it.readLines() }
        require(manifest.size >= 2) { "فایل مشخصات مدل ناقص است." }
        val expectedSha = manifest[0].trim().lowercase()
        val expectedBytes = manifest[1].trim().toLongOrNull()
            ?: error("اندازهٔ مدل معتبر نیست.")
        require(expectedSha.matches(Regex("[a-f0-9]{64}"))) { "SHA-256 مدل معتبر نیست." }
        require(expectedBytes > 2_000_000_000L) { "فایل مدل ناقص یا نسخهٔ اشتباهی است." }

        val directory = File(context.filesDir, "offline_ai_models").apply { mkdirs() }
        val model = File(directory, MODEL_NAME)
        val marker = File(directory, "$MODEL_NAME.sha256")
        if (model.isFile && model.length() == expectedBytes &&
            marker.isFile && marker.readText().trim() == expectedSha
        ) return model

        val required = expectedBytes + if (model.exists()) model.length() else 0L
        val available = StatFs(context.filesDir.absolutePath).availableBytes
        require(available > required + 256L * 1024L * 1024L) {
            "فضای خالی کافی برای آماده‌سازی مدل ۲٫۶ گیگابایتی وجود ندارد."
        }

        val temporary = File(directory, "$MODEL_NAME.installing")
        val digest = MessageDigest.getInstance("SHA-256")
        var copied = 0L
        try {
            FileOutputStream(temporary, false).use { output ->
                PART_NAMES.forEach { part ->
                    context.assets.open(part).use { input ->
                        val buffer = ByteArray(1024 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            digest.update(buffer, 0, count)
                            copied += count
                        }
                    }
                }
                output.fd.sync()
            }
            require(copied == expectedBytes) { "اندازهٔ مدل کامل نیست: $copied از $expectedBytes بایت." }
            val actualSha = digest.digest().joinToString("") { "%02x".format(it) }
            require(actualSha == expectedSha) { "بررسی سلامت مدل موفق نبود؛ قطعات با نسخهٔ اصلی مطابقت ندارند." }

            if (model.exists() && !model.delete()) error("نسخهٔ ناقص قبلی مدل حذف نشد.")
            if (!temporary.renameTo(model)) {
                temporary.copyTo(model, overwrite = true)
                temporary.delete()
            }
            marker.writeText(expectedSha)
            Log.i(TAG, "Bundled model verified: " + model.length() + " bytes")
            return model
        } catch (error: Exception) {
            temporary.delete()
            throw error
        }
    }
}
