package com.example.rush.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

@Composable
fun FallbackMapView(
    modifier: Modifier = Modifier,
    route: List<LatLng>,
    currentLocation: LatLng? = null,
    isLiveTracking: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (route.isNotEmpty()) {
                // Draw simple route visualization
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawFallbackRoute(route, currentLocation, isLiveTracking)
                }
                
                // Overlay with route info
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "📍 Route Preview",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${route.size} points tracked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            if (isLiveTracking) {
                                Text(
                                    text = "🔴 Live tracking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // No route data
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🗺️",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Map Preview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLiveTracking) "Start running to see your route" else "No route data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // API Key warning
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Google Maps unavailable - Add valid API key for full map features",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFallbackRoute(
    route: List<LatLng>,
    currentLocation: LatLng?,
    isLiveTracking: Boolean
) {
    if (route.isEmpty()) return
    
    // Calculate bounds
    val minLat = route.minOf { it.latitude }
    val maxLat = route.maxOf { it.latitude }
    val minLng = route.minOf { it.longitude }
    val maxLng = route.maxOf { it.longitude }
    
    // Add some padding
    val latRange = maxLat - minLat
    val lngRange = maxLng - minLng
    val padding = 0.1f
    
    val bounds = Bounds(
        minLat - latRange * padding,
        maxLat + latRange * padding,
        minLng - lngRange * padding,
        maxLng + lngRange * padding
    )
    
    // Convert coordinates to screen positions
    val points = route.map { latLng ->
        Offset(
            x = ((latLng.longitude - bounds.minLng) / (bounds.maxLng - bounds.minLng) * size.width).toFloat(),
            y = ((bounds.maxLat - latLng.latitude) / (bounds.maxLat - bounds.minLat) * size.height).toFloat()
        )
    }
    
    // Draw background grid
    drawGrid()
    
    // Draw route path
    if (points.size > 1) {
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        
        drawPath(
            path = path,
            color = Color(0xFF2196F3),
            style = Stroke(width = 8.dp.toPx())
        )
    }
    
    // Draw start point
    if (points.isNotEmpty()) {
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 12.dp.toPx(),
            center = points.first()
        )
    }
    
    // Draw end point (if not live tracking)
    if (points.isNotEmpty() && !isLiveTracking) {
        drawCircle(
            color = Color(0xFFF44336),
            radius = 12.dp.toPx(),
            center = points.last()
        )
    }
    
    // Draw current location (if live tracking)
    if (isLiveTracking && currentLocation != null && points.isNotEmpty()) {
        drawCircle(
            color = Color(0xFFFF9800),
            radius = 16.dp.toPx(),
            center = points.last()
        )
        // Add pulsing effect
        drawCircle(
            color = Color(0xFFFF9800).copy(alpha = 0.3f),
            radius = 24.dp.toPx(),
            center = points.last()
        )
    }
}

private fun DrawScope.drawGrid() {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val gridSpacing = size.width / 10
    
    // Vertical lines
    for (i in 0..10) {
        val x = i * gridSpacing
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Horizontal lines
    for (i in 0..10) {
        val y = i * gridSpacing
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private data class Bounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)