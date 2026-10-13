package com.vintageflow.app.data.repository

import com.vintageflow.app.domain.PerfilUsuario

/**
 * Contrato de autenticación.
 *
 * Misma convención que PrendaRepository: interfaz aquí, implementación
 * aparte. Permite sustituir Firebase por una implementación falsa en las
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

    suspend fun perfilActual(): Result<PerfilUsuario>?

    fun cerrarSesion()
}