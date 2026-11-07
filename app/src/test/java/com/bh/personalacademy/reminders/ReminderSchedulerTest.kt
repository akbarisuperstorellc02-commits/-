package com.bh.personalacademy.reminders

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderSchedulerTest {
    @Test
    fun oneTimeReminderIsAlwaysScheduledInTheFuture() {
        val now = System.currentTimeMillis()
        val next = ReminderScheduler.nextTrigger(0, 0, 0)
        assertTrue(next.timeInMillis > now)
    }

    @Test
    fun dailyReminderIsScheduledForASelectedDayInTheFuture() {
        val now = System.currentTimeMillis()
        val next = ReminderScheduler.nextTrigger(23, 59, 127)
        assertTrue(next.timeInMillis > now)
        val dayBit = 1 shl (next.get(Calendar.DAY_OF_WEEK) - 1)
        assertTrue((127 and dayBit) != 0)
    }
}
