package com.example.rush.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rush.viewmodel.RunningViewModel
import com.example.rush.utils.FormatUtils
import com.example.rush.ui.components.RunningMapView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RunningScreen(
    viewModel: RunningViewModel
) {
    val context = LocalContext.current
    val runningStats by viewModel.runningStats.collectAsStateWithLifecycle()
    
    // Location permission handling
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (locationPermissionState.status.isGranted) {
            RunningContent(
                runningStats = runningStats,
                onStartClick = { viewModel.startRun() },
                onPauseClick = { viewModel.pauseRun() },
                onResumeClick = { viewModel.resumeRun() },
                onStopClick = { viewModel.stopRun() }
            )
        } else {
            PermissionContent(
                shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                onRequestPermission = { locationPermissionState.launchPermissionRequest() }
            )
        }
    }
}

@Composable
private fun RunningContent(
    runningStats: com.example.rush.data.RunningStats,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // App Title
        Text(
            text = "RUSH",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Map View
        RunningMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            route = runningStats.currentRoute,
            currentLocation = runningStats.currentLocation,
            isLiveTracking = runningStats.isRunning,
            showUserLocation = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Cards
        StatsSection(runningStats)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Control Buttons
        ControlButtons(
            isRunning = runningStats.isRunning,
            isPaused = runningStats.isPaused,
            onStartClick = onStartClick,
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick,
            onStopClick = onStopClick
        )
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatsSection(runningStats: com.example.rush.data.RunningStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time - Main stat
        StatCard(
            title = "TIME",
            value = FormatUtils.formatTime(runningStats.currentDuration),
            isMainStat = true
        )
        
        // Secondary stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "DISTANCE",
                value = FormatUtils.formatDistance(runningStats.currentDistance),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "PACE",
                value = FormatUtils.formatPace(runningStats.currentPace),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "SPEED",
                value = FormatUtils.formatSpeed(runningStats.currentSpeed),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "STATUS",
                value = when {
                    runningStats.isPaused -> "PAUSED"
                    runningStats.isRunning -> "RUNNING"
                    else -> "READY"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isMainStat: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isMainStat) Modifier.height(120.dp) else Modifier.height(80.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = if (isMainStat) 
                    MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                else 
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop button (only shown when running)
        if (isRunning) {
            FloatingActionButton(
                onClick = onStopClick,
                modifier = Modifier.size(60.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Main action button
        FloatingActionButton(
            onClick = {
                when {
                    !isRunning -> onStartClick()
                    isPaused -> onResumeClick()
                    else -> onPauseClick()
                }
            },
            modifier = Modifier.size(80.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = when {
                    !isRunning -> Icons.Default.PlayArrow
                    isPaused -> Icons.Default.PlayArrow
                    else -> Icons.Default.Pause
                },
                contentDescription = when {
                    !isRunning -> "Start"
                    isPaused -> "Resume"
                    else -> "Pause"
                },
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun PermissionContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (shouldShowRationale) {
                "This app needs location access to track your running route and calculate distance, pace, and other metrics."
            } else {
                "Please grant location permission to use the running tracker."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }
    }
}