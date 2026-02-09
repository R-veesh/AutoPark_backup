package com.example.autopark.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
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
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRate by remember { mutableStateOf<ParkingRate?>(null) }

    LaunchedEffect(parkingLotId) {
        parkingLotId?.let { viewModel.loadRatesForLot(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parking Rates") },
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
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Text(
                        errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (rates.isEmpty()) {
                Text(
                    "No rates configured",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rates) { rate ->
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(rate.rateType, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Per Hour: ${CurrencyFormatter.formatCurrency(rate.pricePerHour)}",
                style = MaterialTheme.typography.bodySmall)
            Text("Per Day: ${CurrencyFormatter.formatCurrency(rate.pricePerDay)}",
                style = MaterialTheme.typography.bodySmall)
            Text("Overnight: ${CurrencyFormatter.formatCurrency(rate.overnightPrice)}",
                style = MaterialTheme.typography.bodySmall)
            Text("Min Charge: ${CurrencyFormatter.formatCurrency(rate.minChargeAmount)}",
                style = MaterialTheme.typography.bodySmall)
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
    var pricePerHour by remember { mutableStateOf(rate?.pricePerHour?.toString() ?: "") }
    var pricePerDay by remember { mutableStateOf(rate?.pricePerDay?.toString() ?: "") }
    var overnightPrice by remember { mutableStateOf(rate?.overnightPrice?.toString() ?: "") }
    var minCharge by remember { mutableStateOf(rate?.minChargeAmount?.toString() ?: "") }
    var maxCharge by remember { mutableStateOf(rate?.maxChargePerDay?.toString() ?: "") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rate != null) "Edit Rate" else "Add Rate") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = rateType,
                        onValueChange = { rateType = it },
                        label = { Text("Rate Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = pricePerHour,
                        onValueChange = { pricePerHour = it },
                        label = { Text("Price Per Hour") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = pricePerDay,
                        onValueChange = { pricePerDay = it },
                        label = { Text("Price Per Day") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = overnightPrice,
                        onValueChange = { overnightPrice = it },
                        label = { Text("Overnight Price") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = minCharge,
                        onValueChange = { minCharge = it },
                        label = { Text("Min Charge") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    OutlinedTextField(
                        value = maxCharge,
                        onValueChange = { maxCharge = it },
                        label = { Text("Max Charge Per Day") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lotId = parkingLotId ?: rate?.parkingLotId ?: "default"
                    onConfirm(
                        (rate ?: ParkingRate(parkingLotId = lotId)).copy(
                            parkingLotId = lotId,
                            rateType = rateType,
                            pricePerHour = pricePerHour.toDoubleOrNull() ?: 0.0,
                            pricePerDay = pricePerDay.toDoubleOrNull() ?: 0.0,
                            overnightPrice = overnightPrice.toDoubleOrNull() ?: 0.0,
                            minChargeAmount = minCharge.toDoubleOrNull() ?: 0.0,
                            maxChargePerDay = maxCharge.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
