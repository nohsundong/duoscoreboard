package com.myowndime.duoscoreboard.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.data.local.AppDatabase
import com.myowndime.duoscoreboard.data.repository.RoomMatchRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Replays the version 1 database exactly as Room exported it, then opens the current
 * database on top of it. A match saved before the set feature shipped has to come back
 * intact with zero sets - an update must never drop the user's history.
 */
@RunWith(AndroidJUnit4::class)
class MatchRecordMigrationTest {

    private val databaseName = "migration-test.db"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun `matches saved by version 1 survive the set columns migration`() = runTest {
        createVersionOneDatabaseWithOneMatch()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        try {
            val repository = RoomMatchRepository(database.matchRecordDao())
            val stored = repository.matches.first().single()
            assertEquals("Ann", stored.playerOneName)
            assertEquals("Ben", stored.playerTwoName)
            assertEquals(21, stored.playerOneScore)
            assertEquals(18, stored.playerTwoScore)
            assertEquals(1_000L, stored.playedAtEpochMillis)
            assertEquals(0, stored.playerOneSets)
            assertEquals(0, stored.playerTwoSets)
            assertFalse(stored.hasSets)
        } finally {
            database.close()
        }
    }

    private fun createVersionOneDatabaseWithOneMatch() {
        val schema = JSONObject(readSchema(version = 1)).getJSONObject("database")
        val createSql = schema.getJSONArray("entities")
            .getJSONObject(0)
            .getString("createSql")
            .replace("\${TABLE_NAME}", "match_records")
        val setupQueries = schema.getJSONArray("setupQueries")

        val db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        try {
            db.execSQL(createSql)
            for (index in 0 until setupQueries.length()) {
                db.execSQL(setupQueries.getString(index))
            }
            db.execSQL(
                "INSERT INTO match_records " +
                    "(player_one_name, player_two_name, player_one_score, " +
                    "player_two_score, played_at) VALUES ('Ann', 'Ben', 21, 18, 1000)",
            )
            db.version = schema.getInt("version")
        } finally {
            db.close()
        }
    }

    private fun readSchema(version: Int): String {
        val path = "/${AppDatabase::class.java.canonicalName}/$version.json"
        val stream = checkNotNull(javaClass.getResourceAsStream(path)) {
            "Exported schema $path is missing - run a build so Room writes app/schemas."
        }
        return stream.bufferedReader().use { it.readText() }
    }
}
