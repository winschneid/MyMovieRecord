package com.winschneid.mymovierecord.data.repository

import com.winschneid.mymovierecord.data.local.dao.MovieRecordDao
import com.winschneid.mymovierecord.data.local.entity.MovieRecordEntity
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRecordRepositoryImpl @Inject constructor(
    private val dao: MovieRecordDao,
) : MovieRecordRepository {

    override fun observeAllRecords(): Flow<List<MovieRecord>> =
        dao.getAllRecords().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getRecordById(id: Long): MovieRecord? =
        dao.getRecordById(id)?.toDomain()

    override suspend fun addRecord(record: MovieRecord) {
        dao.insertRecord(record.toEntity())
    }

    override suspend fun updateRecord(record: MovieRecord) {
        dao.updateRecord(record.toEntity())
    }

    override suspend fun deleteRecord(id: Long) {
        dao.deleteRecordById(id)
    }

    override fun observeDistinctTitles(): Flow<List<String>> = dao.getDistinctTitles()

    override fun observeDistinctTheaterNames(): Flow<List<String>> = dao.getDistinctTheaterNames()

    private fun MovieRecordEntity.toDomain() = MovieRecord(
        id = id,
        title = title,
        theaterName = theaterName,
        date = date,
        rating = rating,
        review = review,
    )

    private fun MovieRecord.toEntity() = MovieRecordEntity(
        id = id,
        title = title,
        theaterName = theaterName,
        date = date,
        rating = rating,
        review = review,
    )
}
