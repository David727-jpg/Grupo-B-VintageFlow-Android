package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.vintageflow.app.data.repository.AuthRepository
import com.vintageflow.app.data.repository.ROL_POR_DEFECTO
import com.vintageflow.app.domain.CorreoNoRegistradoException
import com.vintageflow.app.domain.GoogleCanceladoException
import com.vintageflow.app.domain.PerfilUsuario
import com.vintageflow.app.domain.ValidadorAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado completo de la pantalla en un objeto inmutable.
 * Compose observa este estado y se redibuja cuando cambia; no hay un
 */
data class AuthUiState(
    val cargando: Boolean = false,
    val perfil: PerfilUsuario? = null,
    val errorGeneral: String? = null,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val correoRecuperacionEnviado: Boolean = false
) {
    val autenticado: Boolean get() = perfil != null
}

/**
 * Orquesta el inicio de sesión y el registro.
 *
 * Las reglas de validación no están aquí: viven en
 * [com.vintageflow.app.domain.ValidadorAuth]. Este ViewModel decide *cuándo*
 * se valida y qué hacer con el resultado; *qué* es un correo válido es una
 * regla de dominio y se escribe una sola vez.
 */
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    // ---------- Acciones ----------

    fun iniciarSesion(correo: String, contrasena: String) {
        val eCorreo = ValidadorAuth.validarCorreo(correo)
        val eContrasena = ValidadorAuth.validarContrasena(contrasena)

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

        // validaba el correo recortado pero se enviaba el original, así que
        // un espacio de más pasaba la validación y fallaba en Firebase.
        val correoNormalizado = ValidadorAuth.normalizarCorreo(correo)

        viewModelScope.launch {
            repository.iniciarSesion(correoNormalizado, contrasena)
                .onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, errorGeneral = traducir(error)) }
                }
        }
    }

    fun registrar(nombre: String, correo: String, contrasena: String) {
        val eNombre = ValidadorAuth.validarNombre(nombre)
        val eCorreo = ValidadorAuth.validarCorreo(correo)
        val eContrasena = ValidadorAuth.validarContrasena(contrasena)

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

        val nombreNormalizado = ValidadorAuth.normalizarNombre(nombre)
        val correoNormalizado = ValidadorAuth.normalizarCorreo(correo)

        viewModelScope.launch {
            repository.registrar(
                nombreNormalizado,
                correoNormalizado,
                contrasena,
                ROL_POR_DEFECTO
            )
                .onSuccess { perfil ->
                    _estado.update { it.copy(cargando = false, perfil = perfil) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, errorGeneral = traducir(error)) }
                }
        }
    }

    /**
     * Envía el correo de restablecimiento de contraseña.
     *
     * Reutiliza [ValidadorAuth.validarCorreo]: es la misma regla que aplica
     * el inicio de sesión, no una copia. Si mañana cambia el criterio de
     * correo válido, cambia en un solo archivo.
     */
    fun recuperarContrasena(correo: String) {
        val eCorreo = ValidadorAuth.validarCorreo(correo)
        if (eCorreo != null) {
            _estado.update {
                it.copy(
                    errorCorreo = eCorreo,
                    errorGeneral = null,
                    correoRecuperacionEnviado = false
                )
            }
            return
        }

        _estado.update {
            it.copy(
                cargando = true,
                errorCorreo = null,
                errorGeneral = null,
                correoRecuperacionEnviado = false
            )
        }

        viewModelScope.launch {
            repository.recuperarContrasena(ValidadorAuth.normalizarCorreo(correo))
                .onSuccess {
                    _estado.update { it.copy(cargando = false, correoRecuperacionEnviado = true) }
                }
                .onFailure { error ->
                    // Un correo que no está registrado se informa igual que uno
                    // que sí lo está. Decir "no existe esa cuenta" convertiría
                    // esta pantalla en una forma de averiguar qué correos tienen
                    // cuenta en la tienda, probando uno por uno sin autenticarse.
                    if (error is CorreoNoRegistradoException) {
                        _estado.update {
                            it.copy(cargando = false, correoRecuperacionEnviado = true)
                        }
                    } else {
                        _estado.update {
                            it.copy(cargando = false, errorGeneral = traducir(error))
                        }
                    }
                }
        }
    }

    /**
     * Inicia sesión con Google.
     *
     * Recibe cómo conseguir el token, no el token ya conseguido. El
     * parámetro es una función suspendida que la pantalla provee, y es la
     * pantalla la que tiene el `Context` que Credential Manager necesita
     * para abrir su ventana.
     *
     * Eso deja este ViewModel sin ninguna referencia a Android, y permite
     * probarlo pasándole una función falsa que devuelva un token inventado
     * o un error, sin emulador y sin cuenta de Google.
     */
    fun iniciarSesionConGoogle(obtenerIdToken: suspend () -> Result<String>) {
        _estado.update {
            it.copy(
                cargando = true,
                errorCorreo = null,
                errorContrasena = null,
                errorGeneral = null
            )
        }

        viewModelScope.launch {
            obtenerIdToken()
                .onFailure { error -> manejarFalloGoogle(error) }
                .onSuccess { token ->
                    repository.iniciarSesionConGoogle(token)
                        .onSuccess { perfil ->
                            _estado.update { it.copy(cargando = false, perfil = perfil) }
                        }
                        .onFailure { error ->
                            _estado.update {
                                it.copy(cargando = false, errorGeneral = traducir(error))
                            }
                        }
                }
        }
    }

    /**
     *
     * Si el usuario cierra la ventana de Google, se apaga el indicador de
     * carga y no se muestra nada: no hizo nada incorrecto. Los demás casos
     * sí llevan mensaje.
     */
    private fun manejarFalloGoogle(error: Throwable) {
        if (error is GoogleCanceladoException) {
            _estado.update { it.copy(cargando = false) }
            return
        }
        _estado.update { it.copy(cargando = false, errorGeneral = traducir(error)) }
    }

    /**
     * Limpia errores y confirmaciones al cambiar de pantalla.
     */
    fun limpiarMensajes() {
        _estado.update {
            it.copy(
                errorGeneral = null,
                errorNombre = null,
                errorCorreo = null,
                errorContrasena = null,
                correoRecuperacionEnviado = false
            )
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

    /**
     * Firebase devuelve mensajes en inglés y poco claros para el usuario.
     */
    private fun traducir(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "El correo o la contraseña son incorrectos."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con ese correo."
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
        else -> error.message ?: "Ocurrió un error inesperado. Intenta de nuevo."
    }
}