package com.example.autopark.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.components.DashboardCard
import com.example.autopark.ui.components.ModernCard
import com.example.autopark.ui.viewmodel.AuthViewModel
import com.example.autopark.ui.viewmodel.ParkingLotViewModel
import com.example.autopark.ui.viewmodel.ParkingTransactionViewModel
import com.example.autopark.ui.viewmodel.VehicleViewModel
import com.example.autopark.ui.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    parkingLotViewModel: ParkingLotViewModel = hiltViewModel(),
    transactionViewModel: ParkingTransactionViewModel = hiltViewModel(),
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
    userViewModel: UserManagementViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(false) }
    
    // Dynamic data for dashboard counts
    val parkingLots by parkingLotViewModel.parkingLots.collectAsStateWithLifecycle()
    val transactions by transactionViewModel.transactions.collectAsStateWithLifecycle()
    val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
    val users by userViewModel.users.collectAsStateWithLifecycle()
    val isLoading by parkingLotViewModel.isLoading.collectAsStateWithLifecycle()

    // Load data on composition
    LaunchedEffect(Unit) {
        showContent = true
        parkingLotViewModel.loadAllParkingLots()
        transactionViewModel.loadAllTransactions()
        vehicleViewModel.loadAllVehicles()
        userViewModel.loadAllUsers()
    }

    val dashboardItems = listOf(
        DashboardItemData(
            title = "Vehicle Owners",
            icon = Icons.Default.People,
            description = "Manage vehicle owners",
            route = "admin_manage_owners",
            color = MaterialTheme.colorScheme.primary
        ),
        DashboardItemData(
            title = "Vehicles",
            icon = Icons.Default.DirectionsCar,
            description = "Manage vehicle details",
            route = "admin_manage_vehicles",
            color = MaterialTheme.colorScheme.secondary
        ),
        DashboardItemData(
            title = "Parking Lots",
            icon = Icons.Default.LocalParking,
            description = "Create & manage lots",
            route = "admin_manage_lots",
            color = MaterialTheme.colorScheme.tertiary
        ),
        DashboardItemData(
            title = "Parking Rates",
            icon = Icons.Default.AttachMoney,
            description = "Define parking rates",
            route = "admin_manage_rates",
            color = MaterialTheme.colorScheme.primary
        ),
        DashboardItemData(
            title = "QR Scanner",
            icon = Icons.Default.QrCodeScanner,
            description = "Scan for entry/exit",
            route = "admin_qr_scanner",
            color = MaterialTheme.colorScheme.secondary
        ),
        DashboardItemData(
            title = "Reports",
            icon = Icons.Default.Assessment,
            description = "Generate reports",
            route = "admin_reports",
            color = MaterialTheme.colorScheme.tertiary
        ),
        DashboardItemData(
            title = "Overdue Charges",
            icon = Icons.Default.Warning,
            description = "Manage overdue fees",
            route = "admin_overdue_charges",
            color = MaterialTheme.colorScheme.error
        ),
        DashboardItemData(
            title = "Data Import/Export",
            icon = Icons.Default.SwapHoriz,
            description = "Import & export data",
            route = "admin_data_import_export",
            color = MaterialTheme.colorScheme.primary
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome back, ${currentUser?.name?.split(" ")?.first() ?: "Admin"}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { authViewModel.logout() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Header Section with Stats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(animationSpec = tween(400)) { it / 2 }
                ) {
                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(4) {
                                LoadingStatCard(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminStatCard(
                                value = parkingLots.size.toString(),
                                label = "Parking Lots",
                                icon = Icons.Default.LocalParking,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                value = vehicles.count { v -> 
                                    transactions.any { t -> t.vehicleId == v.id && t.status == "ACTIVE" }
                                }.toString(),
                                label = "Active",
                                icon = Icons.Default.DirectionsCar,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                value = users.size.toString(),
                                label = "Users",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                value = transactions.count { 
                                    val today = System.currentTimeMillis()
                                    val startOfDay = today - (today % (24 * 60 * 60 * 1000))
                                    it.entryTime >= startOfDay
                                }.toString(),
                                label = "Today",
                                icon = Icons.Default.Today,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Revenue Summary Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                        slideInVertically(animationSpec = tween(400, delayMillis = 100)) { it / 2 }
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Revenue Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                RevenueItem(
                                    label = "Today's Revenue",
                                    amount = "$1,245",
                                    trend = "+12%"
                                )
                                RevenueItem(
                                    label = "This Month",
                                    amount = "$28,450",
                                    trend = "+8%"
                                )
                                RevenueItem(
                                    label = "This Year",
                                    amount = "$312,800",
                                    trend = "+15%"
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Title
            Text(
                text = "Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            )

            // Dashboard Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 800.dp)
                    .padding(horizontal = 20.dp),
                userScrollEnabled = false
            ) {
                items(dashboardItems, key = { it.title }) { item ->
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                scaleIn(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    initialScale = 0.8f
                                )
                    ) {
                        DashboardCard(
                            title = item.title,
                            description = item.description,
                            icon = item.icon,
                            onClick = {
                                navController.navigate(item.route)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 300))
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Add,
                                label = "Add Lot",
                                onClick = { navController.navigate("admin_manage_lots") }
                            )
                            QuickActionButton(
                                icon = Icons.Default.PersonAdd,
                                label = "Add Owner",
                                onClick = { navController.navigate("admin_manage_owners") }
                            )
                            QuickActionButton(
                                icon = Icons.Default.QrCodeScanner,
                                label = "Scan QR",
                                onClick = { navController.navigate("admin_qr_scanner") }
                            )
                            QuickActionButton(
                                icon = Icons.Default.Assessment,
                                label = "Reports",
                                onClick = { navController.navigate("admin_reports") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AdminStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun LoadingStatCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun RevenueItem(
    label: String,
    amount: String,
    trend: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = trend,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class DashboardItemData(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val route: String,
    val color: Color,
    val badge: String? = null
)
