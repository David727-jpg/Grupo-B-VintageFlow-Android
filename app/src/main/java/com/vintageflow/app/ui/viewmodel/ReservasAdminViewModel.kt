package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vintageflow.app.data.model.Reserva
import com.vintageflow.app.data.repository.ReservaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/** Estado y acciones del panel de reservas del administrador (todas las reservas). */
class ReservasAdminViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var escuchaJob: Job? = null

    /** Escucha en tiempo real todas las reservas. Si se llama de nuevo, reemplaza la escucha. */
    fun escuchar() {
        escuchaJob?.cancel()
        escuchaJob = viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            repository.observarTodasLasReservas()
                .catch { e ->
                    _error.value = e.message ?: "No se pudieron cargar las reservas."
                    _cargando.value = false
                }
                .collect { lista ->
                    _reservas.value = lista
                    _cargando.value = false
                }
        }
    }

    /** Detiene la escucha y limpia el estado (al cerrar sesión). */
    fun detenerEscucha() {
        escuchaJob?.cancel()
        escuchaJob = null
        _reservas.value = emptyList()
        _error.value = null
        _cargando.value = false
    }

    fun aprobar(reserva: Reserva) {
        viewModelScope.launch {
            repository.aprobar(reserva.id)
                .onSuccess { _mensaje.value = "Reserva aprobada: ${reserva.prendaNombre}" }
                .onFailure { _mensaje.value = it.message ?: "No se pudo aprobar la reserva." }
        }
    }

    fun cancelar(reserva: Reserva) {
        viewModelScope.launch {
            repository.cancelar(reserva.id)
                .onSuccess { _mensaje.value = "Reserva cancelada: ${reserva.prendaNombre}" }
                .onFailure { _mensaje.value = it.message ?: "No se pudo cancelar la reserva." }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}

class ReservasAdminViewModelFactory(
    private val repository: ReservaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservasAdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservasAdminViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
