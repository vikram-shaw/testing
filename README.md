# RUSH - Running Tracking App

A modern Android running tracking application built with Jetpack Compose that tracks your runs using GPS and provides detailed statistics.

## Features

### Core Functionality
- **Real-time GPS tracking** with live updates
- **Interactive map display** showing running route in real-time
- **Run controls**: Start, pause, resume, and stop functionality
- **Live statistics**: Distance, duration, pace, and speed
- **Run history**: View all your past running sessions with route maps
- **Detailed route view**: Full-screen map and comprehensive statistics
- **Automatic calculations**: Calories burned, average pace, max speed

### UI/UX Features
- **Modern Material Design 3** interface
- **Beautiful gradient backgrounds** and card-based layouts
- **Bottom navigation** between Run and History screens
- **Permission handling** for location access
- **Responsive design** with proper spacing and typography

## App Structure

### Main Components

1. **MainActivity** - Entry point with navigation setup
2. **RunningScreen** - Main tracking interface with live stats
3. **HistoryScreen** - Displays past running sessions
4. **RunningViewModel** - Manages app state and business logic
5. **LocationService** - Handles GPS tracking and calculations
6. **Data Models** - RunningSession and RunningStats

### Key Files

```
app/src/main/java/com/example/rush/
├── MainActivity.kt                    # Main activity with navigation
├── data/
│   └── RunningSession.kt             # Data models
├── service/
│   └── LocationService.kt            # GPS tracking service
├── viewmodel/
│   ├── RunningViewModel.kt           # State management
│   └── RunningViewModelFactory.kt    # ViewModel factory
├── ui/screens/
│   ├── RunningScreen.kt              # Main tracking screen
│   ├── HistoryScreen.kt              # History display
│   └── RouteDetailScreen.kt          # Detailed route view
├── ui/components/
│   └── RunningMapView.kt             # Map components
├── utils/
│   └── FormatUtils.kt                # Formatting utilities
└── ui/theme/                         # Material Design theme
```

## Technical Details

### Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Navigation Compose** - Screen navigation
- **ViewModel & LiveData** - State management
- **Location Services** - GPS tracking
- **Google Maps Compose** - Interactive map display
- **Material Design 3** - UI components
- **Accompanist Permissions** - Permission handling

### Permissions
- `ACCESS_FINE_LOCATION` - GPS tracking
- `ACCESS_COARSE_LOCATION` - Backup location
- `FOREGROUND_SERVICE` - Background tracking
- `FOREGROUND_SERVICE_LOCATION` - Location service

## Setup Instructions

