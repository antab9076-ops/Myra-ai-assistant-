package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedAiOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow celestial rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 3000
                    AssistantState.SPEAKING -> 6000
                    AssistantState.LISTENING -> 8000
                    AssistantState.IDLE -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.SPEAKING -> 800
                    AssistantState.LISTENING -> 1200
                    AssistantState.THINKING -> 900
                    AssistantState.IDLE -> 2600
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Harmonic wave phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Ripple radius multiplier for sound waves
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    // Smooth dynamic audio scale
    val dynamicAudioAnim = remember { Animatable(0f) }
    LaunchedEffect(audioLevel) {
        dynamicAudioAnim.animateTo(
            targetValue = audioLevel,
            animationSpec = tween(120, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.46f
            val audioBoost = dynamicAudioAnim.value * 0.28f
            val currentScale = (breathingPulse + audioBoost)
            val coreRadius = baseRadius * currentScale

            // 1. Draw outer ambient atmospheric glow
            drawOuterAmbientGlow(center, coreRadius * 1.8f, state)

            // 2. Draw sound wave ripples when listening or speaking
            if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                drawSoundWaveRipples(center, coreRadius, rippleProgress, state, dynamicAudioAnim.value)
            }

            // 3. Draw outer orbital harmonic rings
            drawOrbitalRings(center, coreRadius, rotationAngle, state)

            // 4. Draw sound wave circular frequency spikes
            drawSoundWaveSpikes(center, coreRadius, wavePhase, state, dynamicAudioAnim.value)

            // 5. Draw glowing core sphere with inner 3D specular highlight
            drawGlowingCore(center, coreRadius, state, wavePhase)

            // 6. Draw orbiting energy sparks
            drawEnergyParticles(center, coreRadius * 1.35f, rotationAngle, state)
        }
    }
}

private fun DrawScope.drawOuterAmbientGlow(center: Offset, radius: Float, state: AssistantState) {
    val glowColors = when (state) {
        AssistantState.LISTENING -> listOf(
            Color(0x7000F0FF),
            Color(0x35BD00FF),
            Color(0x000D061F)
        )
        AssistantState.THINKING -> listOf(
            Color(0x80BD00FF),
            Color(0x40E040FB),
            Color(0x000D061F)
        )
        AssistantState.SPEAKING -> listOf(
            Color(0x80E040FB),
            Color(0x45BD00FF),
            Color(0x000D061F)
        )
        AssistantState.IDLE -> listOf(
            Color(0x55BD00FF),
            Color(0x207928CA),
            Color(0x000D061F)
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = glowColors,
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawSoundWaveRipples(
    center: Offset,
    coreRadius: Float,
    progress: Float,
    state: AssistantState,
    audioBoost: Float
) {
    val count = 3
    for (i in 0 until count) {
        val waveFraction = (progress + (i.toFloat() / count)) % 1f
        val rippleRadius = coreRadius + (waveFraction * (coreRadius * 0.9f))
        val alpha = (1f - waveFraction) * (0.6f + audioBoost * 0.4f)
        val rippleColor = if (state == AssistantState.LISTENING) {
            ElectricCyan.copy(alpha = alpha.coerceIn(0f, 1f))
        } else {
            NeonMagenta.copy(alpha = alpha.coerceIn(0f, 1f))
        }

        drawCircle(
            color = rippleColor,
            radius = rippleRadius,
            center = center,
            style = Stroke(width = (2.5f * (1f - waveFraction * 0.5f)).coerceAtLeast(1f))
        )
    }
}

private fun DrawScope.drawOrbitalRings(
    center: Offset,
    coreRadius: Float,
    rotationAngle: Float,
    state: AssistantState
) {
    // Primary tilted orbital ring
    rotate(degrees = rotationAngle, pivot = center) {
        val ringRect = Size(coreRadius * 2.5f, coreRadius * 1.1f)
        val topLeft = Offset(center.x - ringRect.width / 2f, center.y - ringRect.height / 2f)
        drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(
                    ElectricCyan.copy(alpha = 0.8f),
                    NeonMagenta.copy(alpha = 0.9f),
                    NeonPurple.copy(alpha = 0.3f),
                    ElectricCyan.copy(alpha = 0.8f)
                ),
                center = center
            ),
            topLeft = topLeft,
            size = ringRect,
            style = Stroke(width = 2.4f)
        )
    }

    // Secondary counter-rotating orbital ring
    rotate(degrees = -rotationAngle * 0.85f + 45f, pivot = center) {
        val ringRect = Size(coreRadius * 2.2f, coreRadius * 0.85f)
        val topLeft = Offset(center.x - ringRect.width / 2f, center.y - ringRect.height / 2f)
        drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(
                    NeonMagenta.copy(alpha = 0.7f),
                    Color.White.copy(alpha = 0.8f),
                    ElectricViolet.copy(alpha = 0.3f),
                    NeonMagenta.copy(alpha = 0.7f)
                ),
                center = center
            ),
            topLeft = topLeft,
            size = ringRect,
            style = Stroke(width = 1.8f)
        )
    }
}

