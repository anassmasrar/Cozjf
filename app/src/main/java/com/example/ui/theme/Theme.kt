package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val OmniDarkColorScheme = darkColorScheme(
    primary = SteelBlue,
    onPrimary = VoidBlack,
    primaryContainer = SteelBlueDim,
    onPrimaryContainer = TextPrimary,
    secondary = SlateSilver,
    onSecondary = VoidBlack,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = PrecisionAmber,
    onTertiary = VoidBlack,
    tertiaryContainer = PrecisionAmberDim,
    onTertiaryContainer = TextPrimary,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = PanelSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = VoidBlack,
    surfaceContainerLow = PanelSurface,
    surfaceContainer = ElevatedSurface,
    surfaceContainerHigh = CardSurface,
    surfaceContainerHighest = BorderHairline,
    outline = BorderHairline,
    outlineVariant = BorderSubtle,
    error = CrimsonAlert,
    onError = TextPrimary,
    errorContainer = CrimsonAlertDim,
    onErrorContainer = TextPrimary
)

val OmniShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(10.dp)
)

@Composable
fun OmniLinkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OmniDarkColorScheme,
        typography = OmniTypography,
        shapes = OmniShapes,
        content = content
    )
}
