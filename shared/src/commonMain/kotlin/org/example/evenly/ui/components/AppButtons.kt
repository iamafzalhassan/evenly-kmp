package org.example.evenly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) {
    Button(
        modifier = modifier.heightIn(min = AppSpacing.buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            contentColor = AppColors.primaryOn,
            disabledContainerColor = AppColors.surfaceSunken,
            disabledContentColor = AppColors.textDisabled,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
        elevation = null,
        enabled = isEnabled,
        onClick = onClick,
        shape = RoundedCornerShape(AppSpacing.radiusButton),
    ) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.button.copy(color = if (isEnabled) AppColors.primaryOn else AppColors.textDisabled), text = label)
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) = OutlinedPillButton(modifier = modifier, accent = AppColors.primary, isEnabled = isEnabled, label = label, onClick = onClick)

@Composable
fun DangerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) = OutlinedPillButton(modifier = modifier, accent = AppColors.danger, isEnabled = isEnabled, label = label, onClick = onClick)

@Composable
private fun OutlinedPillButton(isEnabled: Boolean, label: String, onClick: () -> Unit, accent: Color, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = modifier.heightIn(min = AppSpacing.buttonHeight),
        border = BorderStroke(AppSpacing.hairline, if (isEnabled) accent else AppColors.divider),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = accent, disabledContentColor = AppColors.textDisabled),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
        enabled = isEnabled,
        onClick = onClick,
        shape = RoundedCornerShape(AppSpacing.radiusButton),
    ) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.button.copy(color = if (isEnabled) accent else AppColors.textDisabled), text = label)
    }
}
