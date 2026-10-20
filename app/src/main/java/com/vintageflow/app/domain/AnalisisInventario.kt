package com.vintageflow.app.domain

import com.vintageflow.app.data.model.Prenda

/**
 * Lógica de estadísticas y analítica del inventario.
 *
 */

data class Distribucion(
    val etiqueta: String,
    val cantidad: Int
)

/** Resumen completo del inventario en un único objeto inmutable. */
data class ResumenInventario(
    val totalPrendas: Int,
    val valorTotal: Double,
    val valorDisponible: Double,
    val precioPromedio: Double,
    val prendaMasCara: Prenda?,
    val porEstado: List<Distribucion>,
    val porTalla: List<Distribucion>,
    val porCategoria: List<Distribucion>,
    val tasaVendidas: Double
) {
    val estaVacio: Boolean get() = totalPrendas == 0
}

object AnalisisInventario {

    const val DISPONIBLE = "DISPONIBLE"
    const val VENDIDO = "VENDIDO"

    /** Suma del precio de todas las prendas registradas. */
    fun valorTotal(prendas: List<Prenda>): Double =
        prendas.sumOf { it.precio }

    /** Suma del precio únicamente de las prendas aún disponibles. */
    fun valorDisponible(prendas: List<Prenda>): Double =
        prendas.filter { it.estado.equals(DISPONIBLE, ignoreCase = true) }
            .sumOf { it.precio }

    /** Precio promedio. Devuelve 0.0 con inventario vacío */
    fun precioPromedio(prendas: List<Prenda>): Double =
        if (prendas.isEmpty()) 0.0 else prendas.map { it.precio }.average()

    /** Prenda de mayor precio, o null si no hay ninguna. */
    fun prendaMasCara(prendas: List<Prenda>): Prenda? =
        prendas.maxByOrNull { it.precio }

    /**
     * Porcentaje de prendas vendidas sobre el total.
     * Indica qué proporción del inventario se ha colocado.
     */
    fun tasaVendidas(prendas: List<Prenda>): Double {
        if (prendas.isEmpty()) return 0.0
        val vendidas = prendas.count { it.estado.equals(VENDIDO, ignoreCase = true) }
        return vendidas * 100.0 / prendas.size
    }

    /**
     * Agrupa y cuenta por un atributo cualquiera, normalizando el texto
     *
     * El resultado se ordena de mayor a menor cantidad: en un gráfico de
     * barras el orden por magnitud permite leer el ranking sin esfuerzo.
     */
    private fun agrupar(
        prendas: List<Prenda>,
        atributo: (Prenda) -> String
    ): List<Distribucion> =
        prendas.groupingBy { atributo(it).trim().uppercase() }
            .eachCount()
            .map { (etiqueta, cantidad) -> Distribucion(etiqueta, cantidad) }
            .sortedByDescending { it.cantidad }

    fun porEstado(prendas: List<Prenda>): List<Distribucion> =
        agrupar(prendas) { it.estado }

    fun porTalla(prendas: List<Prenda>): List<Distribucion> =
        agrupar(prendas) { it.talla }

    fun porCategoria(prendas: List<Prenda>): List<Distribucion> =
        agrupar(prendas) { it.categoria }

    /** Calcula todos los indicadores de una sola pasada. */
    fun resumir(prendas: List<Prenda>): ResumenInventario =
        ResumenInventario(
            totalPrendas = prendas.size,
            valorTotal = valorTotal(prendas),
            valorDisponible = valorDisponible(prendas),
            precioPromedio = precioPromedio(prendas),
            prendaMasCara = prendaMasCara(prendas),
            porEstado = porEstado(prendas),
            porTalla = porTalla(prendas),
            porCategoria = porCategoria(prendas),
            tasaVendidas = tasaVendidas(prendas)
        )
}