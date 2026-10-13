package org.example.evenly.feature.lock

sealed interface AppLockEvent {
    data object AppBackgrounded : AppLockEvent

    data object AppForegrounded : AppLockEvent

    data object DisableUnavailableLock : AppLockEvent

    data object Unlock : AppLockEvent
}
