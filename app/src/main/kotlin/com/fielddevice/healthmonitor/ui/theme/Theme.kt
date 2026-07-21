package com.fielddevice.healthmonitor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    secondary = TealSecondary,
    secondaryContainer = TealSecondaryContainer,
    background = SurfaceLight,
    surface = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = GreenContainer,
    onPrimary = GreenPrimaryDark,
    primaryContainer = GreenPrimaryDark,
    secondary = TealSecondaryContainer,
    background = SurfaceDark,
    surface = SurfaceDark
)

/**
 * App-wide Material 3 theme. Dynamic color (Material You) is opted into on Android 12+ so
 * the app matches a device's wallpaper-derived palette when available, falling back to the
 * fixed brand palette above on older OS versions — a nod to real fleet deployments spanning
 * several Android releases at once.
 */
@Composable
fun FieldHealthMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
