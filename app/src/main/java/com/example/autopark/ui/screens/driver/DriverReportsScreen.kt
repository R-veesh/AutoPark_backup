package com.example.autopark.ui.screens.driver

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.DriverReportsViewModel
import com.example.autopark.util.CurrencyFormatter
import com.example.autopark.util.DateFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReportsScreen(
    navController: NavController,
    viewModel: DriverReportsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val driver by viewModel.driver.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isGeneratingPDF by viewModel.isGeneratingPDF.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val pdfResult by viewModel.pdfGenerationResult.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }

    // PDF Export Launcher
    val pdfExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.generatePDFReport(uri)
            }
        }
    }

    // Show success/error messages
    LaunchedEffect(pdfResult, errorMessage) {
        pdfResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPDFResult()
        }
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("My Reports")
                        driver?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_TITLE, "driver_report_${System.currentTimeMillis()}.pdf")
                    }
                    pdfExportLauncher.launch(intent)
                },
                icon = { Icon(Icons.Default.Done, contentDescription = "Export PDF") },
                text = { Text("Export PDF") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Summary Statistics Card
                item {
                    SummaryCard(
                        totalVehicles = vehicles.size,
                        totalTransactions = transactions.size,
                        completedTransactions = viewModel.getCompletedTransactionsCount(),
                        activeTransactions = viewModel.getActiveTransactionsCount(),
                        totalCharges = viewModel.calculateTotalCharges(),
                        averageCharge = viewModel.calculateAverageCharge()
                    )
                }

                // Vehicles Section
                item {
                    Text(
                        text = "My Vehicles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (vehicles.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.AccountBox,
                            message = "No vehicles registered"
                        )
                    }
                } else {
                    items(vehicles) { vehicle ->
                        VehicleSummaryCard(vehicle = vehicle)
                    }
                }

                // Recent Transactions Section
                item {
                    Text(
                        text = "Recent Parking History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.Refresh,
                            message = "No parking history found"
                        )
                    }
                } else {
                    items(transactions.sortedByDescending { it.entryTime }.take(10)) { transaction ->
                        TransactionSummaryCard(transaction = transaction)
                    }
                }
            }
        }
    }

    // Loading Dialog for PDF Generation
    if (isGeneratingPDF) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Generating PDF") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
private fun SummaryCard(
    totalVehicles: Int,
    totalTransactions: Int,
    completedTransactions: Int,
    activeTransactions: Int,
    totalCharges: Double,
    averageCharge: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Vehicles", totalVehicles.toString())
                StatItem("Parkings", totalTransactions.toString())
                StatItem("Active", activeTransactions.toString())
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Charges:")
                Text(
                    text = CurrencyFormatter.formatCurrency(totalCharges),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Average Charge:")
                Text(
                    text = CurrencyFormatter.formatCurrency(averageCharge),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VehicleSummaryCard(vehicle: com.example.autopark.data.model.Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = vehicle.vehicleNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.brand} ${vehicle.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = vehicle.vehicleType,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Surface(
                color = if (vehicle.parkingLicenseValid) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (vehicle.parkingLicenseValid) "Valid" else "Invalid",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun TransactionSummaryCard(transaction: com.example.autopark.data.model.ParkingTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.vehicleNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(transaction.chargeAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Entry: ${DateFormatter.formatDateTime(transaction.entryTime)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            transaction.exitTime?.let { exitTime ->
                Text(
                    text = "Exit: ${DateFormatter.formatDateTime(exitTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Duration: ${DateFormatter.getDurationString(transaction.entryTime, exitTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                color = when (transaction.status) {
                    "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                    "ACTIVE" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = transaction.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
