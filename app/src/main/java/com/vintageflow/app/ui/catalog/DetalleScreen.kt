package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vintageflow.app.data.model.Prenda


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    prenda: Prenda?,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminar: (String) -> Unit,
    modifier: Modifier = Modifier,
    esAdministrador: Boolean = true,
    onReservar: ((String) -> Unit)? = null
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "VINTAGE FLOW",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        bottomBar = {
            if (prenda != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (esAdministrador) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onEditar(prenda.id) },
                                shape = RectangleShape,
                                modifier = Modifier.weight(1f)
                            ) { Text("EDITAR") }
                            OutlinedButton(
                                onClick = { mostrarConfirmacion = true },
                                shape = RectangleShape,
                                modifier = Modifier.weight(1f)
                            ) { Text("ELIMINAR") }
                        }
                    } else {
                        val disponible = prenda.estado.equals(ESTADO_DISPONIBLE, ignoreCase = true)
                        Button(
                            onClick = { onReservar?.invoke(prenda.id) },
                            enabled = disponible && onReservar != null,
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("RESERVAR ARTÍCULO") }

                        val aviso = when {
                            onReservar == null -> "Las reservas estarán disponibles en una próxima entrega."
                            !disponible -> "Esta prenda ya no está disponible."
                            else -> null
                        }
                        if (aviso != null) {
                            Text(
                                aviso,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                MarcadorFoto(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(prenda.nombre.uppercase(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Talla ${prenda.talla}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("\$%.2f".format(prenda.precio), style = MaterialTheme.typography.titleMedium)
                    EtiquetaEstado(prenda.estado)
                    if (prenda.descripcion.isNotBlank()) {
                        Text(
                            prenda.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        "Categoría: ${prenda.categoria}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (esAdministrador) {
                        Text(
                            "ID: ${prenda.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
private fun EtiquetaEstado(estado: String) {
    Surface(
        shape = RectangleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            estado,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleClientePreview() {
    MaterialTheme {
        DetalleScreen(
            prenda = prendasDePrueba.first(),
            onVolver = {}, onEditar = {}, onEliminar = {},
            esAdministrador = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleAdminPreview() {
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
