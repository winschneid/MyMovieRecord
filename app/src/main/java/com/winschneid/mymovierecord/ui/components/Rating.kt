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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.ui.theme.StarFilled
import java.util.Locale

/** 表示専用の星（0〜5）。塗りつぶし星と未達の星を色で区別する */
@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
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
                tint = if (index < rating) StarFilled else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

/**
 * 入力用の星。n番目の星をタップすると評価n。
 * 現在の評価と同じ星をもう一度タップすると0に戻す。
 */
@Composable
fun RatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..MovieRecord.MAX_RATING).forEach { value ->
            IconButton(onClick = { onRatingChange(if (rating == value) 0 else value) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "☆$value",
                    tint = if (value <= rating) StarFilled else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Text(
            text = "$rating / ${MovieRecord.MAX_RATING}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** 平均評価の表示用（例: ★3.8） */
fun formatAverageRating(average: Double): String =
    "★" + String.format(Locale.JAPAN, "%.1f", average)
