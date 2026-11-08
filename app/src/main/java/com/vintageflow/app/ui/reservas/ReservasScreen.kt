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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.data.model.EstadosReserva
import com.vintageflow.app.data.model.Reserva
import com.vintageflow.app.ui.navigation.EncabezadoAdmin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Panel de reservas del administrador. Recibe los datos por parámetros: la lectura
 * en tiempo real y las transacciones de aprobar y cancelar viven en
 * ReservasAdminViewModel y ReservaRepositoryFirebase.
 *
 * Se muestran las reservas no canceladas. PENDIENTE admite aprobar y cancelar;
 * APROBADA solo admite cancelar (la prenda vuelve a DISPONIBLE).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(
    modifier: Modifier = Modifier,
    reservas: List<Reserva> = emptyList(),
    onAprobar: (Reserva) -> Unit = {},
    onCancelar: (Reserva) -> Unit = {},
    cargando: Boolean = false,
    error: String? = null,
    onReintentar: () -> Unit = {},
    aviso: String? = null,
    onAvisoMostrado: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val activas = reservas.filter { it.estado != EstadosReserva.CANCELADA }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(aviso)
            onAvisoMostrado()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EncabezadoAdmin(
                titulo = "RESERVAS",
                onCerrarSesion = { TextButton(onClick = onCerrarSesion) { Text("SALIR") } }
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

            activas.isEmpty() -> Box(
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

            else -> Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Reservas activas",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(activas, key = { it.id }) { reserva ->
                        Column {
                            FilaReserva(
                                reserva = reserva,
                                onAprobar = { onAprobar(reserva) },
                                onCancelar = { onCancelar(reserva) }
                            )
                            HorizontalDivider()
                        }
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
                    reserva.clienteNombre.ifBlank { "Cliente" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${reserva.prendaNombre} — ${formatearFecha(reserva.fecha)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    reserva.estado,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reserva.estado == EstadosReserva.PENDIENTE) {
                Button(onClick = onAprobar) { Text("APROBAR") }
            }
            OutlinedButton(onClick = onCancelar) { Text("CANCELAR") }
        }
    }
}

private fun formatearFecha(milisegundos: Long): String =
    if (milisegundos <= 0L) "—"
    else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(milisegundos))

private val reservasDePrueba = listOf(
    Reserva("R-001", "VINT-001", "Camiseta básica", "uid-1", "Carlos Martínez", 1_762_000_000_000L, EstadosReserva.PENDIENTE),
    Reserva("R-002", "VINT-002", "Chaqueta Vintage", "uid-2", "Andrea López", 1_761_900_000_000L, EstadosReserva.APROBADA),
    Reserva("R-003", "VINT-003", "Jeans rectos", "uid-3", "Daniel Hernández", 1_761_800_000_000L, EstadosReserva.CANCELADA)
)

@Preview(showBackground = true)
@Composable
private fun ReservasPreview() {
    MaterialTheme { ReservasScreen(reservas = reservasDePrueba) }
}

@Preview(showBackground = true)
@Composable
private fun ReservasVaciasPreview() {
    MaterialTheme { ReservasScreen(reservas = emptyList()) }
}
