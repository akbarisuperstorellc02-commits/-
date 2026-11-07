package com.bh.personalacademy.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bh.personalacademy.data.local.AcademyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = AcademyDatabase.getInstance(context).academyDao().getEnabledReminders()
                reminders.forEach { reminder ->
                    ReminderScheduler.schedule(
                        context = context,
                        id = reminder.reminderId,
                        title = reminder.title,
                        hour = reminder.hour,
                        minute = reminder.minute,
                        repeatDaysMask = reminder.repeatDaysMask
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
