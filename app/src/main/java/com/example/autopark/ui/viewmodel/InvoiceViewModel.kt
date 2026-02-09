package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.Invoice
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val authRepository: AuthRepository,
    private val invoiceGenerationService: com.example.autopark.data.repository.InvoiceGenerationService
) : ViewModel() {

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<Invoice?>(null)
    val selectedInvoice: StateFlow<Invoice?> = _selectedInvoice.asStateFlow()

    private val _pendingInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    val pendingInvoices: StateFlow<List<Invoice>> = _pendingInvoices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadOwnerInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = invoiceRepository.getOwnerInvoices(userId)
                result.onSuccess { inv ->
                    _invoices.value = inv
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load invoices"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadPendingInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = invoiceRepository.getPendingInvoices(userId)
                result.onSuccess { inv ->
                    _pendingInvoices.value = inv
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load pending invoices"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadMonthlyInvoice(month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = invoiceRepository.getMonthlyInvoice(userId, month, year)
                result.onSuccess { inv ->
                    _selectedInvoice.value = inv
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load invoice"
                }
            }
            _isLoading.value = false
        }
    }

    fun selectInvoice(invoice: Invoice) {
        _selectedInvoice.value = invoice
    }

    fun updateInvoice(invoice: Invoice) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = invoiceRepository.updateInvoice(invoice)
            result.onSuccess {
                loadOwnerInvoices()
                loadPendingInvoices()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update invoice"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Generate monthly invoice for current user
     */
    fun generateMonthlyInvoice(month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = invoiceGenerationService.generateUserMonthlyInvoice(userId, month, year)
                result.onSuccess { invoice ->
                    loadOwnerInvoices()
                    _selectedInvoice.value = invoice
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to generate invoice"
                }
            } else {
                _errorMessage.value = "User not authenticated"
            }
            _isLoading.value = false
        }
    }

    /**
     * Generate current month invoice
     */
    fun generateCurrentMonthInvoice() {
        val (month, year) = com.example.autopark.util.DateFormatter.getMonthAndYear()
        generateMonthlyInvoice(month, year)
    }

    /**
     * Pay invoice (mark as paid)
     */
    fun markInvoiceAsPaid(invoice: Invoice, paymentAmount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val updatedInvoice = invoice.copy(
                paymentStatus = "PAID",
                paymentDate = System.currentTimeMillis(),
                amountPaid = paymentAmount
            )
            
            val result = invoiceRepository.updateInvoice(updatedInvoice)
            result.onSuccess {
                loadOwnerInvoices()
                loadPendingInvoices()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update invoice"
            }
            _isLoading.value = false
        }
    }

    /**
     * Get pending amount for user
     */
    fun getTotalPendingAmount(): Double {
        return _pendingInvoices.value.sumOf { it.totalAmount - it.amountPaid }
    }
}
