package com.vintageflow.app.data.repository

import com.vintageflow.app.data.model.Prenda

class PrendaRepositoryLocal : PrendaRepository {

    private val prendas = mutableListOf(
        Prenda(
            id = "VINT-001",
            nombre = "Chaqueta Retro",
            talla = "L",
            categoria = "Chaquetas",
            precio = 45.0,
            estado = "DISPONIBLE"
        ),
        Prenda(
            id = "VINT-002",
            nombre = "Blusa de Seda",
            talla = "S",
            categoria = "Blusas",
            precio = 18.5,
            estado = "DISPONIBLE"
        )
    )

    override suspend fun obtenerPrendas(): List<Prenda> {
        return prendas.toList()
    }

    override suspend fun obtenerPrendaPorId(id: String): Prenda? {
        return prendas.find { it.id == id }
    }

    override suspend fun agregarPrenda(prenda: Prenda): Result<Unit> {
        return try {
            prendas.add(prenda)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarPrenda(prenda: Prenda): Result<Unit> {
        val index = prendas.indexOfFirst { it.id == prenda.id }

        return if (index != -1) {
            prendas[index] = prenda
            Result.success(Unit)
        } else {
            Result.failure(Exception("Prenda no encontrada"))
        }
    }

    override suspend fun eliminarPrenda(id: String): Result<Unit> {
        val eliminada = prendas.removeIf { it.id == id }

        return if (eliminada) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Prenda no encontrada"))
        }
    }
}