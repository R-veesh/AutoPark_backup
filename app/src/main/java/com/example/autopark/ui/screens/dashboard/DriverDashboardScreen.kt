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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.components.DashboardCard
import com.example.autopark.ui.components.ModernCard
import com.example.autopark.ui.screens.dashboard.DashboardItemData
import com.example.autopark.ui.viewmodel.AuthViewModel
import com.example.autopark.ui.viewmodel.VehicleViewModel
import com.example.autopark.ui.viewmodel.ParkingTransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
    transactionViewModel: ParkingTransactionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
    val transactions by transactionViewModel.transactions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
        transactionViewModel.loadOwnerTransactions()
    }

    // Calculate dynamic stats
    val vehicleCount = vehicles.size
    val sessionCount = transactions.count { it.status == "COMPLETED" }
    
    // Calculate this month's spending
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    val thisMonthSpending = transactions
        .filter { transaction ->
            val exitTime = transaction.exitTime
            if (transaction.status != "COMPLETED" || exitTime == null) return@filter false
            val transactionCal = Calendar.getInstance().apply { timeInMillis = exitTime }
            transactionCal.get(Calendar.MONTH) == currentMonth && 
            transactionCal.get(Calendar.YEAR) == currentYear
        }
        .sumOf { it.chargeAmount?.toDouble() ?: 0.0 }

    val dashboardItems = listOf(
        DashboardItemData(
            title = "My Profile",
            icon = Icons.Default.Person,
            description = "View and edit your profile",
            route = "driver_profile",
            color = MaterialTheme.colorScheme.primary
        ),
        DashboardItemData(
            title = "My Vehicles",
            icon = Icons.Default.DirectionsCar,
            description = "View registered vehicles",
            route = "driver_vehicles",
            color = MaterialTheme.colorScheme.secondary
        ),
        DashboardItemData(
            title = "Parking History",
            icon = Icons.Default.History,
            description = "View your parking history",
            route = "driver_parking_history",
            color = MaterialTheme.colorScheme.tertiary
        ),
        DashboardItemData(
            title = "Invoices",
            icon = Icons.Default.Receipt,
            description = "View monthly invoices",
            route = "driver_invoices",
            color = MaterialTheme.colorScheme.primary,
            badge = "2"
        ),
        DashboardItemData(
            title = "Overdue Charges",
            icon = Icons.Default.Warning,
            description = "View overdue charges",
            route = "driver_overdue_charges",
            color = MaterialTheme.colorScheme.error
        ),
        DashboardItemData(
            title = "Find Parking",
            icon = Icons.Default.LocationOn,
            description = "View parking lots on map",
            route = "driver_parking_lots_map",
            color = MaterialTheme.colorScheme.secondary
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome back, ${currentUser?.name?.split(" ")?.first() ?: "Driver"}!",
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
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(animationSpec = tween(400)) { it / 2 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatItem(
                            value = vehicleCount.toString(),
                            label = "Vehicles",
                            icon = Icons.Default.DirectionsCar,
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            value = sessionCount.toString(),
                            label = "Sessions",
                            icon = Icons.Default.LocalParking,
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            value = "%.0f".format(thisMonthSpending),
                            label = "This Month",
                            icon = Icons.Default.AttachMoney,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // VIP Status Card
            AnimatedVisibility(
                visible = currentUser?.isVIP == true,
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
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "VIP",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "VIP Member",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Enjoy exclusive parking benefits and discounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Quick Actions Title
            Text(
                text = "Quick Actions",
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
                            },
                            badge = item.badge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Activity Section
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            TextButton(onClick = { navController.navigate("driver_parking_history") }) {
                                Text("View All")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sample recent activities
                        RecentActivityItem(
                            title = "Parking Session Completed",
                            subtitle = "Lot A - 2 hours",
                            time = "2 hours ago",
                            icon = Icons.Default.CheckCircle,
                            iconColor = MaterialTheme.colorScheme.primary
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        RecentActivityItem(
                            title = "Payment Received",
                            subtitle = "Invoice #1234 - $15.00",
                            time = "Yesterday",
                            icon = Icons.Default.Payment,
                            iconColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatItem(
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
private fun RecentActivityItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


