package com.vintageflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vintageflow.app.data.model.Prenda
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
}