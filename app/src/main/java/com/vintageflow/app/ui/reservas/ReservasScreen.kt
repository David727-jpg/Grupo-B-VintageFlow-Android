package com.vintageflow.app.ui.reservas

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Reserva(
    val id: String,
    val cliente: String,
    val prenda: String,
    val hora: String
)

// Datos de muestra mientras no exista una fuente real de reservas.
val reservasDePrueba = listOf(
    Reserva("R-001", "Carlos Martínez", "Camiseta básica", "10:30 AM"),
    Reserva("R-002", "Andrea López", "Chaqueta Vintage", "12:00 PM"),
    Reserva("R-003", "Daniel Hernández", "Jeans rectos", "2:30 PM"),
    Reserva("R-004", "Sofía García", "Camisa casual", "4:00 PM")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(
    modifier: Modifier = Modifier,
    reservasIniciales: List<Reserva> = reservasDePrueba,
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var reservas by remember { mutableStateOf(reservasIniciales) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "RESERVAS",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                actions = { TextButton(onClick = onCerrarSesion) { Text("SALIR") } }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        if (reservas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay reservas activas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Reservas activas",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(reservas, key = { it.id }) { reserva ->
                        FilaReserva(
                            reserva = reserva,
                            onAprobar = { reservas = reservas - reserva },
                            onCancelar = { reservas = reservas - reserva }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaReserva(
    reserva: Reserva,
    onAprobar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin imagen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    reserva.cliente,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${reserva.prenda} — ${reserva.hora}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onAprobar) { Text("APROBAR") }
            OutlinedButton(onClick = onCancelar) { Text("CANCELAR") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservasPreview() {
    MaterialTheme { ReservasScreen() }
}

@Preview(showBackground = true)
@Composable
private fun ReservasVaciasPreview() {
    MaterialTheme { ReservasScreen(reservasIniciales = emptyList()) }
}
