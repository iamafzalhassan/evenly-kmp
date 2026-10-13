package org.example.evenly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.evenly.core.common.STATE_STOP_TIMEOUT_MILLIS
import org.example.evenly.core.common.succeeds
import org.example.evenly.core.data.SettingsRepository
import org.example.evenly.core.sync.SyncCoordinator

class SettingsViewModel(private val settingsRepository: SettingsRepository, private val syncCoordinator: SyncCoordinator) : ViewModel() {
    private val feedback: MutableStateFlow<SettingsFeedback?> = MutableStateFlow(null)

    val state: StateFlow<SettingsUiState> = combine(settingsRepository.observeBiometricLockEnabled(), feedback, syncCoordinator.status) { isEnabled, currentFeedback, syncStatus ->
        SettingsUiState(isBiometricLockEnabled = isEnabled, isLoading = false, feedback = currentFeedback, syncStatus = syncStatus)
    }.stateIn(initialValue = SettingsUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.DismissFeedback -> feedback.value = null
            is SettingsEvent.SetBiometricLock -> setBiometricLock(event.isEnabled)
            SettingsEvent.SyncNow -> syncNow()
        }
    }

    private fun setBiometricLock(isEnabled: Boolean) {
        viewModelScope.launch {
            val isSaved = succeeds { settingsRepository.setBiometricLockEnabled(isEnabled) }
            feedback.value = when {
                !isSaved -> SettingsFeedback.SAVE_FAILED
                isEnabled -> SettingsFeedback.LOCK_ENABLED
                else -> SettingsFeedback.LOCK_DISABLED
            }
        }
    }

    private fun syncNow() {
        viewModelScope.launch {
            feedback.value = if (syncCoordinator.syncNow()) SettingsFeedback.SYNCED else SettingsFeedback.SYNC_FAILED
        }
    }
}
