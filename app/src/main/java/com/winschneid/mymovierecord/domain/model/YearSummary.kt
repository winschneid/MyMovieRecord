package com.winschneid.mymovierecord.domain.model

data class TheaterCount(
    val theaterName: String,
    val count: Int,
)

data class TitleCount(
    val title: String,
    val count: Int,
)

data class RatedMovie(
    val title: String,
    val rating: Int,
)

data class YearSummary(
    val year: Int,
    val totalCount: Int,
    val averageRating: Double? = null, // 評価済みの記録だけで平均。全て未評価なら null
    // index = 評価（0〜5）、値 = 本数
    val ratingCounts: List<Int> = List(MovieRecord.MAX_RATING + 1) { 0 },
    val unratedCount: Int = 0,
    val topMovies: List<RatedMovie> = emptyList(),
    val rewatchedTitles: List<TitleCount> = emptyList(),
    val theaters: List<TheaterCount> = emptyList(),
)
