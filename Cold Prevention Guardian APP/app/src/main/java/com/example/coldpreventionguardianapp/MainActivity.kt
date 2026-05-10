package com.example.coldpreventionguardianapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import com.example.coldpreventionguardianapp.ui.auth.AuthScreen
import com.example.coldpreventionguardianapp.ui.chat.ChatScreen
import com.example.coldpreventionguardianapp.ui.dashboard.DashboardScreen
import com.example.coldpreventionguardianapp.ui.details.HealthDetailScreen
import com.example.coldpreventionguardianapp.ui.profile.ProfileScreen
import com.example.coldpreventionguardianapp.ui.theme.ColdPreventionGuardianAPPTheme

// Navigation route constants
object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val HEALTH_DETAIL = "health_detail"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColdPreventionGuardianAPPTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val currentUser by SessionManager.currentUser.collectAsState()
    var isInitialCheck by remember { mutableStateOf(true) }

    // Wait briefly for Firebase to restore previous session (if any)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        isInitialCheck = false
    }

    if (isInitialCheck) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (currentUser != null) {
        MainScreen()
    } else {
        AuthScreen(
            onLoginSuccess = { /* StateFlow will update, triggering recomposition */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController: NavHostController = rememberNavController()
    val currentUser by SessionManager.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom bar tabs
    val bottomNavItems = listOf(
        Triple(NavRoutes.DASHBOARD, Icons.Filled.Home, R.string.nav_dashboard),
        Triple(NavRoutes.HEALTH_DETAIL, Icons.Filled.Favorite, R.string.nav_health_detail),
        Triple(NavRoutes.CHAT, Icons.AutoMirrored.Filled.Chat, R.string.nav_ai_assistant),
        Triple(NavRoutes.PROFILE, Icons.Filled.Person, R.string.nav_profile)
    )

    // Only show bottom bar on tab screens
    val showBottomBar = currentRoute in bottomNavItems.map { it.first }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (route, icon, labelRes) ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = stringResource(labelRes)
                                )
                            },
                            label = { Text(stringResource(labelRes)) },
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(NavRoutes.DASHBOARD) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.DASHBOARD
            ) {
                composable(NavRoutes.DASHBOARD) {
                    DashboardScreen()
                }
                composable(NavRoutes.CHAT) {
                    ChatScreen()
                }
                composable(NavRoutes.PROFILE) {
                    if (currentUser != null) {
                        ProfileScreen(
                            user = currentUser!!,
                            onLogout = { /* StateFlow will update, returning to AuthScreen */ }
                        )
                    }
                }
                composable(NavRoutes.HEALTH_DETAIL) {
                    HealthDetailScreen(
                        onNavigateBack = {}
                    )
                }
            }
        }
    }
}