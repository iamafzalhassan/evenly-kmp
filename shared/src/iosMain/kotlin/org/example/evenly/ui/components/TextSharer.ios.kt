package org.example.evenly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberTextSharer(): (String) -> Unit {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        { text -> presentShareSheet(text = text, viewController = viewController) }
    }
}

private fun presentShareSheet(text: String, viewController: UIViewController) {
    val presenter = generateSequence(viewController) { it.presentedViewController }.last()
    val shareController = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    shareController.popoverPresentationController?.sourceView = presenter.view
    presenter.presentViewController(viewControllerToPresent = shareController, animated = true, completion = null)
}
