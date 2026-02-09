package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.User
import com.example.autopark.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAllDrivers()
    }

    fun loadAllDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docs = db.collection("users")
                    .whereEqualTo("role", "driver")
                    .get()
                    .await()
                
                val users = docs.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.apply { id = doc.id }
                }
                
                _drivers.value = users
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load drivers: ${e.message}"
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
                loadAllDrivers()
                _errorMessage.value = null
            } catch (e: Exception) {
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
                loadAllDrivers()
                _errorMessage.value = null
            } catch (e: Exception) {
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
                loadAllDrivers()
                _errorMessage.value = null
            } catch (e: Exception) {
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
                loadAllDrivers()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update VIP status: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
