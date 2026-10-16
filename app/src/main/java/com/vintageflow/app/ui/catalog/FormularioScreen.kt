package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.data.model.Prenda

private val ESTADOS = listOf("DISPONIBLE", "RESERVADO", "VENDIDO")

// CODIGO INTEGRANTE J: Se rediseñó la función genérica de textos para el catálogo.
// Ahora limpia espacios en blanco (.trim()) y exige un mínimo de 3 caracteres, 
// previniendo que se guarden prendas sin nombre o talla definida.
fun validarTextoBasico(valor: String): String? {
    val textoLimpio = valor.trim()
    return when {
        textoLimpio.isEmpty() -> "Este campo es obligatorio"
        textoLimpio.length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }
}
// ______

// CODIGO INTEGRANTE J: Se agregó el manejo de errores para evitar que el usuario
// deje el campo de precio vacío o ingrese valores de $0.00, asegurando la integridad de datos.
fun validarPrecioBasico(valor: String): String? {
    val textoLimpio = valor.trim()
    if (textoLimpio.isEmpty()) return "El precio es obligatorio"

    val numero = textoLimpio.replace(',', '.').toDoubleOrNull()
    return when {
        numero == null -> "Ingresa un número válido (ej. 15.50)"
        numero <= 0 -> "El precio debe ser mayor a $0.00"
        else -> null
    }
}
// ______

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    onGuardar: (Prenda) -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    prendaInicial: Prenda? = null,
    validarTexto: (String) -> String? = ::validarTextoBasico,
    validarPrecio: (String) -> String? = ::validarPrecioBasico
) {
    val esNueva = prendaInicial == null

    var id by remember { mutableStateOf(prendaInicial?.id ?: "") }
    var nombre by remember { mutableStateOf(prendaInicial?.nombre ?: "") }
    var talla by remember { mutableStateOf(prendaInicial?.talla ?: "") }
    var categoria by remember { mutableStateOf(prendaInicial?.categoria ?: "") }
    var precio by remember { mutableStateOf(prendaInicial?.precio?.toString() ?: "") }
    var estado by remember { mutableStateOf(prendaInicial?.estado ?: "DISPONIBLE") }
    var intentoGuardar by remember { mutableStateOf(false) }

    val errId = if (esNueva) validarTexto(id) else null
    val errNombre = validarTexto(nombre)
    val errTalla = validarTexto(talla)
    val errCategoria = validarTexto(categoria)
    val errPrecio = validarPrecio(precio)
    val hayErrores = listOf(errId, errNombre, errTalla, errCategoria, errPrecio).any { it != null }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(if (esNueva) "Nueva prenda" else "Editar prenda") }) }
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
            CampoTexto("Talla", talla, { talla = it }, errTalla, intentoGuardar)
            CampoTexto("Categoría", categoria, { categoria = it }, errCategoria, intentoGuardar)
            CampoTexto(
                "Precio", precio, { precio = it }, errPrecio, intentoGuardar,
                teclado = KeyboardType.Decimal
            )

            Text("Estado", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            ESTADOS.forEach { opcion ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { estado = opcion },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado == opcion, onClick = { estado = opcion })
                    Text(opcion)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = {
                    if (hayErrores) {
                        intentoGuardar = true
                    } else {
                        val precioNumero = precio.trim().replace(',', '.').toDouble()
                        val resultado = prendaInicial?.copy(
                            nombre = nombre.trim(),
                            talla = talla.trim(),
                            categoria = categoria.trim(),
                            precio = precioNumero,
                            estado = estado
                        ) ?: Prenda(
                            id = id.trim(),
                            nombre = nombre.trim(),
                            talla = talla.trim(),
                            categoria = categoria.trim(),
                            precio = precioNumero,
                            estado = estado
                        )
                        onGuardar(resultado)
                    }
                }) { Text("Guardar") }
                OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
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