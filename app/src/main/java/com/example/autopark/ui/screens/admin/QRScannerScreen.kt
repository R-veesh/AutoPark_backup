@file:OptIn(ExperimentalGetImage::class)

package com.example.autopark.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.ui.viewmodel.AdminQRScannerViewModel
import com.example.autopark.ui.viewmodel.ParkingLotViewModel
import com.example.autopark.util.CurrencyFormatter
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    adminQRViewModel: AdminQRScannerViewModel = hiltViewModel(),
    parkingLotViewModel: ParkingLotViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isScanning by remember { mutableStateOf(true) }
    var showResult by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<Result<ParkingTransaction>?>(null) }

    // Use ParkingLotViewModel for parking lots (which works)
    val parkingLots by parkingLotViewModel.parkingLots.collectAsStateWithLifecycle()
    val parkingLotsLoading by parkingLotViewModel.isLoading.collectAsStateWithLifecycle()
    val parkingLotsError by parkingLotViewModel.errorMessage.collectAsStateWithLifecycle()
    
    val selectedParkingLot by adminQRViewModel.selectedParkingLot.collectAsStateWithLifecycle()
    val isProcessing by adminQRViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by adminQRViewModel.errorMessage.collectAsStateWithLifecycle()
    val lastScannedData by adminQRViewModel.lastScannedData.collectAsStateWithLifecycle()
    
    // Manual vehicle number entry
    var manualVehicleNumber by remember { mutableStateOf("") }
    var expandedLotDropdown by remember { mutableStateOf(false) }

    // Load parking lots from both ViewModels
    LaunchedEffect(Unit) {
        parkingLotViewModel.loadAllParkingLots()
        adminQRViewModel.loadParkingLots()
    }

    // Update manual vehicle number when scanned
    LaunchedEffect(lastScannedData) {
        lastScannedData?.let { data ->
            manualVehicleNumber = data.vehicleNumber
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "QR Scanner",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Show loading or error states
            when {
                !hasCameraPermission -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Camera permission required for QR scanning",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
                parkingLotsLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                parkingLotsError != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = parkingLotsError ?: "Unknown error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                else -> {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (isScanning) {
                        CameraPreviewWithQRScanner { code ->
                            val scannedData = adminQRViewModel.processQRCode(code)
                            if (scannedData != null) {
                                isScanning = false
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Scanned",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            lastScannedData?.let { data ->
                                Text(
                                    data.vehicleNumber,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (data.vehicleId.isNotEmpty()) {
                                    Text(
                                        "ID: ${data.vehicleId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Parking lot dropdown - Fixed version with menuAnchor
                if (parkingLots.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No parking lots available",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Please add parking lots in the management section",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedParkingLot?.let { if (it.name.isNotBlank()) it.name else it.id } ?: "Select Parking Lot",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Parking Lot (${parkingLots.size} available)") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select parking lot",
                                    modifier = Modifier.clickable { expandedLotDropdown = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedLotDropdown = true },
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = expandedLotDropdown,
                            onDismissRequest = { expandedLotDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            parkingLots.forEach { lot ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = if (lot.name.isNotBlank()) lot.name else "Lot ${lot.id.take(8)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${lot.availableSpots}/${lot.totalSpots} spots available",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (lot.availableSpots > 0) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    onClick = {
                                        adminQRViewModel.selectParkingLot(lot)
                                        expandedLotDropdown = false
                                        Log.d("QRScanner", "Selected: ${lot.name}")
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Show selected parking lot info
                if (selectedParkingLot != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Selected: ${selectedParkingLot?.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedParkingLot?.availableSpots}/${selectedParkingLot?.totalSpots} spots available",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Editable vehicle number field
                OutlinedTextField(
                    value = manualVehicleNumber,
                    onValueChange = { 
                        manualVehicleNumber = it.uppercase()
                        // Also update the scanned data if exists
                        lastScannedData?.let { currentData ->
                            adminQRViewModel.setManualVehicleNumber(it.uppercase())
                        }
                    },
                    label = { Text("Vehicle Number (or scan QR)") },
                    placeholder = { Text("Enter vehicle number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                // Entry: Process vehicle entry with selected parking lot
                Button(
                    onClick = {
                        val vehicleNumber = manualVehicleNumber.ifEmpty { lastScannedData?.vehicleNumber }
                        val lotId = selectedParkingLot?.id
                        Log.d("QRScanner", "Process Entry clicked - Vehicle: $vehicleNumber, Lot ID: $lotId")
                        if (!vehicleNumber.isNullOrEmpty() && lotId != null) {
                            val scannedData = lastScannedData?.copy(vehicleNumber = vehicleNumber)
                                ?: AdminQRScannerViewModel.QRScannedData(
                                    vehicleNumber = vehicleNumber,
                                    vehicleId = "",
                                    userId = "",
                                    rawCode = vehicleNumber
                                )
                            adminQRViewModel.processEntry(scannedData, lotId) { result ->
                                transactionResult = result
                                showResult = true
                            }
                        } else {
                            Log.e("QRScanner", "Cannot process - Vehicle: $vehicleNumber, Lot: $lotId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (manualVehicleNumber.isNotEmpty() || lastScannedData != null) && selectedParkingLot != null && !isProcessing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isProcessing) "Processing…" else "Process Entry")
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        val vehicleNumber = manualVehicleNumber.ifEmpty { lastScannedData?.vehicleNumber }
                        if (!vehicleNumber.isNullOrEmpty()) {
                            val scannedData = lastScannedData?.copy(vehicleNumber = vehicleNumber)
                                ?: AdminQRScannerViewModel.QRScannedData(
                                    vehicleNumber = vehicleNumber,
                                    vehicleId = "",
                                    userId = "",
                                    rawCode = vehicleNumber
                                )
                            adminQRViewModel.processExit(scannedData) { result ->
                                transactionResult = result
                                showResult = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (manualVehicleNumber.isNotEmpty() || lastScannedData != null) && !isProcessing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isProcessing) "Processing…" else "Process Exit")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isScanning = true
                        manualVehicleNumber = ""
                        adminQRViewModel.resetScannedData()
                        adminQRViewModel.clearError()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan Another")
                }
                }
            }
        }
    }

    if (showResult) {
        ScanResultDialog(
            result = transactionResult,
            onDismiss = {
                showResult = false
                isScanning = true
                adminQRViewModel.resetScannedData()
            }
        )
    }
}

/* ---------------- CAMERA ---------------- */

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithQRScanner(
    onQRCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(barcodeScanner, imageProxy, onQRCodeScanned)
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@ExperimentalGetImage
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: return imageProxy.close()

    val image = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let(onQRCodeScanned)
        }
        .addOnCompleteListener { imageProxy.close() }
}

/* ---------------- RESULT DIALOG ---------------- */

@Composable
fun ScanResultDialog(
    result: Result<ParkingTransaction>?,
    onDismiss: () -> Unit
) {
    val tx = result?.getOrNull()
    val error = result?.exceptionOrNull()
    val success = tx != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (success) "Success" else "Failed",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tx != null) {
                    Text("Vehicle: ${tx.vehicleNumber}", style = MaterialTheme.typography.bodyLarge)
                    Text("Status: ${tx.status}", style = MaterialTheme.typography.bodyMedium)
                    if (tx.status == "COMPLETED" && tx.chargeAmount > 0) {
                        Text(
                            "Charge: ${CurrencyFormatter.formatCurrency(tx.chargeAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        error?.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("OK")
            }
        }
    )
}
