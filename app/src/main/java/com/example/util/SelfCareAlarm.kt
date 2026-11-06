package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

object SelfCareAlarm {
    private const val PREFS = "self_care_alarm"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val REQUEST_CODE = 7319
    private const val CHANNEL_ID = "self_care_reminders"

    fun isEnabled(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_ENABLED, false)

    fun getTime(context: Context): Pair<Int, Int> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getInt(KEY_HOUR, 7) to p.getInt(KEY_MINUTE, 0)
    }

    fun set(context: Context, hour: Int, minute: Int, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    fun schedule(context: Context) {
        if (!isEnabled(context)) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return
        val (hour, minute) = getTime(context)
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent(context))
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, SelfCareAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "یادآورهای خودسازی", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "یادآوری‌های روزانه برای درس و رشد فردی"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("وقت خودسازی است 🌸")
            .setContentText("زمان انجام برنامه‌ای است که برای خودت تعیین کرده‌ای.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()
        try { manager.notify(REQUEST_CODE, notification) } catch (_: SecurityException) { }
    }
}

class SelfCareAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            SelfCareAlarm.schedule(context)
        } else {
            SelfCareAlarm.showNotification(context)
            SelfCareAlarm.schedule(context)
        }
    }
}
