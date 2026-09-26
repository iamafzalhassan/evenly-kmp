package org.example.evenly.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import org.example.evenly.ui.theme.AppColors
import org.example.evenly.ui.theme.AppSpacing
import kotlin.math.ceil
import kotlin.math.floor

private const val ALPHA_SCALE: Float = 255f
private const val FULL_TURN: Float = 360f
private const val TICK_INNER_RATIO: Float = 1f / 3f
private const val TICK_WIDTH_RATIO: Float = 0.2f

private const val PARTIAL_ALPHA: Int = 147
private const val PERIOD_MILLIS: Int = 1000
private const val TICK_COUNT: Int = 8

private val tickAlphas: IntArray = intArrayOf(47, 47, 47, 47, 72, 97, 122, 147)

@Composable
fun ActivityIndicator(modifier: Modifier = Modifier, progress: Float = 1f, color: Color = AppColors.textPrimary) {
    val position by rememberInfiniteTransition(label = "activityIndicator").animateFloat(
        animationSpec = infiniteRepeatable(tween(durationMillis = PERIOD_MILLIS, easing = LinearEasing)),
        initialValue = 0f,
        label = "activityIndicatorPosition",
        targetValue = 1f,
    )
    val isPartial = progress < 1f
    val tickTotal = if (isPartial) ceil(TICK_COUNT * progress.coerceAtLeast(0f)).toInt() else TICK_COUNT

    Canvas(modifier = modifier.progressSemantics().size(AppSpacing.progressIndicator)) {
        val radius = size.minDimension / 2f
        val tickWidth = radius * TICK_WIDTH_RATIO
        val activeTick = floor(TICK_COUNT * position).toInt()

        for (index in 0 until tickTotal) {
            val alpha = if (isPartial) PARTIAL_ALPHA else tickAlphas[(index - activeTick).mod(TICK_COUNT)]

            rotate(degrees = FULL_TURN / TICK_COUNT * index) {
                drawRoundRect(
                    color = color.copy(alpha = alpha / ALPHA_SCALE),
                    cornerRadius = CornerRadius(tickWidth / 2f),
                    size = Size(tickWidth, radius * (1f - TICK_INNER_RATIO)),
                    topLeft = Offset(center.x - tickWidth / 2f, center.y - radius),
                )
            }
        }
    }
}
