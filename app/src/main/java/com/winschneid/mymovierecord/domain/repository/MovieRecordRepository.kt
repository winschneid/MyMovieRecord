package com.winschneid.mymovierecord.domain.repository

import com.winschneid.mymovierecord.domain.model.MovieRecord
import kotlinx.coroutines.flow.Flow

interface MovieRecordRepository {
    fun observeAllRecords(): Flow<List<MovieRecord>>
    suspend fun getRecordById(id: Long): MovieRecord?
    suspend fun addRecord(record: MovieRecord)
    suspend fun updateRecord(record: MovieRecord)
    suspend fun deleteRecord(id: Long)
    fun observeDistinctTitles(): Flow<List<String>>
    fun observeDistinctTheaterNames(): Flow<List<String>>
}
