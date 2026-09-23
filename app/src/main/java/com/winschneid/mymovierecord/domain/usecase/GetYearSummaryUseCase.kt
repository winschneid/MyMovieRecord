package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.model.RatedMovie
import com.winschneid.mymovierecord.domain.model.TheaterCount
import com.winschneid.mymovierecord.domain.model.TitleCount
import com.winschneid.mymovierecord.domain.model.YearSummary
import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetYearSummaryUseCase @Inject constructor(
    private val repository: MovieRecordRepository,
) {
    operator fun invoke(): Flow<List<YearSummary>> =
        repository.observeAllRecords().map { records ->
            records
                .groupBy { record ->
                    Calendar.getInstance().apply { timeInMillis = record.date }.get(Calendar.YEAR)
                }
                .map { (year, yearRecords) ->
                    YearSummary(
                        year = year,
                        totalCount = yearRecords.size,
                        averageRating = averageRatingOf(yearRecords),
                        ratingCounts = (MovieRecord.MIN_RATING..MovieRecord.MAX_RATING).map { rating ->
                            yearRecords.count { it.rating == rating }
                        },
                        unratedCount = yearRecords.count { it.rating == null },
                        // 同じ作品を複数回観た場合は最高評価の1件だけを残す
                        topMovies = yearRecords
                            .mapNotNull { record -> record.rating?.takeIf { it > 0 }?.let { record to it } }
                            .sortedWith(
                                compareByDescending<Pair<MovieRecord, Int>> { it.second }
                                    .thenByDescending { it.first.date }
                            )
                            .distinctBy { it.first.title }
                            .take(TOP_MOVIES_LIMIT)
                            .map { (record, rating) -> RatedMovie(record.title, rating) },
                        rewatchedTitles = yearRecords
                            .groupingBy { it.title }
                            .eachCount()
                            .filterValues { it >= 2 }
                            .entries
                            .sortedByDescending { it.value }
                            .map { TitleCount(it.key, it.value) },
                        theaters = yearRecords
                            .filter { it.theaterName.isNotBlank() }
                            .groupingBy { it.theaterName }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .map { TheaterCount(it.key, it.value) },
                    )
                }
                .sortedByDescending { it.year }
        }

    companion object {
        const val TOP_MOVIES_LIMIT = 5
    }
}

/** 未評価（null）を除いた平均評価。評価済みが1件もなければ null */
fun averageRatingOf(records: List<MovieRecord>): Double? =
    records.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()
