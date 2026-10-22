package com.vintageflow.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vintageflow.app.data.model.Prenda
import com.vintageflow.app.domain.AnalisisInventario
import com.vintageflow.app.domain.Distribucion
import com.vintageflow.app.domain.ResumenInventario


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    prendas: List<Prenda>,
    modifier: Modifier = Modifier,
    onCerrarSesion: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val resumen = AnalisisInventario.resumir(prendas)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ESTADÍSTICAS",
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    TextButton(onClick = onCerrarSesion) { Text("SALIR") }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->

        if (resumen.estaVacio) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay prendas registradas.\nRegistra una prenda para ver estadísticas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IndicadoresPrincipales(resumen)

            Spacer(Modifier.height(24.dp))

            GraficoDistribucion(
                titulo = "PRENDAS POR ESTADO",
                datos = resumen.porEstado
            )

            Spacer(Modifier.height(24.dp))

            GraficoDistribucion(
                titulo = "PRENDAS POR TALLA",
                datos = resumen.porTalla
            )

            Spacer(Modifier.height(24.dp))

            GraficoDistribucion(
                titulo = "PRENDAS POR CATEGORÍA",
                datos = resumen.porCategoria
            )

            Spacer(Modifier.height(24.dp))

            PrendaDestacada(resumen)

            Spacer(Modifier.height(32.dp))
        }
    }
}



@Composable
private fun IndicadoresPrincipales(resumen: ResumenInventario) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            "RESUMEN DEL INVENTARIO",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Indicador(
                etiqueta = "Prendas",
                valor = resumen.totalPrendas.toString(),
                modifier = Modifier.weight(1f)
            )
            Indicador(
                etiqueta = "Valor total",
                valor = moneda(resumen.valorTotal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Indicador(
                etiqueta = "Disponible",
                valor = moneda(resumen.valorDisponible),
                modifier = Modifier.weight(1f)
            )
            Indicador(
                etiqueta = "Precio promedio",
                valor = moneda(resumen.precioPromedio),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Indicador(
            etiqueta = "Porcentaje vendido",
            valor = "%.1f %%".format(resumen.tasaVendidas),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Indicador(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RectangleShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            etiqueta.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/* ------------------------------------------------------------------
 * Gráfico de barras horizontales.
 *
 * Un solo color para todas las barras: el dato que se compara es la
 * magnitud, no la identidad de cada categoría. Cada barra lleva su
 * etiqueta y su valor escritos
 * ------------------------------------------------------------------ */

@Composable
private fun GraficoDistribucion(
    titulo: String,
    datos: List<Distribucion>,
    modifier: Modifier = Modifier
) {
    if (datos.isEmpty()) return

    val maximo = datos.maxOf { it.cantidad }

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        datos.forEach { fila ->
            BarraHorizontal(
                etiqueta = fila.etiqueta,
                cantidad = fila.cantidad,
                maximo = maximo
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BarraHorizontal(
    etiqueta: String,
    cantidad: Int,
    maximo: Int
) {
    val proporcion = if (maximo == 0) 0f else cantidad.toFloat() / maximo

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Etiqueta escrita: la identidad no depende del color.
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(96.dp)
        )

        // Riel de fondo más la barra proporcional.
        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(proporcion)
                    .height(18.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            topStart = 2.dp,
                            bottomStart = 2.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                    )
            )
        }

        Spacer(Modifier.width(10.dp))

        // Valor escrito al final de cada barra.
        Text(
            cantidad.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

/* ------------------------------------------------------------------ */

@Composable
private fun PrendaDestacada(resumen: ResumenInventario) {
    val prenda = resumen.prendaMasCara ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "PRENDA DE MAYOR VALOR",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RectangleShape)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    prenda.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${prenda.categoria} · Talla ${prenda.talla} · ${prenda.estado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                moneda(prenda.precio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun moneda(valor: Double): String = "$%.2f".format(valor)

/* ------------------------------------------------------------------
 * Vistas previas
 * ------------------------------------------------------------------ */

private val PRENDAS_DEMO = listOf(
    Prenda("V-001", "Chaqueta Retro", "L", "Chaquetas", 45.0, "DISPONIBLE"),
    Prenda("V-002", "Blusa de Seda", "S", "Blusas", 18.5, "DISPONIBLE"),
    Prenda("V-003", "Jeans Levis 501", "M", "Pantalones", 35.0, "VENDIDO"),
    Prenda("V-004", "Vestido Floral", "S", "Vestidos", 22.5, "RESERVADO"),
    Prenda("V-005", "Camisa a Cuadros", "M", "Camisas", 15.75, "DISPONIBLE"),
    Prenda("V-006", "Abrigo de Lana", "XL", "Chaquetas", 55.0, "DISPONIBLE"),
    Prenda("V-007", "Botines de Cuero", "M", "Zapatos", 40.0, "VENDIDO")
)

@Preview(showBackground = true, name = "Estadísticas", heightDp = 1100)
@Composable
private fun EstadisticasPreview() {
    MaterialTheme {
        EstadisticasScreen(prendas = PRENDAS_DEMO)
    }
}

@Preview(showBackground = true, name = "Estadísticas sin datos")
@Composable
private fun EstadisticasVaciasPreview() {
    MaterialTheme {
        EstadisticasScreen(prendas = emptyList())
    }
}