### Prerequisites
1. Android Studio Arctic Fox or newer
2. Android SDK with minimum API level 30
3. Device or emulator with GPS/location services

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Set up Android SDK path in `local.properties`:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```
5. **Get a Google Maps API key**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable Maps SDK for Android
   - Create credentials (API Key)
   - Replace `YOUR_API_KEY_HERE` in `AndroidManifest.xml` with your actual API key
6. Build and run the app

### Usage
1. **Grant location permission** when prompted
2. **Start a run** by tapping the play button
3. **View live stats** including distance, time, pace, and speed
4. **Pause/Resume** as needed during your run
5. **Stop** the run to save it to history
6. **View history** in the History tab

## App Screens

### Running Screen
- Large time display as the main stat
- **Interactive map** showing real-time route tracking
- Distance, pace, speed, and status cards
- Start/pause/stop floating action buttons
- Permission request handling

### History Screen
- List of all completed runs
- **Compact map view** for each running session
- Date, time, and duration for each session
- Distance, pace, calories, and speed statistics
- Empty state when no runs exist

### Route Detail Screen
- **Full-screen map view** with complete route
- Detailed statistics and session information
- Start/end markers and route polyline
- Comprehensive running metrics

## Data Tracking

### Tracked Metrics
- **Distance**: Calculated from GPS coordinates
- **Duration**: Total time excluding pauses
- **Pace**: Minutes per kilometer
- **Speed**: Current and average speeds
- **Calories**: Estimated based on distance and duration
- **Location points**: GPS coordinates for route mapping

### Calculations
- Distance uses GPS coordinate differences
- Pace calculated as duration/distance
- Calories use MET (Metabolic Equivalent) values
- Speed derived from location updates

## Code Architecture

### MVVM Pattern
- **Model**: Data classes (RunningSession, RunningStats)
- **View**: Composable UI screens
- **ViewModel**: Business logic and state management

### State Management
- **StateFlow** for reactive UI updates
- **Coroutines** for background operations
- **Flow** for location updates

### UI Components
- **Cards** for displaying statistics
- **FABs** for primary actions
- **Bottom Navigation** for screen switching
- **Material Design 3** components throughout

## Future Enhancements

### Potential Features
- ✅ Route mapping with Google Maps (implemented)
- Running goals and achievements
- Social sharing capabilities
- Workout plans and training programs
- Heart rate monitoring integration
- Weather information display
- Audio coaching and feedback
- Export routes to GPX/KML formats
- Offline map support

### Technical Improvements
- Local database for offline storage
- Cloud sync for data backup
- Advanced analytics and insights
- Performance optimizations
- Battery usage improvements

## Contributing

Feel free to contribute to this project by:
1. Reporting bugs
2. Suggesting new features
3. Submitting pull requests
4. Improving documentation

## License

This project is open source and available under the MIT License.

---

**Important Notes**: 
- This app requires GPS/location services to function properly. Make sure to test on a physical device or emulator with location simulation enabled.
- **Google Maps API Key Required**: You need a valid Google Maps API key to use the map features. Replace `YOUR_API_KEY_HERE` in `AndroidManifest.xml` with your actual API key from Google Cloud Console.
- Map features will not work without a valid API key.

## Troubleshooting

### Map Not Showing At All
If the map area is blank or shows error messages:

1. **🔑 API Key Issues**:
   - Verify you've replaced `YOUR_API_KEY_HERE` with your actual API key
   - Check the API key has no restrictions or is restricted to your package name
   - Ensure the API key is valid and not expired

2. **📱 Enable Required APIs**:
   - Maps SDK for Android ✅
   - Places API (optional) ✅

3. **🚀 Fallback Map**:
   - App automatically shows a fallback map if Google Maps fails
   - Shows route visualization with basic graphics
   - Warns about missing API key

### "No route data" Message
If you see "No route data" on the map:

1. **📍 Location Setup**:
   - Grant location permission when prompted
   - Enable "Precise location" if available  
   - Check that location services are enabled in device settings

2. **🛰️ GPS Signal**:
   - Go outdoors for better GPS reception
   - Wait 10-30 seconds for GPS to initialize
   - Try restarting the app if location isn't working

### Built-in Debug Tools
The app includes helpful debugging features:

- **🔧 Map Debug Screen**: Test Google Maps setup and API key
- **📊 Status Indicators**: Shows what's working and what needs fixing
- **🗺️ Map Comparison**: Side-by-side Google Maps vs Fallback
- **📱 System Info**: Package name, permissions, and API key status

### Debugging Logs
Check Android Studio Logcat for these debug messages:
- `🚀 RunningMapView`: Map initialization and loading status
- `📍 LocationService`: GPS permission and location updates  
- `📊 RunningViewModel`: Route data processing
- `📱 RunningScreen`: Map data being passed to components

### Common Issues & Solutions

**🔧 Development Issues**:
- **Emulator**: Use extended controls → Location to simulate GPS
- **Indoor testing**: GPS may not work well indoors - try near a window
- **API quota**: Check if you've exceeded Google Maps API limits

**⚙️ Production Issues**:
- **Package restrictions**: API key restricted to wrong package name
- **Play Store**: Signed APK may use different package signature
- **Network**: Maps require internet connection to load tiles

**📱 Device Issues**:
- **Permissions**: Location permission denied or restricted
- **GPS disabled**: Location services turned off in device settings
- **Outdated Play Services**: Update Google Play Services app