package com.example.autopark.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingRate
import com.example.autopark.ui.viewmodel.ParkingRateViewModel
import com.example.autopark.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingRateManagementScreen(
    navController: NavController,
    parkingLotId: String? = null,
    viewModel: ParkingRateViewModel = hiltViewModel()
) {
    val rates by viewModel.ratesForLot.collectAsStateWithLifecycle()
    val allRates by viewModel.allRates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRate by remember { mutableStateOf<ParkingRate?>(null) }

    // Load rates when screen opens
    LaunchedEffect(parkingLotId) {
        if (parkingLotId != null) {
            Log.d("ParkingRateScreen", "Loading rates for lot: $parkingLotId")
            viewModel.loadRatesForLot(parkingLotId)
        } else {
            Log.d("ParkingRateScreen", "Loading all rates")
            viewModel.loadAllRates()
        }
    }

    val displayRates = if (parkingLotId != null) rates else allRates

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (parkingLotId != null) "Parking Rates" else "All Parking Rates"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedRate = null
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Rate")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading && displayRates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (displayRates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No rates configured",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap + to add a rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    "${displayRates.size} rate(s) found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayRates) { rate ->
                        ParkingRateCard(
                            rate = rate,
                            onEdit = {
                                selectedRate = rate
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteRate(rate.id)
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ParkingRateDialog(
                rate = selectedRate,
                parkingLotId = parkingLotId,
                onDismiss = { showAddDialog = false },
                onConfirm = { newRate ->
                    if (selectedRate != null) {
                        viewModel.updateRate(newRate)
                    } else {
                        viewModel.addRate(newRate)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ParkingRateCard(
    rate: ParkingRate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    rate.rateType,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (rate.isActive) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "Active",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Per Hour:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        CurrencyFormatter.formatCurrency(rate.pricePerHour),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        "Per Day:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        CurrencyFormatter.formatCurrency(rate.pricePerDay),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        "Overnight:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        CurrencyFormatter.formatCurrency(rate.overnightPrice),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Min Charge: ${CurrencyFormatter.formatCurrency(rate.minChargeAmount)} | " +
                "Max: ${CurrencyFormatter.formatCurrency(rate.maxChargePerDay)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun ParkingRateDialog(
    rate: ParkingRate?,
    parkingLotId: String?,
    onDismiss: () -> Unit,
    onConfirm: (ParkingRate) -> Unit
) {
    var rateType by remember { mutableStateOf(rate?.rateType ?: "NORMAL") }
    var pricePerHour by remember { mutableStateOf(rate?.pricePerHour?.toString() ?: "5.0") }
    var pricePerDay by remember { mutableStateOf(rate?.pricePerDay?.toString() ?: "50.0") }
    var overnightPrice by remember { mutableStateOf(rate?.overnightPrice?.toString() ?: "30.0") }
    var minCharge by remember { mutableStateOf(rate?.minChargeAmount?.toString() ?: "5.0") }
    var maxCharge by remember { mutableStateOf(rate?.maxChargePerDay?.toString() ?: "100.0") }
    var isActive by remember { mutableStateOf(rate?.isActive ?: true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rate != null) "Edit Rate" else "Add Rate") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = rateType,
                        onValueChange = { rateType = it.uppercase() },
                        label = { Text("Rate Type (NORMAL/VIP/HOURLY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = pricePerHour,
                        onValueChange = { pricePerHour = it },
                        label = { Text("Price Per Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = pricePerDay,
                        onValueChange = { pricePerDay = it },
                        label = { Text("Price Per Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = overnightPrice,
                        onValueChange = { overnightPrice = it },
                        label = { Text("Overnight Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = minCharge,
                        onValueChange = { minCharge = it },
                        label = { Text("Minimum Charge") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = maxCharge,
                        onValueChange = { maxCharge = it },
                        label = { Text("Maximum Charge Per Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lotId = parkingLotId ?: rate?.parkingLotId ?: ""
                    if (lotId.isNotEmpty()) {
                        onConfirm(
                            (rate ?: ParkingRate(parkingLotId = lotId)).copy(
                                parkingLotId = lotId,
                                rateType = rateType,
                                pricePerHour = pricePerHour.toDoubleOrNull() ?: 0.0,
                                pricePerDay = pricePerDay.toDoubleOrNull() ?: 0.0,
                                overnightPrice = overnightPrice.toDoubleOrNull() ?: 0.0,
                                minChargeAmount = minCharge.toDoubleOrNull() ?: 0.0,
                                maxChargePerDay = maxCharge.toDoubleOrNull() ?: 0.0,
                                isActive = isActive
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
