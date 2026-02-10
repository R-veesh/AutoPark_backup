package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.repository.ParkingLotRepository
import com.example.autopark.data.repository.ParkingTransactionRepository
import com.example.autopark.data.repository.VehicleRepository
import com.example.autopark.util.QRCodeValidator
import com.example.autopark.util.TimestampUtils
import com.example.autopark.util.ValidationResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminQRScannerViewModel @Inject constructor(
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingTransactionRepository: ParkingTransactionRepository,
    private val vehicleRepository: VehicleRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _parkingLots = MutableStateFlow<List<ParkingLot>>(emptyList())
    val parkingLots: StateFlow<List<ParkingLot>> = _parkingLots.asStateFlow()

    private val _selectedParkingLot = MutableStateFlow<ParkingLot?>(null)
    val selectedParkingLot: StateFlow<ParkingLot?> = _selectedParkingLot.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastScannedData = MutableStateFlow<QRScannedData?>(null)
    val lastScannedData: StateFlow<QRScannedData?> = _lastScannedData.asStateFlow()

    private var parkingLotsListener: ListenerRegistration? = null

    private fun parseParkingLotFromDocument(docId: String, data: Map<String, Any?>?): ParkingLot? {
        if (data == null) return null
        return try {
            ParkingLot(
                id = docId,
                name = data["name"] as? String ?: "",
                address = data["address"] as? String ?: "",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                city = data["city"] as? String ?: "",
                state = data["state"] as? String ?: "",
                zipCode = data["zipCode"] as? String ?: "",
                totalSpots = (data["totalSpots"] as? Number)?.toInt() ?: 0,
                availableSpots = (data["availableSpots"] as? Number)?.toInt() ?: 0,
                description = data["description"] as? String ?: "",
                contactNumber = data["contactNumber"] as? String ?: "",
                openingTime = data["openingTime"] as? String ?: "",
                closingTime = data["closingTime"] as? String ?: "",
                is24Hours = data["is24Hours"] as? Boolean ?: false,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
        } catch (e: Exception) {
            Log.e("AdminQRScannerVM", "Error parsing parking lot: ${e.message}")
            null
        }
    }

    fun loadParkingLots() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Remove previous listener
            parkingLotsListener?.remove()
            
            // Set up real-time listener
            parkingLotsListener = db.collection("parking_lots")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AdminQRScannerVM", "Listen failed: ${error.message}")
                        _errorMessage.value = error.message ?: "Failed to load parking lots"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val lots = snapshot.documents.mapNotNull { doc ->
                            parseParkingLotFromDocument(doc.id, doc.data)
                        }
                        _parkingLots.value = lots
                        _errorMessage.value = null
                        Log.d("AdminQRScannerVM", "Loaded ${lots.size} parking lots")
                    }
                    _isLoading.value = false
                }
        }
    }

    fun selectParkingLot(parkingLot: ParkingLot) {
        _selectedParkingLot.value = parkingLot
        Log.d("AdminQRScannerVM", "Selected parking lot: ${parkingLot.name} (${parkingLot.id})")
    }

    fun processQRCode(qrCode: String): QRScannedData? {
        return try {
            // Try to parse as new format first (PARKTRACK|userId|vehicleNumber|timestamp|qrType|hash)
            val qrCodeData = com.example.autopark.data.model.QRCodeData.fromQRString(qrCode)
            
            if (qrCodeData != null) {
                // Validate using QRCodeValidator
                val validationResult = QRCodeValidator.validateQRCode(qrCodeData)
                
                if (validationResult is ValidationResult.Valid) {
                    val scannedData = QRScannedData(
                        vehicleNumber = qrCodeData.vehicleNumber,
                        vehicleId = "", // Not available in new format
                        userId = qrCodeData.userId,
                        rawCode = qrCode
                    )
                    _lastScannedData.value = scannedData
                    _errorMessage.value = null
                    return scannedData
                } else {
                    _errorMessage.value = when (validationResult) {
                        ValidationResult.Expired -> "QR code has expired"
                        ValidationResult.InvalidFormat -> "Invalid QR code format"
                        ValidationResult.InvalidHash -> "QR code validation failed"
                        ValidationResult.Valid -> "Unknown error"
                    }
                    return null
                }
            }
            
            // Fallback: Try parsing as simple format "vehicleNumber|vehicleId|userId"
            val parts = qrCode.split("|").map { it.trim() }
            if (parts.size >= 3) {
                val scannedData = QRScannedData(
                    vehicleNumber = parts[0],
                    vehicleId = parts[1],
                    userId = parts[2],
                    rawCode = qrCode
                )
                _lastScannedData.value = scannedData
                _errorMessage.value = null
                scannedData
            } else {
                _errorMessage.value = "Invalid QR code format. Expected format: vehicleNumber|vehicleId|userId"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to parse QR code: ${e.message}"
            null
        }
    }

    fun processEntry(
        scannedData: QRScannedData,
        parkingLotId: String,
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validate parking lot selection
                val parkingLot = _parkingLots.value.find { it.id == parkingLotId }
                if (parkingLot == null) {
                    callback(Result.failure(Exception("Please select a parking lot")))
                    return@launch
                }

                // Check availability
                if (parkingLot.availableSpots <= 0) {
                    callback(Result.failure(Exception("Parking lot is full")))
                    return@launch
                }

                // Get or find vehicle
                val vehicle = if (scannedData.vehicleId.isNotEmpty()) {
                    vehicleRepository.getVehicle(scannedData.vehicleId).getOrNull()
                } else {
                    // Find by vehicle number
                    val allVehicles = vehicleRepository.getAllVehicles().getOrNull() ?: emptyList()
                    allVehicles.find { it.vehicleNumber.equals(scannedData.vehicleNumber, ignoreCase = true) }
                }

                if (vehicle == null) {
                    callback(Result.failure(Exception("Vehicle not found: ${scannedData.vehicleNumber}")))
                    return@launch
                }

                // Check for existing active parking
                val existingTransaction = parkingTransactionRepository
                    .getActiveParkingByVehicle(vehicle.id)
                    .getOrNull()

                if (existingTransaction != null) {
                    callback(Result.success(existingTransaction))
                    return@launch
                }

                // Create new entry transaction
                val transaction = ParkingTransaction(
                    parkingLotId = parkingLotId,
                    vehicleId = vehicle.id,
                    ownerId = vehicle.ownerId,
                    vehicleNumber = vehicle.vehicleNumber,
                    entryTime = System.currentTimeMillis(),
                    rateType = "NORMAL", // Default rate, can be enhanced later
                    status = "ACTIVE"
                )

                val result = parkingTransactionRepository.addTransaction(transaction)
                result.onSuccess { transactionId ->
                    // Update parking lot availability
                    val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots - 1)
                    parkingLotRepository.updateParkingLot(updatedLot)
                    
                    val completedTransaction = transaction.copy(id = transactionId)
                    callback(Result.success(completedTransaction))
                }.onFailure { error ->
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processExit(
        scannedData: QRScannedData,
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get or find vehicle
                val vehicle = if (scannedData.vehicleId.isNotEmpty()) {
                    vehicleRepository.getVehicle(scannedData.vehicleId).getOrNull()
                } else {
                    // Find by vehicle number
                    val allVehicles = vehicleRepository.getAllVehicles().getOrNull() ?: emptyList()
                    allVehicles.find { it.vehicleNumber.equals(scannedData.vehicleNumber, ignoreCase = true) }
                }

                if (vehicle == null) {
                    callback(Result.failure(Exception("Vehicle not found: ${scannedData.vehicleNumber}")))
                    return@launch
                }

                // Find active transaction
                val activeTransaction = parkingTransactionRepository
                    .getActiveParkingByVehicle(vehicle.id)
                    .getOrNull()

                if (activeTransaction == null) {
                    callback(Result.failure(Exception("No active parking found for this vehicle")))
                    return@launch
                }

                // Process exit - update transaction
                val exitTime = System.currentTimeMillis()
                val duration = (exitTime - activeTransaction.entryTime) / (1000 * 60) // in minutes

                // For now, use a simple rate calculation - this can be enhanced
                val chargeAmount = calculateBasicCharge(duration)

                val updatedTransaction = activeTransaction.copy(
                    exitTime = exitTime,
                    duration = duration,
                    chargeAmount = chargeAmount,
                    status = "COMPLETED",
                    paymentStatus = "PENDING"
                )

                val result = parkingTransactionRepository.updateTransaction(updatedTransaction)
                result.onSuccess {
                    // Update parking lot availability
                    val parkingLot = parkingLotRepository
                        .getParkingLot(activeTransaction.parkingLotId)
                        .getOrNull()
                    
                    if (parkingLot != null) {
                        val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots + 1)
                        parkingLotRepository.updateParkingLot(updatedLot)
                    }
                    
                    callback(Result.success(updatedTransaction))
                }.onFailure { error ->
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateBasicCharge(durationMinutes: Long): Double {
        // Simple calculation: $5 per hour minimum
        val hours = (durationMinutes + 59) / 60 // Round up to nearest hour
        return hours * 5.0
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetScannedData() {
        _lastScannedData.value = null
    }

    override fun onCleared() {
        super.onCleared()
        parkingLotsListener?.remove()
    }
}

data class QRScannedData(
    val vehicleNumber: String,
    val vehicleId: String,
    val userId: String,
    val rawCode: String
)
