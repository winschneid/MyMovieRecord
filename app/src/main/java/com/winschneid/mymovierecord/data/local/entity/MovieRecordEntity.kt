package com.winschneid.mymovierecord.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movie_records",
    indices = [Index("title")],
)
data class MovieRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "theater_name")
    val theaterName: String,
    val date: Long,
    // 評価（0〜5の整数）
    val rating: Int,
    val review: String,
)
