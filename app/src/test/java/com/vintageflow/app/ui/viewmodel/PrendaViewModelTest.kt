package com.vintageflow.app.ui.viewmodel

import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.repository.PrendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrendaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun preparar() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @After
    fun limpiar() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun cargaSoloAlSolicitarlaYRecibeActualizaciones() = runTest(dispatcher) {
        val actualizaciones = MutableSharedFlow<List<Prenda>>(replay = 1)
        val repository = RepositorioPrueba(actualizaciones)
        val viewModel = PrendaViewModel(repository)
        assertEquals(0, repository.escuchas)

        viewModel.cargarPrendas()
        assertTrue(viewModel.cargando.value)
        val primera = Prenda(id = "1", nombre = "Camisa", estado = "DISPONIBLE")
        actualizaciones.emit(listOf(primera))
        advanceUntilIdle()
        assertEquals(listOf(primera), viewModel.prendas.value)
        assertEquals(listOf(primera), viewModel.prendasDisponibles.value)
        assertFalse(viewModel.cargando.value)

        val segunda = primera.copy(estado = "RESERVADO")
        actualizaciones.emit(listOf(segunda))
        advanceUntilIdle()
        assertEquals(listOf(segunda), viewModel.prendasReservadas.value)
        assertTrue(viewModel.prendasDisponibles.value.isEmpty())

        viewModel.detenerCarga()
        assertTrue(viewModel.prendas.value.isEmpty())
        assertTrue(viewModel.prendasReservadas.value.isEmpty())
        actualizaciones.emit(listOf(primera))
        advanceUntilIdle()
        assertTrue(viewModel.prendas.value.isEmpty())
    }

    @Test
    fun errorSeMuestraYReintentoRecuperaLaEscucha() = runTest(dispatcher) {
        val repository = RepositorioPrueba(flow { throw IllegalStateException("Sin permisos") })
        val viewModel = PrendaViewModel(repository)
        viewModel.cargarPrendas()
        advanceUntilIdle()
        assertEquals("No se pudieron cargar las prendas: Sin permisos", viewModel.errorCarga.value)
        assertFalse(viewModel.cargando.value)

        val prenda = Prenda(id = "2", nombre = "Vestido")
        repository.fuente = flowOf(listOf(prenda))
        viewModel.cargarPrendas()
        advanceUntilIdle()
        assertEquals(2, repository.escuchas)
        assertNull(viewModel.errorCarga.value)
        assertEquals(listOf(prenda), viewModel.prendas.value)
    }

    private class RepositorioPrueba(var fuente: Flow<List<Prenda>>) : PrendaRepository {
        var escuchas = 0

        override fun observarPrendas(): Flow<List<Prenda>> = flow {
            escuchas++
            emitAll(fuente)
        }

        override suspend fun obtenerPrendas(): List<Prenda> =
            error("La carga debe usar solo la escucha en tiempo real")
        override suspend fun obtenerPrendaPorId(id: String): Prenda? = null
        override suspend fun agregarPrenda(prenda: Prenda): Result<Unit> = Result.success(Unit)
        override suspend fun actualizarPrenda(prenda: Prenda): Result<Unit> = Result.success(Unit)
        override suspend fun eliminarPrenda(id: String): Result<Unit> = Result.success(Unit)
    }
}
