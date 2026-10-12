package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.data.repository.PrendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrendaViewModel(
    private val repository: PrendaRepository
) : ViewModel() {

    private val _prendas = MutableStateFlow<List<Prenda>>(emptyList())
    val prendas: StateFlow<List<Prenda>> = _prendas.asStateFlow()

    private val _prendasDisponibles =
        MutableStateFlow<List<Prenda>>(emptyList())

    val prendasDisponibles: StateFlow<List<Prenda>> =
        _prendasDisponibles.asStateFlow()

    private val _prendasReservadas =
        MutableStateFlow<List<Prenda>>(emptyList())

    val prendasReservadas: StateFlow<List<Prenda>> =
        _prendasReservadas.asStateFlow()

    private val _prendasVendidas =
        MutableStateFlow<List<Prenda>>(emptyList())

    val prendasVendidas: StateFlow<List<Prenda>> =
        _prendasVendidas.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargarPrendas() {
        viewModelScope.launch {

            val lista = repository.obtenerPrendas()

            _prendas.value = lista

            actualizarListasPorEstado(lista)
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
                cargarPrendas()
            } else {
                _mensaje.value =
                    resultado.exceptionOrNull()?.message
            }
        }
    }

    fun actualizarPrenda(prenda: Prenda) {
        viewModelScope.launch {

            val resultado = repository.actualizarPrenda(prenda)

            if (resultado.isSuccess) {
                _mensaje.value = "Prenda actualizada correctamente"
                cargarPrendas()
            } else {
                _mensaje.value =
                    resultado.exceptionOrNull()?.message
            }
        }
    }

    fun eliminarPrenda(id: String) {
        viewModelScope.launch {

            val resultado = repository.eliminarPrenda(id)

            if (resultado.isSuccess) {
                _mensaje.value = "Prenda eliminada correctamente"
                cargarPrendas()
            } else {
                _mensaje.value =
                    resultado.exceptionOrNull()?.message
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