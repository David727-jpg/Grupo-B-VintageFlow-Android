package com.vintageflow.app.domain

import com.vintageflow.app.data.model.Usuario

/**
 * Jerarquía de perfiles o roles del sistema.
 *
 * El data class Usuario (data/model) es el DTO que viaja desde Firestore:
 * campos planos, sin comportamiento. Esta jerarquía es el modelo de dominio:
 * misma información, pero con las reglas de cada rol.
 *
 *
 */
sealed class PerfilUsuario(
    val uid: String,
    val nombre: String,
    val correo: String
) {
    abstract fun descripcion(): String
    abstract fun puedeGestionarInventario(): Boolean
}

class Administrador(
    uid: String,
    nombre: String,
    correo: String
) : PerfilUsuario(uid, nombre, correo) {

    override fun descripcion(): String = "Administrador: $nombre"

    override fun puedeGestionarInventario(): Boolean = true
}

class Cliente(
    uid: String,
    nombre: String,
    correo: String
) : PerfilUsuario(uid, nombre, correo) {

    override fun descripcion(): String = "Cliente: $nombre"

    override fun puedeGestionarInventario(): Boolean = false
}

object Roles {
    const val ADMINISTRADOR = "ADMINISTRADOR"
    const val CLIENTE = "CLIENTE"
}

/**
 * Convierte el DTO en el perfil correspondiente.
 */
fun Usuario.aPerfil(): PerfilUsuario =
    when (rol.trim().uppercase()) {
        Roles.ADMINISTRADOR -> Administrador(id, nombre, correo)
        else -> Cliente(id, nombre, correo)
    }