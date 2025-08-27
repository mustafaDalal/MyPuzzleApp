package com.md.mypuzzleapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val primaryBackground: Color,
    val onPrimaryBackground: Color,
    val secondaryBackground: Color,
    val onSecondaryBackground: Color,
    val cardBackground: Color,
    val onCardBackground: Color,
    val tileBackground: Color,
    val onTileBackground: Color,
    val fabBackground: Color,
    val onFabBackground: Color,
    val puzzlePieceBackground: Color,
    val onPuzzlePieceBackground: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    // Sensible defaults, overridden by Theme provider
    ExtendedColors(
        primaryBackground = Color(0xFFE0E0FF),
        onPrimaryBackground = Color(0xFF12123A),
        secondaryBackground = Color(0xFFBBF1EC),
        onSecondaryBackground = Color(0xFF00201D),
        cardBackground = Color.White,
        onCardBackground = Color(0xFF1B1B1F),
        tileBackground = Color(0xFFE3E1EC),
        onTileBackground = Color(0xFF46464F),
        fabBackground = Color(0xFF5B5BD6),
        onFabBackground = Color.White,
        puzzlePieceBackground = Color(0xFFFFD8CC),
        onPuzzlePieceBackground = Color(0xFF2B0A02)
    )
}

fun extendedColorsFrom(colorScheme: ColorScheme): ExtendedColors = ExtendedColors(
    primaryBackground = colorScheme.primaryContainer,
    onPrimaryBackground = colorScheme.onPrimaryContainer,
    secondaryBackground = colorScheme.secondaryContainer,
    onSecondaryBackground = colorScheme.onSecondaryContainer,
    cardBackground = colorScheme.surface,
    onCardBackground = colorScheme.onSurface,
    tileBackground = colorScheme.surfaceVariant,
    onTileBackground = colorScheme.onSurfaceVariant,
    fabBackground = colorScheme.primary,
    onFabBackground = colorScheme.onPrimary,
    puzzlePieceBackground = colorScheme.tertiaryContainer,
    onPuzzlePieceBackground = colorScheme.onTertiaryContainer
)
