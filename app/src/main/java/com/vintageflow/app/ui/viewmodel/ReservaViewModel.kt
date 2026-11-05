package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.model.Reserva
import com.vintageflow.app.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado y acciones de las reservas del cliente (Mis reservas y botón Reservar). */
class ReservaViewModel(
    private val repository: ReservaRepository
) : ViewModel() {

    private val _misReservas = MutableStateFlow<List<Reserva>>(emptyList())
    val misReservas: StateFlow<List<Reserva>> = _misReservas.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargarMisReservas(clienteUid: String) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            repository.reservasDeCliente(clienteUid)
                .onSuccess { _misReservas.value = it }
                .onFailure { _error.value = it.message ?: "No se pudieron cargar tus reservas." }
            _cargando.value = false
        }
    }

    /** onExito se ejecuta solo si la reserva se creó (por ejemplo, para navegar). */
    fun reservar(
        prenda: Prenda,
        clienteUid: String,
        clienteNombre: String,
        onExito: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.reservar(prenda, clienteUid, clienteNombre)
                .onSuccess {
                    _mensaje.value = "Reserva creada: ${prenda.nombre}"
                    cargarMisReservas(clienteUid)
                    onExito()
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo reservar la prenda." }
        }
    }

    fun cancelar(reserva: Reserva, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            repository.cancelar(reserva.id)
                .onSuccess {
                    _mensaje.value = "Reserva cancelada"
                    cargarMisReservas(reserva.clienteUid)
                    onExito()
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo cancelar la reserva." }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
