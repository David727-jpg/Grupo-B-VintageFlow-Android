package com.vintageflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vintageflow.app.data.repository.PrendaRepositoryLocal
import com.vintageflow.app.ui.auth.LoginScreen
import com.vintageflow.app.ui.auth.RegistroScreen
import com.vintageflow.app.ui.catalog.CatalogoScreen
import com.vintageflow.app.ui.viewmodel.AuthViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModelFactory

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val estado by authViewModel.estado.collectAsStateWithLifecycle()

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
            val prendaViewModel: PrendaViewModel = viewModel(
                factory = remember { PrendaViewModelFactory(PrendaRepositoryLocal()) }
            )
            val prendas by prendaViewModel.prendas.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { prendaViewModel.cargarPrendas() }

            CatalogoScreen(
                prendas = prendas,
                onPrendaClick = {},
                onAgregarClick = {},
                esAdministrador = estado.perfil?.puedeGestionarInventario() ?: false
            )
        }
    }
}
