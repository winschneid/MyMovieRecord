package com.winschneid.mymovierecord.ui.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object History : Routes("history")
    data object YearSummary : Routes("year_summary")
    /** title を渡すと作品名を入力済みで開く（「もう一度観た」用） */
    data object AddMovie : Routes("add_movie?title={title}") {
        fun createRoute(title: String? = null) =
            if (title == null) "add_movie" else "add_movie?title=${Uri.encode(title)}"
    }
    data object EditMovie : Routes("edit_movie/{recordId}") {
        fun createRoute(recordId: Long) = "edit_movie/$recordId"
    }
    data object MovieDetail : Routes("movie_detail/{title}") {
        fun createRoute(title: String) = "movie_detail/${Uri.encode(title)}"
    }
}
