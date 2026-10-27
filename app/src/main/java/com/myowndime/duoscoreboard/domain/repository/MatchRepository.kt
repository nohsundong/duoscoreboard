package com.myowndime.duoscoreboard.domain.repository

import com.myowndime.duoscoreboard.domain.model.MatchRecord
import kotlinx.coroutines.flow.Flow

/** Local, on-device match history. */
interface MatchRepository {
    val matches: Flow<List<MatchRecord>>

    suspend fun add(record: MatchRecord): Long

    suspend fun delete(id: Long)

    suspend fun clear()
}
