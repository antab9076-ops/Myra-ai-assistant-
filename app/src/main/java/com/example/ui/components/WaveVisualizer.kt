package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveVisualizer(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 9,
    maxHeight: Dp = 38.dp,
    barWidth: Dp = 4.dp,
    barSpacing: Dp = 4.dp,
    audioLevel: Float = 0f
) {
    val transition = rememberInfiniteTransition(label = "wave_vis")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val normalizedIdx = (i - (barCount / 2f)) / (barCount / 2f)
            val bellCurve = 1f - abs(normalizedIdx) * 0.4f

            val heightMultiplier = if (isAnimating) {
                val wave = abs(sin(phase + (i * 0.7f)))
                ((0.25f + wave * 0.75f) * bellCurve + audioLevel * 0.4f).coerceIn(0.15f, 1f)
            } else {
                0.2f
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(heightMultiplier)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ElectricCyan,
                                NeonMagenta,
                                NeonPurple
                            )
                        )
                    )
            )
        }
    }
}
