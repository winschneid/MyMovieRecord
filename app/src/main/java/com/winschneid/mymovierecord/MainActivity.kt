package com.winschneid.mymovierecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.winschneid.mymovierecord.ui.navigation.NavGraph
import com.winschneid.mymovierecord.ui.theme.MyMovieRecordTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMovieRecordTheme {
                NavGraph()
            }
        }
    }
}
