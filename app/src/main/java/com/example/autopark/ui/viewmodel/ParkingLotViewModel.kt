package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.data.repository.ParkingLotRepository
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

    private fun parseParkingLotFromDocument(docId: String, data: Map<String, Any?>?): ParkingLot? {
        if (data == null) return null
        return try {
            ParkingLot(
                id = docId,
                name = data["name"] as? String ?: "",
                address = data["address"] as? String ?: "",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                city = data["city"] as? String ?: "",
                state = data["state"] as? String ?: "",
                zipCode = data["zipCode"] as? String ?: "",
                totalSpots = (data["totalSpots"] as? Number)?.toInt() ?: 0,
                availableSpots = (data["availableSpots"] as? Number)?.toInt() ?: 0,
                description = data["description"] as? String ?: "",
                contactNumber = data["contactNumber"] as? String ?: "",
                openingTime = data["openingTime"] as? String ?: "",
                closingTime = data["closingTime"] as? String ?: "",
                is24Hours = data["is24Hours"] as? Boolean ?: false,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
        } catch (e: Exception) {
            null
        }
    }

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
                            parseParkingLotFromDocument(doc.id, doc.data)
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
