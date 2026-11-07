package com.bh.personalacademy.content

import com.example.academy.domain.model.AcademyLocale
import com.example.academy.domain.model.LearningBand
import org.junit.Assert.*
import org.junit.Test

class AcademyGrammarCatalogTest {
    @Test fun grammarCoversEveryLearningBand() {
        LearningBand.values().forEach { band ->
            assertTrue("Missing grammar for $band", AcademyGrammarCatalog.forBand(band).size >= 3)
        }
        assertEquals(24, AcademyGrammarCatalog.all.size)
        assertEquals(AcademyGrammarCatalog.all.size, AcademyGrammarCatalog.all.map { it.id }.toSet().size)
    }

    @Test fun eachGrammarLessonHasFourLocalesAndValidAssessment() {
        AcademyGrammarCatalog.all.forEach { lesson ->
            listOf(AcademyLocale.ENGLISH_US, AcademyLocale.DARI_AF, AcademyLocale.PASHTO_AF, AcademyLocale.ARABIC).forEach { locale ->
                assertTrue(lesson.title.forLocale(locale).isNotBlank())
                assertTrue(lesson.explanation.forLocale(locale).isNotBlank())
                assertTrue(lesson.translation.forLocale(locale).isNotBlank())
                assertTrue(lesson.question.forLocale(locale).isNotBlank())
            }
            assertTrue(lesson.example.isNotBlank())
            assertEquals(3, lesson.options.size)
            assertTrue(lesson.correctIndex in lesson.options.indices)
        }
    }
}