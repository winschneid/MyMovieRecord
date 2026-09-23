package com.winschneid.mymovierecord.ui.screens.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.model.RatedMovie
import com.winschneid.mymovierecord.domain.model.TheaterCount
import com.winschneid.mymovierecord.domain.model.TitleCount
import com.winschneid.mymovierecord.domain.model.YearSummary
import com.winschneid.mymovierecord.ui.components.RatingStars
import com.winschneid.mymovierecord.ui.components.formatAverageRating
import com.winschneid.mymovierecord.ui.theme.MyMovieRecordTheme
import com.winschneid.mymovierecord.ui.theme.StarFilled

@Composable
fun YearSummaryScreen(
    onNavigateToMovie: (title: String) -> Unit,
    viewModel: YearSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    YearSummaryContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMovieClick = onNavigateToMovie,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSummaryContent(
    uiState: YearSummaryUiState,
    onAction: (YearSummaryAction) -> Unit,
    onMovieClick: (title: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("年別集計") })
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
            uiState.years.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "鑑賞履歴がありません",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.overall?.let { overall ->
                        item(key = "overall") {
                            OverallSummaryCard(overall = overall)
                        }
                    }
                    items(uiState.years, key = { it.year }) { summary ->
                        YearCard(
                            summary = summary,
                            isExpanded = summary.year in uiState.expandedYears,
                            onToggle = { onAction(YearSummaryAction.ToggleYear(summary.year)) },
                            onMovieClick = onMovieClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallSummaryCard(overall: OverallSummary) {
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
            OverallItem(label = "通算", value = "${overall.totalCount}本")
            OverallItem(label = "作品数", value = "${overall.titleCount}作品")
            OverallItem(label = "平均評価", value = formatAverageRating(overall.averageRating))
        }
    }
}

@Composable
private fun OverallItem(label: String, value: String) {
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
private fun YearCard(
    summary: YearSummary,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMovieClick: (title: String) -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation",
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${summary.year}年",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "平均" + formatAverageRating(summary.averageRating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${summary.totalCount}本",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "折りたたむ" else "展開する",
                        modifier = Modifier.rotate(arrowRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider()
                    SectionLabel("評価の分布")
                    RatingDistribution(
                        ratingCounts = summary.ratingCounts,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    if (summary.unratedCount > 0) {
                        Text(
                            text = "未評価 ${summary.unratedCount}本",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        )
                    }
                    if (summary.topMovies.isNotEmpty()) {
                        HorizontalDivider()
                        SectionLabel("高評価の作品")
                        val ranks = summary.topMovies.competitionRanks { it.rating }
                        summary.topMovies.forEachIndexed { index, movie ->
                            RankedRow(
                                rank = ranks[index],
                                name = movie.title,
                                onClick = { onMovieClick(movie.title) },
                            ) {
                                RatingStars(rating = movie.rating, starSize = 14.dp)
                            }
                            RowDivider(show = index < summary.topMovies.lastIndex)
                        }
                    }
                    if (summary.rewatchedTitles.isNotEmpty()) {
                        HorizontalDivider()
                        SectionLabel("くり返し観た作品")
                        val ranks = summary.rewatchedTitles.competitionRanks { it.count }
                        summary.rewatchedTitles.forEachIndexed { index, titleCount ->
                            RankedRow(
                                rank = ranks[index],
                                name = titleCount.title,
                                onClick = { onMovieClick(titleCount.title) },
                            ) {
                                CountText(titleCount.count)
                            }
                            RowDivider(show = index < summary.rewatchedTitles.lastIndex)
                        }
                    }
                    if (summary.theaters.isNotEmpty()) {
                        HorizontalDivider()
                        SectionLabel("映画館・鑑賞場所")
                        val ranks = summary.theaters.competitionRanks { it.count }
                        summary.theaters.forEachIndexed { index, theaterCount ->
                            RankedRow(rank = ranks[index], name = theaterCount.theaterName) {
                                CountText(theaterCount.count)
                            }
                            RowDivider(show = index < summary.theaters.lastIndex)
                        }
                    }
                }
            }
        }
    }
}

