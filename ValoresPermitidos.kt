package com.vintageflow.app.ui.catalog

import com.vintageflow.app.data.model.Prenda


val TALLAS = listOf("XS", "S", "M", "L", "XL", "XXL", "UNICA")

val CATEGORIAS = listOf(
    "Camisas", "Blusas", "Pantalones", "Vestidos",
    "Chaquetas", "Faldas", "Zapatos", "Accesorios"
)

const val ESTADO_DISPONIBLE = "DISPONIBLE"
const val ESTADO_RESERVADO = "RESERVADO"
const val ESTADO_VENDIDO = "VENDIDO"


fun estadosPermitidos(actual: String): List<String> = when (actual.uppercase()) {
    ESTADO_DISPONIBLE -> listOf(ESTADO_DISPONIBLE, ESTADO_RESERVADO)
    ESTADO_RESERVADO -> listOf(ESTADO_RESERVADO, ESTADO_VENDIDO, ESTADO_DISPONIBLE)
    else -> listOf(actual.uppercase()) // VENDIDO no admite más cambios
}


enum class RangoPrecio(val etiqueta: String) {
    TODOS("Todos"),
    HASTA_20("Hasta \$20"),
    DE_20_A_40("\$20 – \$40"),
    MAS_DE_40("Más de \$40");

    fun incluye(precio: Double): Boolean = when (this) {
        TODOS -> true
        HASTA_20 -> precio <= 20.0
        DE_20_A_40 -> precio > 20.0 && precio <= 40.0
        MAS_DE_40 -> precio > 40.0
    }
}


fun filtrarPrendas(
    prendas: List<Prenda>,
    talla: String?,
    categoria: String?,
    rango: RangoPrecio
): List<Prenda> = prendas.filter { p ->
    (talla == null || p.talla.equals(talla, ignoreCase = true)) &&
        (categoria == null || p.categoria.equals(categoria, ignoreCase = true)) &&
        rango.incluye(p.precio)
}
