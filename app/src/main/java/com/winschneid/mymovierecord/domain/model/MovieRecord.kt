package com.winschneid.mymovierecord.domain.model

data class MovieRecord(
    val id: Long = 0,
    val title: String,
    val theaterName: String = "",
    val date: Long,
    val rating: Int? = null, // null は未評価。0〜5 は評価済み
    val review: String = "",
) {
    companion object {
        const val MIN_RATING = 0
        const val MAX_RATING = 5
    }
}
