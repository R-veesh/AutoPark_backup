package com.example.autopark.ui.screens.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.Vehicle
import com.example.autopark.ui.viewmodel.DriverQRViewModel
import com.example.autopark.ui.viewmodel.QRGenerationState
import com.example.autopark.ui.viewmodel.VehicleViewModel
import com.example.autopark.util.QRCodeConfig
import com.example.autopark.util.QRCodeGenerator
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRDisplayScreen(
    navController: NavController,
    initialVehicleId: String = "",
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
    qrViewModel: DriverQRViewModel = hiltViewModel()
) {
    val vehicles by vehicleViewModel.vehicles.collectAsStateWithLifecycle()
    val qrBitmap by qrViewModel.qrCodeBitmap.collectAsStateWithLifecycle()
    val qrCountdown by qrViewModel.qrCountdown.collectAsStateWithLifecycle()
    val generationState by qrViewModel.generationState.collectAsStateWithLifecycle()
    val isLoading by qrViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by qrViewModel.errorMessage.collectAsStateWithLifecycle()
    val qrData by qrViewModel.qrCodeData.collectAsStateWithLifecycle()
    
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var qrCodeType by remember { mutableStateOf("ENTRY") }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    // Get current user ID from Firebase Auth
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        vehicleViewModel.loadOwnerVehicles()
    }

    // Preselect vehicle when opened with initialVehicleId
    LaunchedEffect(vehicles, initialVehicleId) {
        if (initialVehicleId.isNotBlank() && selectedVehicle == null) {
            selectedVehicle = vehicles.find { it.id == initialVehicleId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedVehicle == null) "Select Vehicle" else "Parking QR Code",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedVehicle != null) {
                            selectedVehicle = null
                            qrViewModel.resetQRCode()
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            if (selectedVehicle == null) {
                Text(
                    text = "Select a vehicle to generate its parking QR code",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (vehicles.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No vehicles registered",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add a vehicle first to generate a QR code for entry and exit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigate("driver_vehicles") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedVehicle!!.vehicleNumber,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selectedVehicle!!.brand} ${selectedVehicle!!.model}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // QR Type Selection
                            if (generationState == QRGenerationState.IDLE || generationState == QRGenerationState.EXPIRED) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { showTypeMenu = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(qrCodeType, fontWeight = FontWeight.SemiBold)
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showTypeMenu,
                                        onDismissRequest = { showTypeMenu = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .align(Alignment.BottomStart)
                                    ) {
                                        listOf("ENTRY", "EXIT").forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type) },
                                                onClick = {
                                                    qrCodeType = type
                                                    showTypeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        qrViewModel.generateQRCode(
                                            userId = currentUserId,
                                            vehicleNumber = selectedVehicle!!.vehicleNumber,
                                            qrType = qrCodeType
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = if (generationState == QRGenerationState.EXPIRED) "Regenerate QR Code" else "Generate QR Code",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // QR Code Display Section
                            AnimatedVisibility(
                                visible = generationState == QRGenerationState.GENERATED || generationState == QRGenerationState.EXPIRED,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (qrBitmap != null) {
                                        // QR Code with Card
                                        Card(
                                            modifier = Modifier
                                                .size(280.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Image(
                                                bitmap = qrBitmap!!.asImageBitmap(),
                                                contentDescription = "QR Code",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(20.dp))
                                        
                                        // Countdown Timer with Progress Indicator
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = when {
                                                        generationState == QRGenerationState.EXPIRED ->
                                                            MaterialTheme.colorScheme.errorContainer
                                                        QRCodeConfig.isLowTimeWarning(qrCountdown) ->
                                                            MaterialTheme.colorScheme.tertiaryContainer
                                                        else ->
                                                            MaterialTheme.colorScheme.secondaryContainer
                                                    },
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Countdown Display
                                            Box(
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .background(
                                                        color = if (generationState == QRGenerationState.EXPIRED)
                                                            MaterialTheme.colorScheme.error
                                                        else
                                                            MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "$qrCountdown",
                                                        fontSize = 40.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                    Text(
                                                        text = "seconds",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                            
                                            // Progress Bar
                                            LinearProgressIndicator(
                                                progress = QRCodeConfig.getProgressPercentage(qrCountdown),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = if (generationState == QRGenerationState.EXPIRED)
                                                    MaterialTheme.colorScheme.error
                                                else if (QRCodeConfig.isLowTimeWarning(qrCountdown))
                                                    MaterialTheme.colorScheme.tertiary
                                                else
                                                    MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                            )
                                            
                                            // Status Text
                                            Text(
                                                text = when {
                                                    generationState == QRGenerationState.EXPIRED ->
                                                        "QR Code Expired"
                                                    QRCodeConfig.isLowTimeWarning(qrCountdown) ->
                                                        "Expiring soon"
                                                    else ->
                                                        "Valid QR Code"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (generationState == QRGenerationState.EXPIRED)
                                                    MaterialTheme.colorScheme.onErrorContainer
                                                else
                                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(20.dp))
                                    } else if (errorMessage != null) {
                                        Box(
                                            modifier = Modifier.size(280.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "Failed to generate QR code",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = errorMessage ?: "Unknown error",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Show this QR at the parking entrance or exit",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Instructions",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "1. Generate a fresh QR code using the button above",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "2. Select ENTRY or EXIT type based on your need",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "3. Show this QR code to the parking attendant",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "4. QR code expires in 30 seconds, regenerate as needed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "5. Keep your screen brightness high for scanning",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedButton(
                                onClick = {
                                    selectedVehicle = null
                                    qrViewModel.resetQRCode()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.vehicleNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${vehicle.color} ${vehicle.brand} ${vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Type: ${vehicle.vehicleType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = if (vehicle.parkingLicenseValid)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (vehicle.parkingLicenseValid) "Active" else "Inactive",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (vehicle.parkingLicenseValid)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}