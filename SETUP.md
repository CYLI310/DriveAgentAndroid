# DriveAgent Android - Quick Setup Guide

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 26+
- Google Maps API Key

## Step-by-Step Setup

### 1. Get Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Maps SDK for Android**
4. Go to **Credentials** → **Create Credentials** → **API Key**
5. Copy your API key

### 2. Configure API Key
Create a file named `secrets.properties` in the project root:
```properties
MAPS_API_KEY=your_actual_api_key_here
```

Or add to `local.properties`:
```properties
MAPS_API_KEY=your_actual_api_key_here
```

### 3. Open Project
1. Launch Android Studio
2. Click **Open** → Navigate to `DriveAgentAndroid` folder
3. Wait for Gradle sync to complete (may take a few minutes)

### 4. Build Project
```bash
./gradlew build
```

Or in Android Studio: **Build** → **Make Project** (Ctrl+F9 / Cmd+F9)

### 5. Run on Device/Emulator
1. Connect Android device via USB (enable USB debugging) OR start an emulator
2. Click **Run** button (green play icon) or press Shift+F10
3. Select your device
4. Grant location permissions when prompted

## Troubleshooting

### Gradle Sync Failed
- Check internet connection
- Update Android Studio to latest version
- Invalidate caches: **File** → **Invalidate Caches / Restart**

### Maps Not Showing
- Verify API key is correctly set in `secrets.properties`
- Ensure Maps SDK for Android is enabled in Google Cloud Console
- Check API key restrictions (should allow Android apps)

### Location Not Working
- Grant location permissions in app
- Enable GPS on device
- For emulator: send location via Extended Controls (...)

### Build Errors
- Clean project: **Build** → **Clean Project**
- Rebuild: **Build** → **Rebuild Project**
- Check JDK version: **File** → **Project Structure** → JDK 17

## Testing

### On Physical Device (Recommended)
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect via USB
4. Run the app
5. Drive around to test speed tracking and camera alerts

### On Emulator
1. Create AVD with Google Play Services
2. Use Extended Controls to simulate GPS location
3. Send route or single location points
4. Note: Speed tracking may not work realistically in emulator

## Features to Test

- ✅ Location permission request
- ✅ Speed display updates
- ✅ Particle effects (change in settings)
- ✅ Speed camera detection (if near Taiwan cameras)
- ✅ Speed limit alerts (audio + haptic)
- ✅ Settings changes (theme, language, units)
- ✅ Trip statistics
- ✅ Speed trap list view

## Next Steps

1. **Customize Speed Trap Data**: Replace `speedtraps.geojson` with your region's data
2. **Add More Languages**: Extend `LanguageManager.kt`
3. **Implement Full Map**: Complete Google Maps integration in `MainContentView.kt`
4. **Add Widgets**: Create home screen widgets using Glance API
5. **Background Service**: Implement foreground service for continuous tracking

## Project Structure

```
DriveAgentAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/driveagent/android/
│   │   │   ├── model/              # Data models
│   │   │   ├── manager/            # Business logic
│   │   │   ├── viewmodel/          # ViewModels
│   │   │   ├── ui/
│   │   │   │   ├── screens/        # UI screens
│   │   │   │   ├── components/     # Reusable components
│   │   │   │   └── theme/          # Material 3 theme
│   │   │   ├── MainActivity.kt
│   │   │   └── DriveAgentApplication.kt
│   │   ├── res/
│   │   │   ├── raw/                # speedtraps.geojson, audio files
│   │   │   ├── values/             # strings, colors, themes
│   │   │   └── xml/                # backup rules
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Support

For issues or questions:
1. Check the main [README.md](README.md)
2. Review error logs in Android Studio Logcat
3. Open an issue on GitHub

---

Happy Driving! 🚗💨
