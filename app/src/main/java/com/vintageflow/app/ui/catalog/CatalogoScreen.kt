package com.vintageflow.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vintageflow.app.data.model.Prenda


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    prendas: List<Prenda>,
    onPrendaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
    modifier: Modifier = Modifier,
    esAdministrador: Boolean = true,
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var talla by remember { mutableStateOf<String?>(null) }
    var categoria by remember { mutableStateOf<String?>(null) }
    var rango by remember { mutableStateOf(RangoPrecio.TODOS) }

    val visibles = filtrarPrendas(prendas, talla, categoria, rango)
    val hayFiltros = talla != null || categoria != null || rango != RangoPrecio.TODOS

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "VINTAGE FLOW",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    TextButton(onClick = onCerrarSesion) { Text("SALIR") }
                }
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            if (esAdministrador) {
                FloatingActionButton(onClick = onAgregarClick) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroDesplegable(
                    texto = talla ?: "TALLA",
                    opciones = listOf("Todas") + TALLAS,
                    onSeleccion = { talla = if (it == "Todas") null else it },
                    modifier = Modifier.weight(1f)
                )
                FiltroDesplegable(
                    texto = categoria ?: "CATEGORÍA",
                    opciones = listOf("Todas") + CATEGORIAS,
                    onSeleccion = { categoria = if (it == "Todas") null else it },
                    modifier = Modifier.weight(1f)
                )
                FiltroDesplegable(
                    texto = if (rango == RangoPrecio.TODOS) "PRECIO" else rango.etiqueta,
                    opciones = RangoPrecio.values().map { it.etiqueta },
                    onSeleccion = { etiqueta ->
                        rango = RangoPrecio.values().first { it.etiqueta == etiqueta }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                prendas.isEmpty() -> MensajeCentrado("El catálogo está vacío.")
                visibles.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ninguna prenda coincide con los filtros.")
                    if (hayFiltros) {
                        TextButton(onClick = {
                            talla = null
                            categoria = null
                            rango = RangoPrecio.TODOS
                        }) { Text("QUITAR FILTROS") }
                    }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sin "key": si hubiera ids repetidos, una key duplicada cerraría la app.
                    items(visibles) { prenda ->
                        PrendaTarjeta(prenda = prenda, onClick = { onPrendaClick(prenda.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroDesplegable(
    texto: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { abierto = true },
            shape = RectangleShape,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
            Text(
                "$texto ▾",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        abierto = false
                        onSeleccion(opcion)
                    }
                )
            }
        }
    }
}

@Composable
private fun PrendaTarjeta(prenda: Prenda, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        MarcadorFoto(modifier = Modifier.fillMaxWidth().aspectRatio(0.85f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            prenda.nombre.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Talla ${prenda.talla}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("\$%.2f".format(prenda.precio), style = MaterialTheme.typography.labelMedium)
        }
        if (!prenda.estado.equals(ESTADO_DISPONIBLE, ignoreCase = true)) {
            Text(
                prenda.estado,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}


@Composable
internal fun MarcadorFoto(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "FOTO PENDIENTE",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MensajeCentrado(texto: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(texto) }
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
