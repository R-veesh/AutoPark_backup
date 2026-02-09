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

    var vehicleNumber by remember { mutableStateOf("") }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<Result<ParkingTransaction>?>(null) }

    // 🚨 force List type
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
                title = { Text("QR Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (hasCameraPermission) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    if (isScanning) {
                        CameraPreviewWithQRScanner { code ->
                            if (scannedCode == null) {
                                scannedCode = code
                                vehicleNumber = code
                                isScanning = false
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Scanned: $vehicleNumber")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Parking lot dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedLotDropdown,
                    onExpandedChange = { expandedLotDropdown = !expandedLotDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedParkingLot?.id ?: "Select Parking Lot",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parking Lot") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expandedLotDropdown)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLotDropdown,
                        onDismissRequest = { expandedLotDropdown = false }
                    ) {
                        parkingLots.forEach { lot ->
                            DropdownMenuItem(
                                text = { Text(lot.id) },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        viewModel.processVehicleEntryByNumber(
                            vehicleNumber,
                            selectedParkingLot?.id ?: ""
                        ) { result ->
                            transactionResult = result
                            showResult = true
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vehicleNumber.isNotBlank() && selectedParkingLot != null && !isProcessing
                ) {
                    Text("Process Entry")
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
            }
        )
    }
}

/* ---------------- CAMERA ---------------- */

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tx != null) "Success" else "Failed") },
        text = {
            if (tx != null) {
                Column {
                    Text("Vehicle: ${tx.vehicleNumber}")
                    Text("Status: ${tx.status}")
                    Text("Charge: ${CurrencyFormatter.formatCurrency(tx.chargeAmount)}")
                }
            } else {
                Text(error?.message ?: "Unknown error")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}
