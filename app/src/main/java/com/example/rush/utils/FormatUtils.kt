package com.example.rush.utils

import kotlin.math.roundToInt

object FormatUtils {
    
    fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun formatDistance(meters: Float): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000)
        } else {
            String.format("%.0f m", meters)
        }
    }
    
    fun formatPace(pace: Float): String {
        if (pace <= 0 || pace.isInfinite() || pace.isNaN()) return "--:--"
        
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).roundToInt()
        
        return String.format("%d:%02d", minutes, seconds)
    }
    
    fun formatSpeed(speedMps: Float): String {
        val speedKmh = speedMps * 3.6f
        return String.format("%.1f km/h", speedKmh)
    }
    
    fun formatCalories(calories: Int): String {
        return "$calories cal"
    }
}