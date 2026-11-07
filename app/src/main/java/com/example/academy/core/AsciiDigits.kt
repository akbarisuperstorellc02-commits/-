package com.example.academy.core

/**
 * Converts Persian and Arabic-Indic numerals to ASCII digits.
 * Apply to every numeric string rendered in the UI for consistent 0-9 output.
 */
object AsciiDigits {
    fun normalize(value: String): String = buildString(value.length) {
        value.forEach { char ->
            val normalized = when (char) {
                in '\u06F0'..'\u06F9' -> ('0'.code + (char.code - '\u06F0'.code)).toChar()
                in '\u0660'..'\u0669' -> ('0'.code + (char.code - '\u0660'.code)).toChar()
                else -> char
            }
            append(normalized)
        }
    }
}
