package com.example.rush.service

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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
                Log.d("LocationService", "Location received: ${location.latitude}, ${location.longitude}")
                trySend(location)
            }
            
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d("LocationService", "Status changed: $provider, status: $status")
            }
            
            override fun onProviderEnabled(provider: String) {
                Log.d("LocationService", "Provider enabled: $provider")
            }
            
            override fun onProviderDisabled(provider: String) {
                Log.d("LocationService", "Provider disabled: $provider")
            }
        }
        
        // Check permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            Log.d("LocationService", "Location permission granted, requesting updates")
            
            // Check if GPS provider is available
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            
            Log.d("LocationService", "GPS enabled: $isGpsEnabled, Network enabled: $isNetworkEnabled")
            
            // Try to get last known location first
            try {
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                
                lastKnownLocation?.let {
                    Log.d("LocationService", "Sending last known location: ${it.latitude}, ${it.longitude}")
                    trySend(it)
                }
            } catch (e: SecurityException) {
                Log.e("LocationService", "SecurityException getting last known location: ${e.message}")
            }
            
            // Request location updates from both GPS and Network providers
            try {
                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L, // minimum time interval between updates (1 second)
                        1f, // minimum distance between updates (1 meter)
                        locationListener
                    )
                    Log.d("LocationService", "GPS location updates requested")
                }
                
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        1f,
                        locationListener
                    )
                    Log.d("LocationService", "Network location updates requested")
                }
            } catch (e: SecurityException) {
                Log.e("LocationService", "SecurityException requesting location updates: ${e.message}")
            }
        } else {
            Log.e("LocationService", "Location permission not granted")
        }
        
        awaitClose {
            Log.d("LocationService", "Removing location updates")
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