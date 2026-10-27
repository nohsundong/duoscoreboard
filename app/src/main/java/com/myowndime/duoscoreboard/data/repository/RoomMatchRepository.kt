package com.myowndime.duoscoreboard.data.repository

import com.myowndime.duoscoreboard.data.local.MatchRecordDao
import com.myowndime.duoscoreboard.data.local.toDomain
import com.myowndime.duoscoreboard.data.local.toEntity
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMatchRepository(
    private val dao: MatchRecordDao,
) : MatchRepository {

    override val matches: Flow<List<MatchRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(record: MatchRecord): Long = dao.insert(record.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun clear() = dao.deleteAll()
}
