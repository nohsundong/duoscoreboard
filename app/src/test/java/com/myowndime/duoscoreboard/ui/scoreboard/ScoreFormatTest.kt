package com.myowndime.duoscoreboard.ui.scoreboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreFormatTest {

    @Test
    fun `plain scores are not padded`() {
        assertEquals("0", formatScore(0, padded = false))
        assertEquals("7", formatScore(7, padded = false))
        assertEquals("999", formatScore(999, padded = false))
    }

    @Test
    fun `padded scores always have three digits`() {
        assertEquals("000", formatScore(0, padded = true))
        assertEquals("007", formatScore(7, padded = true))
        assertEquals("042", formatScore(42, padded = true))
        assertEquals("999", formatScore(999, padded = true))
    }
}
