package com.vintageflow.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vintageflow.app.data.model.EstadosReserva
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.model.Reserva
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservaRepositoryFirebase(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ReservaRepository {

    private companion object {
        const val COLECCION_PRENDAS = "prendas"
        const val COLECCION_RESERVAS = "reservas"

        const val ESTADO_DISPONIBLE = "DISPONIBLE"
        const val ESTADO_RESERVADO = "RESERVADO"
        const val ESTADO_VENDIDO = "VENDIDO"
    }

    override suspend fun reservasDeCliente(clienteUid: String): Result<List<Reserva>> = try {
        val uid = clienteUid.ifBlank { auth.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) {
            throw IllegalStateException("No hay un usuario autenticado.")
        }

        val lista = db.collection(COLECCION_RESERVAS)
            .whereEqualTo("clienteUid", uid)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Reserva::class.java)?.copy(id = doc.id)
            }
            .sortedByDescending { it.fecha }

        Result.success(lista)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observarReservasDeCliente(clienteUid: String): Flow<List<Reserva>> = callbackFlow {
        val uid = clienteUid.ifBlank { auth.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(COLECCION_RESERVAS)
            .whereEqualTo("clienteUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reservas = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(Reserva::class.java)?.copy(id = doc.id) }
                    ?.sortedByDescending { it.fecha }
                    .orEmpty()
                trySend(reservas)
            }
        awaitClose { listener.remove() }
    }

    override fun observarTodasLasReservas(): Flow<List<Reserva>> = callbackFlow {
        val listener = db.collection(COLECCION_RESERVAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reservas = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(Reserva::class.java)?.copy(id = doc.id) }
                    ?.sortedByDescending { it.fecha }
                    .orEmpty()
                trySend(reservas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun reservar(
        prenda: Prenda,
        clienteUid: String,
        clienteNombre: String
    ): Result<Reserva> = try {
        val uid = clienteUid.ifBlank {
            auth.currentUser?.uid
                ?: throw IllegalStateException("Debes iniciar sesión para reservar.")
        }
        val nombreFinal = clienteNombre.ifBlank {
            auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Cliente"
        }

        val prendaRef = db.collection(COLECCION_PRENDAS).document(prenda.id)
        val nuevaReservaRef = db.collection(COLECCION_RESERVAS).document()

        val reservaCreada = db.runTransaction { transaccion ->
            val prendaSnap = transaccion.get(prendaRef)
            val prendaActual = prendaSnap.toObject(Prenda::class.java)?.copy(id = prendaSnap.id)
                ?: throw IllegalStateException("La prenda seleccionada no existe.")

            if (!prendaActual.estado.equals(ESTADO_DISPONIBLE, ignoreCase = true)) {
                throw IllegalStateException("Esta prenda ya no está disponible.")
            }

            val nuevaReserva = Reserva(
                id = nuevaReservaRef.id,
                prendaId = prendaActual.id,
                prendaNombre = prendaActual.nombre,
                clienteUid = uid,
                clienteNombre = nombreFinal,
                fecha = System.currentTimeMillis(),
                estado = EstadosReserva.PENDIENTE
            )

            transaccion.update(prendaRef, "estado", ESTADO_RESERVADO)
            transaccion.set(nuevaReservaRef, nuevaReserva)
            nuevaReserva
        }.await()

        Result.success(reservaCreada)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun aprobar(reservaId: String): Result<Unit> = try {
        val reservaRef = db.collection(COLECCION_RESERVAS).document(reservaId)

        db.runTransaction { transaccion ->
            val reservaSnap = transaccion.get(reservaRef)
            val reserva = reservaSnap.toObject(Reserva::class.java)
                ?: throw IllegalStateException("No se encontró la reserva.")

            if (reserva.estado != EstadosReserva.PENDIENTE) {
                throw IllegalStateException("Solo se pueden aprobar reservas pendientes.")
            }

            val prendaRef = db.collection(COLECCION_PRENDAS).document(reserva.prendaId)
            val prendaSnap = transaccion.get(prendaRef)

            transaccion.update(reservaRef, "estado", EstadosReserva.APROBADA)
            if (prendaSnap.exists()) {
                transaccion.update(prendaRef, "estado", ESTADO_VENDIDO)
            }
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun cancelar(reservaId: String): Result<Unit> = try {
        val reservaRef = db.collection(COLECCION_RESERVAS).document(reservaId)

        db.runTransaction { transaccion ->
            val reservaSnap = transaccion.get(reservaRef)
            val reserva = reservaSnap.toObject(Reserva::class.java)
                ?: throw IllegalStateException("No se encontró la reserva.")

            if (reserva.estado == EstadosReserva.CANCELADA) {
                throw IllegalStateException("La reserva ya estaba cancelada.")
            }

            val prendaRef = db.collection(COLECCION_PRENDAS).document(reserva.prendaId)
            val prendaSnap = transaccion.get(prendaRef)

            transaccion.update(reservaRef, "estado", EstadosReserva.CANCELADA)
            if (prendaSnap.exists()) {
                transaccion.update(prendaRef, "estado", ESTADO_DISPONIBLE)
            }
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}