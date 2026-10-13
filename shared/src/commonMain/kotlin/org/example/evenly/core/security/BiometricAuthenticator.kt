package org.example.evenly.core.security

enum class BiometricAvailability { AVAILABLE, NOT_ENROLLED, TEMPORARILY_UNAVAILABLE, UNAVAILABLE }

enum class BiometricResult { FAILED, SUCCESS }

interface BiometricAuthenticator {
    suspend fun authenticate(reason: String, title: String): BiometricResult

    fun availability(): BiometricAvailability
}
