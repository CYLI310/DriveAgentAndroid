# DriveAgent Android

DriveAgent Android is a powerful and intuitive driving companion app built with Kotlin and Jetpack Compose. It provides real-time speed and location tracking, speed camera detection, and multi-sensory alerts to enhance your driving experience.

## Features

### Core Features
- ✅ **Real-Time Speed and Location Tracking** - Accurate GPS-based speed monitoring with street name display
- ✅ **Speed Camera Detection** - Intelligent detection of nearby speed cameras using GeoJSON data
- ✅ **Multi-Sensory Alerts** - Visual, audio, and haptic feedback when exceeding speed limits
  - Visual: Red blinking particle effects and glowing border
  - Audio: Navigation Pop alert sound with adaptive intervals (4s normal, 2s severe)
  - Haptic: Gentle warning vibrations synchronized with audio
- ✅ **Interactive Map View** - Visualize your current location (Google Maps integration ready)
- ✅ **Customizable Particle Effects** - Choose from Orbit, Gradient, or turn them off entirely
- ✅ **Trip Statistics** - Track distance traveled and maximum speed
- ✅ **Multi-Language Support** - English, Chinese (Traditional & Simplified), Spanish, and more
- ✅ **Theme Customization** - System, Light, or Dark themes
- ✅ **Glassmorphism UI** - Modern Material 3 design with beautiful glass effects

### Advanced Features
- **Infinite Proximity Mode** - Always show the closest camera regardless of distance
- **Smart Camera Matching** - Intelligent road name and direction matching for accurate alerts
- **Configurable Alert Distance** - Set custom distance (100m - 2000m) for speed camera warnings
- **Metric/Imperial Units** - Switch between km/h and mph
- **Background Location Tracking** - Continue tracking even when app is in background (requires permission)

## Technology Stack

