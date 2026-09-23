package com.winschneid.mymovierecord.ui.screens.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.winschneid.mymovierecord.data.transfer.MovieRecordsJson
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.usecase.AddMovieRecordUseCase
import com.winschneid.mymovierecord.domain.usecase.DeleteMovieRecordUseCase
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordByIdUseCase
import com.winschneid.mymovierecord.domain.usecase.GetMovieRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MovieRecordItem(
    val id: Long,
    val title: String,
    val theaterName: String = "",
    val date: Long,
    val rating: Int = 0,
    val review: String = "",
    val viewCount: Int = 1, // その作品の累計何回目の鑑賞か
)

data class HistorySection(
    val label: String, // 例: 2026年6月
    val items: List<MovieRecordItem>,
)

data class HistoryUiState(
    val sections: List<HistorySection> = emptyList(),
    val searchQuery: String = "",
    val hasAnyRecords: Boolean = false,
    val isLoading: Boolean = true,
)

data class HistoryMessage(
    val text: String,
    val withUndo: Boolean = false,
)

/**
 * 日付昇順で処理し、各レコード時点で作品ごとの「n回目」を計算する。
 * 戻り値: record.id → n回目
 */
internal fun computeViewCounts(records: List<MovieRecord>): Map<Long, Int> {
    val titleRunningCounts = mutableMapOf<String, Int>()
    return records.sortedWith(compareBy<MovieRecord> { it.date }.thenBy { it.id })
        .associate { record ->
            val count = (titleRunningCounts[record.title] ?: 0) + 1
            titleRunningCounts[record.title] = count
            record.id to count
        }
}

/**
 * 検索フィルタを適用し、年月ごとのセクションに分ける。
 * 「n回目」はフィルタ前の全履歴を基準に計算するため、検索しても値は変わらない。
 * records は日付降順前提（DAO の ORDER BY を維持）。
 */
internal fun buildHistorySections(records: List<MovieRecord>, query: String): List<HistorySection> {
    val viewCountsById = computeViewCounts(records)
    val trimmed = query.trim()
    val filtered = if (trimmed.isEmpty()) records else records.filter { record ->
        record.title.contains(trimmed, ignoreCase = true) ||
            record.theaterName.contains(trimmed, ignoreCase = true) ||
            record.review.contains(trimmed, ignoreCase = true)
    }
    val monthFormat = SimpleDateFormat("yyyy年M月", Locale.JAPAN)
    return filtered
        .map { record ->
            MovieRecordItem(
                id = record.id,
                title = record.title,
                theaterName = record.theaterName,
                date = record.date,
                rating = record.rating,
                review = record.review,
                viewCount = viewCountsById[record.id] ?: 1,
            )
        }
        .groupBy { monthFormat.format(Date(it.date)) }
        .map { (label, items) -> HistorySection(label, items) }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMovieRecords: GetMovieRecordsUseCase,
    private val addMovieRecord: AddMovieRecordUseCase,
    private val deleteMovieRecord: DeleteMovieRecordUseCase,
    private val getMovieRecordById: GetMovieRecordByIdUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _message = MutableStateFlow<HistoryMessage?>(null)
    val message = _message.asStateFlow()

    private var pendingUndo: MovieRecord? = null

    val uiState = combine(getMovieRecords(), _searchQuery) { records, query ->
        HistoryUiState(
            sections = buildHistorySections(records, query),
            searchQuery = query,
            hasAnyRecords = records.isNotEmpty(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            val record = getMovieRecordById(id) ?: return@launch
            deleteMovieRecord(id)
            pendingUndo = record
            _message.value = HistoryMessage("削除しました", withUndo = true)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            pendingUndo?.let { addMovieRecord(it.copy(id = 0)) }
            pendingUndo = null
        }
    }

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val records = getMovieRecords().first()
                val json = MovieRecordsJson.encode(records)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")
                        ?.use { it.write(json.toByteArray()) }
                        ?: error("failed to open output stream")
                }
                records.size
            }.onSuccess { count ->
                _message.value = HistoryMessage("${count}件をエクスポートしました")
            }.onFailure {
                _message.value = HistoryMessage("エクスポートに失敗しました")
            }
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                        ?.use { it.readBytes().decodeToString() }
                        ?: error("failed to open input stream")
                }
                val records = MovieRecordsJson.decode(text)
                records.forEach { addMovieRecord(it) }
                records.size
            }.onSuccess { count ->
                _message.value = HistoryMessage("${count}件をインポートしました")
            }.onFailure {
                _message.value = HistoryMessage("インポートに失敗しました（ファイル形式を確認してください）")
            }
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
