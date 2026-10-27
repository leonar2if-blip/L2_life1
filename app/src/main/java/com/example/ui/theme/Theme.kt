package com.example.ui.theme

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
    primary = L2PrimaryDark,
    onPrimary = SlateBackgroundDark,
    primaryContainer = L2Primary.copy(alpha = 0.2f),
    onPrimaryContainer = L2PrimaryDark,
    secondary = L2SecondaryDark,
    onSecondary = SlateBackgroundDark,
    secondaryContainer = L2Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = L2SecondaryDark,
    tertiary = L2TertiaryDark,
    onTertiary = SlateBackgroundDark,
    background = SlateBackgroundDark,
    onBackground = SlateTextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateOutlineDark,
    outlineVariant = SlateOutlineDark.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = L2Primary,
    onPrimary = SlateSurfaceLight,
    primaryContainer = L2Primary.copy(alpha = 0.12f),
    onPrimaryContainer = L2Primary,
    secondary = L2Secondary,
    onSecondary = SlateSurfaceLight,
    secondaryContainer = L2Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = L2Secondary,
    tertiary = L2Tertiary,
    onTertiary = SlateSurfaceLight,
    background = SlateBackgroundLight,
    onBackground = SlateTextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateOutlineLight,
    outlineVariant = SlateOutlineLight.copy(alpha = 0.5f)
)

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

@Composable
fun L2LifeTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    dynamicColor: Boolean = false, // Keep consistent L2 brand colors by default
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSetting) {
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
    }

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
        content = content
    )
}
