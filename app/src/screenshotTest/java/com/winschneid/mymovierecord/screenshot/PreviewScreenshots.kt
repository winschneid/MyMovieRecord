package com.winschneid.mymovierecord.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
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
 * 対象にするには @PreviewTest が必要（プラグイン 0.0.1-alpha10 以降、無いものは描画されない）。
 *
 * 参照画像の生成: ./gradlew updateDebugScreenshotTest（出力先: app/src/screenshotTestDebug/reference）
 * 検証（CIで実行）: ./gradlew validateDebugScreenshotTest
 */

@PreviewTest
@Preview(showBackground = true)
@Composable
fun HistoryWithData_ss() = HistoryWithDataPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun HistoryEmpty_ss() = HistoryEmptyPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun HistoryLoading_ss() = HistoryLoadingPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun HistoryNoSearchResult_ss() = HistoryNoSearchResultPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AddMovieEmpty_ss() = AddMovieEmptyPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AddMovieFilled_ss() = AddMovieFilledPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AddMovieEdit_ss() = AddMovieEditPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun YearSummaryCollapsed_ss() = YearSummaryCollapsedPreview()

// 展開時は縦に長いため高さを伸ばして全体を撮る
@PreviewTest
@Preview(showBackground = true, heightDp = 1200)
@Composable
fun YearSummaryExpanded_ss() = YearSummaryExpandedPreview()

@PreviewTest
@Preview(showBackground = true)
@Composable
fun MovieDetail_ss() = MovieDetailPreview()
