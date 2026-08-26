package com.example.falcon.ui.orb

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.falcon.model.AgentState
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

private class Particle(
    var x: Float,
    var y: Float,
    var z: Float,
    var baseRadius: Float,
    var speed: Float,
    var theta: Float,
    var phi: Float,
    var orbitRadius: Float,
    var colorType: Int // 0: Cyan, 1: Violet, 2: Bright White
)

@Composable
fun FalconOrb(
    state: AgentState,
    audioAmplitude: Float, // 0.0 to 1.0 (from mic or TTS)
    modifier: Modifier = Modifier,
    particleDensity: Float = 1.0f,
    reduceMotion: Boolean = false,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Rotation phase for 3D sphere rotation
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AgentState.THINKING, AgentState.PLANNING -> 3500
                    AgentState.EXECUTING -> 4500
                    AgentState.LISTENING -> 8000
                    AgentState.SPEAKING -> 6000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationY"
    )

    // Breathing pulse for idle & speaking
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Scanning line phase for VERIFYING state
    val scanPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanPhase"
    )

    // Energy wave ripple
    val energyRipple by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "energyRipple"
    )

    // Particle system allocation
    val particleCount = (140 * particleDensity).toInt().coerceIn(60, 240)
    val particles = remember {
        List(particleCount) {
            val theta = Random.nextFloat() * 2 * PI.toFloat()
            val phi = acos(2f * Random.nextFloat() - 1f)
            val r = 0.7f + Random.nextFloat() * 0.4f
            Particle(
                x = 0f,
                y = 0f,
                z = 0f,
                baseRadius = 1.2f + Random.nextFloat() * 2.2f,
                speed = 0.8f + Random.nextFloat() * 0.8f,
                theta = theta,
                phi = phi,
                orbitRadius = r,
                colorType = Random.nextInt(3)
            )
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = min(size.width, size.height) * 0.36f

            // Dynamic scale factoring in audio amplitude & state
            val amplitudeScale = 1f + (audioAmplitude * 0.35f)
            val stateScale = when (state) {
                AgentState.LISTENING -> 1.12f * amplitudeScale
                AgentState.SPEAKING -> 1.08f * amplitudeScale
                AgentState.THINKING -> 1.05f
                AgentState.EXECUTING -> 1.06f
                AgentState.SUCCESS -> 1.15f
                AgentState.ERROR -> 0.98f
                else -> breathingPulse
            }
            val orbRadius = baseRadius * stateScale

            // Draw Deep Volumetric Glow
            drawOrbCoreGlow(center, orbRadius, state, audioAmplitude)

            // Draw 3D Spherical Particle System
            draw3DParticles(
                center = center,
                radius = orbRadius,
                particles = particles,
                rotationAngleDeg = rotationY,
                state = state,
                audioAmp = audioAmplitude
            )

            // Draw Dynamic Energy Rings / Scanning Sweep
            drawOrbEnergyStructures(
                center = center,
                radius = orbRadius,
                state = state,
                rotationAngle = rotationY,
                scanPhase = scanPhase,
                energyRipple = energyRipple,
                audioAmp = audioAmplitude
            )
        }
    }
}

private fun DrawScope.drawOrbCoreGlow(
    center: Offset,
    radius: Float,
    state: AgentState,
    audioAmp: Float
) {
    val (primaryColor, secondaryColor, coreColor) = when (state) {
        AgentState.ERROR -> Triple(FalconError, Color(0x66FF3366), Color(0xCCFF3366))
        AgentState.SUCCESS -> Triple(FalconSuccess, Color(0x6600FF9D), Color(0xCC00FF9D))
        AgentState.LISTENING -> Triple(FalconCyanBright, Color(0x8000F0FF), Color(0xEEFFFFFF))
        AgentState.SPEAKING -> Triple(FalconCyanBright, Color(0x808A2BE2), Color(0xEEFFFFFF))
        AgentState.THINKING, AgentState.PLANNING -> Triple(FalconVioletLight, Color(0x8000F0FF), Color(0xEE8A2BE2))
        AgentState.EXECUTING -> Triple(FalconCyan, Color(0x808A2BE2), Color(0xEE5CF9FF))
        else -> Triple(FalconCyan, Color(0x4400F0FF), Color(0xAA00F0FF))
    }

    // Outer Ambient Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = 0.35f + audioAmp * 0.3f),
                primaryColor.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.55f
        ),
        radius = radius * 1.55f,
        center = center
    )

    // Inner Radiant Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                coreColor,
                primaryColor.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.75f
        ),
        radius = radius * 0.75f,
        center = center
    )
}

