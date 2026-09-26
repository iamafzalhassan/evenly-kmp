package org.example.evenly.ui.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PLAIN_TEXT_MIME_TYPE: String = "text/plain"

@Composable
actual fun rememberTextSharer(): (String) -> Unit {
    val context = LocalContext.current

    return remember(context) {
        { text -> context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(PLAIN_TEXT_MIME_TYPE).putExtra(Intent.EXTRA_TEXT, text), null)) }
    }
}
