package org.example.evenly.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.sf_pro_display_bold
import evenly.shared.generated.resources.sf_pro_display_medium
import evenly.shared.generated.resources.sf_pro_display_regular
import org.jetbrains.compose.resources.Font

private const val SELECTION_ALPHA: Float = 0.24f

private val EvenlyColorScheme: ColorScheme = lightColorScheme(
    background = AppColors.surfaceBase,
    error = AppColors.danger,
    errorContainer = AppColors.danger,
    inverseOnSurface = AppColors.primaryOn,
    inverseSurface = AppColors.surfaceInverse,
    onBackground = AppColors.textPrimary,
    onError = AppColors.primaryOn,
    onErrorContainer = AppColors.primaryOn,
    onPrimary = AppColors.primaryOn,
    onSecondary = AppColors.primaryOn,
    onSurface = AppColors.textPrimary,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.divider,
    outlineVariant = AppColors.divider,
    primary = AppColors.primary,
    primaryContainer = AppColors.primary,
    scrim = AppColors.surfaceInverse,
    secondary = AppColors.primary,
    surface = AppColors.surfaceCard,
    surfaceContainer = AppColors.surfaceCard,
    surfaceContainerHigh = AppColors.surfaceCard,
    surfaceContainerHighest = AppColors.surfaceField,
    surfaceContainerLow = AppColors.surfaceBase,
    surfaceContainerLowest = AppColors.surfaceBase,
    surfaceVariant = AppColors.surfaceField,
    tertiary = AppColors.primary,
)

private val LocalAppTextStyles: ProvidableCompositionLocal<AppTextStyles> = staticCompositionLocalOf { error("AppTextStyles are provided by EvenlyTheme") }

@Composable
fun EvenlyTheme(content: @Composable () -> Unit) {
    val sfProDisplayFamily = FontFamily(
        Font(Res.font.sf_pro_display_regular, FontWeight.Normal),
        Font(Res.font.sf_pro_display_medium, FontWeight.Medium),
        Font(Res.font.sf_pro_display_bold, FontWeight.Bold),
    )
    val textStyles = remember(sfProDisplayFamily) { AppTextStyles(sfProDisplayFamily) }
    val typography = remember(textStyles) { textStyles.toTypography() }
    val selectionColors = TextSelectionColors(backgroundColor = AppColors.primary.copy(alpha = SELECTION_ALPHA), handleColor = AppColors.primary)

    CompositionLocalProvider(LocalAppTextStyles provides textStyles, LocalTextSelectionColors provides selectionColors) {
        MaterialTheme(colorScheme = EvenlyColorScheme, typography = typography, content = content)
    }
}

private fun AppTextStyles.toTypography(): Typography = Typography(
    bodyLarge = body,
    bodyMedium = listSecondary,
    bodySmall = errorHint,
    labelLarge = button,
    labelMedium = label,
    labelSmall = fieldLabel,
    titleLarge = screenTitle,
    titleMedium = sectionHeading,
    titleSmall = listPrimary,
)

object AppTheme {
    val textStyles: AppTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTextStyles.current
}
