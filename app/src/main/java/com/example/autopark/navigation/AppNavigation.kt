package com.example.autopark.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autopark.ui.screens.auth.LoginScreen
import com.example.autopark.ui.screens.auth.RegisterScreen
import com.example.autopark.ui.screens.dashboard.AdminDashboardScreen
import com.example.autopark.ui.screens.dashboard.DriverDashboardScreen
import com.example.autopark.ui.screens.admin.*
import com.example.autopark.ui.screens.driver.*
import com.example.autopark.ui.viewmodel.AuthUiState
import com.example.autopark.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Navigate based on authentication state
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.NotAuthenticated -> {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthUiState.Authenticated -> {
                val user = (uiState as AuthUiState.Authenticated).user
                val destination = if (user.role == "admin") "admin_dashboard" else "driver_dashboard"
                navController.navigate(destination) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthUiState.Error -> {
                // Optional: show snackbar or navigate to an error screen
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "loading") {
        // Loading Screen
        composable("loading") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Auth Screens
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }

        // Admin Screens
        composable("admin_dashboard") {
            AdminDashboardScreen(navController)
        }
        composable("admin_manage_owners") {
            VehicleOwnerManagementScreen(navController)
        }
        composable("admin_manage_vehicles") {
            VehicleManagementScreen(navController)
        }
        composable("admin_manage_lots") {
            ParkingLotManagementScreen(navController)
        }
        composable("admin_manage_rates") {
            ParkingRateManagementScreen(navController)
        }
        composable("admin_qr_scanner") {
            QRScannerScreen(navController)
        }
        composable("admin_reports") {
            AdminReportsScreen(navController)
        }
        composable("admin_overdue_charges") {
            AdminOverdueChargesScreen(navController)
        }
        composable("admin_data_import_export") {
            DataImportExportScreen(navController)
        }

        // Driver Screens
        composable("driver_dashboard") {
            DriverDashboardScreen(navController)
        }
        composable("driver_profile") {
            DriverProfileScreen(navController)
        }
        composable("driver_vehicles") {
            DriverVehiclesScreen(navController)
        }
        composable(
            route = "driver_qr_display/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            QRDisplayScreen(navController, initialVehicleId = vehicleId)
        }
        composable("driver_parking_history") {
            ParkingHistoryScreen(navController)
        }
        composable("driver_invoices") {
            InvoiceScreen(navController)
        }
        composable("driver_overdue_charges") {
            OverdueChargesScreen(navController)
        }
        composable("driver_parking_lots_map") {
            ParkingLotsMapScreen(navController)
        }
    }
}
