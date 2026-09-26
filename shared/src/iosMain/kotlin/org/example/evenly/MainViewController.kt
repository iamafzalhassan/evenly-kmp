package org.example.evenly

import androidx.compose.ui.window.ComposeUIViewController
import org.example.evenly.security.BiometricAuthenticator
import org.example.evenly.security.IosBiometricAuthenticator
import platform.UIKit.UIViewController

private val appGraph: AppGraph by lazy { createAppGraph() }

private val biometricAuthenticator: BiometricAuthenticator by lazy { IosBiometricAuthenticator() }

fun MainViewController(): UIViewController = ComposeUIViewController { App(biometricAuthenticator = biometricAuthenticator, graph = appGraph) }
