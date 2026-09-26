package org.example.evenly.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import kotlin.coroutines.resume

class IosBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun authenticate(reason: String, title: String): BiometricResult = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        context.evaluatePolicy(LAPolicyDeviceOwnerAuthentication, localizedReason = reason) { isSuccess, _ ->
            if (continuation.isActive) {
                continuation.resume(if (isSuccess) BiometricResult.SUCCESS else BiometricResult.FAILED)
            }
        }
        continuation.invokeOnCancellation { context.invalidate() }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun availability(): BiometricAvailability = if (LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)) BiometricAvailability.AVAILABLE else BiometricAvailability.UNAVAILABLE
}
