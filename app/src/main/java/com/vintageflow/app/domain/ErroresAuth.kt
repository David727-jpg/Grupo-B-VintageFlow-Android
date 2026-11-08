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
/**
 * Falla de resolución DNS, timeout o ausencia de conexión a internet.
 * Atrapa los casos donde el dispositivo pierde señal antes o durante la petición.
 */
class ErrorDeRedException(
    mensaje: String = "No hay conexión a internet. Verifica tu red."
) : Exception(mensaje)

/**
 * Violación de las reglas de seguridad de Firestore.
 * Fundamental para atrapar rechazos cuando José David configure los accesos por rol.
 */
class PermisoDenegadoException(
    mensaje: String = "No tienes permisos suficientes para realizar esta acción."
) : Exception(mensaje)

/**
 * Excepción de respaldo para caídas internas del servidor o códigos no tipificados.
 */
class ErrorDesconocidoException(
    mensaje: String = "Ocurrió un error inesperado en el servidor."
) : Exception(mensaje)