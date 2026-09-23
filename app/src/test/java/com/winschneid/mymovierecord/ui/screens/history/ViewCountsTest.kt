package com.winschneid.mymovierecord.ui.screens.history

import com.winschneid.mymovierecord.domain.model.MovieRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewCountsTest {

    private fun record(id: Long, date: Long, title: String) = MovieRecord(
        id = id,
        title = title,
        date = date,
    )

    @Test
    fun `同じ作品は日付昇順に1から連番が振られる`() {
        val records = listOf(
            record(id = 3, date = 3000, "ゴジラ-1.0"),
            record(id = 1, date = 1000, "ゴジラ-1.0"),
            record(id = 2, date = 2000, "ゴジラ-1.0"),
        )

        val counts = computeViewCounts(records)

        assertEquals(mapOf(1L to 1, 2L to 2, 3L to 3), counts)
    }

    @Test
    fun `作品ごとに独立してカウントされる`() {
        val records = listOf(
            record(id = 1, date = 1000, "A"),
            record(id = 2, date = 2000, "B"),
            record(id = 3, date = 3000, "A"),
        )

        val counts = computeViewCounts(records)

        assertEquals(mapOf(1L to 1, 2L to 1, 3L to 2), counts)
    }

    @Test
    fun `同じ日時の記録は登録順（id順）で数える`() {
        val records = listOf(
            record(id = 5, date = 1000, "A"),
            record(id = 4, date = 1000, "A"),
        )

        val counts = computeViewCounts(records)

        assertEquals(mapOf(4L to 1, 5L to 2), counts)
    }

    @Test
    fun `空リストは空マップを返す`() {
        assertEquals(emptyMap<Long, Int>(), computeViewCounts(emptyList()))
    }
}
