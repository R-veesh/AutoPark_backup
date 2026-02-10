package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.OverdueChargeRepository
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverdueChargesViewModel @Inject constructor(
    private val overdueChargeRepository: OverdueChargeRepository,
    private val authRepository: AuthRepository,
    private val invoiceGenerationService: com.example.autopark.data.repository.InvoiceGenerationService,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _overdueCharges = MutableStateFlow<List<OverdueCharge>>(emptyList())
    val overdueCharges: StateFlow<List<OverdueCharge>> = _overdueCharges.asStateFlow()

    private val _totalOverdue = MutableStateFlow(0.0)
    val totalOverdue: StateFlow<Double> = _totalOverdue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    private fun parseOverdueChargeFromDocument(docId: String, data: Map<String, Any?>?): OverdueCharge? {
        if (data == null) {
            Log.w("OverdueChargesVM", "Document $docId has null data")
            return null
        }
        return try {
            Log.d("OverdueChargesVM", "Parsing document $docId with fields: ${data.keys}")
            val charge = OverdueCharge(
                id = docId,
                ownerId = data["ownerId"] as? String ?: "",
                ownerName = data["ownerName"] as? String ?: "",
                invoiceId = data["invoiceId"] as? String ?: "",
                invoiceNumber = data["invoiceNumber"] as? String ?: "",
                originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                lateFeePercentage = (data["lateFeePercentage"] as? Number)?.toDouble() ?: 0.0,
                lateFeeAmount = (data["lateFeeAmount"] as? Number)?.toDouble() ?: 0.0,
                totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                totalDueAmount = (data["totalDueAmount"] as? Number)?.toDouble() ?: 0.0,
                overdueDays = (data["overdueDays"] as? Number)?.toInt() ?: 0,
                daysOverdue = (data["daysOverdue"] as? Number)?.toInt() ?: 0,
                dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0,
                status = data["status"] as? String ?: "PENDING",
                paymentStatus = data["paymentStatus"] as? String ?: "PENDING",
                paymentDate = (data["paymentDate"] as? Number)?.toLong(),
                amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
            Log.d("OverdueChargesVM", "Successfully parsed charge for owner: ${charge.ownerName}")
            charge
        } catch (e: Exception) {
            Log.e("OverdueChargesVM", "Error parsing charge $docId: ${e.message}", e)
            null
        }
    }

    fun loadOverdueCharges() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = overdueChargeRepository.getPendingOverdueCharges(userId)
                result.onSuccess { charges ->
                    _overdueCharges.value = charges
                    _totalOverdue.value = charges.sumOf { it.totalDueAmount }
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load overdue charges"
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Load all overdue charges for admin (real-time from Firestore)
     */
    fun loadAllOverdueCharges() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Remove previous listener
            listenerRegistration?.remove()
            
            // Set up real-time listener for all overdue charges
            listenerRegistration = db.collection("overdue_charges")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("OverdueChargesVM", "Firestore error: ${error.message}")
                        _errorMessage.value = error.message ?: "Failed to load overdue charges"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d("OverdueChargesVM", "Received ${snapshot.documents.size} documents from Firestore")
                        val chargesList = snapshot.documents.mapNotNull { doc ->
                            parseOverdueChargeFromDocument(doc.id, doc.data)
                        }
                        Log.d("OverdueChargesVM", "Successfully parsed ${chargesList.size} charges")
                        _overdueCharges.value = chargesList
                        _totalOverdue.value = chargesList
                            .filter { it.status == "PENDING" }
                            .sumOf { it.totalAmount }
                        _errorMessage.value = null
                    } else {
                        Log.w("OverdueChargesVM", "Snapshot is null")
                    }
                    _isLoading.value = false
                }
        }
    }

    /**
     * Mark an overdue charge as paid
     */
    fun markAsPaid(chargeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val charge = _overdueCharges.value.find { it.id == chargeId }
            if (charge != null) {
                val updatedCharge = charge.copy(
                    status = "PAID",
                    paymentStatus = "COMPLETED",
                    paymentDate = System.currentTimeMillis()
                )
                
                val result = overdueChargeRepository.updateOverdueCharge(updatedCharge)
                result.onSuccess {
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to mark as paid"
                }
            }
            
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadOwnerOverdueCharges() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = overdueChargeRepository.getOwnerOverdueCharges(userId)
                result.onSuccess { charges ->
                    _overdueCharges.value = charges
                    _totalOverdue.value = charges.sumOf { it.totalDueAmount }
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load overdue charges"
                }
            } else {
                _errorMessage.value = "User not authenticated"
            }
            _isLoading.value = false
        }
    }

    fun markOverdueChargeAsPaid(chargeId: String, paymentAmount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val charge = _overdueCharges.value.find { it.id == chargeId }
            if (charge != null) {
                val updatedCharge = charge.copy(
                    paymentStatus = "COMPLETED",
                    paymentDate = System.currentTimeMillis(),
                    amountPaid = paymentAmount
                )
                
                val result = overdueChargeRepository.updateOverdueCharge(updatedCharge)
                result.onSuccess {
                    loadOverdueCharges()
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update payment status"
                }
            }
            _isLoading.value = false
        }
    }

    fun processAllOverdueInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = invoiceGenerationService.processOverdueInvoices()
            result.onSuccess { count ->
                loadOverdueCharges()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to process overdue invoices"
            }
            _isLoading.value = false
        }
    }

    /**
     * Get total overdue amount
     */
    fun getTotalOverdue(): Double {
        return _overdueCharges.value
            .filter { it.paymentStatus == "PENDING" }
            .sumOf { it.totalDueAmount - it.amountPaid }
    }

    /**
     * Get overdue charges count
     */
    fun getOverdueChargesCount(): Int {
        return _overdueCharges.value.count { it.paymentStatus == "PENDING" }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
