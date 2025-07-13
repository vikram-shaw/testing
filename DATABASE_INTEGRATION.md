# 🗄️ Room Database Integration

The running tracking app now includes comprehensive local data storage using Android Room database.

## 📊 Database Structure

### Entities

#### RunningSessionEntity
```kotlin
@Entity(tableName = "running_sessions")
data class RunningSessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val distance: Float,
    val duration: Long,
    val avgPace: Float,
    val maxSpeed: Float,
    val calories: Int,
    val route: List<LatLng>, // JSON serialized
    val notes: String = "",
    val weather: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### RunningStatisticsEntity
```kotlin
@Entity(tableName = "running_statistics")
data class RunningStatisticsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalRuns: Int,
    val totalDistance: Float,
    val totalDuration: Long,
    val totalCalories: Int,
    val averagePace: Float,
    val bestPace: Float,
    val longestRun: Float,
    val longestDuration: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

## 🔧 Architecture

### Repository Pattern
```
UI Layer (Composables)
    ↓
ViewModel Layer
    ↓
Repository Layer (RunningRepository)
    ↓
DAO Layer (Room DAOs)
    ↓
Database Layer (SQLite)
```

### Key Components

1. **RunningDatabase** - Main Room database class
2. **RunningRepository** - Data access abstraction layer
3. **DAOs** - Data Access Objects for database operations
4. **TypeConverters** - Handle complex data type serialization

## 📱 Features Implemented

### Data Persistence
- ✅ **Running sessions** automatically saved to local database
- ✅ **Route coordinates** stored as JSON (List<LatLng>)
- ✅ **Statistics** automatically calculated and updated
- ✅ **Offline access** - no internet required

### Database Operations
- ✅ **Insert** new running sessions
- ✅ **Update** existing sessions
- ✅ **Delete** individual sessions
- ✅ **Query** sessions by date ranges
- ✅ **Calculate** aggregate statistics
- ✅ **Clean up** old data

### UI Integration
- ✅ **Real-time updates** using Flow and StateFlow
- ✅ **Statistics screen** with comprehensive analytics
- ✅ **History screen** with persistent data
- ✅ **Weekly/Monthly** summaries

## 🚀 Usage Examples

### Save a Running Session
```kotlin
// Automatically called when you stop a run
viewModel.stopRun() // Saves to database
```

### Access Running History
```kotlin
// In your Composable
val sessions by viewModel.sessions.collectAsStateWithLifecycle()
val recentSessions by viewModel.getRecentSessions(10).collectAsStateWithLifecycle()
val thisWeekSessions by viewModel.getThisWeekSessions().collectAsStateWithLifecycle()
```

### View Statistics
```kotlin
// Access comprehensive statistics
val statistics by viewModel.statistics.collectAsStateWithLifecycle()

// Statistics include:
// - Total runs, distance, duration, calories
// - Average and best pace
// - Longest run and duration
// - Automatically updated
```

### Database Management
```kotlin
// Clear all data
viewModel.clearAllData()

// Clean old sessions (keep 1 year)
viewModel.cleanOldSessions(365)

// Delete specific session
viewModel.deleteSession(sessionId)
```

## 🔍 Database Queries Available

### Session Queries
- `getAllSessions()` - All sessions ordered by date
- `getRecentSessions(limit)` - Last N sessions
- `getThisWeekSessions()` - Current week's sessions
- `getThisMonthSessions()` - Current month's sessions
- `getSessionsInDateRange(start, end)` - Custom date range

### Analytics Queries
- `getTotalDistance()` - Sum of all distances
- `getTotalDuration()` - Sum of all durations
- `getAveragePace()` - Average pace across all runs
- `getBestPace()` - Fastest pace achieved
- `getLongestRun()` - Session with longest distance
- `getFastestRun()` - Session with best pace

## 🛠️ Technical Details

### TypeConverters
Custom converters handle complex data types:

```kotlin
@TypeConverter
fun fromLatLngList(value: List<LatLng>): String {
    return gson.toJson(value)
}

@TypeConverter
fun toLatLngList(value: String): List<LatLng> {
    return gson.fromJson(value, listType) ?: emptyList()
}
```

### Database Initialization
```kotlin
// Singleton pattern ensures single database instance
val database = RunningDatabase.getDatabase(context)
```

### Error Handling
- ✅ **Graceful failures** with try-catch blocks
- ✅ **Logging** for debugging database operations
- ✅ **Fallback** to in-memory data if database fails

## 📈 Statistics Auto-Calculation

The app automatically maintains running statistics:

1. **Insert Session** → Update Statistics
2. **Delete Session** → Recalculate Statistics
3. **Statistics Always Current** → Real-time updates

## 🔄 Data Flow

```
1. User completes a run
2. RunningViewModel.stopRun() called
3. Session data collected and formatted
4. Repository.insertSession() saves to database
5. Statistics automatically updated
6. UI reactively updates via StateFlow
7. All screens show latest data immediately
```

## 🧪 Testing & Debugging

### Database Logging
Check Android Studio Logcat for:
```
RunningRepository: ✅ Session saved: [session-id]
RunningRepository: ✅ Statistics updated
RunningRepository: ❌ Error saving session: [error]
```

### Database Inspector
- Use Android Studio's Database Inspector
- View real-time database contents
- Debug data structure and content

### Clean Database
For testing, clear all data:
```kotlin
viewModel.clearAllData()
```

## 📱 User Experience

### Seamless Integration
- **No user action required** - sessions save automatically
- **Instant access** - history and stats load immediately
- **Offline capable** - works without internet
- **Data safety** - persists across app restarts and device reboots

### Performance
- **Lazy loading** using Room's Flow integration
- **Efficient queries** with proper indexing
- **Background operations** don't block UI
- **Memory efficient** with proper StateFlow scoping

---

**Result**: Users now have a complete running history with detailed analytics, all stored locally and accessible instantly! 🎉