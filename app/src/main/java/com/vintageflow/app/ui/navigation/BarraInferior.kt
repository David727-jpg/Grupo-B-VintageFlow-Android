package com.vintageflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Barra inferior del administrador: alterna entre Dashboard e Inventario. */
@Composable
fun BarraInferior(
    rutaActual: String,
    onDashboard: () -> Unit,
    onInventario: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = rutaActual == Rutas.ESTADISTICAS,
            onClick = onDashboard,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("DASHBOARD") }
        )
        NavigationBarItem(
            selected = rutaActual == Rutas.CATALOGO,
            onClick = onInventario,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("INVENTARIO") }
        )
    }
}
