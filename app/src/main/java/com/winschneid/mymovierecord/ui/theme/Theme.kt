package com.winschneid.mymovierecord.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Crimson80,
    primaryContainer = CrimsonContainer30,
    onPrimaryContainer = CrimsonContainer90,
    secondary = RoseGrey80,
    tertiary = Gold80,
)

private val LightColorScheme = lightColorScheme(
    primary = Crimson40,
    primaryContainer = CrimsonContainer90,
    onPrimaryContainer = OnCrimsonContainer10,
    secondary = RoseGrey40,
    tertiary = Gold40,
)

@Composable
fun MyMovieRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
