package com.vintageflow.app.ui.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.ui.navigation.EncabezadoAdmin

@Composable
fun ReservasScreen(
    prendasReservadas: List<Prenda>,
    onPrendaClick: (String) -> Unit,
    onAprobar: (String) -> Unit,
    onCancelar: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    cargando: Boolean = false,
    error: String? = null,
    onReintentar: () -> Unit = {},
    resolviendoReserva: String? = null,
    errorReserva: String? = null
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EncabezadoAdmin(
                titulo = "RESERVAS",
                onCerrarSesion = { TextButton(onClick = onCerrarSesion) { Text("SALIR") } }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        when {
            cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error, textAlign = TextAlign.Center)
                TextButton(onClick = onReintentar) { Text("REINTENTAR") }
            }
            prendasReservadas.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (errorReserva != null) {
                    Text(errorReserva, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                Text(
                    "No hay prendas reservadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Prendas reservadas",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                if (errorReserva != null) {
                    Text(
                        errorReserva,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(prendasReservadas, key = { it.id }) { prenda ->
                        FilaPrendaReservada(
                            prenda = prenda,
                            onPrendaClick = onPrendaClick,
                            onAprobar = onAprobar,
                            onCancelar = onCancelar,
                            accionesHabilitadas = resolviendoReserva == null
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaPrendaReservada(
    prenda: Prenda,
    onPrendaClick: (String) -> Unit,
    onAprobar: (String) -> Unit,
    onCancelar: (String) -> Unit,
    accionesHabilitadas: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onPrendaClick(prenda.id) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imagen", style = MaterialTheme.typography.labelSmall)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(prenda.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${prenda.categoria} · Talla ${prenda.talla}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("$%.2f".format(prenda.precio), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { onAprobar(prenda.id) }, enabled = accionesHabilitadas) {
                Text("APROBAR")
            }
            OutlinedButton(onClick = { onCancelar(prenda.id) }, enabled = accionesHabilitadas) {
                Text("CANCELAR")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservasPreview() {
    MaterialTheme {
        ReservasScreen(
            prendasReservadas = listOf(
                Prenda(id = "1", nombre = "Chaqueta", talla = "M", categoria = "Chaquetas", precio = 30.0, estado = "RESERVADO")
            ),
            onPrendaClick = {},
            onAprobar = {},
            onCancelar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservasVaciasPreview() {
    MaterialTheme {
        ReservasScreen(prendasReservadas = emptyList(), onPrendaClick = {}, onAprobar = {}, onCancelar = {})
    }
}
