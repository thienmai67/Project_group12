package com.example.project_group12.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_group12.data.UserLocal
import com.example.project_group12.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserLocal) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(username: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(username, pass)
            result.onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Lỗi") }
        }
    }

    fun register(username: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(username, pass)
            result.onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Lỗi") }
        }
    }

    fun loginAsGuest() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.loginAsGuest()
            result.onSuccess { _authState.value = AuthState.Success(it) }
        }
    }
}