package com.example.rush.data

import android.location.Location

data class RunningSession(
    val id: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val distance: Float = 0f, // in meters
    val duration: Long = 0, // in milliseconds
    val avgPace: Float = 0f, // in minutes per kilometer
    val maxSpeed: Float = 0f, // in m/s
    val calories: Int = 0,
    val locations: List<Location> = emptyList()
) {
    val isActive: Boolean get() = endTime == 0L && startTime > 0
    val averageSpeed: Float get() = if (duration > 0) distance / (duration / 1000f) else 0f
    val distanceInKm: Float get() = distance / 1000f
    val durationInSeconds: Long get() = duration / 1000
}

data class RunningStats(
    val currentDistance: Float = 0f,
    val currentDuration: Long = 0,
    val currentPace: Float = 0f,
    val currentSpeed: Float = 0f,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)