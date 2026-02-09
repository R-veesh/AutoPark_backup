package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingRate
import com.example.autopark.data.repository.ParkingRateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingRateViewModel @Inject constructor(
    private val rateRepository: ParkingRateRepository
) : ViewModel() {

    private val _ratesForLot = MutableStateFlow<List<ParkingRate>>(emptyList())
    val ratesForLot: StateFlow<List<ParkingRate>> = _ratesForLot.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadRatesForLot(lotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.getRatesForLot(lotId)
            result.onSuccess { rates ->
                _ratesForLot.value = rates
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load rates"
            }
            _isLoading.value = false
        }
    }

    fun addRate(rate: ParkingRate) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.addParkingRate(rate)
            result.onSuccess {
                loadRatesForLot(rate.parkingLotId)
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to add rate"
            }
            _isLoading.value = false
        }
    }

    fun updateRate(rate: ParkingRate) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.updateParkingRate(rate)
            result.onSuccess {
                loadRatesForLot(rate.parkingLotId)
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update rate"
            }
            _isLoading.value = false
        }
    }

    fun deleteRate(rateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = rateRepository.deleteParkingRate(rateId)
            result.onSuccess {
                // Reload the rates (need to get the lot ID somehow)
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete rate"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
