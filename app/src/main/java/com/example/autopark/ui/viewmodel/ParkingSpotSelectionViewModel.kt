package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.model.ParkingSpot
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.data.repository.ParkingLotRepository
import com.example.autopark.data.repository.ParkingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingSpotSelectionViewModel @Inject constructor(
    private val parkingRepository: ParkingRepository,
    private val parkingLotRepository: ParkingLotRepository,
    private val authRepository: AuthRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _parkingLot = MutableStateFlow<ParkingLot?>(null)
    val parkingLot: StateFlow<ParkingLot?> = _parkingLot.asStateFlow()

    private val _parkingSpots = MutableStateFlow<List<ParkingSpot>>(emptyList())
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots.asStateFlow()

    private val _selectedSpot = MutableStateFlow<ParkingSpot?>(null)
    val selectedSpot: StateFlow<ParkingSpot?> = _selectedSpot.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _reservationSuccess = MutableStateFlow<Boolean?>(null)
    val reservationSuccess: StateFlow<Boolean?> = _reservationSuccess.asStateFlow()

    private var spotsListener: ListenerRegistration? = null

    fun loadParkingLotAndSpots(parkingLotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Load parking lot details
                val lotResult = parkingLotRepository.getParkingLot(parkingLotId)
                lotResult.onSuccess { lot ->
                    _parkingLot.value = lot
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load parking lot"
                }

                // Set up real-time listener for spots
                setupSpotsListener(parkingLotId)
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
                _isLoading.value = false
            }
        }
    }

    private fun setupSpotsListener(parkingLotId: String) {
        spotsListener?.remove()
        
        spotsListener = db.collection("parking_spots")
            .whereEqualTo("parkingLotId", parkingLotId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message ?: "Failed to load parking spots"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val spots = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ParkingSpot::class.java)?.apply { id = doc.id }
                    }
                    _parkingSpots.value = spots
                }
                _isLoading.value = false
            }
    }

    fun selectSpot(spot: ParkingSpot) {
        _selectedSpot.value = spot
    }

    fun reserveSelectedSpot() {
        viewModelScope.launch {
            val spot = _selectedSpot.value ?: return@launch
            
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Small delay to ensure Firebase Auth is fully initialized
                kotlinx.coroutines.delay(300)
                
                var userId = authRepository.getCurrentUserId()
                
                // Retry once if userId is null
                if (userId == null) {
                    kotlinx.coroutines.delay(800)
                    userId = authRepository.getCurrentUserId()
                }
                
                if (userId == null) {
                    _errorMessage.value = "Please login to reserve a spot"
                    _isLoading.value = false
                    return@launch
                }
                
                // TODO: Get user's vehicle number from their profile
                // For now using a placeholder
                val vehicleNumber = getUserVehicleNumber(userId)
                
                val result = parkingRepository.reserveParkingSpot(
                    spotId = spot.id,
                    userId = userId,
                    vehicleNumber = vehicleNumber
                )
                
                result.onSuccess {
                    _reservationSuccess.value = true
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to reserve spot"
                    _reservationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
                _reservationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getUserVehicleNumber(userId: String): String {
        // TODO: Implement actual vehicle lookup from user's vehicles
        // For now return a placeholder
        return "TEMP-VEHICLE"
    }

    fun clearReservationSuccess() {
        _reservationSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        spotsListener?.remove()
    }
}
