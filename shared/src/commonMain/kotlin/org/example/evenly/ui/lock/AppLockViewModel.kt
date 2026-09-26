package org.example.evenly.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.evenly.data.SettingsRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class AppLockViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    companion object {
        val RELOCK_AFTER: Duration = 30.seconds
    }

    private val mutableState: MutableStateFlow<AppLockUiState> = MutableStateFlow(AppLockUiState())

    val state: StateFlow<AppLockUiState> = mutableState.asStateFlow()

    private var backgroundedAt: Instant? = null

    init {
        viewModelScope.launch {
            settingsRepository.observeBiometricLockEnabled().collect { isEnabled ->
                mutableState.update { state -> state.copy(isLoading = false, isLocked = if (state.isLoading) isEnabled else state.isLocked && isEnabled, isLockEnabled = isEnabled) }
            }
        }
    }

    fun onEvent(event: AppLockEvent) {
        when (event) {
            AppLockEvent.AppBackgrounded -> backgroundedAt = Clock.System.now()
            AppLockEvent.AppForegrounded -> relockIfAway()
            AppLockEvent.DisableUnavailableLock -> disableLock()
            AppLockEvent.Unlock -> mutableState.update { state -> state.copy(isLocked = false) }
        }
    }

    private fun relockIfAway() {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (Clock.System.now() - since >= RELOCK_AFTER) {
            mutableState.update { state -> state.copy(isLocked = state.isLockEnabled) }
        }
    }

    private fun disableLock() {
        mutableState.update { state -> state.copy(isLocked = false) }
        viewModelScope.launch { settingsRepository.setBiometricLockEnabled(false) }
    }
}
