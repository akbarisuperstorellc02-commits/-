package com.example.data

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import java.io.File
import java.security.MessageDigest

internal data class GemmaDownloadProgress(
    val state: State,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val message: String? = null
) {
    enum class State { NOT_DOWNLOADED, QUEUED, DOWNLOADING, PAUSED, VERIFYING, READY, FAILED }
    val percent: Int
        get() = if (totalBytes > 0L) ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100) else 0
}

/**
 * Resumable in-app model download. Android DownloadManager owns the transfer and notification,
 * so it can continue when the chat screen is closed or the app goes to the background.
 */
internal object GemmaModelDownloader {
    private const val MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private const val MODEL_FILE = "gemma-4-E2B-it.litertlm"
    private const val PREFS = "gemma_model_download"
    private const val KEY_ID = "download_id"
    private const val KEY_VERIFIED = "verified_sha256"

    private data class Manifest(val sha256: String, val bytes: Long)

    fun enqueue(context: Context): Long {
        val app = context.applicationContext
        val existing = status(app)
        if (existing.state == GemmaDownloadProgress.State.DOWNLOADING ||
            existing.state == GemmaDownloadProgress.State.QUEUED ||
            existing.state == GemmaDownloadProgress.State.PAUSED
        ) return app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ID, -1L)

        val request = DownloadManager.Request(Uri.parse(MODEL_URL))
            .setTitle("مدل هوش مصنوعی آکادمی BH")
            .setDescription("دانلود مدل Gemma 4 E2B برای اجرای کاملاً آفلاین")
            .setMimeType("application/octet-stream")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(app, Environment.DIRECTORY_DOWNLOADS, MODEL_FILE)

        val id = manager(app).enqueue(request)
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_ID, id).remove(KEY_VERIFIED).apply()
        return id
    }

    fun status(context: Context): GemmaDownloadProgress {
        val app = context.applicationContext
        val model = modelFile(app)
        val manifest = runCatching { readManifest(app) }.getOrNull()
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (model.isFile && manifest != null &&
            model.length() == manifest.bytes &&
            prefs.getString(KEY_VERIFIED, null) == manifest.sha256
        ) return GemmaDownloadProgress(GemmaDownloadProgress.State.READY, model.length(), manifest.bytes)

        val id = prefs.getLong(KEY_ID, -1L)
        if (id < 0L) return GemmaDownloadProgress(GemmaDownloadProgress.State.NOT_DOWNLOADED)

        var cursor: Cursor? = null
        return try {
            cursor = manager(app).query(DownloadManager.Query().setFilterById(id))
            if (cursor == null || !cursor.moveToFirst()) {
                GemmaDownloadProgress(GemmaDownloadProgress.State.NOT_DOWNLOADED)
            } else {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                when (status) {
                    DownloadManager.STATUS_PENDING -> GemmaDownloadProgress(GemmaDownloadProgress.State.QUEUED, downloaded, total)
                    DownloadManager.STATUS_RUNNING -> GemmaDownloadProgress(GemmaDownloadProgress.State.DOWNLOADING, downloaded, total)
                    DownloadManager.STATUS_PAUSED -> GemmaDownloadProgress(GemmaDownloadProgress.State.PAUSED, downloaded, total,
                        "دانلود موقتاً متوقف شده؛ پس از اتصال دوباره ادامه می‌یابد.")
                    DownloadManager.STATUS_SUCCESSFUL -> GemmaDownloadProgress(GemmaDownloadProgress.State.VERIFYING, downloaded, total,
                        "دانلود تمام شد؛ در حال بررسی سلامت فایل هستیم.")
                    DownloadManager.STATUS_FAILED -> GemmaDownloadProgress(GemmaDownloadProgress.State.FAILED, downloaded, total,
                        "دانلود ناموفق بود. اتصال اینترنت را بررسی و دوباره تلاش کنید.")
                    else -> GemmaDownloadProgress(GemmaDownloadProgress.State.NOT_DOWNLOADED)
                }
            }
        } catch (_: Exception) {
            GemmaDownloadProgress(GemmaDownloadProgress.State.NOT_DOWNLOADED)
        } finally {
            cursor?.close()
        }
    }

    /** Call off the main thread after DownloadManager reports SUCCESSFUL. */
    fun verifyCompleted(context: Context): Boolean {
        val app = context.applicationContext
        val id = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ID, -1L)
        if (id < 0L) return false
        val query = manager(app).query(DownloadManager.Query().setFilterById(id)) ?: return false
        val completed = query.use {
            it.moveToFirst() && it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                DownloadManager.STATUS_SUCCESSFUL
        }
        if (!completed) return false

        val expected = readManifest(app)
        val file = modelFile(app)
        if (!file.isFile || file.length() != expected.bytes) {
            file.delete()
            return false
        }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (!actual.equals(expected.sha256, ignoreCase = true)) {
            file.delete()
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_VERIFIED).apply()
            return false
        }
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_VERIFIED, expected.sha256).apply()
        return true
    }

    fun verifiedModelFile(context: Context): File {
        val app = context.applicationContext
        val manifest = readManifest(app)
        val file = modelFile(app)
        val verified = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_VERIFIED, null)
        require(file.isFile && file.length() == manifest.bytes && verified == manifest.sha256) {
            "مدل هنوز دانلود و بررسی نشده است. ابتدا آن را از داخل برنامه دانلود کنید."
        }
        return file
    }

    private fun readManifest(context: Context): Manifest {
        val lines = context.assets.open("gemma4.manifest").bufferedReader().use { it.readLines() }
        require(lines.size >= 2) { "مشخصات مدل در نسخهٔ برنامه موجود نیست." }
        val sha = lines[0].trim().lowercase()
        val bytes = lines[1].trim().toLongOrNull() ?: error("اندازهٔ مدل معتبر نیست.")
        require(sha.matches(Regex("[a-f0-9]{64}")) && bytes > 2_000_000_000L) {
            "مشخصات مدل معتبر نیست."
        }
        return Manifest(sha, bytes)
    }

    private fun modelFile(context: Context): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, MODEL_FILE)

    private fun manager(context: Context): DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
}
