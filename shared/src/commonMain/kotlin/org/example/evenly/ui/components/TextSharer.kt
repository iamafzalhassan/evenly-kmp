package org.example.evenly.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTextSharer(): (String) -> Unit
