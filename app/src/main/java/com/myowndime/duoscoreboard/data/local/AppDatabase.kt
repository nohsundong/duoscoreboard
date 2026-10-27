package com.myowndime.duoscoreboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MatchRecordEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchRecordDao(): MatchRecordDao

    companion object {
        const val NAME: String = "duo_scoreboard.db"

        /** Adds the set counters. Matches recorded before the update keep zero sets. */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE match_records " +
                        "ADD COLUMN player_one_sets INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE match_records " +
                        "ADD COLUMN player_two_sets INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
    }
}
