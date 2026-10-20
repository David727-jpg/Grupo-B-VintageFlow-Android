package com.vintageflow.app.domain

import com.vintageflow.app.data.model.Prenda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalisisInventarioTest {

    private fun inventario() = listOf(
        Prenda("V-001", "Chaqueta", "L", "Chaquetas", 45.0, "DISPONIBLE"),
        Prenda("V-002", "Blusa", "S", "Blusas", 15.0, "DISPONIBLE"),
        Prenda("V-003", "Jeans", "M", "Pantalones", 30.0, "VENDIDO"),
        Prenda("V-004", "Vestido", "m", "Vestidos", 10.0, "disponible")
    )

    @Test
    fun valorTotalSumaTodasLasPrendas() {
        assertEquals(100.0, AnalisisInventario.valorTotal(inventario()), 0.001)
    }

    @Test
    fun valorDisponibleIgnoraMayusculasYMinusculas() {
        // La prenda V-004 tiene el estado en minúsculas y debe contarse igual.
        assertEquals(70.0, AnalisisInventario.valorDisponible(inventario()), 0.001)
    }

    @Test
    fun precioPromedioEsCorrecto() {
        assertEquals(25.0, AnalisisInventario.precioPromedio(inventario()), 0.001)
    }

    @Test
    fun precioPromedioConInventarioVacioEsCeroYNoNaN() {
        val promedio = AnalisisInventario.precioPromedio(emptyList())
        assertEquals(0.0, promedio, 0.001)
        assertTrue(!promedio.isNaN())
    }

    @Test
    fun prendaMasCaraEsLaDeMayorPrecio() {
        assertEquals("V-001", AnalisisInventario.prendaMasCara(inventario())?.id)
    }

    @Test
    fun prendaMasCaraConInventarioVacioEsNula() {
        assertNull(AnalisisInventario.prendaMasCara(emptyList()))
    }

    @Test
    fun agrupacionPorTallaNormalizaMayusculas() {
        // "M" y "m" se contaran como una sola talla.
        val talla = AnalisisInventario.porTalla(inventario()).first { it.etiqueta == "M" }
        assertEquals(2, talla.cantidad)
    }

    @Test
    fun distribucionSeOrdenaDeMayorAMenor() {
        val estados = AnalisisInventario.porEstado(inventario())
        assertEquals("DISPONIBLE", estados.first().etiqueta)
        assertEquals(3, estados.first().cantidad)
    }

    @Test
    fun tasaVendidasEsElPorcentajeSobreElTotal() {
        // 1 vendida de 4 prendas.
        assertEquals(25.0, AnalisisInventario.tasaVendidas(inventario()), 0.001)
    }

    @Test
    fun resumenConInventarioVacioSeMarcaComoVacio() {
        assertTrue(AnalisisInventario.resumir(emptyList()).estaVacio)
    }
}