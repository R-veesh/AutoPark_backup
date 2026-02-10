package com.example.autopark.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.User
import com.example.autopark.data.repository.AuthRepository
import com.example.autopark.util.TimestampUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var usersListener: ListenerRegistration? = null

    init {
        loadAllUsersRealtime()
    }

    private fun parseUserFromDocument(docId: String, data: Map<String, Any?>?): User? {
        if (data == null) return null
        return try {
            User(
                id = docId,
                email = data["email"] as? String ?: "",
                role = data["role"] as? String ?: "",
                name = data["name"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                address = data["address"] as? String ?: "",
                city = data["city"] as? String ?: "",
                state = data["state"] as? String ?: "",
                zipCode = data["zipCode"] as? String ?: "",
                licenseNumber = data["licenseNumber"] as? String ?: "",
                licenseExpiry = (data["licenseExpiry"] as? Number)?.toLong() ?: 0,
                profilePictureUrl = data["profilePictureUrl"] as? String,
                parkingLicenseValid = data["parkingLicenseValid"] as? Boolean ?: true,
                createdAt = TimestampUtils.toMillis(data["createdAt"]),
                updatedAt = TimestampUtils.toMillis(data["updatedAt"]),
                lastLoginAt = TimestampUtils.toMillis(data["lastLoginAt"]),
                isVIP = data["isVIP"] as? Boolean ?: false,
                totalSpent = (data["totalSpent"] as? Number)?.toDouble() ?: 0.0,
                totalParkings = (data["totalParkings"] as? Number)?.toInt() ?: 0,
                totalCharges = (data["totalCharges"] as? Number)?.toDouble() ?: 0.0,
                loyaltyPoints = (data["loyaltyPoints"] as? Number)?.toInt() ?: 0,
                preferredParkingLot = data["preferredParkingLot"] as? String,
                notificationSettings = (data["notificationSettings"] as? Map<String, Boolean>) ?: mapOf(
                    "parking_reminders" to true,
                    "payment_reminders" to true,
                    "promotional_notifications" to false
                )
            )
        } catch (e: Exception) {
            Log.e("UserManagement", "Error parsing user: ${e.message}")
            null
        }
    }

    /**
     * Load all drivers (one-time fetch for compatibility)
     */
    fun loadAllDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docs = db.collection("users")
                    .whereEqualTo("role", "driver")
                    .get()
                    .await()
                
                val users = docs.documents.mapNotNull { doc ->
                    parseUserFromDocument(doc.id, doc.data)
                }
                
                _drivers.value = users
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to load drivers: ${e.message}")
                _errorMessage.value = "Failed to load drivers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all users with real-time updates
     */
    fun loadAllUsersRealtime() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Remove previous listener
            usersListener?.remove()
            
            // Set up real-time listener
            usersListener = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("UserManagement", "Listen failed: ${error.message}")
                        _errorMessage.value = "Failed to load users: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val usersList = snapshot.documents.mapNotNull { doc ->
                            parseUserFromDocument(doc.id, doc.data)
                        }
                        
                        _users.value = usersList
                        _drivers.value = usersList.filter { it.role == "driver" }
                        _errorMessage.value = null
                        Log.d("UserManagement", "Loaded ${usersList.size} users")
                    }
                    _isLoading.value = false
                }
        }
    }

    /**
     * Load all users (one-time fetch for compatibility)
     */
    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docs = db.collection("users")
                    .get()
                    .await()
                
                val users = docs.documents.mapNotNull { doc ->
                    parseUserFromDocument(doc.id, doc.data)
                }
                
                _users.value = users
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to load users: ${e.message}")
                _errorMessage.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If user has an ID, update existing; otherwise create new
                if (user.id.isNotBlank()) {
                    db.collection("users").document(user.id).set(user).await()
                } else {
                    db.collection("users").add(user).await()
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to add user: ${e.message}")
                _errorMessage.value = "Failed to add user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("users").document(user.id).set(user).await()
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to update user: ${e.message}")
                _errorMessage.value = "Failed to update user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("users").document(userId).delete().await()
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to delete user: ${e.message}")
                _errorMessage.value = "Failed to delete user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleVIPStatus(userId: String, isVIP: Boolean) {
        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .update("isVIP", isVIP)
                    .await()
            } catch (e: Exception) {
                Log.e("UserManagement", "Failed to update VIP status: ${e.message}")
                _errorMessage.value = "Failed to update VIP status: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        usersListener?.remove()
    }
}
