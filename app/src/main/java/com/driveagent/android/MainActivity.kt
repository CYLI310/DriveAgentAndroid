package com.driveagent.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.driveagent.android.manager.LanguageManager
import com.driveagent.android.model.AppTheme
import com.driveagent.android.ui.screens.MainContentView
import com.driveagent.android.ui.screens.SettingsView
import com.driveagent.android.ui.theme.DriveAgentAndroidTheme
import com.driveagent.android.viewmodel.DriveAgentViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    
    private val viewModel: DriveAgentViewModel by viewModels()
    private val languageManager = LanguageManager()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val theme by viewModel.theme.collectAsState()
            val language by viewModel.language.collectAsState()
            
            val darkTheme = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            
            DriveAgentAndroidTheme(darkTheme = darkTheme) {
                DriveAgentApp(
                    viewModel = viewModel,
                    languageManager = languageManager
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.locationManager.checkPermissions()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DriveAgentApp(
    viewModel: DriveAgentViewModel,
    languageManager: LanguageManager
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    val showSettings by viewModel.showSettings.collectAsState()
    val language by viewModel.language.collectAsState()
    
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.locationManager.checkPermissions()
            viewModel.startLocationTracking()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            !locationPermissionsState.allPermissionsGranted -> {
                PermissionRequestView(
                    onRequestPermissions = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    },
                    languageManager = languageManager,
                    language = language
                )
            }
            else -> {
                MainContentView(
                    viewModel = viewModel,
                    languageManager = languageManager
                )
                
                // Settings Sheet
                if (showSettings) {
                    SettingsView(
                        viewModel = viewModel,
                        languageManager = languageManager,
                        onDismiss = { viewModel.closeSettings() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestView(
    onRequestPermissions: () -> Unit,
    languageManager: LanguageManager,
    language: com.driveagent.android.model.AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = languageManager.localize("Location Access Denied", language),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = languageManager.localize("Location Permission Text", language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(languageManager.localize("Open Settings", language))
        }
    }
}
