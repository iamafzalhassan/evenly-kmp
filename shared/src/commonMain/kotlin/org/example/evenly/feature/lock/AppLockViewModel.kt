package org.example.evenly.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.evenly.core.data.SettingsRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource

class AppLockViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    companion object {
        val RELOCK_AFTER: Duration = 30.seconds
    }

    private val mutableState: MutableStateFlow<AppLockUiState> = MutableStateFlow(AppLockUiState())

    val state: StateFlow<AppLockUiState> = mutableState.asStateFlow()

    private var backgroundedAt: Instant? = null

    private var backgroundedMark: TimeSource.Monotonic.ValueTimeMark? = null

    init {
        viewModelScope.launch {
            settingsRepository.observeBiometricLockEnabled().collect { isEnabled ->
                mutableState.update { state -> state.copy(isLoading = false, isLocked = if (state.isLoading) isEnabled else state.isLocked && isEnabled, isLockEnabled = isEnabled) }
            }
        }
    }

    fun onEvent(event: AppLockEvent) {
        when (event) {
            AppLockEvent.AppBackgrounded -> markBackgrounded()
            AppLockEvent.AppForegrounded -> relockIfAway()
            AppLockEvent.DisableUnavailableLock -> disableLock()
            AppLockEvent.Unlock -> mutableState.update { state -> state.copy(isLocked = false) }
        }
    }

    private fun markBackgrounded() {
        backgroundedAt = Clock.System.now()
        backgroundedMark = TimeSource.Monotonic.markNow()
    }

    private fun relockIfAway() {
        val since = backgroundedAt ?: return
        val mark = backgroundedMark ?: return
        backgroundedAt = null
        backgroundedMark = null
        val wallClockAway = Clock.System.now() - since
        if (wallClockAway.isNegative() || wallClockAway >= RELOCK_AFTER || mark.elapsedNow() >= RELOCK_AFTER) {
            mutableState.update { state -> state.copy(isLocked = state.isLockEnabled) }
        }
    }

    private fun disableLock() {
        mutableState.update { state -> state.copy(isLocked = false) }
        viewModelScope.launch { settingsRepository.setBiometricLockEnabled(false) }
    }
}
