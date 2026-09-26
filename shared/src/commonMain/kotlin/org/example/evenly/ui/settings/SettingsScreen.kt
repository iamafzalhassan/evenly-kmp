package org.example.evenly.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_sync_now
import evenly.shared.generated.resources.lock_prompt_title
import evenly.shared.generated.resources.settings_app_lock_available
import evenly.shared.generated.resources.settings_app_lock_disabled
import evenly.shared.generated.resources.settings_app_lock_enabled
import evenly.shared.generated.resources.settings_app_lock_hint
import evenly.shared.generated.resources.settings_app_lock_not_enrolled
import evenly.shared.generated.resources.settings_app_lock_reason
import evenly.shared.generated.resources.settings_app_lock_title
import evenly.shared.generated.resources.settings_app_lock_unavailable
import evenly.shared.generated.resources.settings_data_heading
import evenly.shared.generated.resources.settings_data_message
import evenly.shared.generated.resources.settings_save_failed
import evenly.shared.generated.resources.settings_security_heading
import evenly.shared.generated.resources.settings_sync_heading
import evenly.shared.generated.resources.settings_sync_hint
import evenly.shared.generated.resources.settings_sync_last
import evenly.shared.generated.resources.settings_sync_never
import evenly.shared.generated.resources.settings_sync_offline
import evenly.shared.generated.resources.settings_sync_server
import evenly.shared.generated.resources.settings_sync_syncing
import evenly.shared.generated.resources.settings_sync_title
import evenly.shared.generated.resources.settings_sync_unexpected
import evenly.shared.generated.resources.settings_synced
import evenly.shared.generated.resources.settings_title
import evenly.shared.generated.resources.sync_failed
import kotlinx.coroutines.launch
import org.example.evenly.data.sync.SyncFailure
import org.example.evenly.data.sync.SyncStatus
import org.example.evenly.security.BiometricAuthenticator
import org.example.evenly.security.BiometricAvailability
import org.example.evenly.security.BiometricResult
import org.example.evenly.ui.components.AppListTile
import org.example.evenly.ui.components.AppSnackbarHost
import org.example.evenly.ui.components.AppSwitchTile
import org.example.evenly.ui.components.AppTopBar
import org.example.evenly.ui.components.SecondaryButton
import org.example.evenly.ui.components.SectionHeader
import org.example.evenly.ui.components.rememberAppSnackbarState
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme
import org.example.evenly.util.DateFormat
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(biometricAuthenticator: BiometricAuthenticator, onBack: () -> Unit, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarState = rememberAppSnackbarState()
    var availability by remember { mutableStateOf(biometricAuthenticator.availability()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        availability = biometricAuthenticator.availability()
    }

    LaunchedEffect(state.feedback) {
        val feedback = state.feedback ?: return@LaunchedEffect
        scope.launch {
            when (feedback) {
                SettingsFeedback.LOCK_DISABLED -> snackbarState.showSuccess(getString(Res.string.settings_app_lock_disabled))
                SettingsFeedback.LOCK_ENABLED -> snackbarState.showSuccess(getString(Res.string.settings_app_lock_enabled))
                SettingsFeedback.SAVE_FAILED -> snackbarState.showError(getString(Res.string.settings_save_failed))
                SettingsFeedback.SYNC_FAILED -> snackbarState.showError(getString(Res.string.sync_failed))
                SettingsFeedback.SYNCED -> snackbarState.showSuccess(getString(Res.string.settings_synced))
            }
        }
        viewModel.onEvent(SettingsEvent.DismissFeedback)
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onBack = onBack, title = stringResource(Res.string.settings_title))
            Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = AppSpacing.xl, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg)) {
                SectionHeader(label = stringResource(Res.string.settings_security_heading))
                AppSwitchTile(
                    icon = Icons.Outlined.Fingerprint,
                    isChecked = state.isBiometricLockEnabled && availability == BiometricAvailability.AVAILABLE,
                    isEnabled = !state.isLoading && availability == BiometricAvailability.AVAILABLE,
                    onCheckedChange = { isChecked ->
                        scope.launch {
                            if (biometricAuthenticator.authenticate(reason = getString(Res.string.settings_app_lock_reason), title = getString(Res.string.lock_prompt_title)) == BiometricResult.SUCCESS) {
                                viewModel.onEvent(SettingsEvent.SetBiometricLock(isChecked))
                            }
                        }
                    },
                    subtitle = stringResource(
                        when (availability) {
                            BiometricAvailability.AVAILABLE -> Res.string.settings_app_lock_available
                            BiometricAvailability.NOT_ENROLLED -> Res.string.settings_app_lock_not_enrolled
                            BiometricAvailability.UNAVAILABLE -> Res.string.settings_app_lock_unavailable
                        },
                    ),
                    title = stringResource(Res.string.settings_app_lock_title),
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(style = AppTheme.textStyles.listSecondary, text = stringResource(Res.string.settings_app_lock_hint))
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                SectionHeader(label = stringResource(Res.string.settings_sync_heading))
                AppListTile(icon = Icons.Outlined.Sync, subtitle = syncStatusText(state.syncStatus), title = stringResource(Res.string.settings_sync_title))
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                SecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = !state.syncStatus.isSyncing,
                    label = stringResource(Res.string.action_sync_now),
                    onClick = { viewModel.onEvent(SettingsEvent.SyncNow) },
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(style = AppTheme.textStyles.listSecondary, text = stringResource(Res.string.settings_sync_hint))
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                SectionHeader(label = stringResource(Res.string.settings_data_heading))
                Text(style = AppTheme.textStyles.body, text = stringResource(Res.string.settings_data_message))
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }
}

@Composable
private fun syncStatusText(status: SyncStatus): String {
    val lastSyncedAt = status.lastSyncedAt
    return when {
        status.isSyncing -> stringResource(Res.string.settings_sync_syncing)
        status.failure == SyncFailure.OFFLINE -> stringResource(Res.string.settings_sync_offline)
        status.failure == SyncFailure.SERVER -> stringResource(Res.string.settings_sync_server)
        status.failure == SyncFailure.UNEXPECTED -> stringResource(Res.string.settings_sync_unexpected)
        lastSyncedAt != null -> stringResource(Res.string.settings_sync_last, DateFormat.formatDateTime(lastSyncedAt))
        else -> stringResource(Res.string.settings_sync_never)
    }
}
