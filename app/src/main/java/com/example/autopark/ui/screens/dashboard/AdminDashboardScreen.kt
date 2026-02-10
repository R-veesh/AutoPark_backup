package com.example.autopark.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material.icons.automirrored.filled.Logout
//import androidx.compose.material.icons.filled.Assessment
//import androidx.compose.material.icons.filled.AttachMoney
//import androidx.compose.material.icons.filled.DirectionsCar
//import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.QrCode2
//import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val dashboardItems = listOf(
        DashboardItem(
            title = "Manage Vehicle Owners",
            icon = Icons.Default.Person,
            description = "Register and manage vehicle owners",
            route = "admin_manage_owners"
        ),
        DashboardItem(
            title = "Manage Vehicles",
//            icon = Icons.Default.DirectionsCar,
            icon = Icons.Default.ArrowDropDown,
            description = "Register and manage vehicle details",
            route = "admin_manage_vehicles"
        ),
        DashboardItem(
            title = "Parking Lots",
//            icon = Icons.Default.LocalParking,
            icon = Icons.Default.ArrowDropDown,
            description = "Create and manage parking lots",
            route = "admin_manage_lots"
        ),
        DashboardItem(
            title = "Parking Rates",
//            icon = Icons.Default.AttachMoney,
            icon = Icons.Default.ArrowDropDown,
            description = "Define parking rates",
            route = "admin_manage_rates"
        ),
        DashboardItem(
            title = "QR Scanner",
//            icon = Icons.Default.QrCode2,
            icon = Icons.Default.ArrowDropDown,
            description = "Scan QR codes for entry/exit",
            route = "admin_qr_scanner"
        ),
        DashboardItem(
            title = "Reports",
//            icon = Icons.Default.Assessment,
            icon = Icons.Default.ArrowDropDown,
            description = "Generate monthly reports",
            route = "admin_reports"
        ),
        DashboardItem(
            title = "Overdue Charges",
            icon = Icons.Default.Warning,
            description = "Manage overdue charges",
            route = "admin_overdue_charges"
        ),
        DashboardItem(
            title = "Data Import/Export",
//            icon = Icons.Default.SwapHoriz,
            icon = Icons.Default.ArrowDropDown,
            description = "Import and export data",
            route = "admin_data_import_export"
        ),
        DashboardItem(
            title = "Logout",
//            icon = Icons.AutoMirrored.Filled.Logout,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            description = "Sign out of the application",
            route = "logout"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard")
                        Text(
                            text = "Welcome, ${currentUser?.name ?: "Admin"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            
            Text(
                text = "Select an option to manage the parking facility",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dashboardItems) { item ->
                    DashboardCard(
                        item = item,
                        onClick = {
                            if (item.route == "logout") {
                                authViewModel.logout()
                            } else {
                                navController.navigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    item: DashboardItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val route: String
)
