package com.bh.personalacademy.content

import com.example.academy.domain.model.LearningBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyLessonCatalogTest {
    @Test
    fun providesAtLeastSixDistinctLessonsForEveryBand() {
        LearningBand.values().forEach { band ->
            val lessons = AcademyLessonCatalog.forBand(band)
            assertTrue(lessons.size >= 6)
            assertEquals(lessons.size, lessons.map { it.id }.distinct().size)
        }
        assertTrue(AcademyLessonCatalog.all.size >= 70)
    }

    @Test
    fun animalLessonContainsFifteenMultilingualAnimalNames() {
        val lesson = AcademyLessonCatalog.all.single { it.id == "pre_a1_animals" }
        assertEquals(15, lesson.words.size)
        assertTrue(lesson.words.all { it.english.isNotBlank() && it.dari.isNotBlank() && it.pashto.isNotBlank() && it.arabic.isNotBlank() && it.pronunciation.isNotBlank() && it.emoji.isNotBlank() })
        assertEquals("سپی", lesson.words.first { it.english == "dog" }.meaningFor(com.example.academy.domain.model.AcademyLocale.PASHTO_AF))
        assertEquals("قطة", lesson.words.first { it.english == "cat" }.meaningFor(com.example.academy.domain.model.AcademyLocale.ARABIC))
    }

    @Test
    fun animalLessonContainsFifteenMultilingualAnimalNames() {
        val lesson = AcademyLessonCatalog.all.single { it.id == "pre_a1_animals" }
        assertEquals(15, lesson.words.size)
        assertTrue(lesson.words.all { it.english.isNotBlank() && it.dari.isNotBlank() && it.pashto.isNotBlank() && it.arabic.isNotBlank() && it.pronunciation.isNotBlank() && it.emoji.isNotBlank() })
        assertEquals("سپی", lesson.words.first { it.english == "dog" }.meaningFor(com.example.academy.domain.model.AcademyLocale.PASHTO_AF))
        assertEquals("قطة", lesson.words.first { it.english == "cat" }.meaningFor(com.example.academy.domain.model.AcademyLocale.ARABIC))
    }

    @Test
    fun nextLessonUnlocksWhenImmediatelyPreviousLessonIsCompleted() {
        val band = LearningBand.PRE_A1
        val lessons = AcademyLessonCatalog.forBand(band)
        assertTrue(!AcademyLessonCatalog.isLessonUnlocked(band, 1, emptySet()))
        assertTrue(AcademyLessonCatalog.isLessonUnlocked(band, 1, setOf(lessons[0].id)))
        assertTrue(!AcademyLessonCatalog.isLessonUnlocked(band, 2, setOf(lessons[1].id)))
        assertTrue(AcademyLessonCatalog.isLessonUnlocked(band, 2, setOf(lessons[0].id, lessons[1].id)))
    }

    @Test
    fun nextBandUnlocksWhenPreviousBandFinalLessonIsCompleted() {
        val finalLesson = AcademyLessonCatalog.forBand(LearningBand.PRE_A1).last()
        assertTrue(!AcademyLessonCatalog.isBandUnlocked(LearningBand.A1, emptySet()))
        assertTrue(!AcademyLessonCatalog.isBandUnlocked(LearningBand.A1, setOf(finalLesson.id)))
        val allPrevious = AcademyLessonCatalog.forBand(LearningBand.PRE_A1).map { it.id }.toSet()
        assertTrue(AcademyLessonCatalog.isBandUnlocked(LearningBand.A1, allPrevious))
    }

    @Test
    fun everyLessonHasLearningMaterialAndVocabulary() {
        assertTrue(AcademyLessonCatalog.all.all { lesson ->
            lesson.objective.isNotBlank() &&
                lesson.explanation.isNotBlank() &&
                lesson.words.size >= 3 &&
                lesson.example.isNotBlank() &&
                lesson.translation.isNotBlank()
        })
    }
}
