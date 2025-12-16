package com.driveagent.android.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.driveagent.android.model.AccelerationState
import com.driveagent.android.model.LatLng
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.math.abs

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()
    
    private val _currentSpeed = MutableStateFlow("0 km/h")
    val currentSpeed: StateFlow<String> = _currentSpeed.asStateFlow()
    
    private val _currentSpeedMps = MutableStateFlow(0.0)
    val currentSpeedMps: StateFlow<Double> = _currentSpeedMps.asStateFlow()
    
    private val _currentCourse = MutableStateFlow(-1.0)
    val currentCourse: StateFlow<Double> = _currentCourse.asStateFlow()
    
    private val _currentStreetName = MutableStateFlow("Finding your location...")
    val currentStreetName: StateFlow<String> = _currentStreetName.asStateFlow()
    
    private val _accelerationState = MutableStateFlow(AccelerationState.STOPPED)
    val accelerationState: StateFlow<AccelerationState> = _accelerationState.asStateFlow()
    
    private val _accelerationMagnitude = MutableStateFlow(0.0)
    val accelerationMagnitude: StateFlow<Double> = _accelerationMagnitude.asStateFlow()
    
    private val _tripDistance = MutableStateFlow(0.0)
    val tripDistance: StateFlow<Double> = _tripDistance.asStateFlow()
    
    private val _maxSpeed = MutableStateFlow(0.0)
    val maxSpeed: StateFlow<Double> = _maxSpeed.asStateFlow()
    
    private val _useMetric = MutableStateFlow(true)
    val useMetric: StateFlow<Boolean> = _useMetric.asStateFlow()
    
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    
    private var previousSpeed = 0.0
    private var lastTripLocation: Location? = null
    private var lastGeocodedLocation: Location? = null
    private var isGeocoding = false
    
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L // Update every second
    ).apply {
        setMinUpdateIntervalMillis(500L)
        setMaxUpdateDelayMillis(2000L)
    }.build()
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleLocationUpdate(location)
            }
        }
    }
    
    init {
        checkPermissions()
    }
    
    fun checkPermissions() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        _hasLocationPermission.value = hasPermission
    }
    
    fun startLocationUpdates() {
        if (!_hasLocationPermission.value) {
            return
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    private fun handleLocationUpdate(location: Location) {
        _currentLocation.value = LatLng(location.latitude, location.longitude)
        
        if (location.hasBearing()) {
            _currentCourse.value = location.bearing.toDouble()
        }
        
        val speedInMetersPerSecond = if (location.hasSpeed()) location.speed.toDouble() else 0.0
        
        // Update trip distance
        lastTripLocation?.let { lastLocation ->
            val distance = location.distanceTo(lastLocation).toDouble()
            if (distance > 0 && distance < 100) { // Sanity check
                _tripDistance.value += distance
            }
        }
        lastTripLocation = location
        
        if (speedInMetersPerSecond > 0) {
            _currentSpeed.value = formatSpeed(speedInMetersPerSecond)
            _currentSpeedMps.value = speedInMetersPerSecond
            
            // Track max speed
            if (speedInMetersPerSecond > _maxSpeed.value) {
                _maxSpeed.value = speedInMetersPerSecond
            }
            
            // Determine acceleration
            val speedDiff = speedInMetersPerSecond - previousSpeed
            _accelerationMagnitude.value = abs(speedDiff)
            
            _accelerationState.value = when {
                speedDiff > 0.1 -> AccelerationState.ACCELERATING
                speedDiff < -0.1 -> AccelerationState.DECELERATING
                else -> AccelerationState.STEADY
            }
        } else {
            _currentSpeed.value = if (_useMetric.value) "0 km/h" else "0 mph"
            _currentSpeedMps.value = 0.0
            _accelerationState.value = AccelerationState.STOPPED
            _accelerationMagnitude.value = 0.0
        }
        
        previousSpeed = speedInMetersPerSecond
        
        // Geocode if needed
        if (shouldGeocode(location)) {
            CoroutineScope(Dispatchers.IO).launch {
                geocodeLocation(location)
            }
        }
    }
    
    private fun shouldGeocode(newLocation: Location): Boolean {
        if (isGeocoding) return false
        
        val lastLocation = lastGeocodedLocation ?: return true
        
        return newLocation.distanceTo(lastLocation) > 50
    }
    
    private suspend fun geocodeLocation(location: Location) {
        isGeocoding = true
        
        try {
            val addresses = suspendCancellableCoroutine<List<Address>?> { continuation ->
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addresses ->
                            continuation.resume(addresses)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val result = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        continuation.resume(result)
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
            
            val streetName = addresses?.firstOrNull()?.let { address ->
                address.thoroughfare ?: address.featureName ?: "Unnamed Road"
            } ?: "Unknown Street"
            
            _currentStreetName.value = streetName
            lastGeocodedLocation = location
            
        } catch (e: Exception) {
            _currentStreetName.value = "Unknown Street"
        } finally {
            isGeocoding = false
        }
    }
    
    private fun formatSpeed(speedInMetersPerSecond: Double): String {
        return if (_useMetric.value) {
            val speedInKmh = speedInMetersPerSecond * 3.6
            String.format("%.0f km/h", speedInKmh)
        } else {
            val speedInMph = speedInMetersPerSecond * 2.23694
            String.format("%.0f mph", speedInMph)
        }
    }
    
    fun getFormattedDistance(): String {
        val distance = _tripDistance.value
        return if (_useMetric.value) {
            if (distance < 1000) {
                String.format("%.0f m", distance)
            } else {
                String.format("%.2f km", distance / 1000)
            }
        } else {
            val miles = distance * 0.000621371
            if (miles < 0.1) {
                val feet = distance * 3.28084
                String.format("%.0f ft", feet)
            } else {
                String.format("%.2f mi", miles)
            }
        }
    }
    
    fun getFormattedMaxSpeed(): String {
        return formatSpeed(_maxSpeed.value)
    }
    
    fun resetTrip() {
        _tripDistance.value = 0.0
        _maxSpeed.value = 0.0
        lastTripLocation = null
    }
    
    fun setUseMetric(useMetric: Boolean) {
        _useMetric.value = useMetric
    }
}
