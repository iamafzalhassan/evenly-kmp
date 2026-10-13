package org.example.evenly.core.sync

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.evenly.core.common.succeeds
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val JOIN_RATE_LIMITED_CODE: String = "EV429"

class SyncCoordinator internal constructor(private val scope: CoroutineScope, private val engine: SyncEngine) {
    companion object {
        val REQUEST_DEBOUNCE: Duration = 800.milliseconds
    }

    private val requests: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val mutableStatus: MutableStateFlow<SyncStatus> = MutableStateFlow(SyncStatus())

    private val mutex: Mutex = Mutex()

    val status: StateFlow<SyncStatus> = mutableStatus.asStateFlow()

    init {
        scope.launch {
            requests.collect {
                delay(REQUEST_DEBOUNCE)
                syncNow()
            }
        }
    }

    suspend fun syncNow(): Boolean = runExclusive(::syncAndRecord)

    suspend fun joinGroup(code: String): JoinResult = runExclusive {
        try {
            val groupId = engine.joinGroup(code.trim())
            if (groupId == null) {
                JoinResult.NotFound
            } else {
                syncAndRecord()
                JoinResult.Joined(groupId)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            joinFailure(exception)
        }
    }

    fun requestSync() {
        requests.tryEmit(Unit)
    }

    suspend fun rotateInviteCode(groupId: String): Boolean = runExclusive {
        val isRotated = succeeds { engine.rotateInviteCode(groupId) }
        if (isRotated) syncAndRecord()
        isRotated
    }

    private suspend fun <T> runExclusive(action: suspend () -> T): T = scope.async {
        mutex.withLock {
            mutableStatus.update { status -> status.copy(isSyncing = true) }
            try {
                action()
            } finally {
                mutableStatus.update { status -> status.copy(isSyncing = false) }
            }
        }
    }.await()

    private suspend fun syncAndRecord(): Boolean = try {
        engine.sync()
        mutableStatus.update { status -> status.copy(failure = null, lastSyncedAt = Clock.System.now()) }
        true
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        mutableStatus.update { status -> status.copy(failure = exception.toFailure()) }
        false
    }

    private fun joinFailure(exception: Exception): JoinResult {
        val isRateLimited = exception is PostgrestRestException && exception.code == JOIN_RATE_LIMITED_CODE
        if (!isRateLimited) mutableStatus.update { status -> status.copy(failure = exception.toFailure()) }
        return when {
            isRateLimited -> JoinResult.RateLimited
            exception is HttpRequestException -> JoinResult.Offline
            else -> JoinResult.Failed
        }
    }

    private fun Exception.toFailure(): SyncFailure = when (this) {
        is HttpRequestException -> SyncFailure.OFFLINE
        is RestException, is SyncRejectedException -> SyncFailure.SERVER
        else -> SyncFailure.UNEXPECTED
    }
}
