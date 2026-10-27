package com.myowndime.duoscoreboard.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.data.local.AppDatabase
import com.myowndime.duoscoreboard.data.repository.RoomMatchRepository
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatchRecordDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomMatchRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomMatchRepository(database.matchRecordDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saved match is returned with its fields intact`() = runTest {
        repository.add(record(playerOneScore = 21, playerTwoScore = 18, playedAt = 1_000L))

        val stored = repository.matches.first().single()
        assertEquals("Ann", stored.playerOneName)
        assertEquals("Ben", stored.playerTwoName)
        assertEquals(21, stored.playerOneScore)
        assertEquals(18, stored.playerTwoScore)
        assertEquals(1_000L, stored.playedAtEpochMillis)
    }

    @Test
    fun `set counts round trip through the database`() = runTest {
        repository.add(
            MatchRecord(
                playerOneName = "Ann",
                playerTwoName = "Ben",
                playerOneScore = 7,
                playerTwoScore = 5,
                playedAtEpochMillis = 1_000L,
                playerOneSets = 2,
                playerTwoSets = 1,
            ),
        )

        val stored = repository.matches.first().single()
        assertEquals(2, stored.playerOneSets)
        assertEquals(1, stored.playerTwoSets)
        assertTrue(stored.hasSets)
        assertEquals("Ann", stored.winnerName)
    }

    @Test
    fun `matches are ordered newest first`() = runTest {
        repository.add(record(playedAt = 1_000L))
        repository.add(record(playedAt = 3_000L))
        repository.add(record(playedAt = 2_000L))

        val timestamps = repository.matches.first().map { it.playedAtEpochMillis }
        assertEquals(listOf(3_000L, 2_000L, 1_000L), timestamps)
    }

    @Test
    fun `deleting one match keeps the others`() = runTest {
        val keptId = repository.add(record(playedAt = 1_000L))
        val removedId = repository.add(record(playedAt = 2_000L))

        repository.delete(removedId)

        val remaining = repository.matches.first()
        assertEquals(1, remaining.size)
        assertEquals(keptId, remaining.single().id)
    }

    @Test
    fun `clear removes every match`() = runTest {
        repository.add(record(playedAt = 1_000L))
        repository.add(record(playedAt = 2_000L))

        repository.clear()

        assertTrue(repository.matches.first().isEmpty())
    }

    private fun record(
        playerOneScore: Int = 1,
        playerTwoScore: Int = 0,
        playedAt: Long,
    ) = MatchRecord(
        playerOneName = "Ann",
        playerTwoName = "Ben",
        playerOneScore = playerOneScore,
        playerTwoScore = playerTwoScore,
        playedAtEpochMillis = playedAt,
    )
}
