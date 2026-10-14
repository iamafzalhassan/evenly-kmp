package org.example.evenly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Immutable
class AppTextStyles(private val fontFamily: FontFamily) {
    val amount: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 15, tabular = true, weight = FontWeight.SemiBold)
    val body: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 15, weight = FontWeight.Normal)
    val button: TextStyle = style(color = AppColors.primaryOn, height = 1.2f, size = 16, tracking = 0.2f, weight = FontWeight.SemiBold)
    val errorHint: TextStyle = style(color = AppColors.danger, height = 1.3f, size = 12, weight = FontWeight.Medium)
    val fieldLabel: TextStyle = style(color = AppColors.textSecondary, height = 1.3f, size = 11, weight = FontWeight.Medium)
    val fieldValue: TextStyle = style(color = AppColors.textPrimary, height = 1.35f, size = 15, tabular = true, weight = FontWeight.SemiBold)
    val inviteCode: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 17, tabular = true, tracking = 4f, weight = FontWeight.Bold)
    val label: TextStyle = style(color = AppColors.textSecondary, height = 1.2f, size = 14, weight = FontWeight.Normal)
    val listMeta: TextStyle = style(color = AppColors.textSecondary, height = 1.3f, size = 13, tabular = true, weight = FontWeight.Normal)
    val listPrimary: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 15, weight = FontWeight.SemiBold)
    val listSecondary: TextStyle = style(color = AppColors.textSecondary, height = 1.3f, size = 13, weight = FontWeight.Normal)
    val overline: TextStyle = style(color = AppColors.textSecondary, height = 1.2f, size = 11, tracking = 0.8f, weight = FontWeight.SemiBold)
    val screenTitle: TextStyle = style(color = AppColors.textPrimary, height = 1.2f, size = 20, weight = FontWeight.Bold)
    val sectionHeading: TextStyle = style(color = AppColors.textPrimary, height = 1.2f, size = 17, weight = FontWeight.SemiBold)
    val snack: TextStyle = style(color = AppColors.primaryOn, height = 1.3f, size = 14, weight = FontWeight.Medium)
    val totalsValue: TextStyle = style(color = AppColors.textPrimary, height = 1.3f, size = 17, tabular = true, weight = FontWeight.Medium)

    val minFittedFontSize: TextUnit = 11.sp

    private fun style(height: Float, size: Int, color: Color, weight: FontWeight, tabular: Boolean = false, tracking: Float = 0f): TextStyle = TextStyle(
        color = color,
        fontFamily = fontFamily,
        fontFeatureSettings = if (tabular) "tnum" else null,
        fontSize = size.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
        lineHeight = (size * height).sp,
    )
}
