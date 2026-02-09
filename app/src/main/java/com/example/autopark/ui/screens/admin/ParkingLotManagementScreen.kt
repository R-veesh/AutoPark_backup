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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.ui.viewmodel.ParkingLotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotManagementScreen(
    navController: NavController,
    viewModel: ParkingLotViewModel = hiltViewModel()
) {
    val lots by viewModel.parkingLots.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLot by remember { mutableStateOf<ParkingLot?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllParkingLots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parking Lots Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedLot = null
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Parking Lot")
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
            } else if (lots.isEmpty()) {
                Text(
                    "No parking lots found",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(lots) { lot ->
                        ParkingLotCard(
                            lot = lot,
                            onEdit = {
                                selectedLot = lot
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteParkingLot(lot.id)
                            },
                            onManageRates = {
                                navController.navigate("parking_rates/${lot.id}")
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ParkingLotDialog(
                lot = selectedLot,
                onDismiss = { showAddDialog = false },
                onConfirm = { newLot ->
                    if (selectedLot != null) {
                        viewModel.updateParkingLot(newLot)
                    } else {
                        viewModel.addParkingLot(newLot)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ParkingLotCard(
    lot: ParkingLot,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageRates: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(lot.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(lot.address, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total Spots: ${lot.totalSpots} | Available: ${lot.availableSpots}",
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
                Button(onClick = onManageRates, modifier = Modifier.padding(start = 8.dp)) {
                    Text("Rates")
                }
            }
        }
    }
}

@Composable
fun ParkingLotDialog(
    lot: ParkingLot?,
    onDismiss: () -> Unit,
    onConfirm: (ParkingLot) -> Unit
) {
    var name by remember { mutableStateOf(lot?.name ?: "") }
    var address by remember { mutableStateOf(lot?.address ?: "") }
    var city by remember { mutableStateOf(lot?.city ?: "") }
    var state by remember { mutableStateOf(lot?.state ?: "") }
    var zipCode by remember { mutableStateOf(lot?.zipCode ?: "") }
    var totalSpots by remember { mutableStateOf(lot?.totalSpots?.toString() ?: "") }
    var latitude by remember { mutableStateOf(lot?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(lot?.longitude?.toString() ?: "") }
    var contactNumber by remember { mutableStateOf(lot?.contactNumber ?: "") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (lot != null) "Edit Parking Lot" else "Add Parking Lot") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = { Text("Zip Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = totalSpots,
                        onValueChange = { totalSpots = it },
                        label = { Text("Total Spots") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        (lot ?: ParkingLot()).copy(
                            name = name,
                            address = address,
                            city = city,
                            state = state,
                            zipCode = zipCode,
                            totalSpots = totalSpots.toIntOrNull() ?: 0,
                            availableSpots = lot?.availableSpots ?: (totalSpots.toIntOrNull() ?: 0),
                            latitude = latitude.toDoubleOrNull() ?: 0.0,
                            longitude = longitude.toDoubleOrNull() ?: 0.0,
                            contactNumber = contactNumber
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
