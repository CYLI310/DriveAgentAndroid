package com.driveagent.android.manager

import android.content.Context
import android.location.Location
import com.driveagent.android.R
import com.driveagent.android.model.LatLng
import com.driveagent.android.model.SpeedTrap
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min

class SpeedTrapDetector(private val context: Context) {
    
    private val _closestTrap = MutableStateFlow<SpeedTrap?>(null)
    val closestTrap: StateFlow<SpeedTrap?> = _closestTrap.asStateFlow()
    
    private val _isWithinRange = MutableStateFlow(false)
    val isWithinRange: StateFlow<Boolean> = _isWithinRange.asStateFlow()
    
    private val _isSpeeding = MutableStateFlow(false)
    val isSpeeding: StateFlow<Boolean> = _isSpeeding.asStateFlow()
    
    private val _speedingAmount = MutableStateFlow(0.0)
    val speedingAmount: StateFlow<Double> = _speedingAmount.asStateFlow()
    
    private val _alertDistance = MutableStateFlow(500.0)
    val alertDistance: StateFlow<Double> = _alertDistance.asStateFlow()
    
    private val _infiniteProximity = MutableStateFlow(false)
    val infiniteProximity: StateFlow<Boolean> = _infiniteProximity.asStateFlow()
    
    private var lastCheckLocation: Location? = null
    private var isChecking = false
    
    fun setAlertDistance(distance: Double) {
        _alertDistance.value = distance
    }
    
    fun setInfiniteProximity(enabled: Boolean) {
        _infiniteProximity.value = enabled
    }
    
    suspend fun checkForNearbyTraps(
        userLocation: LatLng,
        currentSpeed: Double = 0.0,
        currentStreetName: String = "",
        currentCourse: Double = -1.0
    ) = withContext(Dispatchers.IO) {
        if (isChecking) return@withContext
        
        val currentLocation = Location("").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        
        // Only check if moved significantly (100m) unless infinite proximity is on
        lastCheckLocation?.let { lastLocation ->
            val distance = currentLocation.distanceTo(lastLocation)
            if (distance < 100 && !_infiniteProximity.value) {
                return@withContext
            }
        }
        
        lastCheckLocation = currentLocation
        isChecking = true
        
        try {
            val traps = loadSpeedTraps()
            var closestDistance = Double.POSITIVE_INFINITY
            var closestTrapData: SpeedTrap? = null
            
            var bestCandidate: SpeedTrap? = null
            var bestCandidateDistance = Double.POSITIVE_INFINITY
            var bestCandidateScore = 0
            
            for (trap in traps) {
                val trapLocation = Location("").apply {
                    latitude = trap.coordinate.latitude
                    longitude = trap.coordinate.longitude
                }
                
                val distance = currentLocation.distanceTo(trapLocation).toDouble()
                
                val withinRange = _infiniteProximity.value || distance < 2000
                
                if (withinRange) {
                    var score = 0
                    
                    // Road name match
                    if (currentStreetName.isNotEmpty() &&
                        currentStreetName != "Finding your location..." &&
                        currentStreetName != "Unknown Street"
                    ) {
                        if (isRoadMatching(currentStreetName, trap.address)) {
                            score += 1000
                        }
                    }
                    
                    // Direction match
                    if (currentCourse >= 0) {
                        if (isDirectionMatching(currentCourse, trap.direction)) {
                            score += 500
                        }
                    }
                    
                    // Distance score
                    if (distance < 2000) {
                        score += ((2000 - distance) / 10).toInt()
                    }
                    
                    if (score > bestCandidateScore) {
                        bestCandidateScore = score
                        bestCandidateDistance = distance
                        bestCandidate = trap.copy(distance = distance)
                    } else if (score == bestCandidateScore && distance < bestCandidateDistance) {
                        bestCandidateDistance = distance
                        bestCandidate = trap.copy(distance = distance)
                    }
                }
            }
            
            if (bestCandidate != null) {
                closestDistance = bestCandidateDistance
                closestTrapData = bestCandidate
            }
            
            withContext(Dispatchers.Main) {
                if (closestTrapData != null) {
                    _closestTrap.value = closestTrapData
                    _isWithinRange.value = closestDistance <= _alertDistance.value
                    
                    // Check if speeding
                    val speedLimitValue = closestTrapData.speedLimit
                        .replace(".0", "")
                        .toDoubleOrNull()
                    
                    if (speedLimitValue != null) {
                        val currentSpeedKmh = currentSpeed * 3.6
                        _speedingAmount.value = currentSpeedKmh - speedLimitValue
                        _isSpeeding.value = (_isWithinRange.value || _infiniteProximity.value) &&
                                _speedingAmount.value > 0
                    } else {
                        _isSpeeding.value = false
                        _speedingAmount.value = 0.0
                    }
                } else {
                    _closestTrap.value = null
                    _isWithinRange.value = false
                    _isSpeeding.value = false
                    _speedingAmount.value = 0.0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isChecking = false
        }
    }
    
    suspend fun getNearestSpeedTraps(
        userLocation: LatLng,
        count: Int = 10
    ): List<SpeedTrap> = withContext(Dispatchers.IO) {
        val currentLocation = Location("").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        
        try {
            val traps = loadSpeedTraps()
            
            traps.map { trap ->
                val trapLocation = Location("").apply {
                    latitude = trap.coordinate.latitude
                    longitude = trap.coordinate.longitude
                }
                val distance = currentLocation.distanceTo(trapLocation).toDouble()
                trap.copy(distance = distance)
            }
                .sortedBy { it.distance }
                .take(count)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun loadSpeedTraps(): List<SpeedTrap> {
        val inputStream = context.resources.openRawResource(R.raw.speedtraps)
        val json = inputStream.bufferedReader().use { it.readText() }
        
        val gson = Gson()
        val featureCollection = gson.fromJson(json, FeatureCollection::class.java)
        
        return featureCollection.features.mapNotNull { feature ->
            if (feature.geometry.coordinates.size >= 2) {
                SpeedTrap(
                    coordinate = LatLng(
                        feature.geometry.coordinates[1],
                        feature.geometry.coordinates[0]
                    ),
                    speedLimit = feature.properties.name,
                    address = feature.properties.address,
                    direction = feature.properties.direction ?: ""
                )
            } else {
                null
            }
        }
    }
    
    private fun isRoadMatching(userStreet: String, trapAddress: String): Boolean {
        if (userStreet.length < 2) return false
        return trapAddress.contains(userStreet)
    }
    
    private fun isDirectionMatching(userCourse: Double, trapDirection: String): Boolean {
        if (trapDirection.contains("雙向")) {
            return true
        }
        
        val targetHeading = when {
            trapDirection.contains("南向北") -> 0.0  // North
            trapDirection.contains("北向南") -> 180.0 // South
            trapDirection.contains("西向東") -> 90.0  // East
            trapDirection.contains("東向西") -> 270.0 // West
            else -> return true // Unknown direction, assume match
        }
        
        val diff = abs(userCourse - targetHeading)
        val minDiff = min(diff, 360 - diff)
        
        return minDiff < 60
    }
    
    // GeoJSON data classes
    private data class FeatureCollection(
        val features: List<Feature>
    )
    
    private data class Feature(
        val geometry: Geometry,
        val properties: Properties
    )
    
    private data class Geometry(
        val coordinates: List<Double>
    )
    
    private data class Properties(
        val name: String,
        @SerializedName("設置地址") val address: String,
        @SerializedName("拍攝方向") val direction: String?
    )
}
