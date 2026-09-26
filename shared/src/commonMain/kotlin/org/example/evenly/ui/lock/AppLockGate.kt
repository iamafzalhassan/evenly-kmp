package org.example.evenly.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.action_unlock
import evenly.shared.generated.resources.lock_message
import evenly.shared.generated.resources.lock_prompt_reason
import evenly.shared.generated.resources.lock_prompt_title
import evenly.shared.generated.resources.lock_title
import kotlinx.coroutines.launch
import org.example.evenly.security.BiometricAuthenticator
import org.example.evenly.security.BiometricAvailability
import org.example.evenly.security.BiometricResult
import org.example.evenly.ui.components.EmptyState
import org.example.evenly.ui.components.PrimaryButton
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppLockGate(biometricAuthenticator: BiometricAuthenticator, viewModel: AppLockViewModel, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onEvent(AppLockEvent.AppBackgrounded)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.onEvent(AppLockEvent.AppForegrounded)
    }

    LaunchedEffect(state.isLocked) {
        if (state.isLocked) {
            focusManager.clearFocus(force = true)
        }
    }

    ScreenPrivacyEffect(isEnabled = state.isLockEnabled)

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        if (!state.isLoading) {
            Box(modifier = if (state.isLocked) Modifier.fillMaxSize().clearAndSetSemantics {} else Modifier.fillMaxSize()) {
                content()
            }
        }
        if (state.isLocked) {
            LockScreen(biometricAuthenticator = biometricAuthenticator, onEvent = viewModel::onEvent)
        }
    }
}

@Composable
private fun LockScreen(biometricAuthenticator: BiometricAuthenticator, onEvent: (AppLockEvent) -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val unlock: () -> Unit = {
        scope.launch {
            if (biometricAuthenticator.availability() != BiometricAvailability.AVAILABLE) {
                onEvent(AppLockEvent.DisableUnavailableLock)
            } else if (biometricAuthenticator.authenticate(reason = getString(Res.string.lock_prompt_reason), title = getString(Res.string.lock_prompt_title)) == BiometricResult.SUCCESS) {
                onEvent(AppLockEvent.Unlock)
            }
        }
    }

    LaunchedEffect(Unit) {
        unlock()
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase).consumeAllPointerInput().statusBarsPadding().navigationBarsPadding()) {
        EmptyState(
            modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.screenPadding),
            icon = Icons.Outlined.Lock,
            message = stringResource(Res.string.lock_message),
            title = stringResource(Res.string.lock_title),
        ) {
            PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.action_unlock), onClick = unlock)
        }
    }
}

private fun Modifier.consumeAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent().changes.forEach { it.consume() }
        }
    }
}
