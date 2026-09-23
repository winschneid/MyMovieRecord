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
                        averageRating = yearRecords.map { it.rating }.average(),
                        ratingCounts = (MovieRecord.MIN_RATING..MovieRecord.MAX_RATING).map { rating ->
                            yearRecords.count { it.rating == rating }
                        },
                        // 同じ作品を複数回観た場合は最高評価の1件だけを残す
                        topMovies = yearRecords
                            .filter { it.rating > 0 }
                            .sortedWith(compareByDescending<MovieRecord> { it.rating }.thenByDescending { it.date })
                            .distinctBy { it.title }
                            .take(TOP_MOVIES_LIMIT)
                            .map { RatedMovie(it.title, it.rating) },
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
