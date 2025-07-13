package com.example.rush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rush.ui.screens.HistoryScreen
import com.example.rush.ui.screens.RunningScreen
import com.example.rush.ui.theme.RushTheme
import com.example.rush.viewmodel.RunningViewModel
import com.example.rush.viewmodel.RunningViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Debug Google Maps API key configuration
        debugApiKeySetup()
        
        setContent {
            RushTheme {
                RushApp()
            }
        }
    }
    
    private fun debugApiKeySetup() {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            
            Log.d("🔧 MAP_DEBUG", "=== Google Maps API Debug ===")
            Log.d("🔧 MAP_DEBUG", "Package Name: $packageName")
            Log.d("🔧 MAP_DEBUG", "API Key Found: ${apiKey != null}")
            Log.d("🔧 MAP_DEBUG", "API Key Valid: ${apiKey != null && apiKey != "YOUR_API_KEY_HERE"}")
            Log.d("🔧 MAP_DEBUG", "API Key Preview: ${apiKey?.take(10)}...")
            Log.d("🔧 MAP_DEBUG", "============================")
            
            if (apiKey == null) {
                Log.e("🔧 MAP_DEBUG", "❌ API key not found in AndroidManifest.xml!")
            } else if (apiKey == "YOUR_API_KEY_HERE") {
                Log.e("🔧 MAP_DEBUG", "❌ API key placeholder not replaced!")
            } else {
                Log.i("🔧 MAP_DEBUG", "✅ API key configured correctly")
            }
            
        } catch (e: Exception) {
            Log.e("🔧 MAP_DEBUG", "❌ Error reading API key: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RushApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Create ViewModel with context
    val viewModel: RunningViewModel = viewModel(
        factory = RunningViewModelFactory(context)
    )
    
    val items = listOf(
        BottomNavItem(
            name = "Run",
            route = "running",
            icon = Icons.Default.DirectionsRun
        ),
        BottomNavItem(
            name = "History",
            route = "history",
            icon = Icons.Default.History
        )
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        label = { Text(item.name) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "running",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("running") {
                RunningScreen(viewModel = viewModel)
            }
            composable("history") {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)