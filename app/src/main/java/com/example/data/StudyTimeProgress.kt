package com.example.data

/**
 * Immutable study-time total after adding one session.
 * Minutes are normalized to the range 0..59 and negative session values
 * are ignored so invalid input can never reduce a learner's progress.
 */
data class StudyTimeProgress(
    val totalHours: Int,
    val remainingMinutes: Int
)

/**
 * Adds a study session to an existing hours/minutes total.
 *
 * @param currentHours previously completed whole hours
 * @param currentRemainingMinutes previously accumulated minutes
 * @param sessionMinutes minutes in the new study session
 */
fun calculateStudyTimeProgress(
    currentHours: Int,
    currentRemainingMinutes: Int,
    sessionMinutes: Int
): StudyTimeProgress {
    val safeHours = currentHours.coerceAtLeast(0)
    val safeRemainder = currentRemainingMinutes.coerceIn(0, 59)
    val safeSession = sessionMinutes.coerceAtLeast(0)

    val totalMinutes = safeHours.toLong() * 60L + safeRemainder + safeSession
    val boundedTotal = totalMinutes.coerceAtMost(Int.MAX_VALUE.toLong())

    return StudyTimeProgress(
        totalHours = (boundedTotal / 60L).toInt(),
        remainingMinutes = (boundedTotal % 60L).toInt()
    )
}
