package com.winschneid.mymovierecord.data.transfer

import com.winschneid.mymovierecord.domain.model.MovieRecord
import org.json.JSONArray
import org.json.JSONObject

/**
 * バックアップ用のJSON形式。
 * {
 *   "version": 1,
 *   "records": [
 *     { "title": "...", "theater": "", "date": <epoch millis>, "rating": <0〜5>, "review": "" }
 *   ]
 * }
 */
object MovieRecordsJson {

    const val FORMAT_VERSION = 1

    fun encode(records: List<MovieRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject().apply {
                    put("title", record.title)
                    put("theater", record.theaterName)
                    put("date", record.date)
                    put("rating", record.rating)
                    put("review", record.review)
                }
            )
        }
        return JSONObject()
            .put("version", FORMAT_VERSION)
            .put("records", array)
            .toString(2)
    }

    /** 不正なJSONや必須項目欠落時は IllegalArgumentException / JSONException を投げる */
    fun decode(text: String): List<MovieRecord> {
        val root = JSONObject(text)
        val array = root.getJSONArray("records")
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            val title = obj.getString("title").trim()
            require(title.isNotEmpty()) { "record[$i]: title is empty" }
            MovieRecord(
                title = title,
                theaterName = obj.optString("theater", "").trim(),
                date = obj.getLong("date"),
                rating = obj.optInt("rating", 0).coerceIn(MovieRecord.MIN_RATING, MovieRecord.MAX_RATING),
                review = obj.optString("review", ""),
            )
        }
    }
}
