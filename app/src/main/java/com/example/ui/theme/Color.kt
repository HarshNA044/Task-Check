package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.model.TaskPriority

// Professional Polish Theme Colors
val M3Primary = Color(0xFF6750A4)
val M3PrimaryContainer = Color(0xFFEADDFF)
val M3OnPrimaryContainer = Color(0xFF21005D)
val M3FabContainer = Color(0xFFD0BCFF)

val M3Secondary = Color(0xFF625B71)
val M3SecondaryContainer = Color(0xFFE8DEF8)
val M3OnSecondaryContainer = Color(0xFF1D192B)

val M3Background = Color(0xFFFEF7FF)
val M3Surface = Color(0xFFFFFFFF)
val M3SurfaceVariant = Color(0xFFF3EDF7)
val M3OnSurface = Color(0xFF1D1B20)
val M3OnSurfaceVariant = Color(0xFF49454F)

val M3BorderOutline = Color(0xFFCAC4D0)
val M3BorderSubtle = Color(0xFFE7E0EC)
val M3NavBackground = Color(0xFFF3EDF7)

// Priority Colors (Light Mode)
val PriorityHigh = Color(0xFFB3261E)
val PriorityHighBg = Color(0xFFF9DEDC)
val PriorityHighText = Color(0xFF410E0B)

val PriorityMedium = Color(0xFF6750A4)
val PriorityMediumBg = Color(0xFFE8DEF8)
val PriorityMediumText = Color(0xFF1D192B)

val PriorityLow = Color(0xFF10B981)
val PriorityLowBg = Color(0xFFD1E7DD)
val PriorityLowText = Color(0xFF0F5132)

// Compatibility Tokens
val PrimaryIndigo = M3Primary
val PrimaryIndigoLight = Color(0xFFD0BCFF)
val PrimaryIndigoDark = Color(0xFF21005D)
val SecondaryCyan = M3Primary
val SecondaryCyanDark = M3OnPrimaryContainer
val TertiaryEmerald = Color(0xFF10B981)
val TertiaryEmeraldDark = Color(0xFF059669)

// Dark Theme Variants
val M3DarkPrimary = Color(0xFFD0BCFF)
val M3DarkOnPrimary = Color(0xFF381E72)
val M3DarkPrimaryContainer = Color(0xFF4F378B)
val M3DarkOnPrimaryContainer = Color(0xFFEADDFF)
val M3DarkFabContainer = Color(0xFF4F378B)

val M3DarkSecondary = Color(0xFFCCC2DC)
val M3DarkOnSecondary = Color(0xFF332D41)
val M3DarkSecondaryContainer = Color(0xFF4A4458)
val M3DarkOnSecondaryContainer = Color(0xFFE8DEF8)

val M3DarkBackground = Color(0xFF141218)
val M3DarkSurface = Color(0xFF211F26)
val M3DarkSurfaceVariant = Color(0xFF2B2930)
val M3DarkOnSurface = Color(0xFFE6E0E9)
val M3DarkOnSurfaceVariant = Color(0xFFCAC4D0)
val M3DarkOutline = Color(0xFF49454F)
val M3DarkOutlineVariant = Color(0xFF332F37)
val M3DarkNavBackground = Color(0xFF1D1B20)

val PriorityHighDarkBg = Color(0xFF601410)
val PriorityHighDarkText = Color(0xFFF2B8B5)
val PriorityMediumDarkBg = Color(0xFF4F378B)
val PriorityMediumDarkText = Color(0xFFEADDFF)
val PriorityLowDarkBg = Color(0xFF064E3B)
val PriorityLowDarkText = Color(0xFFA7F3D0)

// Score & Graph Colors
val ScoreExcellent = Color(0xFF6750A4)
val ScoreGood = Color(0xFF7E57C2)
val ScoreAverage = Color(0xFFE8DEF8)
val ScoreLow = Color(0xFFB3261E)

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
