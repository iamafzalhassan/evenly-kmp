package org.example.evenly

import androidx.compose.ui.window.ComposeUIViewController
import org.example.evenly.app.App
import org.example.evenly.app.AppGraph
import org.example.evenly.app.createAppGraph
import org.example.evenly.core.security.BiometricAuthenticator
import org.example.evenly.core.security.IosBiometricAuthenticator
import platform.UIKit.UIViewController

private val appGraph: AppGraph by lazy { createAppGraph() }

private val biometricAuthenticator: BiometricAuthenticator by lazy { IosBiometricAuthenticator() }

fun MainViewController(): UIViewController = ComposeUIViewController { App(biometricAuthenticator = biometricAuthenticator, graph = appGraph) }
