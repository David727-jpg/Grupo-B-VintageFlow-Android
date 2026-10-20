package com.vintageflow.app.ui.navigation

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val CATALOGO = "catalogo"

    const val ESTADISTICAS = "estadisticas"

    const val ARG_ID = "id"
    const val DETALLE = "detalle/{$ARG_ID}"
    const val FORMULARIO = "formulario?$ARG_ID={$ARG_ID}"

    fun detalle(id: String) = "detalle/$id"
    fun formulario(id: String? = null) = if (id == null) "formulario" else "formulario?$ARG_ID=$id"
}
