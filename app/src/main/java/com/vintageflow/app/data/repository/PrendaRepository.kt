package com.vintageflow.app.data.repository

import com.vintageflow.app.data.model.Prenda
import kotlinx.coroutines.flow.Flow

interface PrendaRepository {

    fun observarPrendas(): Flow<List<Prenda>>

    suspend fun obtenerPrendas(): List<Prenda>

    suspend fun obtenerPrendaPorId(id: String): Prenda?

    suspend fun agregarPrenda(prenda: Prenda): Result<Unit>

    suspend fun actualizarPrenda(prenda: Prenda): Result<Unit>

    suspend fun eliminarPrenda(id: String): Result<Unit>

    suspend fun resolverReserva(id: String, aprobar: Boolean): Result<Unit>
}