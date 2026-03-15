package com.driveagent.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageManager.localize("Settings", language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = languageManager.localize("Close", language)
                    )
                }
            }
            
            // Language
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
                        label = { Text(languageManager.localize("Select Language", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = { viewModel.setLanguage(lang); expanded = false }
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Appearance
            SettingsSection(title = languageManager.localize("Appearance", language)) {
                ListItem(
                    headlineContent = { Text(languageManager.localize("Show Top Bar", language)) },
                    trailingContent = {
                        Switch(checked = showTopBar, onCheckedChange = { viewModel.setShowTopBar(it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = languageManager.localize("Theme", language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Visual Effects
            SettingsSection(title = languageManager.localize("Visual Effects", language)) {
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ParticleEffectStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(languageManager.localize(style.displayName, language)) },
                                onClick = { viewModel.setParticleEffect(style); expanded = false }
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Units
            SettingsSection(title = languageManager.localize("Units", language)) {
                ListItem(
                    headlineContent = { Text(languageManager.localize("Use Metric System", language)) },
                    supportingContent = {
                        Text(text = if (useMetric) languageManager.localize("Metric Description", language) else languageManager.localize("Imperial Description", language))
                    },
                    trailingContent = {
                        Switch(checked = useMetric, onCheckedChange = { viewModel.setUseMetric(it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Safety
            SettingsSection(title = languageManager.localize("Safety Alerts", language)) {
                ListItem(
                    headlineContent = { Text(languageManager.localize("Infinite Proximity", language)) },
                    supportingContent = {
                        Text(text = if (infiniteProximity) languageManager.localize("Infinite Proximity On", language) else languageManager.localize("Infinite Proximity Off", language))
                    },
                    trailingContent = {
                        Switch(checked = infiniteProximity, onCheckedChange = { viewModel.setInfiniteProximity(it) })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${languageManager.localize("Alert Distance", language)}: ${alertDistanceSlider.toInt()} m",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = alertDistanceSlider,
                    onValueChange = { alertDistanceSlider = it },
                    onValueChangeFinished = { viewModel.setAlertDistance(alertDistanceSlider.toDouble()) },
                    valueRange = 100f..2000f,
                    steps = 37,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Stats
            SettingsSection(title = languageManager.localize("Trip Statistics", language)) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = languageManager.localize("Distance", language), style = MaterialTheme.typography.bodyMedium)
                            Text(text = viewModel.locationManager.getFormattedDistance(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = languageManager.localize("Max Speed", language), style = MaterialTheme.typography.bodyMedium)
                            Text(text = viewModel.locationManager.getFormattedMaxSpeed(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.resetTrip() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(languageManager.localize("Reset Trip Stats", language))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}
