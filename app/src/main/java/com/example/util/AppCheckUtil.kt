package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

/**
 * Install App Check before any Firebase feature is used.
 * Debug builds use the local debug provider; release builds use Android Play Integrity.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        val appCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrBlank()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
                Log.d(TAG, "App Check debug token supplied through launch intent.")
            } else {
                Log.i(TAG, "Using the Firebase App Check debug provider; register its Logcat token in Firebase Console.")
            }
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        Log.i(TAG, "Firebase App Check provider installed.")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}
