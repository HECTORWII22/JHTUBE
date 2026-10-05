package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// JHTube Pure White Theme ("QUE EL FONDO SEA BLANCO")
private val JhTubeLightColorScheme = lightColorScheme(
    primary = JhTubeRed,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = JhTubeRed,
    secondary = PureBlack,
    onSecondary = PureWhite,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = PureBlack,
    tertiary = IconGray,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = TextHighEmphasis,
    surface = PureWhite,
    onSurface = TextHighEmphasis,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMediumEmphasis,
    outline = LightCardBorder,
    error = Color(0xFFBA1A1A),
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PureWhite.toArgb()
                window.navigationBarColor = PureWhite.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = JhTubeLightColorScheme,
        typography = Typography,
        content = content
    )
}
