package com.example.autopark.ui.screens.driver

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.Vehicle
import com.example.autopark.ui.viewmodel.VehicleViewModel
import com.example.autopark.util.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRDisplayScreen(
    navController: NavController,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadOwnerVehicles()
    }

    // Generate QR code when vehicle is selected
    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let { vehicle ->
            val qrText = "${vehicle.vehicleNumber}|${vehicle.id}"
            qrBitmap = QRCodeGenerator.generateQRCode(qrText)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (selectedVehicle == null) "Select Vehicle" else "Parking QR Code") 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectedVehicle != null) {
                            selectedVehicle = null
                            qrBitmap = null
                        } else {
                            navController.navigateUp() 
                        }
                    }) {
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
            if (selectedVehicle == null) {
                // Show vehicle list
                Text(
                    text = "Select a vehicle to generate QR code",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (vehicles.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No vehicles registered",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add a vehicle first to generate a QR code",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("driver_vehicles") }) {
                                Text("Add Vehicle")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vehicles) { vehicle ->
                            VehicleSelectionCard(
                                vehicle = vehicle,
                                onClick = { selectedVehicle = vehicle }
                            )
                        }
                    }
                }
            } else {
                // Show QR Code
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedVehicle!!.vehicleNumber,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedVehicle!!.brand} ${selectedVehicle!!.model}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(280.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(280.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Show this QR code at the parking entrance/exit",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Instructions
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Instructions:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. Show this QR code to the parking attendant",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "2. The attendant will scan it for entry/exit",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "3. Keep your phone screen brightness high",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { 
                                    selectedVehicle = null
                                    qrBitmap = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Select Different Vehicle")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectionCard(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = "${vehicle.color} ${vehicle.brand} ${vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Type: ${vehicle.vehicleType}",
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
                    text = if (vehicle.parkingLicenseValid) "Active" else "Inactive",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
