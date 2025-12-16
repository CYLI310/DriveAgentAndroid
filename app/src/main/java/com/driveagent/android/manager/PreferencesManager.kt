package com.driveagent.android.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.driveagent.android.model.AppLanguage
import com.driveagent.android.model.AppTheme
import com.driveagent.android.model.ParticleEffectStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val PARTICLE_EFFECT = stringPreferencesKey("particle_effect")
        val SHOW_TOP_BAR = booleanPreferencesKey("show_top_bar")
        val USE_METRIC = booleanPreferencesKey("use_metric")
        val LANGUAGE = stringPreferencesKey("language")
        val ALERT_DISTANCE = doublePreferencesKey("alert_distance")
        val INFINITE_PROXIMITY = booleanPreferencesKey("infinite_proximity")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }
    
    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        AppTheme.fromString(preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name)
    }
    
    val particleEffect: Flow<ParticleEffectStyle> = context.dataStore.data.map { preferences ->
        ParticleEffectStyle.fromString(
            preferences[PreferencesKeys.PARTICLE_EFFECT] ?: ParticleEffectStyle.OFF.name
        )
    }
    
    val showTopBar: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_TOP_BAR] ?: true
    }
    
    val useMetric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USE_METRIC] ?: true
    }
    
    val language: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        AppLanguage.fromCode(preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.ENGLISH.code)
    }
    
    val alertDistance: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALERT_DISTANCE] ?: 500.0
    }
    
    val infiniteProximity: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INFINITE_PROXIMITY] ?: false
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }
    
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }
    
    suspend fun setParticleEffect(style: ParticleEffectStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARTICLE_EFFECT] = style.name
        }
    }
    
    suspend fun setShowTopBar(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TOP_BAR] = show
        }
    }
    
    suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_METRIC] = useMetric
        }
    }
    
    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }
    
    suspend fun setAlertDistance(distance: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALERT_DISTANCE] = distance
        }
    }
    
    suspend fun setInfiniteProximity(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INFINITE_PROXIMITY] = enabled
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }
}
