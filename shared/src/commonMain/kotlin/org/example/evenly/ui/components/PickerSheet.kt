package org.example.evenly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing

@Composable
fun <T> PickerSheet(
    title: String,
    options: List<T>,
    onDismiss: () -> Unit,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    icon: ImageVector,
    selected: T?,
    modifier: Modifier = Modifier,
    subtitle: @Composable (T) -> String? = { null },
) {
    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = title) { sheet ->
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            options.forEach { option ->
                AppListTile(icon = icon, onClick = { sheet.closeThen { onSelect(option) } }, subtitle = subtitle(option), title = label(option)) {
                    if (option == selected) {
                        Icon(contentDescription = null, imageVector = Icons.Outlined.Check, tint = AppColors.primary)
                    }
                }
            }
        }
    }
}
