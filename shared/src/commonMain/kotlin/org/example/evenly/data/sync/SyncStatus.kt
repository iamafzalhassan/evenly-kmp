package org.example.evenly.data.sync

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

enum class SyncFailure { OFFLINE, SERVER, UNEXPECTED }

sealed interface JoinResult {
    data object Failed : JoinResult

    data class Joined(val groupId: String) : JoinResult

    data object NotFound : JoinResult

    data object Offline : JoinResult

    data object RateLimited : JoinResult
}

@Immutable
data class SyncStatus(val isSyncing: Boolean = false, val failure: SyncFailure? = null, val lastSyncedAt: Instant? = null)

class SyncRejectedException(val rowCount: Int) : Exception("The server rejected $rowCount rows")
