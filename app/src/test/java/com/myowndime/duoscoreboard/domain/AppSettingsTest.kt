package com.myowndime.duoscoreboard.domain

import com.myowndime.duoscoreboard.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `manual match format needs no sets`() {
        assertEquals(0, AppSettings.setsToWinMatch(AppSettings.MATCH_FORMAT_MANUAL))
    }

    @Test
    fun `a single set match is won by one set`() {
        assertEquals(1, AppSettings.setsToWinMatch(1))
    }

    @Test
    fun `best-of-N is won by the majority of sets`() {
        assertEquals(2, AppSettings.setsToWinMatch(3))
        assertEquals(3, AppSettings.setsToWinMatch(5))
        assertEquals(4, AppSettings.setsToWinMatch(7))
    }
}