/** ★5〜★0 の本数を横棒で表示する。棒の長さは最多の評価を100%とした相対値 */
@Composable
private fun RatingDistribution(ratingCounts: List<Int>, modifier: Modifier = Modifier) {
    val maxCount = ratingCounts.maxOrNull()?.takeIf { it > 0 } ?: 1
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        (MovieRecord.MAX_RATING downTo MovieRecord.MIN_RATING).forEach { rating ->
            val count = ratingCounts.getOrElse(rating) { 0 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "★$rating",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(28.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(count.toFloat() / maxCount)
                                .clip(RoundedCornerShape(5.dp))
                                .background(StarFilled),
                        )
                    }
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(24.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
    )
}

@Composable
private fun RowDivider(show: Boolean) {
    if (show) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun CountText(count: Int) {
    Text(
        text = "${count}回",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
    )
}

/**
 * 同じ値は同順位にする（例: 3, 3, 1 → 1位, 1位, 3位）。
 * リストは値の降順に並んでいる前提。
 */
internal fun <T> List<T>.competitionRanks(value: (T) -> Int): List<Int> =
    map { item -> 1 + count { value(it) > value(item) } }

@Composable
private fun RankedRow(
    rank: Int,
    name: String,
    onClick: (() -> Unit)? = null, // 作品の行はタップで作品別履歴へ
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(20.dp),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

// region Previews

private val previewOverall = OverallSummary(
    totalCount = 15,
    titleCount = 13,
    averageRating = 3.8,
)

private val previewYears = listOf(
    YearSummary(
        year = 2025,
        totalCount = 3,
        averageRating = 4.0,
        ratingCounts = listOf(0, 0, 0, 1, 1, 1),
        topMovies = listOf(
            RatedMovie("国宝", 5),
            RatedMovie("F1/エフワン", 4),
            RatedMovie("ミッション:インポッシブル/ファイナル・レコニング", 3),
        ),
        theaters = listOf(
            TheaterCount("TOHOシネマズ 日比谷", 2),
            TheaterCount("グランドシネマサンシャイン池袋", 1),
        ),
    ),
    YearSummary(
        year = 2024,
        totalCount = 12,
        averageRating = 3.75,
        ratingCounts = listOf(0, 1, 1, 3, 4, 3),
        topMovies = listOf(
            RatedMovie("ゴジラ-1.0", 5),
            RatedMovie("パーフェクト・デイズ", 5),
            RatedMovie("哀れなるものたち", 5),
            RatedMovie("オッペンハイマー", 4),
            RatedMovie("ルックバック", 4),
        ),
        rewatchedTitles = listOf(
            TitleCount("ゴジラ-1.0", 2),
        ),
        theaters = listOf(
            TheaterCount("109シネマズプレミアム新宿", 6),
            TheaterCount("TOHOシネマズ 日比谷", 4),
            TheaterCount("自宅", 2),
        ),
    ),
)

@Preview(name = "年別集計 - 折りたたみ", showBackground = true)
@Composable
internal fun YearSummaryCollapsedPreview() {
    MyMovieRecordTheme {
        YearSummaryContent(
            uiState = YearSummaryUiState(years = previewYears, overall = previewOverall, isLoading = false),
            onAction = {},
            onMovieClick = {},
        )
    }
}

@Preview(name = "年別集計 - 展開", showBackground = true, heightDp = 1200)
@Composable
internal fun YearSummaryExpandedPreview() {
    MyMovieRecordTheme {
        YearSummaryContent(
            uiState = YearSummaryUiState(
                years = previewYears,
                overall = previewOverall,
                isLoading = false,
                expandedYears = setOf(2024),
            ),
            onAction = {},
            onMovieClick = {},
        )
    }
}

// endregion
