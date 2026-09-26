package org.example.evenly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import org.example.evenly.ui.theme.AppTheme

@Immutable
data class TotalsLine(val label: String, val value: String)

@Composable
fun TotalsBlock(lines: List<TotalsLine>, modifier: Modifier = Modifier) {
    val valueStyle = AppTheme.textStyles.totalsValue

    Column(modifier = modifier.fillMaxWidth().background(color = AppColors.surfaceField, shape = RoundedCornerShape(AppSpacing.radiusCard)).padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.xs)) {
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                DottedDivider()
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.textStyles.overline, text = line.label.uppercase())
                BasicText(
                    modifier = Modifier.weight(1f),
                    autoSize = TextAutoSize.StepBased(maxFontSize = valueStyle.fontSize, minFontSize = AppTheme.textStyles.minFittedFontSize),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = valueStyle.copy(textAlign = TextAlign.End),
                    text = line.value,
                )
            }
        }
    }
}
