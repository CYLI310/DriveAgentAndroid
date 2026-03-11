package com.driveagent.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.driveagent.android.model.AccelerationState
import com.driveagent.android.model.ParticleEffectStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ParticleEffectView(
    accelerationState: AccelerationState,
    accelerationMagnitude: Double,
    style: ParticleEffectStyle,
    isSpeeding: Boolean,
    modifier: Modifier = Modifier
) {
    when (style) {
        ParticleEffectStyle.OFF -> { }
        ParticleEffectStyle.LINEAR_GRADIENT -> {
            GradientEffect(
                accelerationState = accelerationState,
                isSpeeding = isSpeeding,
                modifier = modifier
            )
        }
        ParticleEffectStyle.ORBIT -> {
            OrbitEffect(
                accelerationState = accelerationState,
                isSpeeding = isSpeeding,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun GradientEffect(
    accelerationState: AccelerationState,
    isSpeeding: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "grad")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val colors = getGradientColors(accelerationState, isSpeeding)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = colors,
                center = center
            ),
            radius = size.minDimension * 0.8f * pulseScale,
            alpha = 0.2f,
            center = center
        )
    }
}

@Composable
private fun OrbitEffect(
    accelerationState: AccelerationState,
    isSpeeding: Boolean,
    modifier: Modifier = Modifier
) {
    val particleCount = 40
    val particles = remember {
        List(particleCount) {
            Particle(
                baseAngle = (it * (360.0 / particleCount)).toFloat(),
                radius = (120..180).random().toFloat(),
                size = (4..8).random().toFloat(),
                speedMultiplier = 0.5f + Random.nextFloat() * 1.0f // Random float between 0.5 and 1.5
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val color by animateColorAsState(
        targetValue = getParticleColor(accelerationState, isSpeeding),
        animationSpec = tween(1000),
        label = "color"
    )
    
    if (accelerationState != AccelerationState.STOPPED || isSpeeding) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            particles.forEach { particle ->
                val currentAngle = Math.toRadians((particle.baseAngle + (rotation * particle.speedMultiplier)).toDouble())
                val r = particle.radius * (if (isSpeeding) pulse else 1f)
                val x = centerX + (cos(currentAngle) * r).toFloat()
                val y = centerY + (sin(currentAngle) * r).toFloat()
                
                // Trail (drawn simply as a larger lower-alpha circle for glow)
                drawCircle(
                    color = color,
                    radius = particle.size * 2.5f,
                    center = Offset(x, y),
                    alpha = 0.1f
                )
                
                drawCircle(
                    color = color,
                    radius = particle.size / 2,
                    center = Offset(x, y),
                    alpha = 0.8f
                )
            }
        }
    }
}

private fun getParticleColor(accelerationState: AccelerationState, isSpeeding: Boolean): Color {
    return if (isSpeeding) {
        Color.Red
    } else {
        when (accelerationState) {
            AccelerationState.ACCELERATING -> Color(0xFF42A5F5)
            AccelerationState.DECELERATING -> Color(0xFF66BB6A)
            AccelerationState.STEADY -> Color(0xFF26C6DA)
            AccelerationState.STOPPED -> Color.Gray.copy(alpha = 0.3f)
        }
    }
}

private fun getGradientColors(accelerationState: AccelerationState, isSpeeding: Boolean): List<Color> {
    return if (isSpeeding) {
        listOf(Color.Red, Color.Transparent, Color.Red)
    } else {
        when (accelerationState) {
            AccelerationState.ACCELERATING -> listOf(Color(0xFF42A5F5), Color.Transparent, Color(0xFF42A5F5))
            AccelerationState.DECELERATING -> listOf(Color(0xFF66BB6A), Color.Transparent, Color(0xFF66BB6A))
            AccelerationState.STEADY -> listOf(Color(0xFF26C6DA), Color.Transparent, Color(0xFF26C6DA))
            AccelerationState.STOPPED -> listOf(Color.Transparent, Color.Transparent)
        }
    }
}

private data class Particle(
    val baseAngle: Float,
    val radius: Float,
    val size: Float,
    val speedMultiplier: Float
)
