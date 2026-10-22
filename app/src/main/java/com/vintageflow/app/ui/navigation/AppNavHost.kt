package com.vintageflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.vintageflow.app.ui.catalog.DetalleScreen
import com.vintageflow.app.ui.catalog.FormularioScreen
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vintageflow.app.data.repository.PrendaRepositoryLocal
import com.vintageflow.app.ui.auth.LoginScreen
import com.vintageflow.app.ui.auth.RegistroScreen
import com.vintageflow.app.ui.catalog.CatalogoScreen
import com.vintageflow.app.ui.viewmodel.AuthViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModelFactory
import com.vintageflow.app.ui.stats.EstadisticasScreen
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val estado by authViewModel.estado.collectAsStateWithLifecycle()

    // Un único ViewModel compartido por catálogo, detalle y formulario.
    val prendaViewModel: PrendaViewModel = viewModel(
        factory = remember { PrendaViewModelFactory(PrendaRepositoryLocal()) }
    )
    val prendas by prendaViewModel.prendas.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { prendaViewModel.cargarPrendas() }
    val esAdministrador = estado.perfil?.puedeGestionarInventario() ?: false

    // Al autenticarse (login o registro) se pasa al catálogo y se limpia el
    // historial de auth para que "atrás" no regrese al formulario.
    LaunchedEffect(estado.autenticado) {
        if (estado.autenticado) {
            navController.navigate(Rutas.CATALOGO) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                estado = estado,
                onIniciarSesion = authViewModel::iniciarSesion,
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                estado = estado,
                onRegistrar = authViewModel::registrar,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CATALOGO) {
            CatalogoScreen(
                prendas = prendas,
                onPrendaClick = { id -> navController.navigate(Rutas.detalle(id)) },
                onAgregarClick = { navController.navigate(Rutas.formulario()) },
                esAdministrador = esAdministrador,
                onEstadisticasClick = { navController.navigate(Rutas.ESTADISTICAS) },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ESTADISTICAS) {
            if (!esAdministrador) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            EstadisticasScreen(
                prendas = prendas,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString(Rutas.ARG_ID)
            DetalleScreen(
                prenda = prendas.find { it.id == id },
                onVolver = { navController.popBackStack() },
                onEditar = { navController.navigate(Rutas.formulario(it)) },
                onEliminar = {
                    prendaViewModel.eliminarPrenda(it)
                    navController.popBackStack()
                },
                esAdministrador = esAdministrador
            )
        }

        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(
                navArgument(Rutas.ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entrada ->
            val id = entrada.arguments?.getString(Rutas.ARG_ID)
            val prenda = prendas.find { it.id == id }

            // Un cliente, o una prenda inexistente, no debe ver el formulario.
            if (!esAdministrador || (id != null && prenda == null)) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }

            FormularioScreen(
                prendaInicial = prenda,
                onGuardar = { nueva ->
                    if (id == null) prendaViewModel.agregarPrenda(nueva)
                    else prendaViewModel.actualizarPrenda(nueva)
                    navController.popBackStack()
                },
                onCancelar = { navController.popBackStack() }
            )
        }
    }
}
