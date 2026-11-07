package com.example

import com.example.data.CurriculumData
import com.example.data.calculateStudyTimeProgress
import com.example.util.SpeechEvaluator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun studyMinutesAccumulateAcrossShortSessions() {
        val afterFirstSession = calculateStudyTimeProgress(14, 0, 25)
        assertEquals(14, afterFirstSession.totalHours)
        assertEquals(25, afterFirstSession.remainingMinutes)

        val afterSecondSession = calculateStudyTimeProgress(
            afterFirstSession.totalHours,
            afterFirstSession.remainingMinutes,
            35
        )
        assertEquals(15, afterSecondSession.totalHours)
        assertEquals(0, afterSecondSession.remainingMinutes)
    }

    @Test
    fun invalidStudyMinutesDoNotReduceProgress() {
        val progress = calculateStudyTimeProgress(3, 20, -15)
        assertEquals(3, progress.totalHours)
        assertEquals(20, progress.remainingMinutes)
    }

    @Test
    fun testZahraAcademyLevelsAndClassrooms() {
        // 30 extended levels from A1 all the way to Z9
        assertEquals(30, CurriculumData.levels.size)
        assertEquals("A1", CurriculumData.levels.first().levelCode)
        assertEquals("Z9", CurriculumData.levels.last().levelCode)

        CurriculumData.levels.forEach { level ->
            assertTrue("Level ${level.levelCode} should have lesson items", level.items.isNotEmpty())
            assertTrue("Level ${level.levelCode} should have exam questions", level.examQuestions.isNotEmpty())
        }
        assertTrue("Golden medals should be configured", CurriculumData.goldenMedals.isNotEmpty())
        assertEquals("Should have 30 continuous word game stages", 30, CurriculumData.wordGameStages.size)
    }

    @Test
    fun testSpeechEvaluatorSloganMatch() {
        val target = "Start from zero, continue until absolute mastery!"
        val spoken = "Start from zero continue until absolute mastery"
        val result = SpeechEvaluator.evaluate(spoken, target)
        assertTrue("Score should be high for slogan match", result.scorePercent >= 90)
        assertEquals(3, result.stars)
    }

    @Test
    fun expandedCurriculumAddsLessonsAndPracticeToEveryStage() {
        val expanded = com.example.data.expandLearningCurriculum(CurriculumData.levels)
        assertEquals(30, expanded.size)
        expanded.forEach { level ->
            val original = CurriculumData.levels.first { it.id == level.id }
            assertEquals(original.items.size + 2, level.items.size)
            assertEquals(original.examQuestions.size + 4, level.examQuestions.size)
            assertTrue(level.items.all { it.levelCode == level.levelCode })
        }
    }

}
