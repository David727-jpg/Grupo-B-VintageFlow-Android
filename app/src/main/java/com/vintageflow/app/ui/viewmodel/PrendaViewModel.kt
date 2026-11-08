package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.repository.PrendaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PrendaViewModel(
    private val repository: PrendaRepository
) : ViewModel() {

    private val _prendas = MutableStateFlow<List<Prenda>>(emptyList())
    val prendas: StateFlow<List<Prenda>> = _prendas.asStateFlow()

    private val _prendasDisponibles = MutableStateFlow<List<Prenda>>(emptyList())
    val prendasDisponibles: StateFlow<List<Prenda>> = _prendasDisponibles.asStateFlow()

    private val _prendasReservadas = MutableStateFlow<List<Prenda>>(emptyList())
    val prendasReservadas: StateFlow<List<Prenda>> = _prendasReservadas.asStateFlow()

    private val _prendasVendidas = MutableStateFlow<List<Prenda>>(emptyList())
    val prendasVendidas: StateFlow<List<Prenda>> = _prendasVendidas.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _errorCarga = MutableStateFlow<String?>(null)
    val errorCarga: StateFlow<String?> = _errorCarga.asStateFlow()

    private val _resolviendoReserva = MutableStateFlow<String?>(null)
    val resolviendoReserva: StateFlow<String?> = _resolviendoReserva.asStateFlow()

    private val _errorReserva = MutableStateFlow<String?>(null)
    val errorReserva: StateFlow<String?> = _errorReserva.asStateFlow()

    private var escuchaJob: Job? = null

    fun cargarPrendas() {
        escuchaJob?.cancel()
        _cargando.value = true
        _errorCarga.value = null
        escuchaJob = viewModelScope.launch {
            repository.observarPrendas()
                .catch { e ->
                    _errorCarga.value = "No se pudieron cargar las prendas: " +
                        (e.message ?: "error desconocido")
                    _cargando.value = false
                }
                .collect { lista ->
                    _prendas.value = lista
                    actualizarListasPorEstado(lista)
                    _cargando.value = false
                }
        }
    }

    fun detenerCarga() {
        escuchaJob?.cancel()
        escuchaJob = null
        _prendas.value = emptyList()
        actualizarListasPorEstado(emptyList())
        _cargando.value = false
        _errorCarga.value = null
        _errorReserva.value = null
    }

    fun resolverReserva(id: String, aprobar: Boolean) {
        if (_resolviendoReserva.value != null) return
        _resolviendoReserva.value = id
        _errorReserva.value = null
        viewModelScope.launch {
            try {
                repository.resolverReserva(id, aprobar)
                    .onFailure { error ->
                        _errorReserva.value = "No se pudo actualizar la reserva: " +
                            (error.message ?: "error desconocido")
                    }
            } finally {
                _resolviendoReserva.value = null
            }
        }
    }

    private fun actualizarListasPorEstado(lista: List<Prenda>) {
        _prendasDisponibles.value = lista.filter {
            it.estado.equals("DISPONIBLE", ignoreCase = true)
        }
        _prendasReservadas.value = lista.filter {
            it.estado.equals("RESERVADO", ignoreCase = true)
        }
        _prendasVendidas.value = lista.filter {
            it.estado.equals("VENDIDO", ignoreCase = true)
        }
    }

    fun agregarPrenda(prenda: Prenda) {
        viewModelScope.launch {
            val resultado = repository.agregarPrenda(prenda)
            if (resultado.isSuccess) {
                _mensaje.value = "Prenda registrada correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
            }
        }
    }

    fun actualizarPrenda(prenda: Prenda) {
        viewModelScope.launch {
            val resultado = repository.actualizarPrenda(prenda)
            if (resultado.isSuccess) {
                _mensaje.value = "Prenda actualizada correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
            }
        }
    }

    fun eliminarPrenda(id: String) {
        viewModelScope.launch {
            val resultado = repository.eliminarPrenda(id)
            if (resultado.isSuccess) {
                _mensaje.value = "Prenda eliminada correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
            }
        }
    }

    fun cambiarEstado(
        prenda: Prenda,
        nuevoEstado: String
    ) {
        val prendaActualizada = prenda.copy(
            estado = nuevoEstado
        )
        actualizarPrenda(prendaActualizada)
    }
}