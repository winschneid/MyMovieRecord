package com.winschneid.mymovierecord.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/*
 * Material3 の DatePicker は「UTC の 0:00」で日付をやり取りする。
 * アプリ内の日時はローカルタイムゾーンで扱うため、ここで相互に変換する。
 * （変換しないと JST 0:00〜8:59 に前日が選択された状態で開いてしまう）
 */

/** ローカル日時 → 同じ暦日の UTC 0:00（DatePicker に渡す値） */
fun localToPickerMillis(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** DatePicker の値（UTC 0:00）→ 同じ暦日のローカル 12:00（日付の境界から遠い時刻で保存する） */
fun pickerToLocalMillis(pickerMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = pickerMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0)
    }.timeInMillis
}

/** 今日（ローカル）より後の日付は選べないようにする */
@OptIn(ExperimentalMaterial3Api::class)
object PastOrTodaySelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= localToPickerMillis(System.currentTimeMillis())

    override fun isSelectableYear(year: Int): Boolean =
        year <= Calendar.getInstance().get(Calendar.YEAR)
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(Date(timestamp))
