package com.winschneid.mymovierecord.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.winschneid.mymovierecord.data.local.dao.MovieRecordDao
import com.winschneid.mymovierecord.data.local.entity.MovieRecordEntity

@Database(
    entities = [MovieRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MovieRecordDatabase : RoomDatabase() {
    abstract fun movieRecordDao(): MovieRecordDao
}
