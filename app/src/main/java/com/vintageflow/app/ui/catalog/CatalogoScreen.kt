package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.data.model.Prenda


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    prendas: List<Prenda>,
    onPrendaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
    modifier: Modifier = Modifier,
    esAdministrador: Boolean = true,
    onCerrarSesion: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Catálogo Vintage Flow") },
                actions = { TextButton(onClick = onCerrarSesion) { Text("Salir") } }
            )
        },
        floatingActionButton = {
            if (esAdministrador) {
                FloatingActionButton(onClick = onAgregarClick) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    ) { padding ->
        if (prendas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("El catálogo está vacío.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prendas, key = { it.id }) { prenda ->
                    PrendaCard(prenda = prenda, onClick = { onPrendaClick(prenda.id) })
                }
            }
        }
    }
}

@Composable
private fun PrendaCard(prenda: Prenda, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(prenda.nombre, style = MaterialTheme.typography.titleMedium)
                Text("\$%.2f".format(prenda.precio), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Talla ${prenda.talla} · ${prenda.categoria}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                prenda.estado,
                style = MaterialTheme.typography.labelLarge,
                color = if (prenda.estado == "DISPONIBLE") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogoPreview() {
    MaterialTheme {
        CatalogoScreen(prendas = prendasDePrueba, onPrendaClick = {}, onAgregarClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogoVacioPreview() {
    MaterialTheme {
        CatalogoScreen(prendas = emptyList(), onPrendaClick = {}, onAgregarClick = {})
    }
}
