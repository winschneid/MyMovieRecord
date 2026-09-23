package com.winschneid.mymovierecord.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.winschneid.mymovierecord.data.local.entity.MovieRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieRecordDao {

    @Query("SELECT * FROM movie_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<MovieRecordEntity>>

    @Query("SELECT * FROM movie_records WHERE id = :id")
    suspend fun getRecordById(id: Long): MovieRecordEntity?

    @Insert
    suspend fun insertRecord(record: MovieRecordEntity): Long

    @Update
    suspend fun updateRecord(record: MovieRecordEntity)

    @Query("DELETE FROM movie_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT DISTINCT title FROM movie_records ORDER BY title ASC")
    fun getDistinctTitles(): Flow<List<String>>

    /** 鑑賞場所は任意入力のため空文字は除外する */
    @Query("SELECT DISTINCT theater_name FROM movie_records WHERE theater_name != '' ORDER BY theater_name ASC")
    fun getDistinctTheaterNames(): Flow<List<String>>
}
