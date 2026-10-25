package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vintageflow.app.data.model.Prenda


fun validarTextoBasico(valor: String): String? =
    if (valor.isBlank()) "Este campo es obligatorio" else null

fun validarPrecioBasico(valor: String): String? {
    val numero = valor.trim().replace(',', '.').toDoubleOrNull()
    return when {
        numero == null -> "Ingresa un número válido"
        numero <= 0 -> "El precio debe ser mayor a 0"
        else -> null
    }
}

/**
 * Formulario para registrar (prendaInicial = null) o editar una prenda.
 *
 * - Talla y categoría se eligen de listas cerradas (manual, sección 10.5).
 * - Una prenda nueva siempre nace DISPONIBLE; al editar solo se ofrecen los
 *   cambios de estado permitidos por la regla RN-04.
 * - idsExistentes (opcional): ids ya registrados, para rechazar duplicados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    onGuardar: (Prenda) -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    prendaInicial: Prenda? = null,
    idsExistentes: Set<String> = emptySet(),
    validarTexto: (String) -> String? = ::validarTextoBasico,
    validarPrecio: (String) -> String? = ::validarPrecioBasico
) {
    val esNueva = prendaInicial == null

    var id by remember { mutableStateOf(prendaInicial?.id ?: "") }
    var nombre by remember { mutableStateOf(prendaInicial?.nombre ?: "") }
    var talla by remember { mutableStateOf(prendaInicial?.talla ?: "") }
    var categoria by remember { mutableStateOf(prendaInicial?.categoria ?: "") }
    var precio by remember { mutableStateOf(prendaInicial?.precio?.toString() ?: "") }
    var estado by remember {
        mutableStateOf(prendaInicial?.estado?.uppercase() ?: ESTADO_DISPONIBLE)
    }
    var intentoGuardar by remember { mutableStateOf(false) }

    val errId = if (esNueva) {
        validarTexto(id)
            ?: if (idsExistentes.any { it.equals(id.trim(), ignoreCase = true) }) {
                "Ya existe una prenda con ese identificador"
            } else null
    } else null
    val errNombre = validarTexto(nombre)
    val errTalla = if (talla.isBlank()) "Selecciona una talla" else null
    val errCategoria = if (categoria.isBlank()) "Selecciona una categoría" else null
    val errPrecio = validarPrecio(precio)
    val hayErrores = listOf(errId, errNombre, errTalla, errCategoria, errPrecio).any { it != null }

    // Si la prenda ya traía un valor fuera de la lista, se conserva como opción.
    val opcionesTalla = if (talla.isNotBlank() && talla !in TALLAS) TALLAS + talla else TALLAS
    val opcionesCategoria =
        if (categoria.isNotBlank() && categoria !in CATEGORIAS) CATEGORIAS + categoria else CATEGORIAS

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esNueva) "NUEVA PRENDA" else "EDITAR PRENDA",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoTexto("ID", id, { id = it }, errId, intentoGuardar, enabled = esNueva)
            CampoTexto("Nombre", nombre, { nombre = it }, errNombre, intentoGuardar)
            SelectorDesplegable("Talla", talla, opcionesTalla, { talla = it }, errTalla, intentoGuardar)
            SelectorDesplegable(
                "Categoría", categoria, opcionesCategoria, { categoria = it },
                errCategoria, intentoGuardar
            )
            CampoTexto(
                "Precio", precio, { precio = it }, errPrecio, intentoGuardar,
                teclado = KeyboardType.Decimal
            )

            Text(
                "Estado",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (esNueva) {
                Text(
                    "Toda prenda nueva se registra como DISPONIBLE.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val permitidos = estadosPermitidos(prendaInicial?.estado ?: ESTADO_DISPONIBLE)
                if (permitidos.size == 1) {
                    Text(
                        "Esta prenda está ${permitidos.first()} y no admite cambios de estado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    permitidos.forEach { opcion ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { estado = opcion },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = estado == opcion, onClick = { estado = opcion })
                            Text(opcion)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (hayErrores) {
                            intentoGuardar = true
                        } else {
                            val precioNumero = precio.trim().replace(',', '.').toDouble()
                            // Al editar se usa copy() para conservar descripcion e imagenUrl,
                            // que este formulario no muestra.
                            val resultado = prendaInicial?.copy(
                                nombre = nombre.trim(),
                                talla = talla,
                                categoria = categoria,
                                precio = precioNumero,
                                estado = estado
                            ) ?: Prenda(
                                id = id.trim(),
                                nombre = nombre.trim(),
                                talla = talla,
                                categoria = categoria,
                                precio = precioNumero,
                                estado = ESTADO_DISPONIBLE
                            )
                            onGuardar(resultado)
                        }
                    },
                    shape = RectangleShape
                ) { Text("GUARDAR") }
                OutlinedButton(onClick = onCancelar, shape = RectangleShape) { Text("CANCELAR") }
            }
        }
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    mostrarError: Boolean,
    enabled: Boolean = true,
    teclado: KeyboardType = KeyboardType.Text
) {
    val verError = mostrarError && error != null
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = verError,
        supportingText = { if (verError) Text(error ?: "") },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SelectorDesplegable(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    error: String?,
    mostrarError: Boolean
) {
    var abierto by remember { mutableStateOf(false) }
    val verError = mostrarError && error != null
    Column(modifier = Modifier.fillMaxWidth()) {
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    if (valor.isBlank()) "$etiqueta ▾" else "$etiqueta: $valor ▾",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            abierto = false
                            onSeleccion(opcion)
                        }
                    )
                }
            }
        }
        if (verError) {
            Text(
                error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormularioNuevaPreview() {
    MaterialTheme { FormularioScreen(onGuardar = {}, onCancelar = {}) }
}

@Preview(showBackground = true)
@Composable
private fun FormularioEdicionPreview() {
    MaterialTheme {
        FormularioScreen(onGuardar = {}, onCancelar = {}, prendaInicial = prendasDePrueba.first())
    }
}
