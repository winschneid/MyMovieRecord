package com.winschneid.mymovierecord.ui.screens.history

import com.winschneid.mymovierecord.domain.model.MovieRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class HistorySectionsTest {

    private fun dateOf(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }.timeInMillis

    private fun record(
        id: Long,
        date: Long,
        title: String,
        theater: String = "",
        review: String = "",
    ) = MovieRecord(
        id = id,
        title = title,
        theaterName = theater,
        date = date,
        review = review,
    )

    // DAO と同じ日付降順で渡す
    private val records = listOf(
        record(3, dateOf(2025, 3, 10), "ゴジラ-1.0", theater = "109シネマズ"),
        record(2, dateOf(2025, 1, 20), "パーフェクト・デイズ", theater = "TOHOシネマズ 日比谷", review = "トイレが美しい"),
        record(1, dateOf(2025, 1, 5), "ゴジラ-1.0", theater = "自宅"),
    )

    @Test
    fun `年月ごとにセクション分けされ日付降順を維持する`() {
        val sections = buildHistorySections(records, query = "")

        assertEquals(listOf("2025年3月", "2025年1月"), sections.map { it.label })
        assertEquals(listOf(3L), sections[0].items.map { it.id })
        assertEquals(listOf(2L, 1L), sections[1].items.map { it.id })
    }

    @Test
    fun `タイトル・映画館・感想で部分一致検索できる`() {
        assertEquals(
            listOf(3L, 1L),
            buildHistorySections(records, "ゴジラ").flatMap { it.items }.map { it.id },
        )
        assertEquals(
            listOf(2L),
            buildHistorySections(records, "toho").flatMap { it.items }.map { it.id },
        )
        assertEquals(
            listOf(2L),
            buildHistorySections(records, "トイレ").flatMap { it.items }.map { it.id },
        )
        assertEquals(
            emptyList<HistorySection>(),
            buildHistorySections(records, "存在しない"),
        )
    }

    @Test
    fun `記録が属する月見出しの一覧上の位置を返す（見出しも1行と数える）`() {
        val sections = buildHistorySections(records, query = "")

        // [0]2025年3月 [1]id=3 [2]2025年1月 [3]id=2 [4]id=1
        assertEquals(0, sections.listIndexOfHeaderFor(3L))
        assertEquals(2, sections.listIndexOfHeaderFor(1L))
        assertEquals(null, sections.listIndexOfHeaderFor(99L))
    }

    @Test
    fun `検索で絞り込んでもn回目はフィルタ前の全履歴基準のまま`() {
        val sections = buildHistorySections(records, "109")

        val item = sections.single().items.single()
        assertEquals(3L, item.id)
        // id=1（1回目）はヒットしていないが、id=3 は通算2回目のまま
        assertEquals(2, item.viewCount)
    }
}
