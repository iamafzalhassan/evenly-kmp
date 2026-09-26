package org.example.evenly.ui.settings

sealed interface SettingsEvent {
    data object DismissFeedback : SettingsEvent

    data class SetBiometricLock(val isEnabled: Boolean) : SettingsEvent

    data object SyncNow : SettingsEvent
}
