package org.example.evenly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalSheet(title: String, onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.(sheet: AppSheetState) -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheet = remember(scope, sheetState) { AppSheetState(scope = scope, sheetState = sheetState) }

    ModalBottomSheet(modifier = modifier, containerColor = AppColors.surfaceCard, onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = AppSpacing.lg, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg)) {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.sectionHeading, text = title)
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            DottedDivider()
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            content(sheet)
        }
    }
}

@Composable
fun SheetActions(onDismiss: () -> Unit, sheet: AppSheetState, modifier: Modifier = Modifier, dismissLabel: String = stringResource(Res.string.action_cancel), primary: @Composable RowScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        DottedDivider()
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SecondaryButton(modifier = Modifier.weight(1f), label = dismissLabel, onClick = { sheet.closeThen(onDismiss) })
            primary()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class AppSheetState internal constructor(private val scope: CoroutineScope, internal val sheetState: SheetState) {
    private var isClosing: Boolean = false

    fun closeThen(action: () -> Unit) {
        if (isClosing) return
        isClosing = true
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }
}
