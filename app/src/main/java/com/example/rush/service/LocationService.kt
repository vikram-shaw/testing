package com.example.rush.service

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.Manifest

class LocationService(private val context: Context) {
    
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location)
            }
            
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        
        // Check permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // minimum time interval between updates (1 second)
                1f, // minimum distance between updates (1 meter)
                locationListener
            )
        }
        
        awaitClose {
            locationManager.removeUpdates(locationListener)
        }
    }
    
    fun calculateDistance(locations: List<Location>): Float {
        if (locations.size < 2) return 0f
        
        var totalDistance = 0f
        for (i in 1 until locations.size) {
            totalDistance += locations[i - 1].distanceTo(locations[i])
        }
        return totalDistance
    }
    
    fun calculatePace(distance: Float, duration: Long): Float {
        if (distance <= 0f || duration <= 0) return 0f
        val distanceInKm = distance / 1000f
        val durationInMinutes = duration / 60000f
        return durationInMinutes / distanceInKm
    }
    
    fun calculateCalories(distance: Float, duration: Long, weightKg: Float = 70f): Int {
        // Simplified calorie calculation (METs based)
        val durationInHours = duration / 3600000f
        val met = 8.0f // Running MET value (moderate intensity)
        return (met * weightKg * durationInHours).toInt()
    }
}