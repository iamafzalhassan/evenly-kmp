package org.example.evenly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun <T> ChoiceRow(options: List<T>, label: (T) -> String, onSelect: (T) -> Unit, selected: T, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppSpacing.radiusButton)

    Row(modifier = modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        options.forEach { option ->
            val isSelected = option == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .heightIn(min = AppSpacing.chipHeight)
                    .clip(shape)
                    .background(color = if (isSelected) AppColors.primary else AppColors.surfaceCard, shape = shape)
                    .border(border = BorderStroke(AppSpacing.hairline, if (isSelected) AppColors.primary else AppColors.divider), shape = shape)
                    .selectable(onClick = { onSelect(option) }, role = Role.RadioButton, selected = isSelected),
                contentAlignment = Alignment.Center,
            ) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.listPrimary.copy(color = if (isSelected) AppColors.primaryOn else AppColors.textPrimary), text = label(option))
            }
        }
    }
}
