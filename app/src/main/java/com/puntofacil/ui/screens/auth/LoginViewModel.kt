package com.puntofacil.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor ingrese usuario y contraseña"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val result = userRepository.login(username, password)
                
                when (result) {
                    is UserRepository.AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    }
                    is UserRepository.AuthResult.InvalidCredentials -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Usuario o contraseña incorrectos"
                        )
                    }
                    is UserRepository.AuthResult.UserLocked -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Usuario bloqueado. Contacte al administrador"
                        )
                    }
                    is UserRepository.AuthResult.UserInactive -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Usuario inactivo. Contacte al administrador"
                        )
                    }
                    is UserRepository.AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error de conexión. Intente nuevamente"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado. Intente nuevamente"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}