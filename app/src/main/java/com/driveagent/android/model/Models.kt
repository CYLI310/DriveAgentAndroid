package com.driveagent.android.model

/**
 * Simple latitude/longitude coordinate
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/**
 * Represents a speed trap/camera location
 */
data class SpeedTrap(
    val coordinate: LatLng,
    val speedLimit: String,
    val address: String,
    val direction: String,
    val distance: Double = 0.0
)

/**
 * Acceleration state of the vehicle
 */
enum class AccelerationState {
    ACCELERATING,
    DECELERATING,
    STEADY,
    STOPPED
}

/**
 * Particle effect styles
 */
enum class ParticleEffectStyle(val displayName: String) {
    OFF("Off"),
    ORBIT("Orbit"),
    LINEAR_GRADIENT("Gradient");
    
    companion object {
        fun fromString(value: String): ParticleEffectStyle {
            return values().find { it.name == value } ?: OFF
        }
    }
}

/**
 * App theme options
 */
enum class AppTheme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");
    
    companion object {
        fun fromString(value: String): AppTheme {
            return values().find { it.name == value } ?: SYSTEM
        }
    }
}

/**
 * Supported languages
 */
enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    CHINESE_TRADITIONAL("繁體中文", "zh-TW"),
    CHINESE_SIMPLIFIED("简体中文", "zh-CN"),
    KOREAN("한국어", "ko"),
    JAPANESE("日本語", "ja"),
    VIETNAMESE("Tiếng Việt", "vi"),
    THAI("ไทย", "th"),
    FILIPINO("Filipino", "fil"),
    HINDI("हिन्दी", "hi"),
    ARABIC("العربية", "ar"),
    SPANISH("Español", "es"),
    GERMAN("Deutsch", "de"),
    FRENCH("Français", "fr"),
    ITALIAN("Italiano", "it"),
    PORTUGUESE("Português", "pt"),
    RUSSIAN("Русский", "ru");
    
    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * Alert sound types
 */
enum class AlertSound(val displayName: String) {
    NAVIGATION_POP("Navigation Pop"),
    SOFT_CHIME("Soft Chime"),
    MODERN("Modern"),
    BLOOM("Bloom");
    
    companion object {
        fun fromString(value: String): AlertSound {
            return values().find { it.name == value } ?: NAVIGATION_POP
        }
    }
}
