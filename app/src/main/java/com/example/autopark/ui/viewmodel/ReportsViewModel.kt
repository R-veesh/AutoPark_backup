package com.example.autopark.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.model.User
import com.example.autopark.data.repository.ParkingLotRepository
import com.example.autopark.data.repository.ParkingTransactionRepository
import com.example.autopark.data.repository.ReportRepository
import com.example.autopark.data.repository.UserManagementRepository
import com.example.autopark.util.AdminReportData
import com.example.autopark.util.LotPerformance
import com.example.autopark.util.PDFGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val transactionRepository: ParkingTransactionRepository,
    private val parkingLotRepository: ParkingLotRepository,
    private val userRepository: UserManagementRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _monthlyReport = MutableStateFlow<Map<String, Any>>(emptyMap())
    val monthlyReport: StateFlow<Map<String, Any>> = _monthlyReport.asStateFlow()

    private val _revenueStats = MutableStateFlow<Map<String, Double>>(emptyMap())
    val revenueStats: StateFlow<Map<String, Double>> = _revenueStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGeneratingPDF = MutableStateFlow(false)
    val isGeneratingPDF: StateFlow<Boolean> = _isGeneratingPDF.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _pdfGenerationResult = MutableStateFlow<String?>(null)
    val pdfGenerationResult: StateFlow<String?> = _pdfGenerationResult.asStateFlow()

    // Store current report data for PDF generation
    private var currentReportData: AdminReportData? = null

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

    /**
     * Generate comprehensive admin report with all data for PDF export
     */
    fun generateAdminReportForExport(period: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Fetch all required data with individual error handling
                val transactionsResult = transactionRepository.getAllTransactions()
                val lotsResult = parkingLotRepository.getAllParkingLots()
                val usersResult = userRepository.getAllUsers()
                val revenueResult = reportRepository.getRevenueStats()
                
                // Check for errors in each request
                var errorMessages = mutableListOf<String>()
                
                transactionsResult.onFailure { errorMessages.add("Transactions: ${it.message}") }
                lotsResult.onFailure { errorMessages.add("Parking Lots: ${it.message}") }
                usersResult.onFailure { errorMessages.add("Users: ${it.message}") }
                revenueResult.onFailure { errorMessages.add("Revenue: ${it.message}") }
                
                if (errorMessages.isNotEmpty()) {
                    _errorMessage.value = "Failed to fetch: ${errorMessages.joinToString(", ")}"
                    _isLoading.value = false
                    return@launch
                }
                
                val transactions = transactionsResult.getOrDefault(emptyList())
                val lots = lotsResult.getOrDefault(emptyList())
                val users = usersResult.getOrDefault(emptyList())
                val revenueStats = revenueResult.getOrDefault(emptyMap())
                
                // Calculate statistics
                val totalRevenue = revenueStats["total"] ?: 0.0
                val normalRevenue = revenueStats["normal"] ?: 0.0
                val vipRevenue = revenueStats["vip"] ?: 0.0
                val completedTransactions = transactions.count { it.status == "COMPLETED" }
                val averageCharge = if (transactions.isNotEmpty()) {
                    totalRevenue / transactions.size
                } else 0.0
                
                // Calculate lot performance
                val lotPerformance = lots.map { lot ->
                    val lotTransactions = transactions.filter { it.parkingLotId == lot.id }
                    val lotRevenue = lotTransactions.sumOf { it.chargeAmount }
                    val occupancyRate = if (lot.totalSpots > 0) {
                        ((lot.totalSpots - lot.availableSpots) * 100) / lot.totalSpots
                    } else 0
                    
                    LotPerformance(
                        name = lot.name,
                        totalSpots = lot.totalSpots,
                        availableSpots = lot.availableSpots,
                        occupancyRate = occupancyRate,
                        revenue = lotRevenue
                    )
                }
                
                currentReportData = AdminReportData(
                    period = period,
                    totalUsers = users.size,
                    totalVehicles = users.sumOf { it.totalParkings },
                    totalParkingLots = lots.size,
                    totalParkingSpots = lots.sumOf { it.totalSpots },
                    availableSpots = lots.sumOf { it.availableSpots },
                    totalRevenue = totalRevenue,
                    normalRevenue = normalRevenue,
                    vipRevenue = vipRevenue,
                    averageCharge = averageCharge,
                    totalTransactions = transactions.size,
                    completedTransactions = completedTransactions,
                    lotPerformance = lotPerformance,
                    recentTransactions = transactions.sortedByDescending { it.entryTime }
                )
                
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error generating report: ${e.message}"
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Generate PDF from current report data
     */
    fun generatePDFReport(uri: Uri) {
        viewModelScope.launch {
            _isGeneratingPDF.value = true
            _pdfGenerationResult.value = null
            
            val reportData = currentReportData
            if (reportData != null) {
                val result = PDFGenerator.generateAdminReport(
                    context = context,
                    uri = uri,
                    reportData = reportData
                )
                
                result.onSuccess { message ->
                    _pdfGenerationResult.value = message
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to generate PDF"
                }
            } else {
                _errorMessage.value = "No report data available. Generate report first."
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
}
