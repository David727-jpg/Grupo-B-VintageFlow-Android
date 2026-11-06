package com.vintageflow.app.data.model

object EstadosReserva {
    const val PENDIENTE = "PENDIENTE"
    const val APROBADA = "APROBADA"
    const val CANCELADA = "CANCELADA"
}

/**
 * Modelo de reserva persistido en Firestore y vinculado al usuario autenticado (clienteUid).
 */
data class Reserva(
    val id: String = "",
    val prendaId: String = "",
    val prendaNombre: String = "",
    val clienteUid: String = "",
    val clienteNombre: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = EstadosReserva.PENDIENTE
)