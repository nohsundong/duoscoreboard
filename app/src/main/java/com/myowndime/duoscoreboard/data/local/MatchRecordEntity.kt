package com.myowndime.duoscoreboard.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myowndime.duoscoreboard.domain.model.MatchRecord

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "player_one_name")
    val playerOneName: String,
    @ColumnInfo(name = "player_two_name")
    val playerTwoName: String,
    @ColumnInfo(name = "player_one_score")
    val playerOneScore: Int,
    @ColumnInfo(name = "player_two_score")
    val playerTwoScore: Int,
    @ColumnInfo(name = "played_at")
    val playedAtEpochMillis: Long,
    // Added in schema version 2; rows written by version 1 default to zero sets.
    @ColumnInfo(name = "player_one_sets", defaultValue = "0")
    val playerOneSets: Int = 0,
    @ColumnInfo(name = "player_two_sets", defaultValue = "0")
    val playerTwoSets: Int = 0,
)

fun MatchRecordEntity.toDomain(): MatchRecord = MatchRecord(
    id = id,
    playerOneName = playerOneName,
    playerTwoName = playerTwoName,
    playerOneScore = playerOneScore,
    playerTwoScore = playerTwoScore,
    playedAtEpochMillis = playedAtEpochMillis,
    playerOneSets = playerOneSets,
    playerTwoSets = playerTwoSets,
)

fun MatchRecord.toEntity(): MatchRecordEntity = MatchRecordEntity(
    id = id,
    playerOneName = playerOneName,
    playerTwoName = playerTwoName,
    playerOneScore = playerOneScore,
    playerTwoScore = playerTwoScore,
    playedAtEpochMillis = playedAtEpochMillis,
    playerOneSets = playerOneSets,
    playerTwoSets = playerTwoSets,
)
