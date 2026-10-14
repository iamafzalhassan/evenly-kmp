package org.example.evenly.platform

import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume

private val ALLOWED_AUTHENTICATORS: Int = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

class AndroidBiometricAuthenticator(private val activity: FragmentActivity) : BiometricAuthenticator {
    override suspend fun authenticate(reason: String, title: String): BiometricResult = suspendCancellableCoroutine { continuation ->
        var finish: ((BiometricResult) -> Unit)? = { result -> if (continuation.isActive) continuation.resume(result) }
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                finish?.invoke(BiometricResult.FAILED)
                finish = null
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                finish?.invoke(BiometricResult.SUCCESS)
                finish = null
            }
        }
        val mainHandler = Handler(Looper.getMainLooper())
        val prompt = BiometricPrompt(activity, Executor { command -> mainHandler.post(command) }, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .setSubtitle(reason)
            .setTitle(title)
            .build()
        continuation.invokeOnCancellation {
            finish = null
            prompt.cancelAuthentication()
        }
        prompt.authenticate(promptInfo)
    }

    override fun availability(): BiometricAvailability = when (BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED, BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.TEMPORARILY_UNAVAILABLE
        else -> BiometricAvailability.UNAVAILABLE
    }
}
