package com.winschneid.mymovierecord.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.winschneid.mymovierecord.domain.model.YearSummary
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordsUseCase
import com.winschneid.mymovierecord.domain.usecase.GetYearSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OverallSummary(
    val totalCount: Int,
    val titleCount: Int,
    val averageRating: Double,
)

data class YearSummaryUiState(
    val years: List<YearSummary> = emptyList(),
    val overall: OverallSummary? = null,
    val isLoading: Boolean = true,
    val expandedYears: Set<Int> = emptySet(),
)

sealed interface YearSummaryAction {
    data class ToggleYear(val year: Int) : YearSummaryAction
}

@HiltViewModel
class YearSummaryViewModel @Inject constructor(
    getYearSummary: GetYearSummaryUseCase,
    getMovieRecords: GetMovieRecordsUseCase,
) : ViewModel() {

    private val _expandedYears = MutableStateFlow<Set<Int>>(emptySet())

    val uiState = combine(
        getYearSummary(),
        getMovieRecords(),
        _expandedYears,
    ) { years, records, expandedYears ->
        YearSummaryUiState(
            years = years,
            overall = if (records.isEmpty()) null else OverallSummary(
                totalCount = records.size,
                titleCount = records.map { it.title }.distinct().size,
                averageRating = records.map { it.rating }.average(),
            ),
            isLoading = false,
            expandedYears = expandedYears,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = YearSummaryUiState(),
    )

    fun onAction(action: YearSummaryAction) {
        when (action) {
            is YearSummaryAction.ToggleYear -> _expandedYears.update { current ->
                if (action.year in current) current - action.year else current + action.year
            }
        }
    }
}
