package com.example

import android.content.Context
import com.example.data.UserProgressManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("خودسازی", appName)
  }
  @Test
  fun `coin hints charge six coins and reject insufficient balance`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("zahra_self_dev_prefs", Context.MODE_PRIVATE)
      .edit().clear().commit()
    val manager = UserProgressManager(context)

    assertTrue(manager.spendGoldCoin(6))
    assertEquals(174, manager.goldCoins.value)
    assertFalse(manager.spendGoldCoin(1000))
    assertEquals(174, manager.goldCoins.value)
  }

  @Test
  fun `daily goal is clamped and study time starts at zero`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("zahra_self_dev_prefs", Context.MODE_PRIVATE)
      .edit().clear().commit()
    val manager = UserProgressManager(context)

    assertEquals(0, manager.todayStudySeconds.value)
    assertEquals(0, manager.totalStudyHours.value)
    manager.setDailyGoal(0)
    assertEquals(5, manager.dailyGoalMinutes.value)
    manager.setDailyGoal(900)
    assertEquals(600, manager.dailyGoalMinutes.value)
    manager.recordStudySeconds(75)
    assertEquals(75, manager.todayStudySeconds.value)
    assertEquals(1, manager.todayStudyMinutes.value)
  }

  @Test
  fun `renaming learner personalizes curriculum immediately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("zahra_self_dev_prefs", Context.MODE_PRIVATE)
      .edit().clear().commit()
    val manager = UserProgressManager(context)

    manager.updateLearnerName("Ali")

    assertEquals("Ali", manager.learnerName.value)
    assertTrue(manager.levelsState.value.first().items.first().englishText.contains("Ali"))
    assertFalse(manager.levelsState.value.first().items.first().englishText.contains("Zahra"))
  }
}
