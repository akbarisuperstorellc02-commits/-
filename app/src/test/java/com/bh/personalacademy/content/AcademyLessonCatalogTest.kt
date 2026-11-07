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
        assertTrue(AcademyLessonCatalog.all.size >= 36)
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
