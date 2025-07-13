package com.example.rush.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun RunningMapView(
    modifier: Modifier = Modifier,
    route: List<LatLng>,
    currentLocation: LatLng? = null,
    isLiveTracking: Boolean = false,
    showUserLocation: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapLoaded by remember { mutableStateOf(false) }
    var showFallback by remember { mutableStateOf(false) }
    
    // Log what data we're receiving
    LaunchedEffect(route.size, currentLocation, isLiveTracking) {
        Log.d("RunningMapView", "Map data - Route size: ${route.size}, Current location: $currentLocation, Live tracking: $isLiveTracking")
    }
    
    // Check for Google Maps availability after a timeout
    LaunchedEffect(Unit) {
        Log.d("🗺️ MAP_INIT", "🚀 Initializing Google Maps...")
        Log.d("🗺️ MAP_INIT", "📍 Route points: ${route.size}")
        Log.d("🗺️ MAP_INIT", "📍 Current location: $currentLocation")
        delay(5000) // 5 second timeout
        if (!mapLoaded) {
            Log.w("🗺️ MAP_TIMEOUT", "⏰ Map loading timeout - switching to fallback")
            Log.w("🗺️ MAP_TIMEOUT", "💡 This usually means:")
            Log.w("🗺️ MAP_TIMEOUT", "   1. API key restrictions are blocking the app")
            Log.w("🗺️ MAP_TIMEOUT", "   2. Maps SDK for Android is not enabled")
            Log.w("🗺️ MAP_TIMEOUT", "   3. Billing is not enabled in Google Cloud")
            Log.w("🗺️ MAP_TIMEOUT", "   4. Network connectivity issues")
            showFallback = true
        }
    }
    
    // Default camera position (fallback)
    val defaultPosition = LatLng(37.7749, -122.4194) // San Francisco
    
    // Set initial camera position
    val initialPosition = currentLocation ?: route.firstOrNull() ?: defaultPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }
    
    // Update camera position when live tracking
    LaunchedEffect(currentLocation, isLiveTracking) {
        if (isLiveTracking && currentLocation != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation, 17f),
                durationMs = 1000
            )
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (showFallback) {
                // Show fallback map view
                Log.d("🗺️ MAP_FALLBACK", "📱 Showing fallback map view")
                Log.d("🗺️ MAP_FALLBACK", "🔧 Check Android Studio Logcat for Google Maps errors")
                Log.d("🗺️ MAP_FALLBACK", "🔧 Look for MAP_DEBUG logs to verify API key setup")
                FallbackMapView(
                    modifier = Modifier.fillMaxSize(),
                    route = route,
                    currentLocation = currentLocation,
                    isLiveTracking = isLiveTracking
                )
            } else {
                // Try to show Google Map
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = showUserLocation && currentLocation != null,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true,
                        myLocationButtonEnabled = false
                    ),
                    onMapLoaded = {
                        Log.d("🗺️ MAP_SUCCESS", "✅ Google Map loaded successfully!")
                        Log.d("🗺️ MAP_SUCCESS", "✅ API key is working correctly")
                        mapLoaded = true
                        showFallback = false // Cancel fallback since map loaded
                    }
                ) {
                    // Draw the route as a polyline
                    if (route.isNotEmpty()) {
                        Polyline(
                            points = route,
                            color = MaterialTheme.colorScheme.primary,
                            width = 8f
                        )
                    }
                    
                    // Add markers for start and end points
                    if (route.isNotEmpty()) {
                        // Start marker
                        Marker(
                            state = MarkerState(position = route.first()),
                            title = "Start",
                            snippet = "Route started here"
                        )
                        
                        // End marker (only if not live tracking)
                        if (!isLiveTracking && route.size > 1) {
                            Marker(
                                state = MarkerState(position = route.last()),
                                title = "End",
                                snippet = "Route ended here"
                            )
                        }
                    }
                    
                    // Current location marker for live tracking
                    if (isLiveTracking && currentLocation != null) {
                        Marker(
                            state = MarkerState(position = currentLocation),
                            title = "Current Location"
                        )
                    }
                }
            }
            
            // Loading indicator
            if (!mapLoaded && !showFallback) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading map...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Map controls overlay (only show for Google Maps, not fallback)
            if (isLiveTracking && mapLoaded && !showFallback) {
                MapControls(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onCenterClick = {
                        currentLocation?.let { location ->
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(location, 17f),
                                    durationMs = 500
                                )
                            }
                        }
                    }
                )
            }
            
            // Show message if no route data
            if (route.isEmpty() && currentLocation == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isLiveTracking) "🛰️ Waiting for GPS..." else "📍 No route data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isLiveTracking) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Make sure location is enabled and you're outdoors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            } else if (route.isEmpty() && currentLocation != null) {
                // Show current location only
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "📍 Location found! Start running to track your route",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapControls(
    modifier: Modifier = Modifier,
    onCenterClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FloatingActionButton(
            onClick = onCenterClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun CompactMapView(
    modifier: Modifier = Modifier,
    route: List<LatLng>,
    height: Int = 200
) {
    RunningMapView(
        modifier = modifier.height(height.dp),
        route = route,
        currentLocation = null,
        isLiveTracking = false,
        showUserLocation = false
    )
}