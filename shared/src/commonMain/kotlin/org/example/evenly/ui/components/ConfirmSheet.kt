package org.example.evenly.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Composable
fun ConfirmSheet(confirmLabel: String, message: String, title: String, onConfirm: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier, isDestructive: Boolean = false) {
    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = title) { sheet ->
        Text(modifier = Modifier.fillMaxWidth(), style = AppTheme.textStyles.body, text = message)
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        SheetActions(onDismiss = onDismiss, sheet = sheet) {
            if (isDestructive) {
                DangerButton(modifier = Modifier.weight(1f), label = confirmLabel, onClick = { sheet.closeThen(onConfirm) })
            } else {
                PrimaryButton(modifier = Modifier.weight(1f), label = confirmLabel, onClick = { sheet.closeThen(onConfirm) })
            }
        }
    }
}
