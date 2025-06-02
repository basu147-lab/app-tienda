package com.puntofacil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.puntofacil.ui.screens.auth.LoginScreen
import com.puntofacil.ui.screens.customers.CustomersScreen
import com.puntofacil.ui.screens.dashboard.DashboardScreen
import com.puntofacil.ui.screens.inventory.InventoryScreen
import com.puntofacil.ui.screens.reports.ReportsScreen
import com.puntofacil.ui.screens.sales.SalesScreen
import com.puntofacil.ui.screens.settings.SettingsScreen
import com.puntofacil.ui.theme.PuntoFacilTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PuntoFacilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PuntoFacilApp()
                }
            }
        }
    }
}

@Composable
fun PuntoFacilApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToSales = {
                    navController.navigate("sales")
                },
                onNavigateToInventory = {
                    navController.navigate("inventory")
                },
                onNavigateToCustomers = {
                    navController.navigate("customers")
                },
                onNavigateToReports = {
                    navController.navigate("reports")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        
        composable("sales") {
            SalesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("inventory") {
            InventoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("customers") {
            CustomersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}