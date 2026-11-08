package com.vintageflow.app.data.repository

import com.vintageflow.app.domain.PerfilUsuario

/**
 * Contrato de autenticación.
 */
interface AuthRepository {

    fun haySesionActiva(): Boolean

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<PerfilUsuario>

    suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: String
    ): Result<PerfilUsuario>

    /**
     * Autentica con el token de identidad que entregó Google.
     *
     * Recibe el token y no un `Context`: quien abre la ventana de Google es
     * `ClienteGoogle`, en la capa de interfaz. Así el repositorio sigue sin
     * depender de Android.
     */
    suspend fun iniciarSesionConGoogle(idToken: String): Result<PerfilUsuario>

    suspend fun perfilActual(): Result<PerfilUsuario>?

    /**
     * Pide a Firebase que envíe el correo de restablecimiento.
     *
     * Devuelve `Result<Unit>` porque no hay dato que recuperar: solo
     * interesa si la petición salió o falló. El repositorio informa el
     * fallo tal como ocurrió —incluido el correo no registrado— y es el
     * ViewModel el que decide qué se le muestra al usuario.
     */
    suspend fun recuperarContrasena(correo: String): Result<Unit>

    fun cerrarSesion()
}