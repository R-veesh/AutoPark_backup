package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _monthlyReport = MutableStateFlow<Map<String, Any>>(emptyMap())
    val monthlyReport: StateFlow<Map<String, Any>> = _monthlyReport.asStateFlow()

    private val _revenueStats = MutableStateFlow<Map<String, Double>>(emptyMap())
    val revenueStats: StateFlow<Map<String, Double>> = _revenueStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun generateMonthlyReport(month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = reportRepository.generateMonthlyReport(month, year)
            result.onSuccess { report ->
                _monthlyReport.value = mapOf(
                    "month" to report.month,
                    "year" to report.year,
                    "totalParkings" to report.totalParkings,
                    "totalRevenue" to report.totalRevenue,
                    "totalOwners" to report.totalOwners,
                    "totalVehicles" to report.totalVehicles,
                    "averageCharge" to report.averageChargePerParking
                )
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to generate report"
            }
            _isLoading.value = false
        }
    }

    fun generateLotMonthlyReport(lotId: String, month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = reportRepository.generateLotMonthlyReport(lotId, month, year)
            result.onSuccess { report ->
                _monthlyReport.value = mapOf(
                    "month" to report.month,
                    "year" to report.year,
                    "totalParkings" to report.totalParkings,
                    "totalRevenue" to report.totalRevenue,
                    "totalOwners" to report.totalOwners,
                    "totalVehicles" to report.totalVehicles,
                    "averageCharge" to report.averageChargePerParking
                )
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to generate report"
            }
            _isLoading.value = false
        }
    }

    fun loadRevenueStats() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = reportRepository.getRevenueStats()
            result.onSuccess { stats ->
                _revenueStats.value = stats
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load revenue stats"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
