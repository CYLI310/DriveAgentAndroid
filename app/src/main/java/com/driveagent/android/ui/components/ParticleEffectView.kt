package com.driveagent.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.driveagent.android.model.AccelerationState
import com.driveagent.android.model.ParticleEffectStyle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ParticleEffectView(
    accelerationState: AccelerationState,
    accelerationMagnitude: Double,
    style: ParticleEffectStyle,
    isSpeeding: Boolean,
    modifier: Modifier = Modifier
) {
    when (style) {
        ParticleEffectStyle.OFF -> {
            // No effect
        }
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
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val colors = getGradientColors(accelerationState, isSpeeding)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = colors,
                center = center
            ),
            radius = size.minDimension * 1.5f,
            alpha = 0.15f
        )
    }
}

@Composable
private fun OrbitEffect(
    accelerationState: AccelerationState,
    isSpeeding: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(20) {
            Particle(
                baseAngle = (it * 18.0).toFloat(), // 360 / 20
                radius = (100..140).random().toFloat(),
                size = (4..10).random().toFloat(),
                opacity = 0.4f + Math.random().toFloat() * (0.9f - 0.4f)
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val breathingOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    val speedingBreathingOpacity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speeding_breathing"
    )
    
    val particleColor = getParticleColor(accelerationState, isSpeeding)
    val glowColor = particleColor.copy(alpha = 0.3f)
    
    if (accelerationState != AccelerationState.STOPPED) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            particles.forEach { particle ->
                val angle = Math.toRadians((particle.baseAngle + rotation).toDouble())
                val x = centerX + (cos(angle) * particle.radius).toFloat()
                val y = centerY + (sin(angle) * particle.radius).toFloat()
                
                val finalOpacity = if (isSpeeding) {
                    speedingBreathingOpacity * particle.opacity * 0.9f
                } else {
                    breathingOpacity * particle.opacity * 0.9f
                }
                
                // Draw glow
                drawCircle(
                    color = glowColor,
                    radius = particle.size * 2,
                    center = Offset(x, y),
                    alpha = finalOpacity * 0.3f
                )
                
                // Draw particle
                drawCircle(
                    color = particleColor,
                    radius = particle.size / 2,
                    center = Offset(x, y),
                    alpha = finalOpacity
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
            AccelerationState.ACCELERATING -> Color(0xFF0066FF) // Blue
            AccelerationState.DECELERATING -> Color(0xFF00FF66) // Green
            AccelerationState.STEADY -> Color(0xFF0088CC) // Teal
            AccelerationState.STOPPED -> Color.Transparent
        }
    }
}

private fun getGradientColors(accelerationState: AccelerationState, isSpeeding: Boolean): List<Color> {
    return if (isSpeeding) {
        listOf(Color.Red, Color(0xFFFF6600), Color.Red)
    } else {
        when (accelerationState) {
            AccelerationState.ACCELERATING -> listOf(
                Color(0xFF0066FF),
                Color(0xFF00CCFF),
                Color(0xFF0066FF)
            )
            AccelerationState.DECELERATING -> listOf(
                Color(0xFF00FF66),
                Color(0xFF66FFCC),
                Color(0xFF00FF66)
            )
            AccelerationState.STEADY -> listOf(
                Color(0xFF00CCCC),
                Color(0xFF0066FF),
                Color(0xFF00CCCC)
            )
            AccelerationState.STOPPED -> listOf(
                Color.Gray.copy(alpha = 0.5f),
                Color.Transparent
            )
        }
    }
}

private data class Particle(
    val baseAngle: Float,
    val radius: Float,
    val size: Float,
    val opacity: Float
)
