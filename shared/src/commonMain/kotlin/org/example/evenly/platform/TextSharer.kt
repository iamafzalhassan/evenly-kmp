package org.example.evenly.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTextSharer(): (String) -> Unit
