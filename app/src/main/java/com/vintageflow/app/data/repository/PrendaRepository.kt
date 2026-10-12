package com.vintageflow.app.data.repository

import com.vintageflow.app.data.model.Prenda

interface PrendaRepository {

    suspend fun obtenerPrendas(): List<Prenda>

    suspend fun obtenerPrendaPorId(id: String): Prenda?

    suspend fun agregarPrenda(prenda: Prenda): Result<Unit>

    suspend fun actualizarPrenda(prenda: Prenda): Result<Unit>

    suspend fun eliminarPrenda(id: String): Result<Unit>
}