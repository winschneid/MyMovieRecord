package com.winschneid.mymovierecord.ui.components

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DatesTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    private fun jst(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0)
        }.timeInMillis

    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month - 1, day)
        }.timeInMillis

    @Test
    fun `深夜（JST 0時台）でも同じ暦日のUTC 0時に変換される`() {
        assertEquals(utcMidnight(2026, 9, 24), localToPickerMillis(jst(2026, 9, 24, 1)))
        assertEquals(utcMidnight(2026, 9, 24), localToPickerMillis(jst(2026, 9, 24, 23)))
    }

    @Test
    fun `DatePickerの値は同じ暦日のローカル12時になる`() {
        assertEquals(jst(2026, 9, 24, 12), pickerToLocalMillis(utcMidnight(2026, 9, 24)))
    }

    @Test
    fun `往復しても暦日が変わらない`() {
        val picked = localToPickerMillis(jst(2026, 1, 1, 0))
        assertEquals("2026/01/01", formatDate(pickerToLocalMillis(picked)))
    }

    @Test
    fun `未来の日付は選べない`() {
        val today = localToPickerMillis(System.currentTimeMillis())
        assertTrue(PastOrTodaySelectableDates.isSelectableDate(today))
        assertFalse(PastOrTodaySelectableDates.isSelectableDate(today + 24 * 60 * 60 * 1000L))
    }
}
