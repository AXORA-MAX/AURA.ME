package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AuraLightColorScheme = lightColorScheme(
    primary = AuraBlack,
    onPrimary = AuraWhite,
    primaryContainer = AuraNearBlack,
    onPrimaryContainer = AuraWhite,
    secondary = AuraNearBlack,
    onSecondary = AuraWhite,
    secondaryContainer = AuraTagBg,
    onSecondaryContainer = AuraTextMain,
    tertiary = AuraGold,
    onTertiary = AuraBlack,
    background = AuraCream,
    onBackground = AuraTextMain,
    surface = AuraWhite,
    onSurface = AuraTextMain,
    surfaceVariant = AuraTagBg,
    onSurfaceVariant = AuraTextSub,
    outline = AuraBorder,
    outlineVariant = AuraSilver,
    error = AuraDanger,
    onError = AuraWhite
)

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraWhite,
    onPrimary = AuraBlack,
    primaryContainer = AuraCardDark,
    onPrimaryContainer = AuraWhite,
    secondary = AuraSilver,
    onSecondary = AuraBlack,
    secondaryContainer = AuraNearBlack,
    onSecondaryContainer = AuraWhite,
    tertiary = AuraGold,
    onTertiary = AuraBlack,
    background = AuraBlack,
    onBackground = AuraWhite,
    surface = AuraNearBlack,
    onSurface = AuraWhite,
    surfaceVariant = AuraCardDark,
    onSurfaceVariant = AuraTextMuted,
    outline = AuraBorderDark,
    outlineVariant = AuraBorderDark,
    error = AuraDanger,
    onError = AuraWhite
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AuraDarkColorScheme else AuraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for previews or tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    AuraTheme(darkTheme = darkTheme, content = content)
}

