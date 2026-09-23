package com.winschneid.mymovierecord.ui.screens.add

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestionsTest {

    @Test
    fun `空入力では候補を出さない`() {
        assertEquals(emptyList<String>(), listOf("ゴジラ-1.0").suggestFor("  "))
    }

    @Test
    fun `完全一致は候補から除き大文字小文字は区別しない`() {
        assertEquals(
            listOf("Godzilla Minus One"),
            listOf("Godzilla", "Godzilla Minus One").suggestFor("godzilla"),
        )
    }

    @Test
    fun `前方一致を部分一致より先に並べる`() {
        assertEquals(
            listOf("ゴジラ-1.0", "シン・ゴジラ"),
            listOf("シン・ゴジラ", "ゴジラ-1.0").suggestFor("ゴジラ"),
        )
    }

    @Test
    fun `候補は最大5件`() {
        val titles = (1..8).map { "映画$it" }
        assertEquals(5, titles.suggestFor("映画").size)
    }
}
