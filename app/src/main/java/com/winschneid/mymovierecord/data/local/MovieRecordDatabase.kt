package com.winschneid.mymovierecord.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.winschneid.mymovierecord.data.local.dao.MovieRecordDao
import com.winschneid.mymovierecord.data.local.entity.MovieRecordEntity

@Database(
    entities = [MovieRecordEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class MovieRecordDatabase : RoomDatabase() {
    abstract fun movieRecordDao(): MovieRecordDao

    companion object {
        /**
         * v1 → v2: rating を NULL 許容にし、NULL を「未評価」とする。
         * v1 では未入力時の既定値が 0 だったため、既存の 0 は未評価（NULL）として移行する。
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE movie_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        theater_name TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        rating INTEGER,
                        review TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO movie_records_new (id, title, theater_name, date, rating, review)
                    SELECT id, title, theater_name, date, NULLIF(rating, 0), review
                    FROM movie_records
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE movie_records")
                db.execSQL("ALTER TABLE movie_records_new RENAME TO movie_records")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_movie_records_title ON movie_records (title)")
            }
        }
    }
}
