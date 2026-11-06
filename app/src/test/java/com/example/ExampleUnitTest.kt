package com.example

import com.example.data.CurriculumData
import com.example.util.SpeechEvaluator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAcademyLevelsAndWordGameStages() {
        assertEquals(30, CurriculumData.levels.size)
        assertEquals("A1", CurriculumData.levels.first().levelCode)
        assertEquals("Z9", CurriculumData.levels.last().levelCode)

        CurriculumData.levels.forEach { level ->
            assertTrue("Level ${level.levelCode} should have lesson items", level.items.isNotEmpty())
            assertTrue("Level ${level.levelCode} should have exam questions", level.examQuestions.isNotEmpty())
        }
        assertTrue("Golden medals should be configured", CurriculumData.goldenMedals.isNotEmpty())

        assertEquals("The expanded word game should contain 60 stages", 60, CurriculumData.wordGameStages.size)
        assertEquals(1, CurriculumData.wordGameStages.first().stageNumber)
        assertEquals(60, CurriculumData.wordGameStages.last().stageNumber)
        CurriculumData.wordGameStages.forEachIndexed { index, stage ->
            assertEquals("Word game stages should be continuous", index + 1, stage.stageNumber)
            assertTrue("Stage ${stage.stageNumber} should have a target word", stage.targetWordEn.isNotBlank())
            assertTrue("Stage ${stage.stageNumber} should have a hint", stage.hintFa.isNotBlank())
            assertEquals("Jumbled letters should match the target word", stage.targetWordEn.lowercase().toList().sorted(), stage.jumbledLetters.map { it.lowercaseChar() }.sorted())
        }
    }

    @Test
    fun testSpeechEvaluatorSloganMatch() {
        val target = "Start from zero, continue until absolute mastery!"
        val spoken = "Start from zero continue until absolute mastery"
        val result = SpeechEvaluator.evaluate(spoken, target)
        assertTrue("Score should be high for slogan match", result.scorePercent >= 90)
        assertEquals(3, result.stars)
    }
}
