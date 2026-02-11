package com.example.autopark.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.ui.viewmodel.OverdueChargesViewModel
import com.example.autopark.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOverdueChargesScreen(
    navController: NavController,
    viewModel: OverdueChargesViewModel = hiltViewModel()
) {
    val overdueCharges by viewModel.overdueCharges.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showPaymentDialog by remember { mutableStateOf<OverdueCharge?>(null) }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredCharges = when (filterStatus) {
        "ALL" -> overdueCharges
        "ACTIVE_PAYMENT" -> overdueCharges.filter { it.parkingStatus == "ACTIVE" }
        "PENDING" -> overdueCharges.filter { 
            it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "PENDING" 
        }
        "PAID" -> overdueCharges.filter { 
            it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "COMPLETED" 
        }
        else -> overdueCharges.filter { it.status == filterStatus }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load overdue charges on launch
    LaunchedEffect(Unit) {
        viewModel.loadAllOverdueCharges()
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Overdue Charges Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processAllOverdueInvoices() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Process",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
            // Error Message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Error loading data:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Debug Info (temporary)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Debug: ${overdueCharges.size} total charges loaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Statuses: ${overdueCharges.map { it.status }.distinct().joinToString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Payment Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    // Active Payment Count
                    val activeCount = overdueCharges.count { it.parkingStatus == "ACTIVE" }
                    Text(
                        text = "$activeCount Active Payment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    // Pending Payment Count
                    val pendingCount = overdueCharges.count { 
                        it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "PENDING" 
                    }
                    Text(
                        text = "$pendingCount Pending Payment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    // Paid Count
                    val paidCount = overdueCharges.count { 
                        it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "COMPLETED" 
                    }
                    Text(
                        text = "$paidCount Paid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            overdueCharges.filter { 
                                it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "PENDING" 
                            }.sumOf { it.totalAmount }
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Total Pending Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("All (${overdueCharges.size})") }
                )
                FilterChip(
                    selected = filterStatus == "ACTIVE_PAYMENT",
                    onClick = { filterStatus = "ACTIVE_PAYMENT" },
                    label = { Text("Active (${overdueCharges.count { it.parkingStatus == "ACTIVE" }})") }
                )
                FilterChip(
                    selected = filterStatus == "PENDING",
                    onClick = { filterStatus = "PENDING" },
                    label = { Text("Pending (${overdueCharges.count { it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "PENDING" }})") }
                )
                FilterChip(
                    selected = filterStatus == "PAID",
                    onClick = { filterStatus = "PAID" },
                    label = { Text("Paid (${overdueCharges.count { it.parkingStatus == "COMPLETED" && it.transactionPaymentStatus == "COMPLETED" }})") }
                )
            }

            // Loading State
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredCharges.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (filterStatus == "ALL") 
                                "No overdue charges found" 
                            else 
                                "No ${filterStatus.lowercase()} charges",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (filterStatus == "ALL" && overdueCharges.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap 'Process' button in top bar to generate overdue charges from overdue invoices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                // Charges List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCharges) { charge ->
                        OverdueChargeCard(
                            charge = charge,
                            onMarkAsPaid = { showPaymentDialog = charge }
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = null },
            title = { Text("Confirm Payment") },
            text = {
                Column {
                    Text("Owner: ${showPaymentDialog!!.ownerName}")
                    Text("Invoice: ${showPaymentDialog!!.invoiceNumber}")
                    Text("Total Amount: ${CurrencyFormatter.formatCurrency(showPaymentDialog!!.totalAmount)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mark this overdue charge as paid?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentDialog?.let { charge ->
                            viewModel.markAsPaid(charge.id)
                        }
                        showPaymentDialog = null
                    }
                ) {
                    Text("Mark as Paid")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OverdueChargeCard(
    charge: OverdueCharge,
    onMarkAsPaid: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (charge.status == "PENDING")
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = charge.ownerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invoice: ${charge.invoiceNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Payment Status Badge
                val paymentStatusText = when {
                    charge.parkingStatus == "ACTIVE" -> "Active Payment"
                    charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "COMPLETED" -> "Paid"
                    charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "PENDING" -> "Pending"
                    else -> charge.status
                }
                
                val paymentStatusColor = when {
                    charge.parkingStatus == "ACTIVE" -> MaterialTheme.colorScheme.tertiaryContainer
                    charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
                
                val paymentStatusTextColor = when {
                    charge.parkingStatus == "ACTIVE" -> MaterialTheme.colorScheme.onTertiaryContainer
                    charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                }
                
                Surface(
                    color = paymentStatusColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = paymentStatusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = paymentStatusTextColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Original Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(charge.originalAmount),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Late Fee (${charge.overdueDays} days)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(charge.lateFeeAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount Due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(charge.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Show Mark as Paid button only for completed parking with pending payment
                if (charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "PENDING") {
                    Button(onClick = onMarkAsPaid) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Paid")
                    }
                } else if (charge.parkingStatus == "ACTIVE") {
                    // Show info for active parking
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Vehicle Parked",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                } else if (charge.parkingStatus == "COMPLETED" && charge.transactionPaymentStatus == "COMPLETED") {
                    // Show paid status
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "✓ Payment Complete",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
