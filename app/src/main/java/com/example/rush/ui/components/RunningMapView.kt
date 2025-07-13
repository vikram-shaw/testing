package com.example.rush.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                )
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
            
            // Map controls overlay
            if (isLiveTracking) {
                MapControls(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onCenterClick = {
                        currentLocation?.let { location ->
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(location, 17f),
                                durationMs = 500
                            )
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
                        Text(
                            text = if (isLiveTracking) "Waiting for GPS..." else "No route data",
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