### Core Technologies
- **Kotlin** - Modern, concise, and safe programming language
- **Jetpack Compose** - Declarative UI framework (Android's equivalent to SwiftUI)
- **Material 3** - Latest Material Design components and theming
- **Coroutines & Flow** - Asynchronous programming and reactive data streams

### Key Libraries
- **Google Play Services Location** - High-accuracy GPS tracking
- **Google Maps Compose** - Interactive map integration
- **Gson** - JSON parsing for GeoJSON speed trap data
- **DataStore** - Modern data storage solution
- **Accompanist Permissions** - Simplified permission handling

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
app/
├── model/              # Data models and enums
├── manager/            # Business logic managers
│   ├── LocationManager.kt
│   ├── SpeedTrapDetector.kt
│   ├── AlertFeedbackManager.kt
│   ├── PreferencesManager.kt
│   └── LanguageManager.kt
├── viewmodel/          # ViewModels coordinating app state
│   └── DriveAgentViewModel.kt
├── ui/
│   ├── screens/        # Composable screens
│   │   ├── MainContentView.kt
│   │   ├── SettingsView.kt
│   │   └── SpeedTrapListView.kt
│   ├── components/     # Reusable UI components
│   │   └── ParticleEffectView.kt
│   └── theme/          # Material 3 theming
└── MainActivity.kt     # Entry point
```

## Getting Started

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or later
- **Android SDK** 26 (Android 8.0) or higher
- **Google Maps API Key** (for map functionality)

### Installation

1. **Clone the repository:**
   ```bash
   cd /Users/justinli/Documents/Android
   # The project is already in DriveAgentAndroid/
   ```

2. **Add Google Maps API Key:**
   Create a `local.properties` file in the root directory and add:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key_here
   ```
   
   Or create `secrets.properties`:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key_here
   ```

3. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to `DriveAgentAndroid` folder
   - Wait for Gradle sync to complete

4. **Build and Run:**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift+F10`

### Required Permissions

The app requires the following permissions:
- **Location (Fine & Coarse)** - For GPS tracking
- **Background Location** - For tracking while app is in background (optional)
- **Vibrate** - For haptic feedback
- **Internet** - For map tiles and geocoding

## Key Differences from iOS Version

### What's Implemented
✅ All core features from iOS version
✅ Speed trap detection with GeoJSON data
✅ Multi-sensory alerts (visual, audio, haptic)
✅ Particle effects (Orbit & Gradient)
✅ Settings with all customization options
✅ Trip statistics
✅ Multi-language support
✅ Theme customization
✅ Glassmorphism UI design

### Platform-Specific Adaptations
- **UI Framework**: SwiftUI → Jetpack Compose
- **Navigation**: NavigationStack → Compose Navigation (simplified for this version)
- **Permissions**: iOS permission system → Android runtime permissions with Accompanist
- **Storage**: UserDefaults → DataStore Preferences
- **Location Services**: CoreLocation → Google Play Services Location
- **Maps**: MapKit → Google Maps Compose
- **Haptics**: UIKit haptics → Android Vibrator API

### Not Yet Implemented
- ⏳ **Dynamic Island / Live Activities** - Android doesn't have an exact equivalent, but could use:
  - Ongoing notification with live updates
  - Picture-in-Picture mode for speed display
- ⏳ **Distraction Detection** - Would require ML Kit Face Detection
- ⏳ **Widgets** - Android Home Screen widgets (Glance API)
- ⏳ **Full Map Implementation** - Google Maps Compose integration is ready but needs API key

## Regarding "Swift for Android"

You mentioned considering "Swift for Android SDK". Here are the options:

### 1. **Skip (skip.tools)** ✨ Recommended for true Swift code sharing
- Transpiles Swift/SwiftUI to Kotlin/Compose
- Allows sharing actual Swift code between iOS and Android
- Still in beta but promising

### 2. **SCADE** 
- Commercial solution for Swift on Android
- Requires licensing

### 3. **Native Kotlin (Current Implementation)** ✅ Best for production
- **Pros**: 
  - Full Android ecosystem support
  - Better performance
  - Easier to maintain and debug
  - Access to all Android APIs
  - Larger developer community
- **Cons**: 
  - Separate codebase from iOS

**This implementation uses native Kotlin/Compose**, which is the recommended approach for production Android apps. It provides the best performance, maintainability, and access to Android-specific features.

## Usage

### First Launch
1. Grant location permissions when prompted
2. Optionally complete the onboarding tutorial
3. The app will start tracking your speed and location

### Main Screen
- **Large Speed Display** - Shows current speed in real-time
- **Particle Effects** - Visual feedback based on acceleration state
- **Speed Camera Warning** - Appears when approaching a camera
- **Bottom Buttons**:
  - Left: View nearby speed cameras list
  - Center: Toggle map view
  - Right: Open settings

### Settings
- **Language** - Choose from 16 supported languages
- **Theme** - System, Light, or Dark mode
- **Particle Effects** - Orbit, Gradient, or Off
- **Units** - Metric (km/h) or Imperial (mph)
- **Trip Stats** - View and reset trip distance/max speed
- **Speed Camera Alerts**:
  - Infinite Proximity toggle
  - Alert distance slider (100m - 2000m)

## Data Source

The app uses the same `speedtraps.geojson` file from the iOS version, containing speed camera locations in Taiwan. The data includes:
- Camera coordinates (latitude/longitude)
- Speed limits
- Installation addresses
- Camera directions

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original iOS version: [DriveAgentiOS](https://github.com/CYLI310/DriveAgentiOS)
- Speed trap data source: Taiwan government open data
- Icons: Material Icons
- Design inspiration: iOS DriveAgent app

## Future Enhancements

- [ ] Implement full Google Maps integration
- [ ] Add home screen widgets
- [ ] Implement foreground service for background tracking
- [ ] Add ML Kit face detection for distraction alerts
- [ ] Add trip history and statistics
- [ ] Export trip data
- [ ] Add more languages
- [ ] Implement picture-in-picture mode
- [ ] Add Android Auto support

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

**Built with ❤️ using Kotlin and Jetpack Compose**
