package com.example.rush.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.rush.database.converters.LocationConverters
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "running_sessions")
@TypeConverters(LocationConverters::class)
data class RunningSessionEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val distance: Float, // in meters
    val duration: Long, // in milliseconds
    val avgPace: Float, // in minutes per kilometer
    val maxSpeed: Float, // in m/s
    val calories: Int,
    val route: List<LatLng> = emptyList(), // Will be converted by TypeConverter
    val notes: String = "", // Optional notes about the run
    val weather: String = "", // Optional weather conditions
    val createdAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean get() = endTime == 0L && startTime > 0
    val averageSpeed: Float get() = if (duration > 0) distance / (duration / 1000f) else 0f
    val distanceInKm: Float get() = distance / 1000f
    val durationInSeconds: Long get() = duration / 1000
}

@Entity(tableName = "running_statistics")
data class RunningStatisticsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalRuns: Int = 0,
    val totalDistance: Float = 0f, // in meters
    val totalDuration: Long = 0L, // in milliseconds
    val totalCalories: Int = 0,
    val averagePace: Float = 0f, // in minutes per kilometer
    val bestPace: Float = 0f, // fastest pace achieved
    val longestRun: Float = 0f, // in meters
    val longestDuration: Long = 0L, // in milliseconds
    val lastUpdated: Long = System.currentTimeMillis()
)