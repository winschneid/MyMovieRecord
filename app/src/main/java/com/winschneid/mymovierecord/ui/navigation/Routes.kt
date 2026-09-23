package com.winschneid.mymovierecord.ui.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object History : Routes("history")
    data object YearSummary : Routes("year_summary")
    data object AddMovie : Routes("add_movie")
    data object EditMovie : Routes("edit_movie/{recordId}") {
        fun createRoute(recordId: Long) = "edit_movie/$recordId"
    }
    data object MovieDetail : Routes("movie_detail/{title}") {
        fun createRoute(title: String) = "movie_detail/${Uri.encode(title)}"
    }
}
