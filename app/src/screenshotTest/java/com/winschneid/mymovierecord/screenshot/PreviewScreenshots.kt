package com.winschneid.mymovierecord.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.winschneid.mymovierecord.ui.screens.add.AddMovieEditPreview
import com.winschneid.mymovierecord.ui.screens.add.AddMovieEmptyPreview
import com.winschneid.mymovierecord.ui.screens.add.AddMovieFilledPreview
import com.winschneid.mymovierecord.ui.screens.history.HistoryEmptyPreview
import com.winschneid.mymovierecord.ui.screens.history.HistoryLoadingPreview
import com.winschneid.mymovierecord.ui.screens.history.HistoryNoSearchResultPreview
import com.winschneid.mymovierecord.ui.screens.history.HistoryWithDataPreview
import com.winschneid.mymovierecord.ui.screens.movie.MovieDetailPreview
import com.winschneid.mymovierecord.ui.screens.summary.YearSummaryCollapsedPreview
import com.winschneid.mymovierecord.ui.screens.summary.YearSummaryExpandedPreview

/**
 * スクリーンショットテストの対象。各画面ファイルにある @Preview をそのまま呼び出して
 * 参照画像と比較する。プレビューの中身（サンプルデータ・テーマ）は各画面ファイル側で定義済み。
 *
 * 参照画像の生成: ./gradlew updateDebugScreenshotTest
 * 検証（CIで実行）: ./gradlew validateDebugScreenshotTest
 */

@Preview(showBackground = true)
@Composable
fun HistoryWithData_ss() = HistoryWithDataPreview()

@Preview(showBackground = true)
@Composable
fun HistoryEmpty_ss() = HistoryEmptyPreview()

@Preview(showBackground = true)
@Composable
fun HistoryLoading_ss() = HistoryLoadingPreview()

@Preview(showBackground = true)
@Composable
fun HistoryNoSearchResult_ss() = HistoryNoSearchResultPreview()

@Preview(showBackground = true)
@Composable
fun AddMovieEmpty_ss() = AddMovieEmptyPreview()

@Preview(showBackground = true)
@Composable
fun AddMovieFilled_ss() = AddMovieFilledPreview()

@Preview(showBackground = true)
@Composable
fun AddMovieEdit_ss() = AddMovieEditPreview()

@Preview(showBackground = true)
@Composable
fun YearSummaryCollapsed_ss() = YearSummaryCollapsedPreview()

// 展開時は縦に長いため高さを伸ばして全体を撮る
@Preview(showBackground = true, heightDp = 1200)
@Composable
fun YearSummaryExpanded_ss() = YearSummaryExpandedPreview()

@Preview(showBackground = true)
@Composable
fun MovieDetail_ss() = MovieDetailPreview()
