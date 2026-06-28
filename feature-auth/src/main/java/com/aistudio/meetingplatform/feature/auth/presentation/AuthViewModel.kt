package com.aistudio.meetingplatform.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.auth.domain.repository.AuthRepository
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

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onGuestToggle(isGuest: Boolean) {
        _uiState.value = _uiState.value.copy(isGuest = isGuest)
    }

    fun toggleLoginMode() {
        _uiState.value = _uiState.value.copy(isLoginMode = !_uiState.value.isLoginMode)
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            // Simulated login/register process
            authRepository.login(state.name, state.email, state.isGuest)
            _uiState.value = state.copy(isLoading = false, isSuccess = true)
        }
    }
}

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isGuest: Boolean = true,
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)
