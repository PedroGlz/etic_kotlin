package com.example.etic.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etic.data.repository.UsuarioRepository
import com.example.etic.data.local.entities.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val usuario: Usuario? = null
)

class LoginViewModel(private val repo: UsuarioRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(usuarioInput: String, passwordInput: String) {
        if (usuarioInput.isBlank() || passwordInput.isBlank()) {
            _state.value = LoginUiState(error = "Usuario y contraseña requeridos")
            return
        }
        _state.value = LoginUiState(loading = true)

        viewModelScope.launch {
            val user = repo.getByUsuario(usuarioInput)
            val ok = user?.password?.let { storedHash ->
                // storedHash ej: "$2y$10$JS8M..."
                BCrypt.verifyer()
                    .verify(passwordInput.toCharArray(), storedHash.toCharArray())
                    .verified
            } ?: false

            _state.value = if (ok) {
                LoginUiState(usuario = user)
            } else {
                LoginUiState(error = "Usuario o contraseña incorrecto")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
