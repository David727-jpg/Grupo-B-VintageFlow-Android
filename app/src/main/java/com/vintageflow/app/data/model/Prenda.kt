package com.vintageflow.app.data.model

data class Prenda(
    val id: String = "",
    val nombre: String = "",
    val talla: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val estado: String = "DISPONIBLE",
    val descripcion: String = "",
    val imagenUrl: String = ""
)