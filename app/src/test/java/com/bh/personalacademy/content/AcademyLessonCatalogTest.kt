package com.bh.personalacademy.content

import com.example.academy.domain.model.LearningBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyLessonCatalogTest {
    @Test
    fun providesSixDistinctLessonsForEveryBand() {
        LearningBand.values().forEach { band ->
            val lessons = AcademyLessonCatalog.forBand(band)
            assertEquals(6, lessons.size)
            assertEquals(6, lessons.map { it.id }.distinct().size)
        }
        assertEquals(36, AcademyLessonCatalog.all.size)
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
