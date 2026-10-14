package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = SoftSage,
    onPrimaryContainer = ForestGreenDark,
    secondary = CatWork,
    onSecondary = Color.White,
    secondaryContainer = CatWorkBg,
    onSecondaryContainer = CharcoalInk,
    tertiary = CatPersonal,
    onTertiary = Color.White,
    background = PaperBg,
    onBackground = CharcoalInk,
    surface = SurfaceWhite,
    onSurface = CharcoalInk,
    surfaceVariant = SurfaceVariantWarm,
    onSurfaceVariant = MutedText,
    outline = BorderLine,
    outlineVariant = BorderLine
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftSage,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color.White,
    secondary = CatWork,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkInk,
    tertiary = CatPersonal,
    onTertiary = Color.White,
    background = DarkPaperBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorderLine,
    outlineVariant = DarkBorderLine
)

@Composable
fun LifeOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
