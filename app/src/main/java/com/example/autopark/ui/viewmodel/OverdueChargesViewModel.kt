package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.OverdueCharge
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.OverdueChargeRepository
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
    private val invoiceGenerationService: com.example.autopark.data.repository.InvoiceGenerationService
) : ViewModel() {

    private val _overdueCharges = MutableStateFlow<List<OverdueCharge>>(emptyList())
    val overdueCharges: StateFlow<List<OverdueCharge>> = _overdueCharges.asStateFlow()

    private val _totalOverdue = MutableStateFlow(0.0)
    val totalOverdue: StateFlow<Double> = _totalOverdue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
}
