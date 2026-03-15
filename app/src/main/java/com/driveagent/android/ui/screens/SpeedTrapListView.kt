package com.driveagent.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.driveagent.android.manager.LanguageManager
import com.driveagent.android.model.AppLanguage
import com.driveagent.android.model.SpeedTrap
import com.driveagent.android.viewmodel.DriveAgentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTrapListView(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager,
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLocation by viewModel.locationManager.currentLocation.collectAsState()
    var nearbyTraps by remember { mutableStateOf<List<SpeedTrap>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            scope.launch {
                nearbyTraps = viewModel.getNearestSpeedTraps(location, 10)
            }
        }
    }
    
    // Using Surface with Tonal Elevation for a standard M3 container look
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(horizontal = 24.dp), // Increased side padding
        shape = RoundedCornerShape(28.dp), // M3 extra large shape
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageManager.localize("Nearby Cameras", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = languageManager.localize("Close", language)
                    )
                }
            }
            
            Divider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // List
            if (nearbyTraps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = languageManager.localize("No cameras nearby", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nearbyTraps) { trap ->
                        SpeedTrapItem(trap = trap, languageManager = languageManager, language = language)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedTrapItem(
    trap: SpeedTrap,
    languageManager: LanguageManager,
    language: AppLanguage
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = trap.address,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = formatDistance(trap.distance))
                    if (trap.direction.isNotEmpty()) {
                        Text(text = " • ", color = MaterialTheme.colorScheme.outline)
                        Text(text = trap.direction)
                    }
                }
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = null
                ) {
                    Text(
                        text = trap.speedLimit.replace(".0", ""),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        )
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.toInt()}m"
    } else {
        java.lang.String.format(java.util.Locale.US, "%.1f km", meters / 1000)
    }
}
