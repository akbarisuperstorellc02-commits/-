package com.example.data

import android.content.Context
import java.io.File

/** Returns the verified model downloaded by the in-app model manager. */
internal object GemmaModelInstaller {
    fun ensureInstalled(context: Context): File =
        GemmaModelDownloader.verifiedModelFile(context)
}
