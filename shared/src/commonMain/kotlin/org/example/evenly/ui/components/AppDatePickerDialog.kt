package org.example.evenly.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_cancel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.evenly.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(initialUtcDateMillis: Long, maxUtcDateMillis: Long, confirmLabel: String, onDismiss: () -> Unit, onConfirm: (Long) -> Unit, modifier: Modifier = Modifier) {
    val selectableDates = remember(maxUtcDateMillis) {
        val maxYear = Instant.fromEpochMilliseconds(maxUtcDateMillis).toLocalDateTime(TimeZone.UTC).year

        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= maxUtcDateMillis

            override fun isSelectableYear(year: Int): Boolean = year <= maxYear
        }
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialUtcDateMillis, selectableDates = selectableDates)
    val colors = DatePickerDefaults.colors(
        containerColor = AppColors.surfaceCard,
        dayContentColor = AppColors.textPrimary,
        disabledDayContentColor = AppColors.textDisabled,
        headlineContentColor = AppColors.textPrimary,
        navigationContentColor = AppColors.primary,
        selectedDayContainerColor = AppColors.primary,
        selectedDayContentColor = AppColors.primaryOn,
        selectedYearContainerColor = AppColors.primary,
        selectedYearContentColor = AppColors.primaryOn,
        subheadContentColor = AppColors.textSecondary,
        titleContentColor = AppColors.textSecondary,
        todayContentColor = AppColors.primary,
        todayDateBorderColor = AppColors.primary,
        weekdayContentColor = AppColors.textSecondary,
        yearContentColor = AppColors.textPrimary,
    )

    DatePickerDialog(
        modifier = modifier,
        colors = colors,
        confirmButton = {
            PrimaryButton(isEnabled = state.selectedDateMillis != null, label = confirmLabel, onClick = { state.selectedDateMillis?.let(onConfirm) })
        },
        dismissButton = {
            SecondaryButton(label = stringResource(Res.string.action_cancel), onClick = onDismiss)
        },
        onDismissRequest = onDismiss,
    ) {
        DatePicker(colors = colors, state = state)
    }
}
