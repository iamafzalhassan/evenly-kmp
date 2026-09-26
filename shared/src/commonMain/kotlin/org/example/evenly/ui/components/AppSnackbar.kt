package org.example.evenly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

private const val SNACK_MAX_LINES: Int = 2

enum class SnackTone { ERROR, NEUTRAL, SUCCESS }

private data class TonedSnackbarVisuals(
    override val withDismissAction: Boolean,
    override val message: String,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    val tone: SnackTone,
) : SnackbarVisuals

@Composable
fun AppSnackbarHost(state: AppSnackbarState, modifier: Modifier = Modifier) {
    SnackbarHost(modifier = modifier, hostState = state.hostState) { data ->
        val tone = (data.visuals as? TonedSnackbarVisuals)?.tone ?: SnackTone.NEUTRAL

        Snackbar(
            containerColor = when (tone) {
                SnackTone.ERROR -> AppColors.danger
                SnackTone.NEUTRAL -> AppColors.surfaceInverse
                SnackTone.SUCCESS -> AppColors.success
            },
            contentColor = AppColors.primaryOn,
            shape = RectangleShape,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                maxLines = SNACK_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.textStyles.snack,
                text = data.visuals.message,
            )
        }
    }
}

@Composable
fun rememberAppSnackbarState(): AppSnackbarState = remember { AppSnackbarState(SnackbarHostState()) }

@Stable
class AppSnackbarState(val hostState: SnackbarHostState) {
    suspend fun showBrief(message: String) = show(message = message, tone = SnackTone.NEUTRAL)

    suspend fun showError(message: String) = show(message = message, tone = SnackTone.ERROR)

    suspend fun showSuccess(message: String) = show(message = message, tone = SnackTone.SUCCESS)

    private suspend fun show(message: String, tone: SnackTone) {
        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(TonedSnackbarVisuals(withDismissAction = false, message = message, actionLabel = null, duration = SnackbarDuration.Long, tone = tone))
    }
}
