package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopark.data.model.User
import com.example.autopark.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { isAuthenticated ->
                if (isAuthenticated) {
                    loadCurrentUser()
                } else {
                    _currentUser.value = null
                    _uiState.value = AuthUiState.NotAuthenticated
                }
            }
        }
    }

    private suspend fun loadCurrentUser() {
        val user = authRepository.getCurrentUserData()
        _currentUser.value = user
        if (user != null) {
            _uiState.value = AuthUiState.Authenticated(user)
        } else {
            _uiState.value = AuthUiState.NotAuthenticated
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        role: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(email, password, name, phoneNumber, role)
            result.onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // The AuthStateListener in init will handle state updates
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Initial
    }
}

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    object NotAuthenticated : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}