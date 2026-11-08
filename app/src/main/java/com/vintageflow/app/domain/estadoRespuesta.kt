package com.vintageflow.app.domain

/**
 * Contenedor de estados para el flujo unidireccional de datos (MVI/MVVM).
 * Envuelve el resultado de cualquier operación asíncrona hacia la UI.
 */
sealed class EstadoRespuesta<out T> {
    object Cargando : EstadoRespuesta<Nothing>()
    data class Exito<out T>(val datos: T) : EstadoRespuesta<T>()
    data class Error(val excepcion: Exception) : EstadoRespuesta<Nothing>()
}