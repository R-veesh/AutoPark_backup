package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.Vehicle
import com.example.autopark.data.repository.AuthRepository
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
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: com.example.autopark.data.repository.VehicleRepository,
    private val authRepository: AuthRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        loadOwnerVehicles()
    }

    private fun parseVehicleFromDocument(docId: String, data: Map<String, Any?>?): Vehicle? {
        if (data == null) return null
        return try {
            Vehicle(
                id = docId,
                ownerId = data["ownerId"] as? String ?: "",
                vehicleNumber = data["vehicleNumber"] as? String ?: "",
                vehicleType = data["vehicleType"] as? String ?: "",
                color = data["color"] as? String ?: "",
                brand = data["brand"] as? String ?: "",
                model = data["model"] as? String ?: "",
                parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                registrationExpiry = (data["registrationExpiry"] as? Number)?.toLong() ?: 0,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"])
            )
        } catch (e: Exception) {
            Log.e("VehicleViewModel", "Error parsing vehicle: ${e.message}")
            null
        }
    }

    fun loadOwnerVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                // Remove previous listener if exists
                listenerRegistration?.remove()
                
                // Set up real-time listener
                listenerRegistration = db.collection("vehicles")
                    .whereEqualTo("ownerId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("VehicleViewModel", "Listen failed", error)
                            _errorMessage.value = error.message ?: "Failed to load vehicles"
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val vehicles = snapshot.documents.mapNotNull { doc ->
                                parseVehicleFromDocument(doc.id, doc.data)
                            }
                            _vehicles.value = vehicles
                            _errorMessage.value = null
                            Log.d("VehicleViewModel", "Loaded ${vehicles.size} vehicles for user $userId")
                        }
                        _isLoading.value = false
                    }
            } else {
                _errorMessage.value = "User not authenticated"
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val vehicleWithOwner = vehicle.copy(ownerId = userId)
                val result = vehicleRepository.addVehicle(vehicleWithOwner)
                result.onSuccess {
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to add vehicle"
                }
            } else {
                _errorMessage.value = "User not authenticated"
            }
            _isLoading.value = false
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vehicleRepository.updateVehicle(vehicle)
            result.onSuccess {
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to update vehicle"
            }
            _isLoading.value = false
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = vehicleRepository.deleteVehicle(vehicleId)
            result.onSuccess {
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete vehicle"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun loadAllVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Remove previous listener if exists
                listenerRegistration?.remove()
                
                // Set up real-time listener for all vehicles (admin only)
                listenerRegistration = db.collection("vehicles")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("VehicleViewModel", "Listen failed", error)
                            _errorMessage.value = error.message ?: "Failed to load vehicles"
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val vehicles = snapshot.documents.mapNotNull { doc ->
                                parseVehicleFromDocument(doc.id, doc.data)
                            }
                            _vehicles.value = vehicles
                            _errorMessage.value = null
                            Log.d("VehicleViewModel", "Loaded ${vehicles.size} total vehicles")
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load vehicles"
                _isLoading.value = false
            }
        }
    }
}
