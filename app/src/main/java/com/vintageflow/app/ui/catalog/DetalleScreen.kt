package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.data.model.Prenda


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    prenda: Prenda?,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminar: (String) -> Unit,
    modifier: Modifier = Modifier,
    esAdministrador: Boolean = true
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(prenda?.nombre ?: "Detalle") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("Volver") } }
            )
        }
    ) { padding ->
        if (prenda == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Prenda no encontrada.")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatoFila("ID", prenda.id)
                DatoFila("Nombre", prenda.nombre)
                DatoFila("Talla", prenda.talla)
                DatoFila("Categoría", prenda.categoria)
                DatoFila("Precio", "\$%.2f".format(prenda.precio))
                DatoFila("Estado", prenda.estado)

                if (esAdministrador) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { onEditar(prenda.id) }) { Text("Editar") }
                        OutlinedButton(onClick = { mostrarConfirmacion = true }) { Text("Eliminar") }
                    }
                }
            }

            if (mostrarConfirmacion) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacion = false },
                    title = { Text("Eliminar prenda") },
                    text = { Text("¿Seguro que quieres eliminar \"${prenda.nombre}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarConfirmacion = false
                            onEliminar(prenda.id)
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetallePreview() {
    MaterialTheme {
        DetalleScreen(
            prenda = prendasDePrueba.first(),
            onVolver = {}, onEditar = {}, onEliminar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleNoEncontradaPreview() {
    MaterialTheme {
        DetalleScreen(prenda = null, onVolver = {}, onEditar = {}, onEliminar = {})
    }
}
