package com.bh.personalacademy.content

import com.example.academy.domain.model.AcademyLocale
import com.example.academy.domain.model.Wallet
import com.example.academy.domain.model.LearningBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyGameCatalogTest {
    @Test
    fun exposesTwentySixUniquePlayableModes() {
        assertEquals(26, AcademyGameCatalog.modes.size)
        assertEquals(26, AcademyGameCatalog.modes.map { it.id }.toSet().size)
    }

    @Test
    fun everyModeProducesFiveQuestionReadyPrompts() {
        AcademyGameCatalog.modes.forEach { mode ->
            repeat(5) { round ->
                val question = AcademyGameCatalog.questionFor(mode, round)
                assertTrue(mode.id, question.prompt.forLocale(AcademyLocale.ENGLISH_US).isNotBlank())
                assertEquals(mode.id, 3, question.options.size)
                assertTrue(mode.id, question.correctIndex in question.options.indices)
                assertFalse(mode.id, question.answer.isBlank())
                assertFalse(mode.id, question.visualEmoji.isBlank())
                assertNotNull(question.hint)
                assertFalse(mode.id, question.explanation.forLocale(AcademyLocale.DARI_AF).isBlank())
            }
        }
    }

    @Test
    fun missingWordPromptDoesNotRevealTheAnswer() {
        val mode = AcademyGameCatalog.modes.first { it.style == GameStyle.MISSING_WORD }
        repeat(5) { round ->
            val question = AcademyGameCatalog.questionFor(mode, round)
            assertTrue(question.prompt.forLocale(AcademyLocale.ENGLISH_US).contains("____"))
            assertFalse(question.prompt.forLocale(AcademyLocale.ENGLISH_US).contains(question.answer))
        }
    }

    @Test
    fun wordBuildClueContainsAllLettersInAScrambledOrder() {
        val mode = AcademyGameCatalog.modes.first { it.style == GameStyle.WORD_BUILD }
        repeat(5) { round ->
            val question = AcademyGameCatalog.questionFor(mode, round)
            val prompt = question.prompt.forLocale(AcademyLocale.ENGLISH_US)
            val scrambled = prompt.substringAfter(": ").trim()
            assertEquals(question.answer.toCharArray().sorted(), scrambled.toCharArray().sorted())
            assertTrue(scrambled != question.answer)
        }
    }

    @Test
    fun hintWalletSpendsExactlySixCoinsAndRejectsInsufficientBalance() {
        assertEquals(6L, Wallet.HINT_COST_COINS)
        assertEquals(14L, Wallet(coins = 20).spendHint()?.coins)
        assertEquals(null, Wallet(coins = 5).spendHint())
    }

    @Test
    fun allSixLearningBandsHaveRealLessonContent() {
        LearningBand.values().forEach { band ->
            assertTrue(band.name, AcademyLessonCatalog.forBand(band).size >= 6)
            AcademyLessonCatalog.forBand(band).forEach { lesson ->
                assertTrue(lesson.id, lesson.explanation.isNotBlank())
                assertTrue(lesson.id, lesson.words.isNotEmpty())
                assertTrue(lesson.id, lesson.example.isNotBlank())
                assertTrue(lesson.id, lesson.translation.isNotBlank())
            }
        }
    }
}
