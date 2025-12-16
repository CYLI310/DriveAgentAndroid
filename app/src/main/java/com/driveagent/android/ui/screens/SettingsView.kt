package com.driveagent.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.driveagent.android.manager.LanguageManager
import com.driveagent.android.model.*
import com.driveagent.android.viewmodel.DriveAgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager,
    onDismiss: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val particleEffect by viewModel.particleEffect.collectAsState()
    val showTopBar by viewModel.showTopBar.collectAsState()
    val useMetric by viewModel.useMetric.collectAsState()
    val alertDistance by viewModel.alertDistance.collectAsState()
    val infiniteProximity by viewModel.infiniteProximity.collectAsState()
    
    var alertDistanceSlider by remember(alertDistance) { mutableFloatStateOf(alertDistance.toFloat()) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = languageManager.localize("Settings", language),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = languageManager.localize("Close", language)
                    )
                }
            }
            
            // Language Section
            SettingsSection(title = languageManager.localize("Language", language)) {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = language.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(languageManager.localize("Language", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Appearance Section
            SettingsSection(title = languageManager.localize("Appearance", language)) {
                // Show Top Bar Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = languageManager.localize("Show Top Bar", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = showTopBar,
                        onCheckedChange = { viewModel.setShowTopBar(it) }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Theme Selector
                Text(
                    text = languageManager.localize("Theme", language),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { themeOption ->
                        FilterChip(
                            selected = theme == themeOption,
                            onClick = { viewModel.setTheme(themeOption) },
                            label = { Text(languageManager.localize(themeOption.displayName, language)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Particle Effects Section
            SettingsSection(title = languageManager.localize("Particle Effects", language)) {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = languageManager.localize(particleEffect.displayName, language),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(languageManager.localize("Effect Style", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ParticleEffectStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(languageManager.localize(style.displayName, language)) },
                                onClick = {
                                    viewModel.setParticleEffect(style)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Units Section
            SettingsSection(title = languageManager.localize("Units", language)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = languageManager.localize("Use Metric", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = useMetric,
                        onCheckedChange = { viewModel.setUseMetric(it) }
                    )
                }
                
                Text(
                    text = if (useMetric) {
                        languageManager.localize("Metric Description", language)
                    } else {
                        languageManager.localize("Imperial Description", language)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Trip Section
            SettingsSection(title = languageManager.localize("Trip", language)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = languageManager.localize("Distance", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = viewModel.locationManager.getFormattedDistance(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = languageManager.localize("Max Speed", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = viewModel.locationManager.getFormattedMaxSpeed(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.resetTrip() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(languageManager.localize("Reset Trip", language))
                }
            }
            
            // Speed Camera Alerts Section
            SettingsSection(title = languageManager.localize("Speed Camera Alerts", language)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = languageManager.localize("Infinite Proximity", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = infiniteProximity,
                        onCheckedChange = { viewModel.setInfiniteProximity(it) }
                    )
                }
                
                Text(
                    text = if (infiniteProximity) {
                        languageManager.localize("Infinite Proximity On", language)
                    } else {
                        languageManager.localize("Infinite Proximity Off", language)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${languageManager.localize("Alert Distance", language)}: ${alertDistanceSlider.toInt()} m",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Slider(
                    value = alertDistanceSlider,
                    onValueChange = { alertDistanceSlider = it },
                    onValueChangeFinished = {
                        viewModel.setAlertDistance(alertDistanceSlider.toDouble())
                    },
                    valueRange = 100f..2000f,
                    steps = 37 // (2000-100)/50 - 1
                )
                
                Text(
                    text = languageManager.localize("Alert Distance Description", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        content()
    }
}
