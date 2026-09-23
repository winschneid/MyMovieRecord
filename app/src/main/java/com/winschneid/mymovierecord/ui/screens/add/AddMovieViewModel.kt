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
    val rating: Int? = null,
    val review: String = "",
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoaded: Boolean = true, // 編集時は既存記録の読み込みが終わるまで false
    val hasChanges: Boolean = false, // 開いた時点から入力が変わったか（破棄確認に使う）
    val titleSuggestions: List<String> = emptyList(),
    val theaterSuggestions: List<String> = emptyList(),
    val duplicateWarning: String? = null, // 同日・同作品の既存記録がある場合の確認メッセージ
)

sealed interface AddMovieAction {
    data class UpdateTitle(val value: String) : AddMovieAction
    data class UpdateTheaterName(val value: String) : AddMovieAction
    data class UpdateDate(val value: Long) : AddMovieAction
    data class UpdateRating(val value: Int?) : AddMovieAction
    data class UpdateReview(val value: String) : AddMovieAction
    data object Save : AddMovieAction
    data object ConfirmSave : AddMovieAction
    data object DismissDuplicateWarning : AddMovieAction
    data object Delete : AddMovieAction
}

/** 入力フォームの内容。変更有無の比較に使うため、画面制御用の状態とは分けて持つ */
private data class FormFields(
    val title: String = "",
    val theaterName: String = "",
    val date: Long = System.currentTimeMillis(),
    val rating: Int? = null,
    val review: String = "",
)

private data class InputState(
    val fields: FormFields = FormFields(),
    val initialFields: FormFields = fields,
    val isLoaded: Boolean = true,
    val isSaved: Boolean = false,
    val duplicateWarning: String? = null,
)

private const val SUGGESTION_LIMIT = 5

/**
 * 入力中の文字列を部分一致で含み、かつ完全一致ではない候補に絞る。
 * 前方一致を優先して並べ、多すぎると選びにくいため上限を設ける。
 */
internal fun List<String>.suggestFor(input: String): List<String> {
    val query = input.trim()
    if (query.isEmpty()) return emptyList()
    return filter { it.contains(query, ignoreCase = true) && !it.equals(query, ignoreCase = true) }
        .sortedBy { !it.startsWith(query, ignoreCase = true) }
        .take(SUGGESTION_LIMIT)
}

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
    private val prefillTitle: String? = savedStateHandle.get<String>("title")

    private val _input = MutableStateFlow(
        InputState(
            fields = FormFields(title = prefillTitle.orEmpty()),
            isLoaded = recordId == null,
        )
    )

    val uiState = combine(
        _input,
        getTitles(),
        getTheaterNames(),
    ) { input, titles, theaterNames ->
        val fields = input.fields
        AddMovieUiState(
            title = fields.title,
            theaterName = fields.theaterName,
            date = fields.date,
            rating = fields.rating,
            review = fields.review,
            isSaved = input.isSaved,
            duplicateWarning = input.duplicateWarning,
            isEditMode = recordId != null,
            isLoaded = input.isLoaded,
            hasChanges = fields != input.initialFields,
            titleSuggestions = titles.suggestFor(fields.title),
            theaterSuggestions = theaterNames.suggestFor(fields.theaterName),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddMovieUiState(isEditMode = recordId != null, isLoaded = recordId == null),
    )

    init {
        when {
            recordId != null -> viewModelScope.launch {
                val record = getMovieRecordById(recordId)
                val fields = record?.let {
                    FormFields(
                        title = it.title,
                        theaterName = it.theaterName,
                        date = it.date,
                        rating = it.rating,
                        review = it.review,
                    )
                } ?: FormFields()
                _input.value = InputState(fields = fields, isLoaded = true)
            }
            // もう一度観た: 前回と同じ映画館で観ることが多いので、直近の鑑賞場所も入れておく
            prefillTitle != null -> viewModelScope.launch {
                val lastTheater = getMovieRecords().first()
                    .firstOrNull { it.title == prefillTitle }
                    ?.theaterName
                    .orEmpty()
                _input.update {
                    // 読み込み中にユーザーが入力を始めていたら上書きしない
                    if (it.fields != it.initialFields) return@update it
                    val fields = it.fields.copy(theaterName = lastTheater)
                    it.copy(fields = fields, initialFields = fields)
                }
            }
        }
    }

    fun onAction(action: AddMovieAction) {
        when (action) {
            is AddMovieAction.UpdateTitle -> updateFields { it.copy(title = action.value) }
            is AddMovieAction.UpdateTheaterName -> updateFields { it.copy(theaterName = action.value) }
            is AddMovieAction.UpdateDate -> updateFields { it.copy(date = action.value) }
            is AddMovieAction.UpdateRating -> updateFields {
                it.copy(rating = action.value?.coerceIn(MovieRecord.MIN_RATING, MovieRecord.MAX_RATING))
            }
            is AddMovieAction.UpdateReview -> updateFields { it.copy(review = action.value) }
            AddMovieAction.Save -> save(force = false)
            AddMovieAction.ConfirmSave -> save(force = true)
            AddMovieAction.DismissDuplicateWarning -> _input.update { it.copy(duplicateWarning = null) }
            AddMovieAction.Delete -> delete()
        }
    }

    private fun updateFields(transform: (FormFields) -> FormFields) {
        _input.update { it.copy(fields = transform(it.fields)) }
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
        if (!input.isLoaded) return
        val fields = input.fields
        val title = fields.title.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            if (!force && hasSameDayRecord(title, fields.date)) {
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
                theaterName = fields.theaterName.trim(),
                date = fields.date,
                rating = fields.rating,
                review = fields.review.trim(),
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
