package org.example.evenly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing

private const val DASH_GAP: Float = 4f
private const val DASH_WIDTH: Float = 3f

@Composable
fun DottedDivider(modifier: Modifier = Modifier, color: Color = AppColors.dividerStrong) {
    Canvas(modifier = modifier.fillMaxWidth().height(AppSpacing.hairline)) {
        val effect = PathEffect.dashPathEffect(floatArrayOf(DASH_WIDTH * density, DASH_GAP * density), 0f)

        drawLine(
            cap = StrokeCap.Round,
            color = color,
            end = Offset(size.width, size.height / 2f),
            pathEffect = effect,
            start = Offset(0f, size.height / 2f),
            strokeWidth = size.height,
        )
    }
}
