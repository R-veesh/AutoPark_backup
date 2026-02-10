package com.example.autopark.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.QRCodeData
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.util.QRCodeConfig
import com.example.autopark.util.QRCodeGenerator
import com.example.autopark.util.QRCodeValidator
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QRGenerationState {
    IDLE,
    GENERATING,
    GENERATED,
    EXPIRED,
    ERROR
}

@HiltViewModel
class DriverQRViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val db: FirebaseFirestore
) : ViewModel() {
    
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()
    
    private val _qrCodeData = MutableStateFlow<QRCodeData?>(null)
    val qrCodeData: StateFlow<QRCodeData?> = _qrCodeData.asStateFlow()
    
    private val _qrCountdown = MutableStateFlow(30)
    val qrCountdown: StateFlow<Int> = _qrCountdown.asStateFlow()
    
    private val _generationState = MutableStateFlow(QRGenerationState.IDLE)
    val generationState: StateFlow<QRGenerationState> = _generationState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var qrRefreshJob: Job? = null
    
    /**
     * Generate QR code for parking entry/exit
     * @param userId User ID
     * @param vehicleNumber Vehicle number
     * @param qrType "ENTRY" or "EXIT" - determines the type of QR code generated
     */
    fun generateQRCode(userId: String, vehicleNumber: String, qrType: String = "ENTRY") {
        viewModelScope.launch {
            _isLoading.value = true
            _generationState.value = QRGenerationState.GENERATING
            _errorMessage.value = null
            
            try {
                // Create QR data with security hash
                val qrData = QRCodeGenerator.createQRCodeData(
                    userId = userId,
                    vehicleNumber = vehicleNumber,
                    qrType = qrType
                )
                
                // Generate QR string
                val qrString = qrData.toQRString()
                _qrCodeData.value = qrData
                
                // Generate bitmap
                val bitmap = QRCodeGenerator.generateQRCode(qrString, size = 512)
                if (bitmap != null) {
                    _qrCodeBitmap.value = bitmap
                    _generationState.value = QRGenerationState.GENERATED
                    
                    // Start countdown timer
                    startQRRefreshTimer()
                    
                    Log.d("DriverQRViewModel", "QR code generated successfully for vehicle: $vehicleNumber")
                } else {
                    _generationState.value = QRGenerationState.ERROR
                    _errorMessage.value = "Failed to generate QR code bitmap"
                    Log.e("DriverQRViewModel", "Failed to generate QR code bitmap")
                }
                
            } catch (e: Exception) {
                _generationState.value = QRGenerationState.ERROR
                _errorMessage.value = "Error generating QR code: ${e.message}"
                Log.e("DriverQRViewModel", "Error generating QR code", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start QR code refresh timer (30 second countdown)
     */
    private fun startQRRefreshTimer() {
        qrRefreshJob?.cancel()
        
        qrRefreshJob = viewModelScope.launch {
            var countdown = 30
            
            while (countdown > 0) {
                _qrCountdown.value = countdown
                delay(1000)
                countdown--
            }
            
            // Mark as expired when timer reaches 0
            _qrCountdown.value = 0
            _generationState.value = QRGenerationState.EXPIRED
            Log.d("DriverQRViewModel", "QR code expired")
        }
    }
    
    /**
     * Reset and close QR dialog
     */
    fun resetQRCode() {
        qrRefreshJob?.cancel()
        _qrCodeBitmap.value = null
        _qrCodeData.value = null
        _qrCountdown.value = 30
        _generationState.value = QRGenerationState.IDLE
        _errorMessage.value = null
    }
    
    /**
     * Validate a scanned QR code
     */
    fun validateQRCode(qrString: String): Boolean {
        val (result, _) = QRCodeValidator.validateAndParseQRCode(qrString)
        return result is com.example.autopark.util.ValidationResult.Valid
    }
    
    override fun onCleared() {
        super.onCleared()
        qrRefreshJob?.cancel()
    }
}
