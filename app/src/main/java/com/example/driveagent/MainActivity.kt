package com.example.driveagent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException

// =========================================================================
// NEW: Bridge for persistent settings using native SharedPreferences
// =========================================================================
class SettingsBridge(context: Context) {
    // Use a specific file name for the preferences, "AppSettings"
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    /**
     * Saves a string value to Android's SharedPreferences.
     * Accessible in JavaScript via: AndroidSettingsBridge.saveSetting(name, value)
     */
    @JavascriptInterface
    fun saveSetting(name: String, value: String) {
        sharedPrefs.edit().putString(name, value).apply()
    }

    /**
     * Retrieves a string value from Android's SharedPreferences.
     * Accessible in JavaScript via: AndroidSettingsBridge.getSetting(name)
     */
    @JavascriptInterface
    fun getSetting(name: String): String? {
        // Return null if the key is not found
        return sharedPrefs.getString(name, null)
    }
}
// =========================================================================

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Existing bridge for map data and location triggers
    inner class WebAppInterface {
        @JavascriptInterface
        fun triggerLocationUpdate() {
            runOnUiThread {
                webView.loadUrl("javascript:triggerLocationUpdate()")
            }
        }

        @JavascriptInterface
        fun getGeoJsonData(): String? {
            return try {
                assets.open("speedtraps.geojson").bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView = findViewById(R.id.webView)

        // Performance and functionality settings
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setGeolocationEnabled(true)

        // 1. Existing bridge for map/data
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // 2. NEW: Add the settings persistence bridge
        val settingsBridge = SettingsBridge(this)
        webView.addJavascriptInterface(settingsBridge, "AndroidSettingsBridge")

        // A simple WebViewClient is sufficient as we are handling data via JS Interface
        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                } else {
                    callback.invoke(origin, true, false)
                }
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Note: The JS triggerLocationUpdate() is vestigial now that we use watchPosition,
                // but we keep it here to allow the JS geolocation logic to run after permission is granted.
                webView.loadUrl("javascript:triggerLocationUpdate()")
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}