package com.myowndime.duoscoreboard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao {

    @Query("SELECT * FROM match_records ORDER BY played_at DESC, id DESC")
    fun observeAll(): Flow<List<MatchRecordEntity>>

    @Insert
    suspend fun insert(record: MatchRecordEntity): Long

    @Query("DELETE FROM match_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM match_records")
    suspend fun deleteAll()
}
