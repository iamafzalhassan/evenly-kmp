package org.example.evenly.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.core.designsystem.theme.AppSpacing
import org.example.evenly.core.designsystem.theme.AppTheme

@Composable
fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.overline, text = label.uppercase())
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        DottedDivider()
        Spacer(modifier = Modifier.height(AppSpacing.lg))
    }
}
