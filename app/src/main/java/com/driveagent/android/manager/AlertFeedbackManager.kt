package com.driveagent.android.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.driveagent.android.R
import com.driveagent.android.model.AlertSound
import kotlinx.coroutines.*

class AlertFeedbackManager(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var alertJob: Job? = null
    private var currentInterval = 4.0 // seconds
    
    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    fun startSpeedingAlert(interval: Double = 4.0) {
        stopSpeedingAlert()
        
        currentInterval = interval
        
        alertJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                playChime()
                triggerHaptic()
                delay((interval * 1000).toLong())
            }
        }
    }
    
    fun stopSpeedingAlert() {
        alertJob?.cancel()
        alertJob = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    private fun playChime() {
        try {
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer.create(
                context,
                R.raw.navigation_pop
            ).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .build()
                )
                
                setVolume(1.0f, 1.0f)
                
                setOnCompletionListener {
                    it.release()
                }
                
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun triggerHaptic() {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Soft pulse pattern
                    val timings = longArrayOf(0, 100, 50, 100)
                    val amplitudes = intArrayOf(0, 80, 0, 80)
                    
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(200)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun cleanup() {
        stopSpeedingAlert()
        vibrator = null
    }
}
