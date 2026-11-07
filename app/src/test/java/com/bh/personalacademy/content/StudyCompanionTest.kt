package com.bh.personalacademy.content

import org.junit.Assert.assertEquals
import org.junit.Test

class StudyCompanionTest {
    @Test
    fun companionChangesAtExpectedStudyMilestones() {
        assertEquals("🌱", StudyCompanion.emojiForMinutes(0))
        assertEquals("🐣", StudyCompanion.emojiForMinutes(1))
        assertEquals("🐣", StudyCompanion.emojiForMinutes(14))
        assertEquals("🐝", StudyCompanion.emojiForMinutes(15))
        assertEquals("🐝", StudyCompanion.emojiForMinutes(29))
        assertEquals("🦊", StudyCompanion.emojiForMinutes(30))
        assertEquals("🦊", StudyCompanion.emojiForMinutes(59))
        assertEquals("🐢", StudyCompanion.emojiForMinutes(60))
        assertEquals("🐢", StudyCompanion.emojiForMinutes(120))
    }

    @Test
    fun negativeStudyTimeUsesTheEmptyStateCompanion() {
        assertEquals("🌱", StudyCompanion.emojiForMinutes(-1))
    }
}
