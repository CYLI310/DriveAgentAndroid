package com.driveagent.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveagent.android.manager.LanguageManager
import com.driveagent.android.model.*
import com.driveagent.android.ui.components.ParticleEffectView
import com.driveagent.android.viewmodel.DriveAgentViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainContentView(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager,
    modifier: Modifier = Modifier
) {
    val currentSpeedMps by viewModel.locationManager.currentSpeedMps.collectAsState()
    val currentStreetName by viewModel.locationManager.currentStreetName.collectAsState()
    val accelerationState by viewModel.locationManager.accelerationState.collectAsState()
    val currentLocation by viewModel.locationManager.currentLocation.collectAsState()
    
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
    
    // Tilt effect based on acceleration
    val tiltAngle by animateFloatAsState(
        targetValue = when (accelerationState) {
            AccelerationState.ACCELERATING -> -1.5f
            AccelerationState.DECELERATING -> 1.5f
            else -> 0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ui_tilt"
    )

    // Background color based on acceleration
    val glowColor by animateColorAsState(
        targetValue = when {
            isSpeeding -> Color.Red.copy(alpha = 0.15f)
            accelerationState == AccelerationState.ACCELERATING -> Color.Blue.copy(alpha = 0.05f)
            accelerationState == AccelerationState.DECELERATING -> Color.Green.copy(alpha = 0.05f)
            else -> Color.Transparent
        },
        animationSpec = tween(1000),
        label = "bg_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = tiltAngle
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = showTopBar,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                TopBarView(
                    streetName = currentStreetName,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1.2f))
            
            // Central Speed Display
            Box(
                modifier = Modifier.size(420.dp),
                contentAlignment = Alignment.Center
            ) {
                ParticleEffectView(
                    accelerationState = accelerationState,
                    accelerationMagnitude = 0.0,
                    style = particleEffect,
                    isSpeeding = isSpeeding
                )
                
                AnimatedSpeedDisplay(
                    speedMps = currentSpeedMps,
                    useMetric = useMetric,
                    isSpeeding = isSpeeding
                )
            }
            
            Spacer(modifier = Modifier.weight(0.8f))
            
            // Location / Info when slow/stopped
            AnimatedVisibility(
                visible = currentSpeedMps < 0.5,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StoppedInfoView(
                    viewModel = viewModel,
                    languageManager = languageManager,
                    language = language,
                    currentLocation = currentLocation
                )
            }

            // Speed Trap Warning
            AnimatedVisibility(
                visible = closestTrap != null && (isWithinRange || infiniteProximity),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn() + scaleIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut() + scaleOut()
            ) {
                closestTrap?.let { trap ->
                    SpeedTrapWarning(
                        trap = trap,
                        isSevere = speedingAmount > 10,
                        languageManager = languageManager,
                        language = language
                    )
                }
            }
            
            // Bottom Controls
            BottomControls(
                viewModel = viewModel,
                showSpeedTrapList = showSpeedTrapList
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        // Overlay Speed Trap List
        AnimatedVisibility(
            visible = showSpeedTrapList,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SpeedTrapListView(
                viewModel = viewModel,
                languageManager = languageManager,
                language = language,
                onClose = { viewModel.closeSpeedTrapList() },
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
fun AnimatedSpeedDisplay(
    speedMps: Double,
    useMetric: Boolean,
    isSpeeding: Boolean
) {
    val speedValue = if (useMetric) speedMps * 3.6 else speedMps * 2.23694
    val animatedSpeed by animateFloatAsState(
        targetValue = speedValue.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "speed_val"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format(Locale.US, "%.0f", animatedSpeed),
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                color = if (isSpeeding) Color.Red else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (useMetric) "km/h" else "mph",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier
                    .alignByBaseline()
                    .padding(bottom = 14.dp)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TopBarView(
    streetName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedContent(
                targetState = streetName,
                transitionSpec = {
                    (fadeIn() + slideInVertically()).with(fadeOut() + slideOutVertically())
                },
                label = "street_anim"
            ) { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StoppedInfoView(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager,
    language: AppLanguage,
    currentLocation: LatLng?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoChip(
                label = languageManager.localize("Distance", language),
                value = viewModel.locationManager.getFormattedDistance()
            )
            // Manual vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
            InfoChip(
                label = languageManager.localize("Max Speed", language),
                value = viewModel.locationManager.getFormattedMaxSpeed()
            )
        }
        
        currentLocation?.let {
            Text(
                text = String.format(Locale.US, "%.5f, %.5f", it.latitude, it.longitude),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpeedTrapWarning(
    trap: SpeedTrap,
    isSevere: Boolean,
    languageManager: LanguageManager,
    language: AppLanguage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = if (isSevere) pulseScale else 1f
                scaleY = if (isSevere) pulseScale else 1f
            },
        shape = RoundedCornerShape(24.dp),
        color = if (isSevere) Color.Red else Color(0xFFF44336),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSevere) {
                        languageManager.localize("REDUCE SPEED", language)
                    } else {
                        languageManager.localize("Speed Camera", language)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                Text(
                    text = formatDistance(trap.distance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Text(
                    text = trap.speedLimit.replace(".0", ""),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomControls(
    viewModel: DriveAgentViewModel,
    showSpeedTrapList: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp, start = 40.dp, end = 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassButton(
            onClick = { viewModel.toggleSpeedTrapList() },
            icon = if (showSpeedTrapList) Icons.Default.ExpandMore else Icons.Default.CameraAlt,
            containerColor = if (showSpeedTrapList) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else null
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
}

@Composable
private fun GlassButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btn_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .shadow(4.dp, CircleShape)
            .background(
                containerColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                CircleShape
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .clip(CircleShape),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        String.format(Locale.US, "%dm", meters.toInt())
    } else {
        String.format(Locale.US, "%.1f km", meters / 1000)
    }
}
