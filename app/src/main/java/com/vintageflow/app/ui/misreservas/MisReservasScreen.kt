package com.vintageflow.app.ui.misreservas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vintageflow.app.data.model.EstadosReserva
import com.vintageflow.app.data.model.Reserva
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Historial de reservas del cliente (requerimiento 4).
 * Recibe todo por parámetros: no conoce Firestore ni el ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    reservas: List<Reserva>,
    onCancelar: (Reserva) -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    error: String? = null,
    onReintentar: () -> Unit = {},
    aviso: String? = null,
    onAvisoMostrado: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(aviso)
            onAvisoMostrado()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MIS RESERVAS",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                actions = { TextButton(onClick = onCerrarSesion) { Text("SALIR") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar
    ) { padding ->
        when {
            cargando && reservas.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null && reservas.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error, textAlign = TextAlign.Center)
                TextButton(onClick = onReintentar) { Text("REINTENTAR") }
            }

            reservas.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no tienes reservas. Reserva una prenda desde el catálogo.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reservas) { reserva ->
                    Column {
                        ReservaFila(reserva = reserva, onCancelar = { reservaACancelar = reserva })
                        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }

        reservaACancelar?.let { reserva ->
            AlertDialog(
                onDismissRequest = { reservaACancelar = null },
                title = { Text("Cancelar reserva") },
                text = { Text("¿Seguro que quieres cancelar la reserva de \"${reserva.prendaNombre}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        reservaACancelar = null
                        onCancelar(reserva)
                    }) { Text("Cancelar reserva") }
                },
                dismissButton = {
                    TextButton(onClick = { reservaACancelar = null }) { Text("Volver") }
                }
            )
        }
    }
}

@Composable
private fun ReservaFila(reserva: Reserva, onCancelar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                reserva.prendaNombre.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                formatearFecha(reserva.fecha),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EtiquetaEstadoReserva(reserva.estado)
        }
        if (reserva.estado != EstadosReserva.CANCELADA) {
            OutlinedButton(onClick = onCancelar, shape = RectangleShape) { Text("CANCELAR") }
        }
    }
}

@Composable
private fun EtiquetaEstadoReserva(estado: String) {
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

private fun formatearFecha(milisegundos: Long): String =
    if (milisegundos <= 0L) "—"
    else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(milisegundos))

private val reservasDePruebaCliente = listOf(
    Reserva("R-001", "VINT-001", "Chaqueta Retro", "uid-1", "Ana", 1_762_000_000_000L, EstadosReserva.PENDIENTE),
    Reserva("R-002", "VINT-004", "Vestido Lunares", "uid-1", "Ana", 1_761_900_000_000L, EstadosReserva.APROBADA),
    Reserva("R-003", "VINT-003", "Camisa Floral 90s", "uid-1", "Ana", 1_761_800_000_000L, EstadosReserva.CANCELADA)
)

@Preview(showBackground = true)
@Composable
private fun MisReservasPreview() {
    MaterialTheme { MisReservasScreen(reservas = reservasDePruebaCliente, onCancelar = {}) }
}

@Preview(showBackground = true)
@Composable
private fun MisReservasVaciasPreview() {
    MaterialTheme { MisReservasScreen(reservas = emptyList(), onCancelar = {}) }
}

@Preview(showBackground = true)
@Composable
private fun MisReservasErrorPreview() {
    MaterialTheme {
        MisReservasScreen(
            reservas = emptyList(), onCancelar = {},
            error = "No se pudieron cargar tus reservas."
        )
    }
}
