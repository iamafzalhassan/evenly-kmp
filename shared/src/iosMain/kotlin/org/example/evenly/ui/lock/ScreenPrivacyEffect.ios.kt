package org.example.evenly.ui.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.example.evenly.ui.theme.AppColors
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScreenPrivacyEffect(isEnabled: Boolean) {
    val viewController = LocalUIViewController.current

    DisposableEffect(isEnabled, viewController) {
        if (!isEnabled) return@DisposableEffect onDispose {}
        val center = NSNotificationCenter.defaultCenter
        val cover = UIView()
        cover.autoresizingMask = UIViewAutoresizingFlexibleHeight or UIViewAutoresizingFlexibleWidth
        cover.backgroundColor = UIColor.colorWithRed(red = AppColors.surfaceBase.red.toDouble(), green = AppColors.surfaceBase.green.toDouble(), blue = AppColors.surfaceBase.blue.toDouble(), alpha = 1.0)
        val resignObserver = center.addObserverForName(name = UIApplicationWillResignActiveNotification, `object` = null, queue = NSOperationQueue.mainQueue) { _ ->
            val window = viewController.view.window
            if (window != null) {
                cover.setFrame(window.bounds)
                window.addSubview(cover)
            }
        }
        val activeObserver = center.addObserverForName(name = UIApplicationDidBecomeActiveNotification, `object` = null, queue = NSOperationQueue.mainQueue) { _ ->
            cover.removeFromSuperview()
        }
        onDispose {
            center.removeObserver(resignObserver)
            center.removeObserver(activeObserver)
            cover.removeFromSuperview()
        }
    }
}
