package org.example.evenly.ui.lock

import androidx.compose.runtime.Immutable

@Immutable
data class AppLockUiState(val isLoading: Boolean = true, val isLocked: Boolean = false, val isLockEnabled: Boolean = false)