private fun DrawScope.drawSoundWaveSpikes(
    center: Offset,
    coreRadius: Float,
    wavePhase: Float,
    state: AssistantState,
    audioBoost: Float
) {
    val numSpikes = 36
    val angleStep = (2 * PI / numSpikes).toFloat()

    val path = Path()
    var firstPoint = Offset.Zero

    for (i in 0 until numSpikes) {
        val angle = i * angleStep
        val waveMod = when (state) {
            AssistantState.SPEAKING -> sin(angle * 5f + wavePhase) * (8f + audioBoost * 18f)
            AssistantState.LISTENING -> sin(angle * 4f + wavePhase * 1.5f) * (6f + audioBoost * 24f)
            AssistantState.THINKING -> cos(angle * 3f + wavePhase * 2f) * 6f
            AssistantState.IDLE -> sin(angle * 3f + wavePhase) * 3f
        }
        val r = coreRadius + 10f + waveMod
        val px = center.x + r * cos(angle)
        val py = center.y + r * sin(angle)

        if (i == 0) {
            path.moveTo(px, py)
            firstPoint = Offset(px, py)
        } else {
            path.lineTo(px, py)
        }
    }
    path.lineTo(firstPoint.x, firstPoint.y)

    val strokeColor = when (state) {
        AssistantState.LISTENING -> ElectricCyan.copy(alpha = 0.65f)
        AssistantState.THINKING -> ElectricViolet.copy(alpha = 0.65f)
        AssistantState.SPEAKING -> NeonMagenta.copy(alpha = 0.75f)
        AssistantState.IDLE -> NeonPurple.copy(alpha = 0.35f)
    }

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawGlowingCore(
    center: Offset,
    coreRadius: Float,
    state: AssistantState,
    wavePhase: Float
) {
    val coreColors = when (state) {
        AssistantState.LISTENING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFF80F7FF),
            ElectricCyan,
            Color(0xFF007A99),
            Color(0xFF0A031F)
        )
        AssistantState.THINKING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFB3FD),
            NeonMagenta,
            NeonPurple,
            Color(0xFF1F0338)
        )
        AssistantState.SPEAKING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFF85F3),
            NeonMagenta,
            ElectricViolet,
            Color(0xFF27054A)
        )
        AssistantState.IDLE -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE5A8FF),
            NeonPurple,
            Color(0xFF4A0E80),
            Color(0xFF0D021F)
        )
    }

    // Main 3D spherical radial gradient core
    drawCircle(
        brush = Brush.radialGradient(
            colors = coreColors,
            center = Offset(center.x - coreRadius * 0.28f, center.y - coreRadius * 0.28f),
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )

    // Inner bright specular glint
    drawCircle(
        color = Color.White.copy(alpha = 0.65f),
        radius = coreRadius * 0.22f,
        center = Offset(center.x - coreRadius * 0.35f, center.y - coreRadius * 0.35f)
    )

    // Central soundwave harmonic bars inside the orb core
    val barWidth = 3f
    val barCount = 5
    val barMaxHeight = coreRadius * 0.55f
    val startX = center.x - ((barCount - 1) * 7f)

    for (b in 0 until barCount) {
        val normalizedIndex = (b - 2) / 2f
        val factor = 1f - kotlin.math.abs(normalizedIndex) * 0.45f
        val animatedH = when (state) {
            AssistantState.SPEAKING -> barMaxHeight * factor * (0.4f + kotlin.math.abs(sin(wavePhase * 2f + b)) * 0.6f)
            AssistantState.LISTENING -> barMaxHeight * factor * (0.3f + kotlin.math.abs(cos(wavePhase * 1.5f + b)) * 0.7f)
            AssistantState.THINKING -> barMaxHeight * factor * (0.25f + kotlin.math.abs(sin(wavePhase * 3f + b)) * 0.4f)
            AssistantState.IDLE -> barMaxHeight * factor * 0.3f
        }
        val bx = startX + b * 14f
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(bx, center.y - animatedH / 2f),
            end = Offset(bx, center.y + animatedH / 2f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawEnergyParticles(
    center: Offset,
    orbitRadius: Float,
    rotationAngle: Float,
    state: AssistantState
) {
    val particleCount = 6
    val step = 360f / particleCount

    for (p in 0 until particleCount) {
        val curAngle = ((rotationAngle * 1.4f) + (p * step)) * (PI / 180f).toFloat()
        val dist = orbitRadius + sin(curAngle * 2f) * 12f
        val px = center.x + dist * cos(curAngle)
        val py = center.y + (dist * 0.45f) * sin(curAngle) // slightly flattened for 3D depth

        val particleColor = if (p % 2 == 0) ElectricCyan else NeonMagenta
        val pRadius = when (state) {
            AssistantState.THINKING -> 3.2f
            AssistantState.SPEAKING -> 2.8f
            else -> 2.2f
        }

        drawCircle(
            color = particleColor.copy(alpha = 0.85f),
            radius = pRadius,
            center = Offset(px, py)
        )
    }
}
