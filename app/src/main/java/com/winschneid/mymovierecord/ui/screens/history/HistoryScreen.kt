package com.winschneid.mymovierecord.ui.screens.history

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winschneid.mymovierecord.ui.components.RatingStars
import com.winschneid.mymovierecord.ui.components.formatDate
import com.winschneid.mymovierecord.ui.theme.MyMovieRecordTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (id: Long) -> Unit,
    onNavigateToMovie: (title: String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val scrollToRecordId by viewModel.scrollToRecordId.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 「元に戻す」で復元した記録が見える位置までスクロールする（一覧に反映されるまで待つ）
    LaunchedEffect(scrollToRecordId, uiState.sections) {
        val id = scrollToRecordId ?: return@LaunchedEffect
        val index = uiState.sections.listIndexOfHeaderFor(id) ?: return@LaunchedEffect
        listState.animateScrollToItem(index)
        viewModel.scrollHandled()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportTo) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importFrom) }

    LaunchedEffect(message) {
        message?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg.text,
                actionLabel = if (msg.withUndo) "元に戻す" else null,
                // 削除は確認なしで行うため、取り消せる時間を長めにとる
                duration = if (msg.withUndo) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
            viewModel.messageShown()
        }
    }

    HistoryContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateToAdd = onNavigateToAdd,
        onCardClick = onNavigateToEdit,
        onTitleClick = onNavigateToMovie,
        onDeleteRecord = viewModel::deleteRecord,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onExportClick = {
            val today = SimpleDateFormat("yyyyMMdd", Locale.JAPAN).format(Date())
            exportLauncher.launch("movie_records_$today.json")
        },
        onImportClick = { importLauncher.launch(arrayOf("*/*")) },
        listState = listState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToAdd: () -> Unit,
    onCardClick: (id: Long) -> Unit,
    onTitleClick: (title: String) -> Unit,
    onDeleteRecord: (id: Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val closeSearch = {
        isSearchActive = false
        onSearchQueryChange("")
    }
    BackHandler(enabled = isSearchActive) { closeSearch() }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchTopBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClose = closeSearch,
                )
            } else {
                TopAppBar(
                    title = { Text("鑑賞履歴") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "検索")
                        }
                        HistoryMenu(
                            onExportClick = onExportClick,
                            onImportClick = onImportClick,
                        )
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "追加")
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            !uiState.hasAnyRecords -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "鑑賞履歴がありません",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onNavigateToAdd,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text("最初の1本を記録する")
                    }
                }
            }
            uiState.sections.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "「${uiState.searchQuery}」に一致する映画がありません",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    // FAB に最後のカードが隠れないよう下に余白をとる
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.sections.forEach { section ->
                        stickyHeader(key = section.label) {
                            Text(
                                text = section.headerText,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(section.items, key = { it.id }) { record ->
                            DismissibleRecordCard(
                                record = record,
                                onClick = { onCardClick(record.id) },
                                onTitleClick = { onTitleClick(record.title) },
                                // 削除は「元に戻す」で取り消せるため、確認ダイアログは挟まない
                                onDeleteRequest = { onDeleteRecord(record.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("タイトル・映画館・感想") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                // 検索は入力に合わせて即時反映されるので、検索キーはキーボードを閉じるだけ
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "クリア")
                        }
                    }
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "検索を閉じる")
            }
        },
    )
}

@Composable
private fun HistoryMenu(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "メニュー")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("エクスポート (JSON)") },
            onClick = {
                expanded = false
                onExportClick()
            },
        )
        DropdownMenuItem(
            text = { Text("インポート (JSON)") },
            onClick = {
                expanded = false
                onImportClick()
            },
        )
    }
}

