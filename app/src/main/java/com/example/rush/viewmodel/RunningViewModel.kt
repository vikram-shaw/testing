package com.example.rush.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rush.data.RunningSession
import com.example.rush.data.RunningStats
import com.example.rush.repository.RunningRepository
import com.example.rush.service.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.UUID

class RunningViewModel(context: Context) : ViewModel() {
    
    private val locationService = LocationService(context)
    private val repository = RunningRepository(context)
    
    private val _runningStats = MutableStateFlow(RunningStats())
    val runningStats: StateFlow<RunningStats> = _runningStats.asStateFlow()
    
    private val _currentSession = MutableStateFlow(RunningSession())
    val currentSession: StateFlow<RunningSession> = _currentSession.asStateFlow()
    
    // Use repository for sessions
    val sessions: StateFlow<List<RunningSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private var locationJob: Job? = null
    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var totalPausedDuration: Long = 0
    private val locations = mutableListOf<Location>()
    private val routePoints = mutableListOf<LatLng>()
    
    fun startRun() {
        startTime = System.currentTimeMillis()
        totalPausedDuration = 0
        locations.clear()
        routePoints.clear()
        
        _runningStats.value = _runningStats.value.copy(
            isRunning = true,
            isPaused = false
        )
        
        _currentSession.value = RunningSession(
            id = UUID.randomUUID().toString(),
            startTime = startTime
        )
        
        startLocationTracking()
        startTimer()
    }
    
    fun pauseRun() {
        if (_runningStats.value.isRunning && !_runningStats.value.isPaused) {
            pausedTime = System.currentTimeMillis()
            _runningStats.value = _runningStats.value.copy(isPaused = true)
            stopLocationTracking()
            stopTimer()
        }
    }
    
    fun resumeRun() {
        if (_runningStats.value.isRunning && _runningStats.value.isPaused) {
            totalPausedDuration += System.currentTimeMillis() - pausedTime
            _runningStats.value = _runningStats.value.copy(isPaused = false)
            startLocationTracking()
            startTimer()
        }
    }
    
    fun stopRun() {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime - totalPausedDuration
        val distance = locationService.calculateDistance(locations)
        val avgPace = locationService.calculatePace(distance, duration)
        val calories = locationService.calculateCalories(distance, duration)
        val maxSpeed = locations.maxOfOrNull { it.speed } ?: 0f
        
        val completedSession = _currentSession.value.copy(
            endTime = endTime,
            distance = distance,
            duration = duration,
            avgPace = avgPace,
            maxSpeed = maxSpeed,
            calories = calories,
            locations = locations.toList(),
            route = routePoints.toList()
        )
        
        // Save to database
        viewModelScope.launch {
            try {
                repository.insertSession(completedSession)
                Log.d("RunningViewModel", "✅ Session saved to database: ${completedSession.id}")
            } catch (e: Exception) {
                Log.e("RunningViewModel", "❌ Failed to save session: ${e.message}", e)
            }
        }
        
        _runningStats.value = RunningStats()
        _currentSession.value = RunningSession()
        
        stopLocationTracking()
        stopTimer()
        
        locations.clear()
        routePoints.clear()
    }
    
    private fun startLocationTracking() {
        Log.d("RunningViewModel", "Starting location tracking")
        locationJob = viewModelScope.launch {
            locationService.getLocationUpdates().collect { location ->
                Log.d("RunningViewModel", "Received location: ${location.latitude}, ${location.longitude}")
                locations.add(location)
                val latLng = LatLng(location.latitude, location.longitude)
                routePoints.add(latLng)
                Log.d("RunningViewModel", "Route points count: ${routePoints.size}")
                updateRunningStats()
            }
        }
    }
    
    private fun stopLocationTracking() {
        locationJob?.cancel()
        locationJob = null
    }
    
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_runningStats.value.isRunning) {
                delay(1000)
                if (!_runningStats.value.isPaused) {
                    updateRunningStats()
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    private fun updateRunningStats() {
        val currentTime = System.currentTimeMillis()
        val duration = currentTime - startTime - totalPausedDuration
        val distance = locationService.calculateDistance(locations)
        val pace = locationService.calculatePace(distance, duration)
        val speed = if (locations.isNotEmpty()) locations.last().speed else 0f
        val currentLocation = if (locations.isNotEmpty()) {
            LatLng(locations.last().latitude, locations.last().longitude)
        } else null
        
        Log.d("RunningViewModel", "Updating stats - Route points: ${routePoints.size}, Distance: $distance, Current location: $currentLocation")
        
        _runningStats.value = _runningStats.value.copy(
            currentDistance = distance,
            currentDuration = duration,
            currentPace = pace,
            currentSpeed = speed,
            currentLocation = currentLocation,
            currentRoute = routePoints.toList()
        )
    }
    
    // Repository functions for UI access
    fun getRecentSessions(limit: Int = 10): StateFlow<List<RunningSession>> {
        return repository.getRecentSessions(limit)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    fun getThisWeekSessions(): StateFlow<List<RunningSession>> {
        return repository.getThisWeekSessions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    fun getThisMonthSessions(): StateFlow<List<RunningSession>> {
        return repository.getThisMonthSessions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
    
    val statistics = repository.getStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    suspend fun deleteSession(sessionId: String) {
        try {
            repository.deleteSession(sessionId)
            Log.d("RunningViewModel", "✅ Session deleted: $sessionId")
        } catch (e: Exception) {
            Log.e("RunningViewModel", "❌ Failed to delete session: ${e.message}", e)
        }
    }
    
    suspend fun clearAllData() {
        try {
            repository.deleteAllSessions()
            Log.d("RunningViewModel", "✅ All data cleared")
        } catch (e: Exception) {
            Log.e("RunningViewModel", "❌ Failed to clear data: ${e.message}", e)
        }
    }
    
    suspend fun cleanOldSessions(daysToKeep: Int = 365) {
        try {
            repository.deleteOldSessions(daysToKeep)
            Log.d("RunningViewModel", "✅ Old sessions cleaned up")
        } catch (e: Exception) {
            Log.e("RunningViewModel", "❌ Failed to clean old sessions: ${e.message}", e)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopTimer()
    }
}