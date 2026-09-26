package org.example.evenly.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume

private val ALLOWED_AUTHENTICATORS: Int = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

class AndroidBiometricAuthenticator(private val activity: FragmentActivity) : BiometricAuthenticator {
    override suspend fun authenticate(reason: String, title: String): BiometricResult = suspendCancellableCoroutine { continuation ->
        fun finish(result: BiometricResult) {
            if (continuation.isActive) continuation.resume(result)
        }
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = finish(BiometricResult.FAILED)

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = finish(BiometricResult.SUCCESS)
        }
        val prompt = BiometricPrompt(activity, Executor { command -> activity.runOnUiThread(command) }, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .setSubtitle(reason)
            .setTitle(title)
            .build()
        continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        prompt.authenticate(promptInfo)
    }

    override fun availability(): BiometricAvailability = when (BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
        else -> BiometricAvailability.UNAVAILABLE
    }
}
