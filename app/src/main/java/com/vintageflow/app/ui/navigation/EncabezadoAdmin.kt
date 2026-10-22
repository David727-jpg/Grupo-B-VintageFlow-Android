package com.vintageflow.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Barra superior del administrador: marca "VINTAGE FLOW - ADMIN" arriba y el título de la pantalla debajo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncabezadoAdmin(
    titulo: String,
    onCerrarSesion: @Composable () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Text(
                "VINTAGE FLOW - ADMIN",
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                actions = { onCerrarSesion() },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    }
}
