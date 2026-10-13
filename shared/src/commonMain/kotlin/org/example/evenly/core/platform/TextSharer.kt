package org.example.evenly.core.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTextSharer(): (String) -> Unit