@Composable
private fun DismissibleRecordCard(
    record: MovieRecordItem,
    onClick: () -> Unit,
    onTitleClick: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val currentOnDeleteRequest by rememberUpdatedState(onDeleteRequest)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnDeleteRequest()
            }
            // 確認ダイアログ経由で削除するため、スワイプでは確定させずカードを元の位置に戻す
            false
        },
        // 誤操作防止のため画面幅の半分までスワイプしないと反応しない
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp),
                )
            }
        },
    ) {
        MovieRecordCard(
            record = record,
            onClick = onClick,
            onTitleClick = onTitleClick,
        )
    }
}

@Composable
private fun MovieRecordCard(
    record: MovieRecordItem,
    onClick: () -> Unit,
    onTitleClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // タイトル部分は作品別履歴へ、それ以外のカード部分は編集へ。矢印でタイトルがリンクだと分かるようにする
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = "作品の鑑賞履歴を見る", onClick = onTitleClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                // 初見は表示せず、2回目以降だけバッジを出す
                if (record.viewCount >= 2) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "${record.viewCount}回目",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
            RatingStars(rating = record.rating)
            if (record.review.isNotBlank()) {
                Text(
                    text = record.review,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = record.theaterName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatDate(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// region Previews

private val previewSections = listOf(
    HistorySection(
        label = "2024年3月",
        items = listOf(
            MovieRecordItem(
                id = 3,
                title = "ゴジラ-1.0",
                theaterName = "109シネマズプレミアム新宿",
                date = 1709424000000L,
                rating = 5,
                review = "IMAXで再鑑賞。音の迫力がまるで違う。",
                viewCount = 2,
            ),
            MovieRecordItem(
                id = 4,
                title = "哀れなるものたち",
                theaterName = "TOHOシネマズ 日比谷",
                date = 1709337600000L,
                rating = 4,
            ),
        ),
    ),
    HistorySection(
        label = "2024年1月",
        items = listOf(
            MovieRecordItem(
                id = 1,
                title = "パーフェクト・デイズ",
                theaterName = "",
                date = 1704067200000L,
                rating = 3,
                review = "淡々とした日常の繰り返しが、こんなに豊かに見えるとは。最後の長回しがずっと頭に残っている。",
            ),
        ),
    ),
)

@Preview(name = "履歴 - ローディング", showBackground = true)
@Composable
internal fun HistoryLoadingPreview() {
    MyMovieRecordTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = true),
            snackbarHostState = SnackbarHostState(),
            onNavigateToAdd = {},
            onCardClick = {},
            onTitleClick = {},
            onDeleteRecord = {},
            onSearchQueryChange = {},
            onExportClick = {},
            onImportClick = {},
        )
    }
}

@Preview(name = "履歴 - 空", showBackground = true)
@Composable
internal fun HistoryEmptyPreview() {
    MyMovieRecordTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = false),
            snackbarHostState = SnackbarHostState(),
            onNavigateToAdd = {},
            onCardClick = {},
            onTitleClick = {},
            onDeleteRecord = {},
            onSearchQueryChange = {},
            onExportClick = {},
            onImportClick = {},
        )
    }
}

@Preview(name = "履歴 - データあり", showBackground = true)
@Composable
internal fun HistoryWithDataPreview() {
    MyMovieRecordTheme {
        HistoryContent(
            uiState = HistoryUiState(
                sections = previewSections,
                hasAnyRecords = true,
                isLoading = false,
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateToAdd = {},
            onCardClick = {},
            onTitleClick = {},
            onDeleteRecord = {},
            onSearchQueryChange = {},
            onExportClick = {},
            onImportClick = {},
        )
    }
}

@Preview(name = "履歴 - 検索ヒットなし", showBackground = true)
@Composable
internal fun HistoryNoSearchResultPreview() {
    MyMovieRecordTheme {
        HistoryContent(
            uiState = HistoryUiState(
                sections = emptyList(),
                searchQuery = "オッペンハイマー",
                hasAnyRecords = true,
                isLoading = false,
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateToAdd = {},
            onCardClick = {},
            onTitleClick = {},
            onDeleteRecord = {},
            onSearchQueryChange = {},
            onExportClick = {},
            onImportClick = {},
        )
    }
}

// endregion
