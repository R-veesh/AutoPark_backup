package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingSpot
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.ParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val parkingRepository: ParkingRepository
) : ViewModel() {

    private val _parkingSpots = MutableStateFlow<List<ParkingSpot>>(emptyList())
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadParkingSpots() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val spots = parkingRepository.getParkingSpots()
                _parkingSpots.value = spots
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleParkingSpot(spotId: String, isOccupied: Boolean) {
        viewModelScope.launch {
            parkingRepository.updateParkingSpotOccupancy(spotId, isOccupied, null)
            loadParkingSpots() // Refresh the list
        }
    }

    fun occupyParkingSpot(spotId: String, vehicleNumber: String) {
        viewModelScope.launch {
            parkingRepository.updateParkingSpotOccupancy(spotId, true, vehicleNumber)
            loadParkingSpots() // Refresh the list
        }
    }

    fun logout() {
        authRepository.logout()
    }
}