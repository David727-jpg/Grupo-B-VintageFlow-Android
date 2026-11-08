package com.vintageflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Barra inferior del cliente: alterna entre el catálogo y sus reservas. */
@Composable
fun BarraInferiorCliente(
    rutaActual: String,
    onCatalogo: () -> Unit,
    onMisReservas: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = rutaActual == Rutas.CATALOGO,
            onClick = onCatalogo,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("CATÁLOGO") }
        )
        NavigationBarItem(
            selected = rutaActual == Rutas.MIS_RESERVAS,
            onClick = onMisReservas,
            icon = { Icon(IconoReservas, contentDescription = null) },
            label = { Text("MIS RESERVAS") }
        )
    }
}
