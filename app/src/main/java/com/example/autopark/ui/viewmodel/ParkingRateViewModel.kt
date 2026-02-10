package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingRate
import com.example.autopark.data.repository.ParkingRateRepository
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
class ParkingRateViewModel @Inject constructor(
    private val rateRepository: ParkingRateRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _ratesForLot = MutableStateFlow<List<ParkingRate>>(emptyList())
    val ratesForLot: StateFlow<List<ParkingRate>> = _ratesForLot.asStateFlow()

    private val _allRates = MutableStateFlow<List<ParkingRate>>(emptyList())
    val allRates: StateFlow<List<ParkingRate>> = _allRates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var ratesListener: ListenerRegistration? = null
    private var currentLotId: String? = null

    private fun parseParkingRateFromDocument(docId: String, data: Map<String, Any?>?): ParkingRate? {
        if (data == null) return null
        return try {
            ParkingRate(
                id = docId,
                parkingLotId = data["parkingLotId"] as? String ?: "",
                rateType = data["rateType"] as? String ?: "",
                pricePerHour = (data["pricePerHour"] as? Number)?.toDouble() ?: 0.0,
                pricePerDay = (data["pricePerDay"] as? Number)?.toDouble() ?: 0.0,
                overnightPrice = (data["overnightPrice"] as? Number)?.toDouble() ?: 0.0,
                minChargeAmount = (data["minChargeAmount"] as? Number)?.toDouble() ?: 0.0,
                maxChargePerDay = (data["maxChargePerDay"] as? Number)?.toDouble() ?: 0.0,
                isActive = data["isActive"] as? Boolean ?: true,
                vipMultiplier = (data["vipMultiplier"] as? Number)?.toDouble() ?: 1.0,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
        } catch (e: Exception) {
            Log.e("ParkingRateVM", "Error parsing parking rate: ${e.message}")
            null
        }
    }

    fun loadRatesForLot(lotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            currentLotId = lotId
            
            // Remove previous listener
            ratesListener?.remove()
            
            // Set up real-time listener
            ratesListener = db.collection("parking_rates")
                .whereEqualTo("parkingLotId", lotId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ParkingRateVM", "Listen failed: ${error.message}")
                        _errorMessage.value = error.message ?: "Failed to load rates"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val rates = snapshot.documents.mapNotNull { doc ->
                            parseParkingRateFromDocument(doc.id, doc.data)
                        }
                        _ratesForLot.value = rates
                        _errorMessage.value = null
                        Log.d("ParkingRateVM", "Loaded ${rates.size} rates for lot $lotId")
                    }
                    _isLoading.value = false
                }
        }
    }

    fun loadAllRates() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Remove previous listener
            ratesListener?.remove()
            
            // Set up real-time listener for all rates
            ratesListener = db.collection("parking_rates")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ParkingRateVM", "Listen failed: ${error.message}")
                        _errorMessage.value = error.message ?: "Failed to load rates"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val rates = snapshot.documents.mapNotNull { doc ->
                            parseParkingRateFromDocument(doc.id, doc.data)
                        }
                        _allRates.value = rates
                        _errorMessage.value = null
                        Log.d("ParkingRateVM", "Loaded ${rates.size} total rates")
                    }
                    _isLoading.value = false
                }
        }
    }

    fun addRate(rate: ParkingRate) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.addParkingRate(rate)
            result.onSuccess {
                _errorMessage.value = null
                Log.d("ParkingRateVM", "Rate added successfully")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to add rate"
                Log.e("ParkingRateVM", "Failed to add rate: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    fun updateRate(rate: ParkingRate) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.updateParkingRate(rate)
            result.onSuccess {
                _errorMessage.value = null
                Log.d("ParkingRateVM", "Rate updated successfully")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update rate"
                Log.e("ParkingRateVM", "Failed to update rate: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    fun deleteRate(rateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.deleteParkingRate(rateId)
            result.onSuccess {
                _errorMessage.value = null
                Log.d("ParkingRateVM", "Rate deleted successfully")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete rate"
                Log.e("ParkingRateVM", "Failed to delete rate: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ratesListener?.remove()
    }
}
