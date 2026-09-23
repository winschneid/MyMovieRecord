package com.winschneid.mymovierecord.ui.screens.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordsUseCase
import com.winschneid.mymovierecord.ui.screens.history.computeViewCounts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ViewingItem(
    val id: Long,
    val theaterName: String,
    val date: Long,
    val rating: Int,
    val review: String,
    val viewCount: Int, // その鑑賞時点でのn回目
)

data class MovieDetailUiState(
    val title: String = "",
    val totalCount: Int = 0,
    val firstDate: Long? = null,
    val averageRating: Double = 0.0,
    val items: List<ViewingItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMovieRecords: GetMovieRecordsUseCase,
) : ViewModel() {

    private val title: String = savedStateHandle.get<String>("title").orEmpty()

    val uiState = getMovieRecords()
        .map { records ->
            val viewCountsById = computeViewCounts(records)
            // 表示順（日付降順）は DAO の ORDER BY date DESC を維持
            val items = records
                .filter { it.title == title }
                .map { record ->
                    ViewingItem(
                        id = record.id,
                        theaterName = record.theaterName,
                        date = record.date,
                        rating = record.rating,
                        review = record.review,
                        viewCount = viewCountsById[record.id] ?: 1,
                    )
                }
            MovieDetailUiState(
                title = title,
                totalCount = items.size,
                firstDate = items.minOfOrNull { it.date },
                averageRating = if (items.isEmpty()) 0.0 else items.map { it.rating }.average(),
                items = items,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovieDetailUiState(title = title),
        )
}
