package com.driveagent.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.driveagent.android.manager.*
import com.driveagent.android.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DriveAgentViewModel(application: Application) : AndroidViewModel(application) {
    
    val locationManager = LocationManager(application)
    val speedTrapDetector = SpeedTrapDetector(application)
    val alertFeedbackManager = AlertFeedbackManager(application)
    val preferencesManager = PreferencesManager(application)
    val languageManager = LanguageManager()
    
    // UI State
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    
    private val _showSpeedTrapList = MutableStateFlow(false)
    val showSpeedTrapList: StateFlow<Boolean> = _showSpeedTrapList.asStateFlow()
    

    
    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()
    
    // Preferences
    val theme = preferencesManager.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppTheme.SYSTEM
    )
    
    val particleEffect = preferencesManager.particleEffect.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ParticleEffectStyle.OFF
    )
    
    val showTopBar = preferencesManager.showTopBar.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )
    
    val useMetric = preferencesManager.useMetric.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )
    
    val language = preferencesManager.language.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppLanguage.ENGLISH
    )
    
    val alertDistance = preferencesManager.alertDistance.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        500.0
    )
    
    val infiniteProximity = preferencesManager.infiniteProximity.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
    
    val hasCompletedOnboarding = preferencesManager.hasCompletedOnboarding.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
    
    init {
        // Observe location changes and check for speed traps
        viewModelScope.launch {
            locationManager.currentLocation.collect { location ->
                location?.let {
                    speedTrapDetector.checkForNearbyTraps(
                        userLocation = it,
                        currentSpeed = locationManager.currentSpeedMps.value,
                        currentStreetName = locationManager.currentStreetName.value,
                        currentCourse = locationManager.currentCourse.value
                    )
                }
            }
        }
        
        // Observe speeding state and trigger alerts
        viewModelScope.launch {
            combine(
                speedTrapDetector.isSpeeding,
                speedTrapDetector.speedingAmount
            ) { isSpeeding, speedingAmount ->
                Pair(isSpeeding, speedingAmount)
            }.collect { (isSpeeding, speedingAmount) ->
                if (isSpeeding) {
                    val isSevere = speedingAmount > 10
                    val interval = if (isSevere) 2.0 else 4.0
                    alertFeedbackManager.startSpeedingAlert(interval)
                } else {
                    alertFeedbackManager.stopSpeedingAlert()
                }
            }
        }
        
        // Sync preferences with managers
        viewModelScope.launch {
            useMetric.collect { metric ->
                locationManager.setUseMetric(metric)
            }
        }
        
        viewModelScope.launch {
            alertDistance.collect { distance ->
                speedTrapDetector.setAlertDistance(distance)
            }
        }
        
        viewModelScope.launch {
            infiniteProximity.collect { enabled ->
                speedTrapDetector.setInfiniteProximity(enabled)
            }
        }
        
        // Check if onboarding is needed
        viewModelScope.launch {
            if (!hasCompletedOnboarding.value) {
                _showOnboarding.value = true
            }
        }
    }
    
    fun startLocationTracking() {
        locationManager.startLocationUpdates()
    }
    
    fun stopLocationTracking() {
        locationManager.stopLocationUpdates()
    }
    
    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }
    
    fun toggleSpeedTrapList() {
        _showSpeedTrapList.value = !_showSpeedTrapList.value
    }
    
    fun closeSettings() {
        _showSettings.value = false
    }
    
    fun closeSpeedTrapList() {
        _showSpeedTrapList.value = false
    }
    

    
    fun closeOnboarding() {
        _showOnboarding.value = false
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }
    
    fun setParticleEffect(style: ParticleEffectStyle) {
        viewModelScope.launch {
            preferencesManager.setParticleEffect(style)
        }
    }
    
    fun setShowTopBar(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowTopBar(show)
        }
    }
    
    fun setUseMetric(useMetric: Boolean) {
        viewModelScope.launch {
            preferencesManager.setUseMetric(useMetric)
        }
    }
    
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
        }
    }
    
    fun setAlertDistance(distance: Double) {
        viewModelScope.launch {
            preferencesManager.setAlertDistance(distance)
        }
    }
    
    fun setInfiniteProximity(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setInfiniteProximity(enabled)
        }
    }
    
    fun resetTrip() {
        locationManager.resetTrip()
    }
    
    suspend fun getNearestSpeedTraps(location: LatLng, count: Int = 10) =
        speedTrapDetector.getNearestSpeedTraps(location, count)
    
    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        alertFeedbackManager.cleanup()
    }
}
