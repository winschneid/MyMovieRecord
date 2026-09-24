package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.model.RatedMovie
import com.winschneid.mymovierecord.domain.model.TheaterCount
import com.winschneid.mymovierecord.domain.model.TitleCount
import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class GetYearSummaryUseCaseTest {

    private class FakeRepository(private val records: List<MovieRecord>) : MovieRecordRepository {
        override fun observeAllRecords(): Flow<List<MovieRecord>> = flowOf(records)
        override suspend fun getRecordById(id: Long): MovieRecord? = null
        override suspend fun addRecord(record: MovieRecord) = Unit
        override suspend fun updateRecord(record: MovieRecord) = Unit
        override suspend fun deleteRecord(id: Long) = Unit
        override fun observeDistinctTitles(): Flow<List<String>> = flowOf(emptyList())
        override fun observeDistinctTheaterNames(): Flow<List<String>> = flowOf(emptyList())
    }

    /** タイムゾーン境界の影響を避けるため年の中央付近の epoch millis を使う */
    private fun inYear(year: Int, day: Int = 1): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, Calendar.JULY, day)
        }.timeInMillis

    private fun record(
        id: Long,
        year: Int,
        title: String,
        rating: Int? = null,
        theater: String = "",
        day: Int = 1,
    ) = MovieRecord(
        id = id,
        title = title,
        theaterName = theater,
        date = inYear(year, day),
        rating = rating,
    )

    private fun summarize(vararg records: MovieRecord) = runBlocking {
        GetYearSummaryUseCase(FakeRepository(records.toList()))().first()
    }

    @Test
    fun `年ごとにグループ化され新しい年が先頭になる`() {
        val summaries = summarize(
            record(1, 2024, "A"),
            record(2, 2025, "B"),
            record(3, 2024, "C"),
        )

        assertEquals(listOf(2025, 2024), summaries.map { it.year })
        assertEquals(1, summaries[0].totalCount)
        assertEquals(2, summaries[1].totalCount)
    }

    @Test
    fun `平均評価と評価の分布が計算され評価0は評価済みとして数える`() {
        val summary = summarize(
            record(1, 2024, "A", rating = 5),
            record(2, 2024, "B", rating = 4),
            record(3, 2024, "C", rating = 0),
            record(4, 2024, "D", rating = 5),
        ).single()

        assertEquals(3.5, summary.averageRating!!, 0.0001)
        assertEquals(listOf(1, 0, 0, 0, 1, 2), summary.ratingCounts)
        assertEquals(0, summary.unratedCount)
    }

    @Test
    fun `未評価は平均と分布から除外され別に数えられる`() {
        val summary = summarize(
            record(1, 2024, "A", rating = 4),
            record(2, 2024, "B", rating = null),
            record(3, 2024, "C", rating = 2),
        ).single()

        assertEquals(3.0, summary.averageRating!!, 0.0001)
        assertEquals(listOf(0, 0, 1, 0, 1, 0), summary.ratingCounts)
        assertEquals(1, summary.unratedCount)
    }

    @Test
    fun `全て未評価なら平均はnull`() {
        val summary = summarize(record(1, 2024, "A")).single()

        assertNull(summary.averageRating)
        assertEquals(1, summary.unratedCount)
    }

    @Test
    fun `高評価の作品は評価順で評価0と未評価は除外し同じ作品は最高評価の1件だけ残す`() {
        val summary = summarize(
            record(1, 2024, "A", rating = 3, day = 1),
            record(2, 2024, "B", rating = 5, day = 2),
            record(3, 2024, "A", rating = 4, day = 3),
            record(4, 2024, "C", rating = 0, day = 4),
            record(5, 2024, "D", rating = null, day = 5),
        ).single()

        assertEquals(listOf(RatedMovie("B", 5), RatedMovie("A", 4)), summary.topMovies)
    }

    @Test
    fun `高評価の作品は最大5件`() {
        val records = (1L..7L).map { record(it, 2024, "T$it", rating = 5, day = it.toInt()) }
        val summary = summarize(*records.toTypedArray()).single()

        assertEquals(GetYearSummaryUseCase.TOP_MOVIES_LIMIT, summary.topMovies.size)
    }

    @Test
    fun `くり返し観た作品は2回以上の作品だけが回数順に並ぶ`() {
        val summary = summarize(
            record(1, 2024, "A"),
            record(2, 2024, "B"),
            record(3, 2024, "B"),
            record(4, 2024, "C"),
            record(5, 2024, "C"),
            record(6, 2024, "C"),
        ).single()

        assertEquals(listOf(TitleCount("C", 3), TitleCount("B", 2)), summary.rewatchedTitles)
    }

    @Test
    fun `映画館は回数の多い順に並び未入力は除外される`() {
        val summary = summarize(
            record(1, 2024, "A", theater = "TOHOシネマズ 日比谷"),
            record(2, 2024, "B", theater = "109シネマズ"),
            record(3, 2024, "C", theater = "TOHOシネマズ 日比谷"),
            record(4, 2024, "D", theater = ""),
        ).single()

        assertEquals(
            listOf(TheaterCount("TOHOシネマズ 日比谷", 2), TheaterCount("109シネマズ", 1)),
            summary.theaters,
        )
    }
}
