package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.model.TaskPriority

// Signature App Logo Color Palette (Blue Checkmark, Green Progress Bar, Amber Lightbulb)
val LogoBluePrimary = Color(0xFF1A73E8)
val LogoBluePrimaryContainer = Color(0xFFE8F0FE)
val LogoBlueOnPrimaryContainer = Color(0xFF041E49)
val LogoBlueFabContainer = Color(0xFFD3E3FD)

val LogoGreenSecondary = Color(0xFF10B981)
val LogoGreenSecondaryContainer = Color(0xFFE6F4EA)
val LogoGreenOnSecondaryContainer = Color(0xFF064E3B)

val LogoAmberTertiary = Color(0xFFF59E0B)
val LogoAmberTertiaryContainer = Color(0xFFFEF7E0)
val LogoAmberOnTertiaryContainer = Color(0xFF78350F)

// M3 Theme Base Colors (Light Mode)
val M3Primary = LogoBluePrimary
val M3PrimaryContainer = LogoBluePrimaryContainer
val M3OnPrimaryContainer = LogoBlueOnPrimaryContainer
val M3FabContainer = LogoBlueFabContainer

val M3Secondary = LogoGreenSecondary
val M3SecondaryContainer = LogoGreenSecondaryContainer
val M3OnSecondaryContainer = LogoGreenOnSecondaryContainer

val M3Tertiary = LogoAmberTertiary
val M3TertiaryContainer = LogoAmberTertiaryContainer
val M3OnTertiaryContainer = LogoAmberOnTertiaryContainer

val M3Background = Color(0xFFF8FAFD)
val M3Surface = Color(0xFFFFFFFF)
val M3SurfaceVariant = Color(0xFFEFF4FA)
val M3OnSurface = Color(0xFF1F2937)
val M3OnSurfaceVariant = Color(0xFF4B5563)

val M3BorderOutline = Color(0xFFD8E0EA)
val M3BorderSubtle = Color(0xFFE8EEF5)
val M3NavBackground = Color(0xFFEFF4FA)

// Priority Colors (Light Mode)
val PriorityHigh = Color(0xFFDC2626)
val PriorityHighBg = Color(0xFFFEE2E2)
val PriorityHighText = Color(0xFF7F1D1D)

val PriorityMedium = LogoBluePrimary
val PriorityMediumBg = LogoBluePrimaryContainer
val PriorityMediumText = LogoBlueOnPrimaryContainer

val PriorityLow = LogoGreenSecondary
val PriorityLowBg = LogoGreenSecondaryContainer
val PriorityLowText = LogoGreenOnSecondaryContainer

// Compatibility Tokens
val PrimaryIndigo = M3Primary
val PrimaryIndigoLight = LogoBlueFabContainer
val PrimaryIndigoDark = LogoBlueOnPrimaryContainer
val SecondaryCyan = M3Secondary
val SecondaryCyanDark = LogoGreenOnSecondaryContainer
val TertiaryEmerald = LogoGreenSecondary
val TertiaryEmeraldDark = Color(0xFF059669)

// Dark Theme Variants (Deep Navy Slate Canvas matching Logo)
val M3DarkPrimary = Color(0xFF8AB4F8)
val M3DarkOnPrimary = Color(0xFF002B75)
val M3DarkPrimaryContainer = Color(0xFF174EA6)
val M3DarkOnPrimaryContainer = Color(0xFFD3E3FD)
val M3DarkFabContainer = Color(0xFF1A73E8)

val M3DarkSecondary = Color(0xFF34D399)
val M3DarkOnSecondary = Color(0xFF064E3B)
val M3DarkSecondaryContainer = Color(0xFF065F46)
val M3DarkOnSecondaryContainer = Color(0xFFA7F3D0)

val M3DarkTertiary = Color(0xFFFBBF24)
val M3DarkOnTertiary = Color(0xFF78350F)
val M3DarkTertiaryContainer = Color(0xFF92400E)
val M3DarkOnTertiaryContainer = Color(0xFFFDE68A)

val M3DarkBackground = Color(0xFF0B132B)
val M3DarkSurface = Color(0xFF16213E)
val M3DarkSurfaceVariant = Color(0xFF1E2D4A)
val M3DarkOnSurface = Color(0xFFF1F5F9)
val M3DarkOnSurfaceVariant = Color(0xFF94A3B8)
val M3DarkOutline = Color(0xFF334155)
val M3DarkOutlineVariant = Color(0xFF1E293B)
val M3DarkNavBackground = Color(0xFF0F172A)

val PriorityHighDarkBg = Color(0xFF7F1D1D)
val PriorityHighDarkText = Color(0xFFFECACA)
val PriorityMediumDarkBg = Color(0xFF174EA6)
val PriorityMediumDarkText = Color(0xFFD3E3FD)
val PriorityLowDarkBg = Color(0xFF065F46)
val PriorityLowDarkText = Color(0xFFA7F3D0)

// Score & Graph Colors matching logo
val ScoreExcellent = LogoGreenSecondary
val ScoreGood = LogoBluePrimary
val ScoreAverage = LogoAmberTertiary
val ScoreLow = Color(0xFFDC2626)

data class PriorityColors(
    val background: Color,
    val text: Color,
    val indicator: Color
)

@Composable
fun getAdaptivePriorityColors(priority: TaskPriority): PriorityColors {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (priority) {
        TaskPriority.HIGH -> PriorityColors(
            background = if (isDark) PriorityHighDarkBg else PriorityHighBg,
            text = if (isDark) PriorityHighDarkText else PriorityHighText,
            indicator = if (isDark) Color(0xFFFF897D) else PriorityHigh
        )
        TaskPriority.MEDIUM -> PriorityColors(
            background = if (isDark) PriorityMediumDarkBg else PriorityMediumBg,
            text = if (isDark) PriorityMediumDarkText else PriorityMediumText,
            indicator = MaterialTheme.colorScheme.primary
        )
        TaskPriority.LOW -> PriorityColors(
            background = if (isDark) PriorityLowDarkBg else PriorityLowBg,
            text = if (isDark) PriorityLowDarkText else PriorityLowText,
            indicator = if (isDark) Color(0xFF6EE7B7) else PriorityLow
        )
    }
}
