package com.example.autopark.ui.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autopark.data.model.Vehicle
import com.example.autopark.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManagementScreen(
    navController: NavController,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    
    // Sample data for demonstration
    val vehicles = remember { mutableStateListOf(
        Vehicle(
            id = "1",
            ownerId = "1",
            vehicleNumber = "ABC123",
            vehicleType = "Car",
            color = "Blue",
            brand = "Toyota",
            model = "Camry",
            parkingLicenseValid = true
        ),
        Vehicle(
            id = "2",
            ownerId = "1",
            vehicleNumber = "XYZ789",
            vehicleType = "Bike",
            color = "Red",
            brand = "Honda",
            model = "CBR",
            parkingLicenseValid = true
        ),
        Vehicle(
            id = "3",
            ownerId = "2",
            vehicleNumber = "DEF456",
            vehicleType = "Car",
            color = "Black",
            brand = "BMW",
            model = "X5",
            parkingLicenseValid = false
        )
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Vehicles") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Registered Vehicles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    AdminVehicleCard(
                        vehicle = vehicle,
                        onEdit = { selectedVehicle = vehicle },
                        onDelete = { vehicles.remove(vehicle) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        VehicleDialog(
            vehicle = null,
            onDismiss = { showAddDialog = false },
            onSave = { newVehicle ->
                vehicles.add(newVehicle.copy(id = (vehicles.size + 1).toString()))
                showAddDialog = false
            }
        )
    }

    if (selectedVehicle != null) {
        VehicleDialog(
            vehicle = selectedVehicle,
            onDismiss = { selectedVehicle = null },
            onSave = { updatedVehicle ->
                val index = vehicles.indexOfFirst { it.id == updatedVehicle.id }
                if (index != -1) {
                    vehicles[index] = updatedVehicle
                }
                selectedVehicle = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = vehicle.vehicleNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // License Status Badge
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
                        style = MaterialTheme.typography.labelMedium,
                        color = if (vehicle.parkingLicenseValid) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Type: ${vehicle.vehicleType}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Color: ${vehicle.color}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit
) {
    var vehicleNumber by remember { mutableStateOf(vehicle?.vehicleNumber ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: "Car") }
    var color by remember { mutableStateOf(vehicle?.color ?: "") }
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var model by remember { mutableStateOf(vehicle?.model ?: "") }
    var ownerId by remember { mutableStateOf(vehicle?.ownerId ?: "") }
    var isLicenseValid by remember { mutableStateOf(vehicle?.parkingLicenseValid ?: true) }

    val vehicleTypes = listOf("Car", "Bike", "Truck", "Van", "SUV")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Add Vehicle" else "Edit Vehicle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it.uppercase() },
                    label = { Text("Vehicle Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vehicleTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    vehicleType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ownerId,
                    onValueChange = { ownerId = it },
                    label = { Text("Owner ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isLicenseValid,
                        onCheckedChange = { isLicenseValid = it }
                    )
                    Text("Parking License Valid")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Vehicle(
                            id = vehicle?.id ?: "",
                            vehicleNumber = vehicleNumber,
                            vehicleType = vehicleType,
                            brand = brand,
                            model = model,
                            color = color,
                            ownerId = ownerId,
                            parkingLicenseValid = isLicenseValid
                        )
                    )
                },
                enabled = vehicleNumber.isNotBlank() && brand.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
