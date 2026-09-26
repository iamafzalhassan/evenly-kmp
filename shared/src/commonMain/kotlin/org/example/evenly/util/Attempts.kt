package org.example.evenly.util

import kotlinx.coroutines.CancellationException

suspend fun <T : Any> attemptOrNull(action: suspend () -> T): T? = try {
    action()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    null
}

suspend fun succeeds(action: suspend () -> Unit): Boolean = attemptOrNull(action) != null
