package com.example.autopark.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.model.User
import com.example.autopark.data.model.Vehicle
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.ParkingTransactionRepository
import com.example.autopark.data.repository.VehicleRepository
import com.example.autopark.util.PDFGenerator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverReportsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val transactionRepository: ParkingTransactionRepository,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _driver = MutableStateFlow<User?>(null)
    val driver: StateFlow<User?> = _driver.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _transactions = MutableStateFlow<List<ParkingTransaction>>(emptyList())
    val transactions: StateFlow<List<ParkingTransaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGeneratingPDF = MutableStateFlow(false)
    val isGeneratingPDF: StateFlow<Boolean> = _isGeneratingPDF.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _pdfGenerationResult = MutableStateFlow<String?>(null)
    val pdfGenerationResult: StateFlow<String?> = _pdfGenerationResult.asStateFlow()

    private var vehiclesListener: ListenerRegistration? = null
    private var transactionsListener: ListenerRegistration? = null

    init {
        loadDriverData()
    }

    private fun loadDriverData() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            
            if (userId != null) {
                // Load driver info
                val userResult = authRepository.getUserData(userId)
                userResult.onSuccess { user ->
                    _driver.value = user
                    setupRealTimeListeners(userId)
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load driver data"
                }
            } else {
                _errorMessage.value = "User not authenticated"
            }
            _isLoading.value = false
        }
    }

    private fun setupRealTimeListeners(userId: String) {
        // Remove existing listeners
        vehiclesListener?.remove()
        transactionsListener?.remove()

        // Set up real-time listener for vehicles
        vehiclesListener = db.collection("vehicles")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message ?: "Failed to load vehicles"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val vehiclesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
                    }
                    _vehicles.value = vehiclesList
                }
            }

        // Set up real-time listener for transactions
        transactionsListener = db.collection("parking_transactions")
            .whereEqualTo("ownerId", userId)
            .orderBy("entryTime")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message ?: "Failed to load transactions"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactionsList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ParkingTransaction::class.java)?.apply { id = doc.id }
                    }
                    _transactions.value = transactionsList
                }
            }
    }

    fun generatePDFReport(uri: Uri) {
        viewModelScope.launch {
            _isGeneratingPDF.value = true
            _pdfGenerationResult.value = null
            
            val currentDriver = _driver.value
            val currentVehicles = _vehicles.value
            val currentTransactions = _transactions.value
            
            if (currentDriver != null) {
                val result = PDFGenerator.generateDriverReport(
                    context = context,
                    uri = uri,
                    driver = currentDriver,
                    vehicles = currentVehicles,
                    transactions = currentTransactions
                )
                
                result.onSuccess { message ->
                    _pdfGenerationResult.value = message
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to generate PDF"
                }
            } else {
                _errorMessage.value = "Driver data not available"
            }
            
            _isGeneratingPDF.value = false
        }
    }

    fun calculateTotalCharges(): Double {
        return _transactions.value.sumOf { it.chargeAmount }
    }

    fun calculateAverageCharge(): Double {
        val transactions = _transactions.value
        return if (transactions.isNotEmpty()) {
            transactions.sumOf { it.chargeAmount } / transactions.size
        } else {
            0.0
        }
    }

    fun getCompletedTransactionsCount(): Int {
        return _transactions.value.count { it.status == "COMPLETED" }
    }

    fun getActiveTransactionsCount(): Int {
        return _transactions.value.count { it.status == "ACTIVE" }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearPDFResult() {
        _pdfGenerationResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        vehiclesListener?.remove()
        transactionsListener?.remove()
    }
}
