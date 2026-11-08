package com.vintageflow.app.domain

/**
 * Errores de autenticación expresados como conceptos del dominio.
 *
 * Existen para que las capas de arriba no tengan que importar las
 * excepciones de Firebase. `
 *
 * Además son construibles en una prueba unitaria, lo que no siempre ocurre
 * con las excepciones del SDK.
 */

/**
 * No hay ninguna cuenta registrada con el correo indicado.
 *
 * Que este caso tenga nombre propio importa: la pantalla de recuperación
 * lo trata como éxito a propósito, para no revelar qué correos tienen
 * cuenta en la tienda.
 */
class CorreoNoRegistradoException(
    mensaje: String = "No existe una cuenta con ese correo."
) : Exception(mensaje)

/**
 * El usuario cerró la ventana de Google sin elegir cuenta.
 */
class GoogleCanceladoException(
    mensaje: String = "Inicio de sesión con Google cancelado."
) : Exception(mensaje)

/**
 * El dispositivo no tiene ninguna cuenta de Google disponible.
 *
 * Es el caso típico de un emulador sin Play Services o sin cuenta añadida.
 * Merece un mensaje distinto porque la solución no está en la app.
 */
class GoogleSinCuentaException(
    mensaje: String = "No hay ninguna cuenta de Google en este dispositivo."
) : Exception(mensaje)