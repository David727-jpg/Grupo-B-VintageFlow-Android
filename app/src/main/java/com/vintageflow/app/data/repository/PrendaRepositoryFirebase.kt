package com.vintageflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.vintageflow.app.data.model.EstadosReserva
import com.vintageflow.app.data.model.Prenda
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

class PrendaRepositoryFirebase(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PrendaRepository {

    private companion object {
        const val COLECCION_PRENDAS = "prendas"
    }

    override fun observarPrendas(): Flow<List<Prenda>> = callbackFlow {
        val listener = db.collection(COLECCION_PRENDAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Prenda::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerPrendas(): List<Prenda> {
        return try {
            db.collection(COLECCION_PRENDAS)
                .get()
                .await()
                .documents
                .mapNotNull { documento ->
                    documento.toObject(Prenda::class.java)?.copy(id = documento.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun obtenerPrendaPorId(id: String): Prenda? {
        return try {
            val doc = db.collection(COLECCION_PRENDAS)
                .document(id)
                .get()
                .await()
            doc.toObject(Prenda::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun agregarPrenda(prenda: Prenda): Result<Unit> {
        return try {
            val docRef = if (prenda.id.isNotBlank()) {
                db.collection(COLECCION_PRENDAS).document(prenda.id)
            } else {
                db.collection(COLECCION_PRENDAS).document()
            }
            val prendaConId = prenda.copy(id = docRef.id)
            docRef.set(prendaConId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarPrenda(prenda: Prenda): Result<Unit> {
        return try {
            db.collection(COLECCION_PRENDAS)
                .document(prenda.id)
                .set(prenda)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarPrenda(id: String): Result<Unit> {
        return try {
            db.collection(COLECCION_PRENDAS)
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolverReserva(id: String, aprobar: Boolean): Result<Unit> {
        return try {
            val prendaRef = db.collection(COLECCION_PRENDAS).document(id)
            val pendientes = db.collection("reservas")
                .whereEqualTo("prendaId", id)
                .get(Source.SERVER)
                .await()
                .documents
                .filter { it.getString("estado") == EstadosReserva.PENDIENTE }
            if (pendientes.size > 1) {
                throw IllegalStateException("Hay varias reservas pendientes para esta prenda. Revisa los datos antes de continuar.")
            }
            val reservaRef = pendientes.singleOrNull()?.reference
            db.runTransaction { transaccion ->
                val prenda = transaccion.get(prendaRef)
                val reserva = reservaRef?.let { transaccion.get(it) }
                if (!prenda.exists() || !prenda.getString("estado").equals("RESERVADO", ignoreCase = true)) {
                    throw IllegalStateException("La prenda ya no está reservada.")
                }
                if (reserva != null &&
                    (reserva.getString("estado") != EstadosReserva.PENDIENTE ||
                        reserva.getString("prendaId") != id)
                ) {
                    throw IllegalStateException("La reserva cambió. Actualiza la pantalla e inténtalo de nuevo.")
                }
                transaccion.update(prendaRef, "estado", if (aprobar) "VENDIDO" else "DISPONIBLE")
                if (reservaRef != null) {
                    transaccion.update(
                        reservaRef,
                        "estado",
                        if (aprobar) EstadosReserva.APROBADA else EstadosReserva.CANCELADA
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}