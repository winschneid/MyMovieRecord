package com.winschneid.mymovierecord.ui.screens.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.usecase.AddMovieRecordUseCase
import com.winschneid.mymovierecord.domain.usecase.DeleteMovieRecordUseCase
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordByIdUseCase
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordsUseCase
import com.winschneid.mymovierecord.domain.usecase.GetTheaterNamesUseCase
import com.winschneid.mymovierecord.domain.usecase.GetTitlesUseCase
import com.winschneid.mymovierecord.domain.usecase.UpdateMovieRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AddMovieUiState(
    val title: String = "",
    val theaterName: String = "",
    val date: Long = System.currentTimeMillis(),
    val rating: Int = 0,
    val review: String = "",
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val titleSuggestions: List<String> = emptyList(),
    val theaterSuggestions: List<String> = emptyList(),
    val duplicateWarning: String? = null, // 同日・同作品の既存記録がある場合の確認メッセージ
)

sealed interface AddMovieAction {
    data class UpdateTitle(val value: String) : AddMovieAction
    data class UpdateTheaterName(val value: String) : AddMovieAction
    data class UpdateDate(val value: Long) : AddMovieAction
    data class UpdateRating(val value: Int) : AddMovieAction
    data class UpdateReview(val value: String) : AddMovieAction
    data object Save : AddMovieAction
    data object ConfirmSave : AddMovieAction
    data object DismissDuplicateWarning : AddMovieAction
    data object Delete : AddMovieAction
}

private data class InputState(
    val title: String = "",
    val theaterName: String = "",
    val date: Long = System.currentTimeMillis(),
    val rating: Int = 0,
    val review: String = "",
    val isSaved: Boolean = false,
    val duplicateWarning: String? = null,
)

/** 入力中の文字列を部分一致で含み、かつ完全一致ではない候補に絞る */
private fun List<String>.suggestFor(input: String): List<String> =
    if (input.isBlank()) emptyList()
    else filter { it.contains(input, ignoreCase = true) && !it.equals(input, ignoreCase = true) }

@HiltViewModel
class AddMovieViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addMovieRecord: AddMovieRecordUseCase,
    private val updateMovieRecord: UpdateMovieRecordUseCase,
    private val deleteMovieRecord: DeleteMovieRecordUseCase,
    private val getMovieRecordById: GetMovieRecordByIdUseCase,
    private val getMovieRecords: GetMovieRecordsUseCase,
    getTitles: GetTitlesUseCase,
    getTheaterNames: GetTheaterNamesUseCase,
) : ViewModel() {

    private val recordId: Long? = savedStateHandle.get<Long>("recordId")

    private val _input = MutableStateFlow(InputState())

    val uiState = combine(
        _input,
        getTitles(),
        getTheaterNames(),
    ) { input, titles, theaterNames ->
        AddMovieUiState(
            title = input.title,
            theaterName = input.theaterName,
            date = input.date,
            rating = input.rating,
            review = input.review,
            isSaved = input.isSaved,
            duplicateWarning = input.duplicateWarning,
            isEditMode = recordId != null,
            titleSuggestions = titles.suggestFor(input.title),
            theaterSuggestions = theaterNames.suggestFor(input.theaterName),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddMovieUiState(isEditMode = recordId != null),
    )

    init {
        if (recordId != null) {
            viewModelScope.launch {
                getMovieRecordById(recordId)?.let { record ->
                    _input.value = InputState(
                        title = record.title,
                        theaterName = record.theaterName,
                        date = record.date,
                        rating = record.rating,
                        review = record.review,
                    )
                }
            }
        }
    }

    fun onAction(action: AddMovieAction) {
        when (action) {
            is AddMovieAction.UpdateTitle -> _input.update { it.copy(title = action.value) }
            is AddMovieAction.UpdateTheaterName -> _input.update { it.copy(theaterName = action.value) }
            is AddMovieAction.UpdateDate -> _input.update { it.copy(date = action.value) }
            is AddMovieAction.UpdateRating -> _input.update {
                it.copy(rating = action.value.coerceIn(MovieRecord.MIN_RATING, MovieRecord.MAX_RATING))
            }
            is AddMovieAction.UpdateReview -> _input.update { it.copy(review = action.value) }
            AddMovieAction.Save -> save(force = false)
            AddMovieAction.ConfirmSave -> save(force = true)
            AddMovieAction.DismissDuplicateWarning -> _input.update { it.copy(duplicateWarning = null) }
            AddMovieAction.Delete -> delete()
        }
    }

    private fun delete() {
        if (recordId == null) return
        viewModelScope.launch {
            deleteMovieRecord(recordId)
            _input.update { it.copy(isSaved = true) }
        }
    }

    private fun save(force: Boolean) {
        val input = _input.value
        val title = input.title.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            if (!force && hasSameDayRecord(title, input.date)) {
                _input.update {
                    it.copy(
                        duplicateWarning = "同じ日に「$title」の記録が登録されています。このまま保存しますか？",
                    )
                }
                return@launch
            }
            val record = MovieRecord(
                id = recordId ?: 0L,
                title = title,
                theaterName = input.theaterName.trim(),
                date = input.date,
                rating = input.rating,
                review = input.review.trim(),
            )
            if (recordId != null) updateMovieRecord(record) else addMovieRecord(record)
            _input.update { it.copy(duplicateWarning = null, isSaved = true) }
        }
    }

    /**
     * 編集中の記録自身を除き、同じ日（ローカルタイムゾーン基準）に同じ作品の記録があるか。
     * 1日に複数本観ることは普通にあるため、作品名まで一致した場合だけ確認する。
     */
    private suspend fun hasSameDayRecord(title: String, date: Long): Boolean {
        val target = Calendar.getInstance().apply { timeInMillis = date }
        return getMovieRecords().first().any { existing ->
            if (existing.id == recordId || existing.title != title) return@any false
            val cal = Calendar.getInstance().apply { timeInMillis = existing.date }
            cal.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        }
    }
}
