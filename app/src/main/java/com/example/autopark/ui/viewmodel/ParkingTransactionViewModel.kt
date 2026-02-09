package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.model.User
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.ParkingTransactionRepository
import com.example.autopark.data.repository.VehicleRepository
import com.example.autopark.data.repository.ParkingRateRepository
import com.example.autopark.data.repository.ParkingLotRepository
import com.example.autopark.util.ParkingChargeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingTransactionViewModel @Inject constructor(
    private val transactionRepository: ParkingTransactionRepository,
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val parkingRateRepository: ParkingRateRepository,
    private val parkingLotRepository: ParkingLotRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<ParkingTransaction>>(emptyList())
    val transactions: StateFlow<List<ParkingTransaction>> = _transactions.asStateFlow()

    private val _activeParking = MutableStateFlow<ParkingTransaction?>(null)
    val activeParking: StateFlow<ParkingTransaction?> = _activeParking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadOwnerTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = transactionRepository.getOwnerTransactions(userId)
                result.onSuccess { trans ->
                    _transactions.value = trans
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load transactions"
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun processVehicleEntry(
        vehicleId: String,
        vehicleNumber: String,
        parkingLotId: String = "default_lot",
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // First check if vehicle already has active parking
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicleId)
                val activeTransaction = activeResult.getOrNull()
                if (activeTransaction != null) {
                    // Vehicle already has active parking
                    callback(Result.success(activeTransaction))
                    return@launch
                }
                
                // No active parking, proceed with entry
                
                // Get vehicle details to get ownerId
                val vehicleResult = vehicleRepository.getVehicle(vehicleId)
                val vehicle = vehicleResult.getOrNull()
                if (vehicle == null) {
                    callback(Result.failure(Exception("Vehicle not found")))
                    return@launch
                }

                // Get parking lot details to update availability
                val parkingLotResult = parkingLotRepository.getParkingLot(parkingLotId)
                val parkingLot = parkingLotResult.getOrNull()
                if (parkingLot == null) {
                    callback(Result.failure(Exception("Parking lot not found")))
                    return@launch
                }

                // Check if parking is available
                if (parkingLot.availableSpots <= 0) {
                    callback(Result.failure(Exception("No available parking spots")))
                    return@launch
                }

                // Get user details to check VIP status
                val userResult = authRepository.getUserData(vehicle.ownerId)
                val user = userResult.getOrNull()

                // Get applicable rate
                val rateResult = parkingRateRepository.getActiveRatesForLot(parkingLotId)
                val rates = rateResult.getOrNull() ?: emptyList()
                val applicableRate = if (user != null && user.isVIP) {
                    rates.find { it.rateType == "VIP" } ?: rates.find { it.rateType == "NORMAL" }
                } else {
                    rates.find { it.rateType == "NORMAL" }
                }

                val transaction = ParkingTransaction(
                    parkingLotId = parkingLotId,
                    vehicleId = vehicleId,
                    ownerId = vehicle.ownerId,
                    vehicleNumber = vehicleNumber,
                    entryTime = System.currentTimeMillis(),
                    rateType = applicableRate?.rateType ?: "NORMAL",
                    status = "ACTIVE"
                )

                val addResult = transactionRepository.addTransaction(transaction)
                addResult.onSuccess { transactionId ->
                    // Update parking lot availability
                    val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots - 1)
                    parkingLotRepository.updateParkingLot(updatedLot)
                    
                    val completedTransaction = transaction.copy(id = transactionId)
                    _activeParking.value = completedTransaction
                    callback(Result.success(completedTransaction))
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to save entry"
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processVehicleExit(
        vehicleId: String,
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicleId)
                val activeTransaction = activeResult.getOrNull()
                
                if (activeTransaction == null) {
                    callback(Result.failure(Exception("No active parking found for this vehicle")))
                    return@launch
                }

                val exitTime = System.currentTimeMillis()
                val duration = (exitTime - activeTransaction.entryTime) / (1000 * 60) // in minutes

                // Get user details to check VIP status
                val userResult = authRepository.getUserData(activeTransaction.ownerId)
                val user = userResult.getOrNull()

                // Get parking rate for charge calculation
                val rateResult = parkingRateRepository.getRateByType(
                    activeTransaction.parkingLotId, 
                    activeTransaction.rateType
                )
                val rate = rateResult.getOrNull()

                // Calculate charge amount
                val chargeAmount = ParkingChargeCalculator.calculateCharge(
                    startTime = activeTransaction.entryTime,
                    endTime = exitTime,
                    rate = rate,
                    isVIP = user != null && user.isVIP
                )

                val updatedTransaction = activeTransaction.copy(
                    exitTime = exitTime,
                    duration = duration,
                    chargeAmount = chargeAmount,
                    status = "COMPLETED",
                    paymentStatus = "PENDING"
                )

                val result = transactionRepository.updateTransaction(updatedTransaction)
                result.onSuccess {
                    // Update parking lot availability
                    val parkingLotResult = parkingLotRepository.getParkingLot(activeTransaction.parkingLotId)
                    val parkingLot = parkingLotResult.getOrNull()
                    if (parkingLot != null) {
                        val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots + 1)
                        parkingLotRepository.updateParkingLot(updatedLot)
                    }
                    
                    _activeParking.value = null
                    callback(Result.success(updatedTransaction))
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to process exit"
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processVehicleEntryByNumber(
        vehicleNumber: String,
        parkingLotId: String = "default_lot",
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Find vehicle by number
                val vehicleResult = vehicleRepository.getAllVehicles()
                val vehicles = vehicleResult.getOrNull() ?: emptyList()
                val vehicle = vehicles.find { 
                    it.vehicleNumber.equals(vehicleNumber, ignoreCase = true) 
                }
                
                if (vehicle == null) {
                    callback(Result.failure(Exception("Vehicle not found: $vehicleNumber")))
                    return@launch
                }
                
                // Check if already has active parking
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicle.id)
                val activeTransaction = activeResult.getOrNull()
                if (activeTransaction != null) {
                    // Vehicle already has active parking
                    callback(Result.success(activeTransaction))
                    return@launch
                }
                
                // No active parking, proceed with entry
                
                // Get parking lot details to update availability
                val parkingLotResult = parkingLotRepository.getParkingLot(parkingLotId)
                val parkingLot = parkingLotResult.getOrNull()
                if (parkingLot == null) {
                    callback(Result.failure(Exception("Parking lot not found")))
                    return@launch
                }

                // Check if parking is available
                if (parkingLot.availableSpots <= 0) {
                    callback(Result.failure(Exception("No available parking spots")))
                    return@launch
                }

                // Get user details to check VIP status
                val userResult = authRepository.getUserData(vehicle.ownerId)
                val user = userResult.getOrNull()

                // Get applicable rate
                val rateResult = parkingRateRepository.getActiveRatesForLot(parkingLotId)
                val rates = rateResult.getOrNull() ?: emptyList()
                val applicableRate = if (user != null && user.isVIP) {
                    rates.find { it.rateType == "VIP" } ?: rates.find { it.rateType == "NORMAL" }
                } else {
                    rates.find { it.rateType == "NORMAL" }
                }

                val transaction = ParkingTransaction(
                    parkingLotId = parkingLotId,
                    vehicleId = vehicle.id,
                    ownerId = vehicle.ownerId,
                    vehicleNumber = vehicle.vehicleNumber,
                    entryTime = System.currentTimeMillis(),
                    rateType = applicableRate?.rateType ?: "NORMAL",
                    status = "ACTIVE"
                )

                val addResult = transactionRepository.addTransaction(transaction)
                addResult.onSuccess { transactionId ->
                    // Update parking lot availability
                    val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots - 1)
                    parkingLotRepository.updateParkingLot(updatedLot)
                    
                    val completedTransaction = transaction.copy(id = transactionId)
                    _activeParking.value = completedTransaction
                    callback(Result.success(completedTransaction))
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to save entry"
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processVehicleExitByNumber(
        vehicleNumber: String,
        callback: (Result<ParkingTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Find vehicle by number
                val vehicleResult = vehicleRepository.getAllVehicles()
                val vehicles = vehicleResult.getOrNull() ?: emptyList()
                val vehicle = vehicles.find { 
                    it.vehicleNumber.equals(vehicleNumber, ignoreCase = true) 
                }
                
                if (vehicle == null) {
                    callback(Result.failure(Exception("Vehicle not found: $vehicleNumber")))
                    return@launch
                }
                
                val activeResult = transactionRepository.getActiveParkingByVehicle(vehicle.id)
                val activeTransaction = activeResult.getOrNull()
                
                if (activeTransaction == null) {
                    callback(Result.failure(Exception("No active parking found for this vehicle")))
                    return@launch
                }

                val exitTime = System.currentTimeMillis()
                val duration = (exitTime - activeTransaction.entryTime) / (1000 * 60) // in minutes

                // Get user details to check VIP status
                val userResult = authRepository.getUserData(activeTransaction.ownerId)
                val user = userResult.getOrNull()

                // Get parking rate for charge calculation
                val rateResult = parkingRateRepository.getRateByType(
                    activeTransaction.parkingLotId, 
                    activeTransaction.rateType
                )
                val rate = rateResult.getOrNull()

                // Calculate charge amount
                val chargeAmount = ParkingChargeCalculator.calculateCharge(
                    startTime = activeTransaction.entryTime,
                    endTime = exitTime,
                    rate = rate,
                    isVIP = user != null && user.isVIP
                )

                val updatedTransaction = activeTransaction.copy(
                    exitTime = exitTime,
                    duration = duration,
                    chargeAmount = chargeAmount,
                    status = "COMPLETED",
                    paymentStatus = "PENDING"
                )

                val result = transactionRepository.updateTransaction(updatedTransaction)
                result.onSuccess {
                    // Update parking lot availability
                    val parkingLotResult = parkingLotRepository.getParkingLot(activeTransaction.parkingLotId)
                    val parkingLot = parkingLotResult.getOrNull()
                    if (parkingLot != null) {
                        val updatedLot = parkingLot.copy(availableSpots = parkingLot.availableSpots + 1)
                        parkingLotRepository.updateParkingLot(updatedLot)
                    }
                    
                    _activeParking.value = null
                    callback(Result.success(updatedTransaction))
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to process exit"
                    callback(Result.failure(error))
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                callback(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getActiveParkingForVehicle(vehicleId: String) {
        viewModelScope.launch {
            val result = transactionRepository.getActiveParkingByVehicle(vehicleId)
            result.onSuccess { transaction ->
                _activeParking.value = transaction
            }
        }
    }

    fun refreshTransactions() {
        loadOwnerTransactions()
    }
}