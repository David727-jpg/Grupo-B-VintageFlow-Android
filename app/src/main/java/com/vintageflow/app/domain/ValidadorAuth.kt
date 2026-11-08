package com.vintageflow.app.domain

/**
 * Reglas de validación de los datos de autenticación.
 *
 * Vive en `domain` y no en el ViewModel por la misma razón por la que en la
 * Etapa 2 las reglas viven en `Validador` y no en `MenuConsola`: la regla es
 * una sola y el lugar donde se aplica puede ser cualquiera. Tener la regla
 * escrita en cada pantalla es lo que produce las inconsistencias.
 *
 */
object ValidadorAuth {

    /** Longitud minima que exige Firebase Authentication. */
    const val LONGITUD_MINIMA_CONTRASENA = 6

    /**
     * Patrón de correo equivalente al de `android.util.Patterns.EMAIL_ADDRESS`
     * para los casos que importan aquí. Se escribe como Regex para que la
     * clase no dependa del SDK de Android
     */
    private val PATRON_CORREO =
        Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    // -----------------------------------------------------------------
    // Normalización
    //
    // El mismo criterio que `Validador.normalizar` de la Etapa 2: el dato se
    // limpia una vez, en un solo lugar, antes de guardarse o enviarse.
    // -----------------------------------------------------------------

    /** Quita espacios y baja a minúsculas. */
    fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    /** Quita espacios sobrantes de los extremos del nombre */
    fun normalizarNombre(nombre: String): String = nombre.trim()

    // -----------------------------------------------------------------
    // Devuelven el mensaje de error, o `null` cuando el dato es válido.
    // Se valida sobre el valor normalizado para que la regla y el dato que
    // se envía a la red sean exactamente el mismo.
    // -----------------------------------------------------------------

    fun validarNombre(nombre: String): String? =
        if (normalizarNombre(nombre).isEmpty()) "El nombre no puede estar vacío." else null

    fun validarCorreo(correo: String): String? {
        val valor = normalizarCorreo(correo)
        return when {
            valor.isEmpty() -> "El correo no puede estar vacío."
            !PATRON_CORREO.matches(valor) -> "El formato del correo no es válido."
            else -> null
        }
    }

    /**
     * La contraseña no se normaliza: un espacio dentro de una contraseña es
     * parte de la contraseña. Solo se verifica que no sea únicamente espacios.
     */
    fun validarContrasena(contrasena: String): String? = when {
        contrasena.isBlank() -> "La contraseña no puede estar vacía."
        contrasena.length < LONGITUD_MINIMA_CONTRASENA ->
            "La contraseña debe tener al menos $LONGITUD_MINIMA_CONTRASENA caracteres."
        else -> null
    }
}