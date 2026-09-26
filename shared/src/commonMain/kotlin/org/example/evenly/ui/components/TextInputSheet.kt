package org.example.evenly.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun TextInputSheet(
    confirmLabel: String,
    initialValue: String,
    label: String,
    title: String,
    onDismiss: () -> Unit,
    errorMessage: (String) -> String?,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
) {
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    val error = if (value.isBlank()) null else errorMessage(value)

    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = title) { sheet ->
        AppTextField(capitalization = capitalization, label = label, onValueChange = { value = it }, value = value)
        if (error != null) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.errorHint, text = error)
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        SheetActions(onDismiss = onDismiss, sheet = sheet) {
            PrimaryButton(modifier = Modifier.weight(1f), isEnabled = value.isNotBlank() && error == null, label = confirmLabel, onClick = { sheet.closeThen { onConfirm(value.trim()) } })
        }
    }
}
