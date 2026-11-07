package com.bh.personalacademy.content

import com.example.academy.domain.model.AcademyLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyStoryCatalogTest {
    @Test fun storyIdsAreUniqueAndCatalogIsPopulated() {
        val stories = AcademyStoryCatalog.all
        assertEquals(16, stories.size)
        assertEquals(stories.size, stories.map { it.id }.toSet().size)
    }

    @Test fun everyStoryHasReadableContentAndAValidQuestion() {
        AcademyStoryCatalog.all.forEach { story ->
            assertTrue(story.englishText.isNotBlank())
            assertTrue(story.translation.forLocale(AcademyLocale.DARI_AF).isNotBlank())
            assertTrue(story.translation.forLocale(AcademyLocale.PASHTO_AF).isNotBlank())
            assertTrue(story.translation.forLocale(AcademyLocale.ARABIC).isNotBlank())
            assertTrue(story.translation.forLocale(AcademyLocale.ENGLISH_US).isNotBlank())
            assertTrue(story.words.isNotEmpty())
            assertTrue(story.options.size >= 2)
            assertTrue(story.correctIndex in story.options.indices)
            assertFalse(story.question.en.isBlank())
        }
    }
}
