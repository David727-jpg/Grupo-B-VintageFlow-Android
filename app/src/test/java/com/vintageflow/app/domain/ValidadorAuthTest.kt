package com.vintageflow.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas de las reglas de validación de autenticación.
 *
 * El énfasis está en los casos de error: una validación que solo se prueba
 * con datos correctos no demuestra nada, porque una función que siempre
 * devuelve `null` pasaría igual.
 */
class ValidadorAuthTest {

    // ---------- Nombre ----------

    @Test
    fun nombreValidoNoProduceError() {
        assertNull(ValidadorAuth.validarNombre("Vladimir Ayala"))
    }

    @Test
    fun nombreVacioProduceError() {
        assertNotNull(ValidadorAuth.validarNombre(""))
    }

    @Test
    fun nombreDeSoloEspaciosProduceError() {
        // Caso que un `isEmpty()` sin normalizar dejaría pasar.
        assertNotNull(ValidadorAuth.validarNombre("     "))
    }

    @Test
    fun nombreSeNormalizaQuitandoEspaciosDeLosExtremos() {
        assertEquals("Vladimir", ValidadorAuth.normalizarNombre("  Vladimir  "))
    }

    // ---------- Correo ----------

    @Test
    fun correoValidoNoProduceError() {
        assertNull(ValidadorAuth.validarCorreo("vladimir@correo.com"))
    }

    @Test
    fun correoVacioProduceError() {
        assertNotNull(ValidadorAuth.validarCorreo(""))
    }

    @Test
    fun correoSinArrobaProduceError() {
        assertNotNull(ValidadorAuth.validarCorreo("vladimircorreo.com"))
    }

    @Test
    fun correoSinDominioProduceError() {
        assertNotNull(ValidadorAuth.validarCorreo("vladimir@"))
    }

    @Test
    fun correoSinExtensionProduceError() {
        assertNotNull(ValidadorAuth.validarCorreo("vladimir@correo"))
    }

    @Test
    fun correoConEspaciosAlrededorEsValido() {
        // El usuario escribe un espacio de más y la cuenta existe: debe entrar.
        assertNull(ValidadorAuth.validarCorreo("  vladimir@correo.com  "))
    }

    @Test
    fun correoSeNormalizaAMinusculasYSinEspacios() {
        assertEquals(
            "vladimir@correo.com",
            ValidadorAuth.normalizarCorreo("  Vladimir@Correo.COM ")
        )
    }

    // ---------- Contraseña ----------

    @Test
    fun contrasenaValidaNoProduceError() {
        assertNull(ValidadorAuth.validarContrasena("vintage2026"))
    }

    @Test
    fun contrasenaVaciaProduceError() {
        assertNotNull(ValidadorAuth.validarContrasena(""))
    }

    @Test
    fun contrasenaDeSoloEspaciosProduceError() {
        assertNotNull(ValidadorAuth.validarContrasena("      "))
    }

    @Test
    fun contrasenaMasCortaQueElMinimoProduceError() {
        assertNotNull(ValidadorAuth.validarContrasena("12345"))
    }

    @Test
    fun contrasenaExactamenteEnElMinimoEsValida() {
        // Frontera de la regla: 6 caracteres debe pasar, 5 no.
        assertNull(ValidadorAuth.validarContrasena("123456"))
    }

    @Test
    fun contrasenaConservaLosEspaciosInternos() {
        // Un espacio dentro de la contraseña es parte de la contraseña:
        // no se recorta como el correo.
        assertNull(ValidadorAuth.validarContrasena("mi clave"))
    }
}