package org.example.evenly.security

enum class BiometricAvailability { AVAILABLE, NOT_ENROLLED, UNAVAILABLE }

enum class BiometricResult { FAILED, SUCCESS }

interface BiometricAuthenticator {
    suspend fun authenticate(reason: String, title: String): BiometricResult

    fun availability(): BiometricAvailability
}
