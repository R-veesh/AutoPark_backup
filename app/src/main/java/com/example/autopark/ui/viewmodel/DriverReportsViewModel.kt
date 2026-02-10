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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
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

    // Summary statistics derived from transactions
    val totalCharges: StateFlow<Double> = _transactions.map { list ->
        list.sumOf { it.chargeAmount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val averageCharge: StateFlow<Double> = _transactions.map { list ->
        if (list.isNotEmpty()) list.sumOf { it.chargeAmount } / list.size else 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val completedTransactionsCount: StateFlow<Int> = _transactions.map { list ->
        list.count { it.status == "COMPLETED" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val activeTransactionsCount: StateFlow<Int> = _transactions.map { list ->
        list.count { it.status == "ACTIVE" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
    private var hasLoaded = false

    init {
        // Listen to auth state changes and load data when authenticated
        viewModelScope.launch {
            authRepository.getAuthState().collect { isAuthenticated ->
                if (isAuthenticated && !hasLoaded) {
                    Log.d("DriverReportsVM", "User authenticated, loading data...")
                    hasLoaded = true
                    loadDriverData()
                } else if (!isAuthenticated) {
                    Log.d("DriverReportsVM", "User not authenticated")
                    hasLoaded = false
                    // Clear data when logged out
                    _driver.value = null
                    _vehicles.value = emptyList()
                    _transactions.value = emptyList()
                }
            }
        }
    }

    private fun loadDriverData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Small delay to ensure Firebase Auth is fully initialized
            kotlinx.coroutines.delay(500)
            
            val userId = authRepository.getCurrentUserId()
            
            if (userId != null) {
                Log.d("DriverReportsVM", "Loading data for user: $userId")
                // Load driver info
                val userResult = authRepository.getUserData(userId)
                userResult.onSuccess { user ->
                    _driver.value = user
                    setupRealTimeListeners(userId)
                }.onFailure { error ->
                    Log.e("DriverReportsVM", "Failed to load driver data: ${error.message}")
                    _errorMessage.value = error.message ?: "Failed to load driver data"
                    _isLoading.value = false
                }
            } else {
                Log.e("DriverReportsVM", "User ID is null after auth state said authenticated")
                // Don't show error immediately, retry once
                kotlinx.coroutines.delay(1000)
                val retryUserId = authRepository.getCurrentUserId()
                if (retryUserId != null) {
                    loadDriverData()
                } else {
                    _errorMessage.value = "User not authenticated. Please login again."
                    _isLoading.value = false
                }
            }
        }
    }

    private fun setupRealTimeListeners(userId: String) {
        try {
            // Remove existing listeners
            vehiclesListener?.remove()
            transactionsListener?.remove()

            // Set up real-time listener for vehicles
            vehiclesListener = db.collection("vehicles")
                .whereEqualTo("ownerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _errorMessage.value = error.message ?: "Failed to load vehicles"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val vehiclesList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
                        }
                        _vehicles.value = vehiclesList
                        _isLoading.value = false
                    }
                }

            // Set up real-time listener for transactions (no composite index needed)
            transactionsListener = db.collection("parking_transactions")
                .whereEqualTo("ownerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _errorMessage.value = "Failed to load transactions: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val transactionsList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ParkingTransaction::class.java)?.apply { id = doc.id }
                        }.sortedByDescending { it.entryTime }
                        _transactions.value = transactionsList
                        _isLoading.value = false
                    }
                }
        } catch (e: Exception) {
            _errorMessage.value = "Error setting up data listeners: ${e.message}"
            _isLoading.value = false
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
