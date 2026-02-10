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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.ui.viewmodel.ParkingTransactionViewModel
import com.example.autopark.ui.viewmodel.ParkingLotViewModel
import com.example.autopark.util.CurrencyFormatter
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: ParkingTransactionViewModel = hiltViewModel(),
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

    // Parsed from QR: driver app encodes "vehicleNumber|vehicleId"
    var vehicleNumber by remember { mutableStateOf("") }
    var scannedVehicleId by remember { mutableStateOf<String?>(null) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<Result<ParkingTransaction>?>(null) }

    val parkingLots: List<ParkingLot> by parkingLotViewModel.parkingLots.collectAsState(initial = emptyList())
    var selectedParkingLot by remember { mutableStateOf<ParkingLot?>(null) }
    var expandedLotDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        parkingLotViewModel.loadAllParkingLots()
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

            if (hasCameraPermission) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (isScanning) {
                        CameraPreviewWithQRScanner { code ->
                            if (scannedCode == null) {
                                scannedCode = code
                                // Driver QR format: "vehicleNumber|vehicleId"
                                val parts = code.split("|").map { it.trim() }
                                vehicleNumber = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: code
                                scannedVehicleId = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
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
                            Text(
                                vehicleNumber,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Parking lot dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedLotDropdown,
                    onExpandedChange = { expandedLotDropdown = !expandedLotDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedParkingLot?.let { if (it.name.isNotBlank()) it.name else it.id } ?: "Select Parking Lot",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parking Lot") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expandedLotDropdown)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLotDropdown,
                        onDismissRequest = { expandedLotDropdown = false }
                    ) {
                        parkingLots.forEach { lot ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (lot.name.isNotBlank()) lot.name else lot.id
                                    )
                                },
                                onClick = {
                                    selectedParkingLot = lot
                                    expandedLotDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it.uppercase() },
                    label = { Text("Vehicle Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Entry: use vehicleId when available (from QR) for reliable lookup
                Button(
                    onClick = {
                        isProcessing = true
                        val lotId = selectedParkingLot?.id ?: ""
                        if (scannedVehicleId != null) {
                            viewModel.processVehicleEntry(
                                vehicleId = scannedVehicleId!!,
                                vehicleNumber = vehicleNumber,
                                parkingLotId = lotId
                            ) { result ->
                                transactionResult = result
                                showResult = true
                                isProcessing = false
                            }
                        } else {
                            viewModel.processVehicleEntryByNumber(
                                vehicleNumber,
                                lotId
                            ) { result ->
                                transactionResult = result
                                showResult = true
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vehicleNumber.isNotBlank() && selectedParkingLot != null && !isProcessing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isProcessing) "Processing…" else "Process Entry")
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        if (scannedVehicleId != null) {
                            viewModel.processVehicleExit(scannedVehicleId!!) { result ->
                                transactionResult = result
                                showResult = true
                                isProcessing = false
                            }
                        } else {
                            viewModel.processVehicleExitByNumber(vehicleNumber) { result ->
                                transactionResult = result
                                showResult = true
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vehicleNumber.isNotBlank() && !isProcessing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Process Exit")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isScanning = true
                        scannedCode = null
                        scannedVehicleId = null
                        vehicleNumber = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan Another")
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
                scannedCode = null
                scannedVehicleId = null
                vehicleNumber = ""
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
