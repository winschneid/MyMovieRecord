package com.winschneid.mymovierecord.ui.screens.movie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winschneid.mymovierecord.ui.components.RatingStars
import com.winschneid.mymovierecord.ui.components.formatAverageRating
import com.winschneid.mymovierecord.ui.components.formatDate
import com.winschneid.mymovierecord.ui.theme.MyMovieRecordTheme

@Composable
fun MovieDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (id: Long) -> Unit,
    onNavigateToRewatch: (title: String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 編集画面で最後の1件を削除・改題して戻ってきた場合、空の画面を見せずに一覧へ戻る
    LaunchedEffect(uiState.isLoading, uiState.items.isEmpty()) {
        if (!uiState.isLoading && uiState.items.isEmpty()) onNavigateBack()
    }

    MovieDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onCardClick = onNavigateToEdit,
        onRewatchClick = { onNavigateToRewatch(uiState.title) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onNavigateBack: () -> Unit,
    onCardClick: (id: Long) -> Unit,
    onRewatchClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRewatchClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("もう一度観た") },
            )
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
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    // FAB に最後のカードが隠れないよう下に余白をとる
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "summary") {
                        MovieSummaryCard(uiState = uiState)
                    }
                    items(uiState.items, key = { it.id }) { item ->
                        ViewingCard(item = item, onClick = { onCardClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieSummaryCard(uiState: MovieDetailUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryItem(label = "鑑賞回数", value = "${uiState.totalCount}回")
            SummaryItem(
                label = "初鑑賞",
                value = uiState.firstDate?.let { formatDate(it) } ?: "-",
            )
            SummaryItem(label = "平均評価", value = formatAverageRating(uiState.averageRating))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ViewingCard(item: ViewingItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RatingStars(rating = item.rating, starSize = 20.dp)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "${item.viewCount}回目",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
            if (item.review.isNotBlank()) {
                Text(
                    text = item.review,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.theaterName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatDate(item.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// region Previews

@Preview(name = "作品別履歴", showBackground = true)
@Composable
internal fun MovieDetailPreview() {
    MyMovieRecordTheme {
        MovieDetailContent(
            uiState = MovieDetailUiState(
                title = "ゴジラ-1.0",
                totalCount = 3,
                firstDate = 1698796800000L,
                averageRating = 4.67,
                items = listOf(
                    ViewingItem(
                        id = 3,
                        theaterName = "109シネマズプレミアム新宿",
                        date = 1709424000000L,
                        rating = 5,
                        review = "IMAXで再鑑賞。音の迫力がまるで違う。",
                        viewCount = 3,
                    ),
                    ViewingItem(
                        id = 2,
                        theaterName = "TOHOシネマズ 日比谷",
                        date = 1704067200000L,
                        rating = 4,
                        review = "",
                        viewCount = 2,
                    ),
                    ViewingItem(
                        id = 1,
                        theaterName = "",
                        date = 1698796800000L,
                        rating = 5,
                        review = "初見。ラストで泣いた。",
                        viewCount = 1,
                    ),
                ),
                isLoading = false,
            ),
            onNavigateBack = {},
            onCardClick = {},
            onRewatchClick = {},
        )
    }
}

// endregion
