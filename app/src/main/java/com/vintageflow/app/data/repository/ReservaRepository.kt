package com.vintageflow.app.data.repository

import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.model.Reserva
import kotlinx.coroutines.flow.Flow

interface ReservaRepository {

    /** Consulta las reservas asociadas al cliente autenticado. */
    suspend fun reservasDeCliente(clienteUid: String): Result<List<Reserva>>

    /** Escucha en tiempo real las reservas de un cliente específico. */
    fun observarReservasDeCliente(clienteUid: String): Flow<List<Reserva>>

    /** Escucha en tiempo real todas las reservas (para el panel de administrador). */
    fun observarTodasLasReservas(): Flow<List<Reserva>>

    /**
     * Ejecuta la transacción atómica de reserva: verifica que la prenda esté
     * DISPONIBLE, pasa su estado a RESERVADO y registra la reserva en Firestore.
     */
    suspend fun reservar(
        prenda: Prenda,
        clienteUid: String,
        clienteNombre: String
    ): Result<Reserva>

    /**
     * Aprueba una reserva (ADMINISTRADOR): cambia la reserva a APROBADA y la
     * prenda a VENDIDO en una sola transacción.
     */
    suspend fun aprobar(reservaId: String): Result<Unit>

    /**
     * Cancela una reserva (CLIENTE o ADMINISTRADOR): cambia la reserva a CANCELADA
     * y devuelve la prenda a DISPONIBLE en una sola transacción.
     */
    suspend fun cancelar(reservaId: String): Result<Unit>
}