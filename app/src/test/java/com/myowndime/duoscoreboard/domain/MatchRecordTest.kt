package com.myowndime.duoscoreboard.domain

import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.model.MatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchRecordTest {

    @Test
    fun `higher score of player one wins`() {
        assertEquals(MatchResult.PLAYER_ONE, MatchResult.of(11, 9))
    }

    @Test
    fun `higher score of player two wins`() {
        assertEquals(MatchResult.PLAYER_TWO, MatchResult.of(3, 21))
    }

    @Test
    fun `equal scores are a draw`() {
        assertEquals(MatchResult.DRAW, MatchResult.of(7, 7))
        assertEquals(MatchResult.DRAW, MatchResult.of(0, 0))
    }

    @Test
    fun `winner name follows the result`() {
        val record = record(playerOneScore = 5, playerTwoScore = 2)
        assertEquals("Ann", record.winnerName)

        val other = record(playerOneScore = 2, playerTwoScore = 5)
        assertEquals("Ben", other.winnerName)
    }

    @Test
    fun `draw has no winner name`() {
        assertNull(record(playerOneScore = 4, playerTwoScore = 4).winnerName)
    }

    private fun record(playerOneScore: Int, playerTwoScore: Int) = MatchRecord(
        playerOneName = "Ann",
        playerTwoName = "Ben",
        playerOneScore = playerOneScore,
        playerTwoScore = playerTwoScore,
        playedAtEpochMillis = 0L,
    )
}
