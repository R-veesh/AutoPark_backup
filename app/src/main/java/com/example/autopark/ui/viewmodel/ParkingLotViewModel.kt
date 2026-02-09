package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.repository.ParkingLotRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParkingLotViewModel @Inject constructor(
    private val parkingLotRepository: ParkingLotRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _parkingLots = MutableStateFlow<List<ParkingLot>>(emptyList())
    val parkingLots: StateFlow<List<ParkingLot>> = _parkingLots.asStateFlow()

    private val _selectedLot = MutableStateFlow<ParkingLot?>(null)
    val selectedLot: StateFlow<ParkingLot?> = _selectedLot.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    fun loadAllParkingLots() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Remove previous listener if exists
            listenerRegistration?.remove()
            
            // Set up real-time listener
            listenerRegistration = db.collection("parking_lots")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _errorMessage.value = error.message ?: "Failed to load parking lots"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val lots = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(ParkingLot::class.java)?.apply { 
                                    id = doc.id 
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _parkingLots.value = lots
                        _errorMessage.value = null
                    }
                    _isLoading.value = false
                }
        }
    }

    fun loadParkingLotsByCity(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = parkingLotRepository.getParkingLotsByCity(city)
            result.onSuccess { lots ->
                _parkingLots.value = lots
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load parking lots"
            }
            _isLoading.value = false
        }
    }

    fun selectParkingLot(lotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = parkingLotRepository.getParkingLot(lotId)
            result.onSuccess { lot ->
                _selectedLot.value = lot
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load parking lot"
            }
            _isLoading.value = false
        }
    }

    fun addParkingLot(lot: ParkingLot) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = parkingLotRepository.addParkingLot(lot)
            result.onSuccess {
                loadAllParkingLots()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to add parking lot"
            }
            _isLoading.value = false
        }
    }

    fun updateParkingLot(lot: ParkingLot) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = parkingLotRepository.updateParkingLot(lot)
            result.onSuccess {
                loadAllParkingLots()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update parking lot"
            }
            _isLoading.value = false
        }
    }

    fun deleteParkingLot(lotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = parkingLotRepository.deleteParkingLot(lotId)
            result.onSuccess {
                loadAllParkingLots()
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete parking lot"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
