package com.vintageflow.app.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrendaRepositoryLocalTest {
    @Test
    fun aprobarMarcaComoVendidoYCancelarComoDisponible() = runTest {
        val repository = PrendaRepositoryLocal()
        val prenda = repository.obtenerPrendaPorId("VINT-001")!!
        repository.actualizarPrenda(prenda.copy(estado = "RESERVADO"))

        assertTrue(repository.resolverReserva(prenda.id, true).isSuccess)
        assertEquals("VENDIDO", repository.obtenerPrendaPorId(prenda.id)?.estado)
        assertTrue(repository.resolverReserva(prenda.id, false).isFailure)
        assertEquals("VENDIDO", repository.obtenerPrendaPorId(prenda.id)?.estado)

        repository.actualizarPrenda(prenda.copy(estado = "RESERVADO"))
        assertTrue(repository.resolverReserva(prenda.id, false).isSuccess)
        assertEquals("DISPONIBLE", repository.obtenerPrendaPorId(prenda.id)?.estado)
        assertTrue(repository.resolverReserva("inexistente", true).isFailure)
    }
}
