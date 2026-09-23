package com.winschneid.mymovierecord.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.ui.theme.StarFilled
import java.util.Locale

/** 未達の星の色。outlineVariant はカード背景上で見えにくいため、文字色を薄めて使う */
@Composable
private fun emptyStarColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

/** 表示専用の星（0〜5）。未評価（null）の場合は「未評価」と表示する */
@Composable
fun RatingStars(
    rating: Int?,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    if (rating == null) {
        Text(
            text = "未評価",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "評価 $rating / ${MovieRecord.MAX_RATING}"
        },
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        repeat(MovieRecord.MAX_RATING) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) StarFilled else emptyStarColor(),
                modifier = Modifier.size(starSize),
            )
        }
    }
}

/**
 * 入力用の星。n番目の星をタップすると評価n。
 * 現在の評価と同じ星をもう一度タップすると0（☆0）にする。「クリア」で未評価（null）に戻す。
 */
@Composable
fun RatingInput(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..MovieRecord.MAX_RATING).forEach { value ->
            val isSelected = rating != null && value <= rating
            IconButton(
                onClick = { onRatingChange(if (rating == value) 0 else value) },
                modifier = Modifier.semantics { selected = rating == value },
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "評価を${value}にする",
                    tint = if (isSelected) StarFilled else emptyStarColor(),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Text(
            text = if (rating == null) "未評価" else "$rating / ${MovieRecord.MAX_RATING}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        if (rating != null) {
            TextButton(onClick = { onRatingChange(null) }) { Text("クリア") }
        }
    }
}

/** 平均評価の表示用（例: ★3.8）。評価済みの記録がなければ「-」 */
fun formatAverageRating(average: Double?): String =
    if (average == null) "-" else "★" + String.format(Locale.JAPAN, "%.1f", average)
