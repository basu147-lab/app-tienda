package com.puntofacil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.puntofacil.ui.screens.auth.LoginScreen
import com.puntofacil.ui.screens.dashboard.DashboardScreen
import com.puntofacil.ui.screens.sales.SalesScreen
import com.puntofacil.ui.screens.inventory.InventoryScreen
import com.puntofacil.ui.screens.customers.CustomersScreen
import com.puntofacil.ui.screens.reports.ReportsScreen
import com.puntofacil.ui.screens.settings.SettingsScreen

@Composable
fun PuntoFacilNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSales = {
                    navController.navigate(Screen.Sales.route)
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onNavigateToCustomers = {
                    navController.navigate(Screen.Customers.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Sales.route) {
            SalesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Customers.route) {
            CustomersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Sales : Screen("sales")
    object Inventory : Screen("inventory")
    object Customers : Screen("customers")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}