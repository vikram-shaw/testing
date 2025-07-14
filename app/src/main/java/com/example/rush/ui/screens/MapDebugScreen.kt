package com.example.rush.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.example.rush.ui.components.RunningMapView
import com.example.rush.ui.components.FallbackMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDebugScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Debug information
    val debugInfo = remember { mutableStateOf(getDebugInfo(context)) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Map Debug",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Overview
            StatusCard(debugInfo.value)
            
            // Test Maps
            TestMapsSection()
            
            // Debug Information
            DebugInfoSection(debugInfo.value)
            
            // Instructions
            InstructionsSection()
            
            // Refresh button
            Button(
                onClick = { 
                    debugInfo.value = getDebugInfo(context)
                    Log.d("MapDebugScreen", "Debug info refreshed")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Debug Info")
            }
        }
    }
}

@Composable
private fun StatusCard(debugInfo: DebugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                debugInfo.hasApiKey && debugInfo.hasLocationPermission -> MaterialTheme.colorScheme.primaryContainer
                debugInfo.hasApiKey || debugInfo.hasLocationPermission -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        debugInfo.hasApiKey && debugInfo.hasLocationPermission -> Icons.Default.CheckCircle
                        debugInfo.hasApiKey || debugInfo.hasLocationPermission -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when {
                        debugInfo.hasApiKey && debugInfo.hasLocationPermission -> Color(0xFF4CAF50)
                        debugInfo.hasApiKey || debugInfo.hasLocationPermission -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Setup Status",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    debugInfo.hasApiKey && debugInfo.hasLocationPermission -> "✅ Ready to use Google Maps"
                    debugInfo.hasApiKey && !debugInfo.hasLocationPermission -> "⚠️ API key found, but location permission needed"
                    !debugInfo.hasApiKey && debugInfo.hasLocationPermission -> "⚠️ Location permission granted, but API key needed"
                    else -> "❌ API key and location permission required"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TestMapsSection() {
    Text(
        text = "Map Tests",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
    )
    
    // Sample route for testing
    val sampleRoute = listOf(
        LatLng(37.7749, -122.4194),
        LatLng(37.7849, -122.4094),
        LatLng(37.7949, -122.3994)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Google Maps (if working):",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        
        RunningMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            route = sampleRoute,
            currentLocation = sampleRoute.last(),
            isLiveTracking = false,
            showUserLocation = false
        )
        
        Text(
            text = "Fallback Map (always works):",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        
        FallbackMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            route = sampleRoute,
            currentLocation = sampleRoute.last(),
            isLiveTracking = false
        )
    }
}

@Composable
private fun DebugInfoSection(debugInfo: DebugInfo) {
    Text(
        text = "Debug Information",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
    )
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DebugItem("API Key Found", debugInfo.hasApiKey)
            DebugItem("Location Permission", debugInfo.hasLocationPermission)
            DebugItem("Google Play Services", debugInfo.hasGooglePlayServices)
            DebugItem("Package Name", debugInfo.packageName, isInfo = true)
            DebugItem("API Key (partial)", debugInfo.apiKeyPreview, isInfo = true)
        }
    }
}

@Composable
private fun DebugItem(
    label: String,
    value: Any,
    isInfo: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (isInfo) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (value as Boolean) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (value) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (value) "Yes" else "No",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (value) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun InstructionsSection() {
    Text(
        text = "Setup Instructions",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
    )
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InstructionStep(
                number = "1",
                title = "Get Google Maps API Key",
                description = "Go to Google Cloud Console → APIs & Services → Credentials"
            )
            
            InstructionStep(
                number = "2",
                title = "Enable Maps SDK",
                description = "Enable 'Maps SDK for Android' in your Google Cloud project"
            )
            
            InstructionStep(
                number = "3",
                title = "Update AndroidManifest.xml",
                description = "Replace YOUR_API_KEY_HERE with your actual API key"
            )
            
            InstructionStep(
                number = "4",
                title = "Grant Location Permission",
                description = "Allow location access when prompted by the app"
            )
            
            InstructionStep(
                number = "5",
                title = "Test Outdoors",
                description = "Go outside for better GPS signal reception"
            )
        }
    }
}

@Composable
private fun InstructionStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getDebugInfo(context: Context): DebugInfo {
    val packageManager = context.packageManager
    val packageName = context.packageName
    
    // Check for API key in manifest
    var apiKey = ""
    var hasApiKey = false
    
    try {
        val appInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        hasApiKey = apiKey.isNotEmpty() && apiKey != "YOUR_API_KEY_HERE"
    } catch (e: Exception) {
        Log.e("MapDebugScreen", "Error reading API key: ${e.message}")
    }
    
    // Check location permission
    val hasLocationPermission = packageManager.checkPermission(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        packageName
    ) == PackageManager.PERMISSION_GRANTED
    
    // Check Google Play Services (simplified)
    val hasGooglePlayServices = try {
        packageManager.getApplicationInfo("com.google.android.gms", 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
    
    return DebugInfo(
        hasApiKey = hasApiKey,
        hasLocationPermission = hasLocationPermission,
        hasGooglePlayServices = hasGooglePlayServices,
        packageName = packageName,
        apiKeyPreview = if (apiKey.length > 10) "${apiKey.take(10)}..." else apiKey
    )
}

private data class DebugInfo(
    val hasApiKey: Boolean,
    val hasLocationPermission: Boolean,
    val hasGooglePlayServices: Boolean,
    val packageName: String,
    val apiKeyPreview: String
)