private fun DrawScope.draw3DParticles(
    center: Offset,
    radius: Float,
    particles: List<Particle>,
    rotationAngleDeg: Float,
    state: AgentState,
    audioAmp: Float
) {
    val rotRad = Math.toRadians(rotationAngleDeg.toDouble()).toFloat()
    val cosRot = cos(rotRad)
    val sinRot = sin(rotRad)

    for (p in particles) {
        // Spherical coordinate mapping to 3D Cartesian
        val px = p.orbitRadius * radius * sin(p.phi) * cos(p.theta)
        val py = p.orbitRadius * radius * cos(p.phi)
        val pz = p.orbitRadius * radius * sin(p.phi) * sin(p.theta)

        // Rotate in 3D around Y axis
        val rotX = px * cosRot + pz * sinRot
        val rotZ = -px * sinRot + pz * cosRot
        val rotY = py

        // Perspective projection
        val fov = 400f
        val scale = fov / (fov + rotZ)
        val projX = center.x + rotX * scale
        val projY = center.y + rotY * scale

        // Depth-based size and opacity
        val depthNorm = ((rotZ / radius) + 1f) / 2f // 0 (far) to 1 (near)
        val alpha = (0.2f + depthNorm * 0.75f + (audioAmp * 0.2f)).coerceIn(0.1f, 1f)
        val drawSize = (p.baseRadius * scale * (0.8f + depthNorm * 0.6f)).coerceAtLeast(1f)

        val particleColor = when {
            state == AgentState.ERROR -> FalconError.copy(alpha = alpha)
            state == AgentState.SUCCESS -> FalconSuccess.copy(alpha = alpha)
            p.colorType == 0 -> FalconCyan.copy(alpha = alpha)
            p.colorType == 1 -> FalconVioletLight.copy(alpha = alpha)
            else -> Color.White.copy(alpha = alpha)
        }

        drawCircle(
            color = particleColor,
            radius = drawSize,
            center = Offset(projX, projY)
        )
    }
}

private fun DrawScope.drawOrbEnergyStructures(
    center: Offset,
    radius: Float,
    state: AgentState,
    rotationAngle: Float,
    scanPhase: Float,
    energyRipple: Float,
    audioAmp: Float
) {
    val primaryColor = when (state) {
        AgentState.ERROR -> FalconError
        AgentState.SUCCESS -> FalconSuccess
        AgentState.THINKING, AgentState.PLANNING -> FalconVioletLight
        else -> FalconCyan
    }

    // Concentric Energy Ring 1 (Tilted orbital ring)
    rotate(degrees = rotationAngle * 0.5f, pivot = center) {
        drawOval(
            color = primaryColor.copy(alpha = 0.45f + audioAmp * 0.4f),
            topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 0.35f),
            size = androidx.compose.ui.geometry.Size(radius * 2.3f, radius * 0.7f),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
        )
    }

    // Concentric Energy Ring 2 (Orthogonal orbital ring)
    rotate(degrees = -rotationAngle * 0.35f + 45f, pivot = center) {
        drawOval(
            color = FalconVioletLight.copy(alpha = 0.35f + audioAmp * 0.3f),
            topLeft = Offset(center.x - radius * 1.05f, center.y - radius * 0.3f),
            size = androidx.compose.ui.geometry.Size(radius * 2.1f, radius * 0.6f),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // Active State Energy Expansion Ripples
    if (state == AgentState.LISTENING || state == AgentState.SPEAKING || state == AgentState.THINKING) {
        drawCircle(
            color = primaryColor.copy(alpha = (1f - (energyRipple - 0.8f) / 0.6f) * 0.35f),
            radius = radius * energyRipple,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // Verifying State: Vertical Scanning Laser Beam
    if (state == AgentState.VERIFYING) {
        val scanY = center.y + (scanPhase * radius)
        val beamWidth = sqrt((radius * radius - (scanPhase * radius).pow(2)).coerceAtLeast(0f))
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, FalconCyanBright, Color.White, FalconCyanBright, Color.Transparent),
                startX = center.x - beamWidth,
                endX = center.x + beamWidth
            ),
            start = Offset(center.x - beamWidth, scanY),
            end = Offset(center.x + beamWidth, scanY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
