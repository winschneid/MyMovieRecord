package com.winschneid.mymovierecord.ui.screens.add

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winschneid.mymovierecord.ui.components.PastOrTodaySelectableDates
import com.winschneid.mymovierecord.ui.components.RatingInput
import com.winschneid.mymovierecord.ui.components.formatDate
import com.winschneid.mymovierecord.ui.components.localToPickerMillis
import com.winschneid.mymovierecord.ui.components.pickerToLocalMillis
import com.winschneid.mymovierecord.ui.theme.MyMovieRecordTheme

@Composable
fun AddMovieScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMovieViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    AddMovieContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMovieContent(
    uiState: AddMovieUiState,
    onAction: (AddMovieAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val theaterFocus = remember { FocusRequester() }
    val reviewFocus = remember { FocusRequester() }
    val canSave = uiState.isLoaded && uiState.title.isNotBlank()

    // 入力途中で戻ると内容が消えるため、変更がある場合だけ確認する（戻る矢印・システムの戻る共通）
    val requestBack = {
        if (uiState.hasChanges && !uiState.isSaved) showDiscardDialog = true else onNavigateBack()
    }
    BackHandler(enabled = uiState.hasChanges && !uiState.isSaved) { showDiscardDialog = true }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("入力内容を破棄しますか？") },
            text = { Text("保存していない変更は失われます。") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) { Text("破棄", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("編集を続ける") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("削除の確認") },
            text = { Text("この鑑賞記録を削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onAction(AddMovieAction.Delete)
                }) { Text("削除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("キャンセル") }
            },
        )
    }

    uiState.duplicateWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { onAction(AddMovieAction.DismissDuplicateWarning) },
            title = { Text("同じ日の記録があります") },
            text = { Text(warning) },
            confirmButton = {
                TextButton(onClick = { onAction(AddMovieAction.ConfirmSave) }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AddMovieAction.DismissDuplicateWarning) }) { Text("キャンセル") }
            },
        )
    }

    if (showDatePicker) {
        // ダイアログを開くたびに現在の日付で作り直す（編集時に読み込んだ日付を反映するため）
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localToPickerMillis(uiState.date),
            selectableDates = PastOrTodaySelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onAction(AddMovieAction.UpdateDate(pickerToLocalMillis(it)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "鑑賞記録を編集" else "映画を記録") },
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
                actions = {
                    // キーボード表示中でも届くよう、上部にも保存ボタンを置く
                    IconButton(
                        onClick = { onAction(AddMovieAction.Save) },
                        enabled = canSave,
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "保存")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            SuggestTextField(
                value = uiState.title,
                onValueChange = { onAction(AddMovieAction.UpdateTitle(it)) },
                label = "映画タイトル *",
                suggestions = uiState.titleSuggestions,
                enabled = uiState.isLoaded,
                onImeNext = { theaterFocus.requestFocus() },
            )

            RatingInput(
                label = "評価",
                rating = uiState.rating,
                onRatingChange = { onAction(AddMovieAction.UpdateRating(it)) },
            )

            // readOnly の TextField はタップを拾わないため、透明なオーバーレイで全体をタップ可能にする
            Box {
                OutlinedTextField(
                    value = formatDate(uiState.date),
                    onValueChange = {},
                    label = { Text("鑑賞日") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "日付を選択")
                    },
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = uiState.isLoaded) { showDatePicker = true },
                )
            }

            SuggestTextField(
                value = uiState.theaterName,
                onValueChange = { onAction(AddMovieAction.UpdateTheaterName(it)) },
                label = "映画館・鑑賞場所",
                suggestions = uiState.theaterSuggestions,
                enabled = uiState.isLoaded,
                onImeNext = { reviewFocus.requestFocus() },
                modifier = Modifier.focusRequester(theaterFocus),
            )

            OutlinedTextField(
                value = uiState.review,
                onValueChange = { onAction(AddMovieAction.UpdateReview(it)) },
                label = { Text("感想") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(reviewFocus),
                enabled = uiState.isLoaded,
                minLines = 3,
            )

            Button(
                onClick = { onAction(AddMovieAction.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
            ) {
                Text("保存")
            }

            if (uiState.isEditMode) {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isLoaded,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text("削除")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    onImeNext: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = enabled),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                expanded = false
                onImeNext()
            }),
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

// region Previews

@Preview(name = "登録 - 空", showBackground = true)
@Composable
internal fun AddMovieEmptyPreview() {
    MyMovieRecordTheme {
        AddMovieContent(
            // 既定 date は System.currentTimeMillis() で日替わりするため、
            // スクリーンショットを決定論的にするよう固定値を渡す。
            uiState = AddMovieUiState(date = 1704067200000L),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "登録 - 入力済み", showBackground = true)
@Composable
internal fun AddMovieFilledPreview() {
    MyMovieRecordTheme {
        AddMovieContent(
            uiState = AddMovieUiState(
                title = "パーフェクト・デイズ",
                theaterName = "TOHOシネマズ 日比谷",
                date = 1704067200000L,
                rating = 4,
                review = "淡々とした日常の繰り返しが、こんなに豊かに見えるとは。",
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "編集モード", showBackground = true)
@Composable
internal fun AddMovieEditPreview() {
    MyMovieRecordTheme {
        AddMovieContent(
            uiState = AddMovieUiState(
                title = "ゴジラ-1.0",
                theaterName = "109シネマズプレミアム新宿",
                date = 1704067200000L,
                rating = 5,
                isEditMode = true,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

// endregion
