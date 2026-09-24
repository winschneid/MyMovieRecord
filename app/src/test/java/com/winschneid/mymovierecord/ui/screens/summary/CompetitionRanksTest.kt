package com.winschneid.mymovierecord.ui.screens.summary

import org.junit.Assert.assertEquals
import org.junit.Test

class CompetitionRanksTest {

    @Test
    fun `同じ値は同順位になり次の順位は人数分とばす`() {
        assertEquals(listOf(1, 2, 2, 4), listOf(5, 3, 3, 1).competitionRanks { it })
    }

    @Test
    fun `全て同じ値なら全員1位`() {
        assertEquals(listOf(1, 1, 1), listOf(1, 1, 1).competitionRanks { it })
    }
}
