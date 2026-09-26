package org.example.evenly.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.example.evenly.ui.theme.AppColors

@Composable
fun AppSwitchTile(isChecked: Boolean, subtitle: String, title: String, onCheckedChange: (Boolean) -> Unit, icon: ImageVector, modifier: Modifier = Modifier, isEnabled: Boolean = true) {
    AppListTile(
        modifier = modifier,
        icon = icon,
        onClick = if (isEnabled) ({ onCheckedChange(!isChecked) }) else null,
        subtitle = subtitle,
        title = title,
    ) {
        Switch(
            checked = isChecked,
            colors = SwitchDefaults.colors(
                checkedBorderColor = AppColors.primary,
                checkedThumbColor = AppColors.primaryOn,
                checkedTrackColor = AppColors.primary,
                disabledUncheckedThumbColor = AppColors.textDisabled,
                disabledUncheckedTrackColor = AppColors.surfaceSunken,
                uncheckedBorderColor = AppColors.dividerStrong,
                uncheckedThumbColor = AppColors.textTertiary,
                uncheckedTrackColor = AppColors.surfaceSunken,
            ),
            enabled = isEnabled,
            onCheckedChange = null,
        )
    }
}
