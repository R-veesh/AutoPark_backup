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
    
    // Sample data for demonstration
    val charges = remember { mutableStateListOf(
        OverdueCharge(
            id = "1",
            ownerId = "1",
            ownerName = "John Doe",
            invoiceId = "INV001",
            invoiceNumber = "INV-001",
            originalAmount = 150.00,
            overdueDays = 15,
            daysOverdue = 15,
            lateFeeAmount = 22.50,
            totalAmount = 172.50,
            totalDueAmount = 172.50,
            status = "PENDING",
            paymentStatus = "PENDING"
        ),
        OverdueCharge(
            id = "2",
            ownerId = "2",
            ownerName = "Jane Smith",
            invoiceId = "INV002",
            invoiceNumber = "INV-002",
            originalAmount = 200.00,
            overdueDays = 30,
            daysOverdue = 30,
            lateFeeAmount = 60.00,
            totalAmount = 260.00,
            totalDueAmount = 260.00,
            status = "PENDING",
            paymentStatus = "PENDING"
        )
    ) }

    var showPaymentDialog by remember { mutableStateOf<OverdueCharge?>(null) }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredCharges = if (filterStatus == "ALL") {
        charges
    } else {
        charges.filter { it.status == filterStatus }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overdue Charges Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        text = "Total Overdue Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            charges.filter { it.status == "PENDING" }
                                .sumOf { it.totalAmount }
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "${charges.count { it.status == "PENDING" }} pending charges",
                        style = MaterialTheme.typography.bodyMedium,
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
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterStatus == "PENDING",
                    onClick = { filterStatus = "PENDING" },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = filterStatus == "PAID",
                    onClick = { filterStatus = "PAID" },
                    label = { Text("Paid") }
                )
            }

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

    if (showPaymentDialog != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = null },
            title = { Text("Confirm Payment") },
            text = {
                Column {
                    Text("Owner: ${showPaymentDialog!!.ownerName}")
                    Text("Invoice: ${showPaymentDialog!!.invoiceId}")
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
                        val index = charges.indexOfFirst { it.id == showPaymentDialog!!.id }
                        if (index != -1) {
                            charges[index] = charges[index].copy(
                                status = "PAID",
                                paymentStatus = "PAID"
                            )
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
                        text = "Invoice: ${charge.invoiceId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = if (charge.status == "PENDING")
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = charge.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (charge.status == "PENDING")
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
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
                
                if (charge.status == "PENDING") {
                    Button(onClick = onMarkAsPaid) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Paid")
                    }
                }
            }
        }
    }
}
