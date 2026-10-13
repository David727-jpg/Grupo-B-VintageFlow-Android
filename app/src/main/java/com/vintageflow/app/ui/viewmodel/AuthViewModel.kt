package com.vintageflow.app.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.vintageflow.app.data.repository.AuthRepository
import com.vintageflow.app.data.repository.ROL_POR_DEFECTO
import com.vintageflow.app.domain.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val perfil: PerfilUsuario? = null,
    val errorGeneral: String? = null,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null
) {
    val autenticado: Boolean get() = perfil != null
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    private fun validarNombre(nombre: String): String? =
        if (nombre.isBlank()) "El nombre no puede estar vacío." else null

    private fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo no puede estar vacío."
        !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
            "El formato del correo no es válido."
        else -> null
    }

    private fun validarContrasena(contrasena: String): String? = when {
        contrasena.isBlank() -> "La contraseña no puede estar vacía."
        contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }

    // ---------- Acciones ----------

    fun iniciarSesion(correo: String, contrasena: String) {
        val eCorreo = validarCorreo(correo)
        val eContrasena = validarContrasena(contrasena)

        if (eCorreo != null || eContrasena != null) {
            _estado.update {
                it.copy(
                    errorCorreo = eCorreo,
                    errorContrasena = eContrasena,
                    errorGeneral = null
                )
            }
            return
        }

        _estado.update {
            it.copy(
                cargando = true,
                errorCorreo = null,
                errorContrasena = null,
                errorGeneral = null
            )
        }

        viewModelScope.launch {
            repository.iniciarSesion(correo, contrasena)
                .onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, errorGeneral = traducir(error)) }
                }
        }
    }

    fun registrar(nombre: String, correo: String, contrasena: String) {
        val eNombre = validarNombre(nombre)
        val eCorreo = validarCorreo(correo)
        val eContrasena = validarContrasena(contrasena)

        if (eNombre != null || eCorreo != null || eContrasena != null) {
            _estado.update {
                it.copy(
                    errorNombre = eNombre,
                    errorCorreo = eCorreo,
                    errorContrasena = eContrasena,
                    errorGeneral = null
                )
            }
            return
        }

        _estado.update {
            it.copy(
                cargando = true,
                errorNombre = null,
                errorCorreo = null,
                errorContrasena = null,
                errorGeneral = null
            )
        }

        viewModelScope.launch {
            repository.registrar(nombre, correo, contrasena, ROL_POR_DEFECTO)
                .onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, errorGeneral = traducir(error)) }
                }
        }
    }

    /** Restaura la sesión al abrir la app, si quedó abierta. */
    fun restaurarSesion() {
        if (!repository.haySesionActiva()) return
        _estado.update { it.copy(cargando = true) }
        viewModelScope.launch {
            repository.perfilActual()
                ?.onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                ?.onFailure {
                    _estado.update { it.copy(cargando = false) }
                }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = AuthUiState()
    }


    private fun traducir(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "El correo o la contraseña son incorrectos."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con ese correo."
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
        else -> error.message ?: "Ocurrió un error inesperado. Intenta de nuevo."
    }
}
