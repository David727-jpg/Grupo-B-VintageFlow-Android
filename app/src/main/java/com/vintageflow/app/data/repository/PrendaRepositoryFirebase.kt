package com.vintageflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vintageflow.app.data.model.Prenda
import kotlinx.coroutines.tasks.await

class PrendaRepositoryFirebase(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PrendaRepository {

    private companion object {
        const val COLECCION_PRENDAS = "prendas"
    }

    override suspend fun obtenerPrendas(): List<Prenda> {
        return try {
            db.collection(COLECCION_PRENDAS)
                .get()
                .await()
                .documents
                .mapNotNull { documento ->
                    documento.toObject(Prenda::class.java)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun obtenerPrendaPorId(id: String): Prenda? {
        return try {
            db.collection(COLECCION_PRENDAS)
                .document(id)
                .get()
                .await()
                .toObject(Prenda::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun agregarPrenda(prenda: Prenda): Result<Unit> {
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