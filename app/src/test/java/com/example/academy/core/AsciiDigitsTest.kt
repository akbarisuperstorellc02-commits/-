package com.example.academy.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AsciiDigitsTest {
    @Test
    fun convertsPersianDigitsToAscii() {
        assertEquals("Level 123", AsciiDigits.normalize("Level ۱۲۳"))
    }

    @Test
    fun convertsArabicIndicDigitsToAscii() {
        assertEquals("Score 456", AsciiDigits.normalize("Score ٤٥٦"))
    }

    @Test
    fun leavesAsciiDigitsUnchanged() {
        assertEquals("0-9", AsciiDigits.normalize("0-9"))
    }
}
