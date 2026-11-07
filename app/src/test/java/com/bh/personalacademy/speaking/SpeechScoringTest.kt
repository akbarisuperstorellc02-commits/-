package com.bh.personalacademy.speaking

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechScoringTest {
    @Test fun exactTranscriptGetsFullScore() {
        assertEquals(100, SpeechScoring.accuracy("Hello, how are you?", "hello how are you"))
    }

    @Test fun missingAndWrongWordsReduceScore() {
        val score = SpeechScoring.accuracy("I go to school every morning", "I go school")
        assertEquals(true, score in 1..99)
    }

    @Test fun emptyTranscriptGetsZero() {
        assertEquals(0, SpeechScoring.accuracy("Practice every day", ""))
    }

    @Test fun emptyTargetGetsZero() {
        assertEquals(0, SpeechScoring.accuracy("", "hello"))
    }
}
