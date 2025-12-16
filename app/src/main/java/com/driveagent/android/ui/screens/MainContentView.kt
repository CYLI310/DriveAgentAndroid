package com.driveagent.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveagent.android.manager.LanguageManager
import com.driveagent.android.model.*
import com.driveagent.android.ui.components.ParticleEffectView
import com.driveagent.android.viewmodel.DriveAgentViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContentView(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager,
    modifier: Modifier = Modifier
) {
    val currentLocation by viewModel.locationManager.currentLocation.collectAsState()
    val currentSpeed by viewModel.locationManager.currentSpeed.collectAsState()
    val currentSpeedMps by viewModel.locationManager.currentSpeedMps.collectAsState()
    val currentStreetName by viewModel.locationManager.currentStreetName.collectAsState()
    val accelerationState by viewModel.locationManager.accelerationState.collectAsState()
    val accelerationMagnitude by viewModel.locationManager.accelerationMagnitude.collectAsState()
    val tripDistance by viewModel.locationManager.tripDistance.collectAsState()
    val maxSpeed by viewModel.locationManager.maxSpeed.collectAsState()
    
    val closestTrap by viewModel.speedTrapDetector.closestTrap.collectAsState()
    val isWithinRange by viewModel.speedTrapDetector.isWithinRange.collectAsState()
    val isSpeeding by viewModel.speedTrapDetector.isSpeeding.collectAsState()
    val speedingAmount by viewModel.speedTrapDetector.speedingAmount.collectAsState()
    val infiniteProximity by viewModel.infiniteProximity.collectAsState()
    
    val showTopBar by viewModel.showTopBar.collectAsState()
    val particleEffect by viewModel.particleEffect.collectAsState()
    val language by viewModel.language.collectAsState()
    val useMetric by viewModel.useMetric.collectAsState()
    
    val showSpeedTrapList by viewModel.showSpeedTrapList.collectAsState()
    
    // Ambient glow animation when speeding
    val alertGlowOpacity by animateFloatAsState(
        targetValue = if (isSpeeding) 0.5f else 0f,
        animationSpec = tween(500),
        label = "alert_glow"
    )
    
    val alertBackgroundOpacity by rememberInfiniteTransition(label = "alert_bg").animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alert_bg_opacity"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient glow when speeding
        if (isSpeeding) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = alertGlowOpacity * 0.5f),
                                Color.Red.copy(alpha = alertGlowOpacity * 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = showTopBar,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                TopBarView(
                    streetName = currentStreetName,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Speed Display with Particle Effect
            Box(
                modifier = Modifier.size(400.dp),
                contentAlignment = Alignment.Center
            ) {
                ParticleEffectView(
                    accelerationState = accelerationState,
                    accelerationMagnitude = accelerationMagnitude,
                    style = particleEffect,
                    isSpeeding = isSpeeding
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentSpeed,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (currentSpeed == if (useMetric) "0 km/h" else "0 mph") {
                        // Show additional info when stopped
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentStreetName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            currentLocation?.let { location ->
                                Text(
                                    text = String.format("%.4f, %.4f", location.latitude, location.longitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = languageManager.localize("Trip Distance", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = viewModel.locationManager.getFormattedDistance(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = languageManager.localize("Max Speed", language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = viewModel.locationManager.getFormattedMaxSpeed(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        currentLocation?.let { location ->
                            Text(
                                text = String.format("%.4f, %.4f", location.latitude, location.longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        } ?: Text(
                            text = languageManager.localize("Searching for location...", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Speed Trap Warning
            AnimatedVisibility(
                visible = closestTrap != null && (isWithinRange || infiniteProximity),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                closestTrap?.let { trap ->
                    SpeedTrapWarning(
                        trap = trap,
                        isSevere = speedingAmount > 10,
                        languageManager = languageManager,
                        language = language,
                        alertBackgroundOpacity = alertBackgroundOpacity
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Speed Trap List (popup) with animation
            AnimatedVisibility(
                visible = showSpeedTrapList,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SpeedTrapListView(
                    viewModel = viewModel,
                    languageManager = languageManager,
                    language = language,
                    onClose = { viewModel.closeSpeedTrapList() }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp, horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    onClick = { viewModel.toggleSpeedTrapList() },
                    icon = Icons.Default.CameraAlt
                )

                GlassButton(
                    onClick = { viewModel.resetTrip() },
                    icon = Icons.Default.Refresh
                )

                GlassButton(
                    onClick = { viewModel.toggleSettings() },
                    icon = Icons.Default.Settings
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TopBarView(
    streetName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = streetName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpeedTrapWarning(
    trap: SpeedTrap,
    isSevere: Boolean,
    languageManager: LanguageManager,
    language: AppLanguage,
    alertBackgroundOpacity: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Red.copy(alpha = alertBackgroundOpacity * 0.8f),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isSevere) {
                        languageManager.localize("Reduce speed immediately", language)
                    } else {
                        languageManager.localize("Speed Camera Ahead!", language)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDistance(trap.distance),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSevere) Color.White else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSevere) Color.Red else Color.White
                        )
                    ) {
                        Text(
                            text = "${languageManager.localize("Limit", language)}: ${trap.speedLimit.replace(".0", "")}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSevere) Color.Red else Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    scale: Float = 1f
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "glass_button_press_scale"
    )

    FilledIconButton(
        onClick = onClick,
        modifier = Modifier
            .size((56 * scale).dp)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale
            )
            .clip(CircleShape),
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size((24 * scale).dp)
        )
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.toInt()}m"
    } else {
        String.format("%.1f km", meters / 1000)
    }
}
