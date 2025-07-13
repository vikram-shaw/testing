package com.example.rush.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rush.data.RunningSession
import com.example.rush.data.RunningStats
import com.example.rush.service.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RunningViewModel(context: Context) : ViewModel() {
    
    private val locationService = LocationService(context)
    
    private val _runningStats = MutableStateFlow(RunningStats())
    val runningStats: StateFlow<RunningStats> = _runningStats.asStateFlow()
    
    private val _currentSession = MutableStateFlow(RunningSession())
    val currentSession: StateFlow<RunningSession> = _currentSession.asStateFlow()
    
    private val _sessions = MutableStateFlow<List<RunningSession>>(emptyList())
    val sessions: StateFlow<List<RunningSession>> = _sessions.asStateFlow()
    
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
        
        _sessions.value = _sessions.value + completedSession
        
        _runningStats.value = RunningStats()
        _currentSession.value = RunningSession()
        
        stopLocationTracking()
        stopTimer()
        
        locations.clear()
        routePoints.clear()
    }
    
    private fun startLocationTracking() {
        locationJob = viewModelScope.launch {
            locationService.getLocationUpdates().collect { location ->
                locations.add(location)
                val latLng = LatLng(location.latitude, location.longitude)
                routePoints.add(latLng)
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
        
        _runningStats.value = _runningStats.value.copy(
            currentDistance = distance,
            currentDuration = duration,
            currentPace = pace,
            currentSpeed = speed,
            currentLocation = currentLocation,
            currentRoute = routePoints.toList()
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopTimer()
    }
}