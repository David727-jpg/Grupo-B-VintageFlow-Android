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
import com.vintageflow.app.data.repository.PrendaRepositoryFirebase
import com.vintageflow.app.ui.auth.LoginScreen
import com.vintageflow.app.ui.auth.RegistroScreen
import com.vintageflow.app.ui.catalog.CatalogoScreen
import com.vintageflow.app.ui.viewmodel.AuthViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModel
import com.vintageflow.app.ui.viewmodel.PrendaViewModelFactory
import com.vintageflow.app.ui.stats.EstadisticasScreen
import com.vintageflow.app.ui.reservas.ReservasScreen
import androidx.compose.ui.platform.LocalContext
import com.vintageflow.app.ui.auth.ClienteGoogle
import com.vintageflow.app.ui.auth.RecuperarContrasenaScreen
import com.vintageflow.app.data.repository.ReservaRepositoryFirebase // [HILDA]
import com.vintageflow.app.ui.misreservas.MisReservasScreen // [HILDA]
import com.vintageflow.app.ui.viewmodel.ReservaViewModel // [HILDA]
import com.vintageflow.app.ui.viewmodel.ReservaViewModelFactory // [HILDA]
import com.vintageflow.app.ui.viewmodel.ReservasAdminViewModel // [ADMIN]
import com.vintageflow.app.ui.viewmodel.ReservasAdminViewModelFactory // [ADMIN]
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val estado by authViewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { authViewModel.restaurarSesion() }
    // Un único ViewModel compartido por catálogo, detalle y formulario.
    val prendaViewModel: PrendaViewModel = viewModel(
        factory = remember {
            PrendaViewModelFactory(PrendaRepositoryFirebase())
        }
    )
    val prendas by prendaViewModel.prendas.collectAsStateWithLifecycle()
    val cargandoPrendas by prendaViewModel.cargando.collectAsStateWithLifecycle()
    val errorPrendas by prendaViewModel.errorCarga.collectAsStateWithLifecycle()
    LaunchedEffect(estado.perfil?.uid) {
        if (estado.perfil != null) prendaViewModel.cargarPrendas()
        else prendaViewModel.detenerCarga()
    }
    val esAdministrador = estado.perfil?.puedeGestionarInventario() ?: false

    // [HILDA] Reservas del cliente, sobre el repositorio de Firestore.
    val reservaViewModel: ReservaViewModel = viewModel(
        factory = remember { ReservaViewModelFactory(ReservaRepositoryFirebase()) }
    )
    val misReservas by reservaViewModel.misReservas.collectAsStateWithLifecycle()
    val cargandoReservas by reservaViewModel.cargando.collectAsStateWithLifecycle()
    val errorReservas by reservaViewModel.error.collectAsStateWithLifecycle()
    val mensajeReservas by reservaViewModel.mensaje.collectAsStateWithLifecycle()
    val uidCliente = estado.perfil?.uid
    val nombreCliente = estado.perfil?.nombre ?: ""

    // [ADMIN] Panel de reservas del administrador (todas las reservas, en tiempo real).
    val reservasAdminViewModel: ReservasAdminViewModel = viewModel(
        factory = remember { ReservasAdminViewModelFactory(ReservaRepositoryFirebase()) }
    )
    val reservasAdmin by reservasAdminViewModel.reservas.collectAsStateWithLifecycle()
    val cargandoReservasAdmin by reservasAdminViewModel.cargando.collectAsStateWithLifecycle()
    val errorReservasAdmin by reservasAdminViewModel.error.collectAsStateWithLifecycle()
    val mensajeReservasAdmin by reservasAdminViewModel.mensaje.collectAsStateWithLifecycle()

    // Al autenticarse (login o registro) se pasa a la pantalla inicial según el rol
    // (administrador: dashboard, cliente: catálogo) y se limpia el historial de auth.
    LaunchedEffect(estado.autenticado) {
        if (estado.autenticado) {
            val inicio = if (esAdministrador) Rutas.ESTADISTICAS else Rutas.CATALOGO
            navController.navigate(inicio) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val cerrarSesion = {
        prendaViewModel.detenerCarga()
        reservaViewModel.detenerEscucha() // [HILDA]
        reservasAdminViewModel.detenerEscucha() // [ADMIN]
        authViewModel.cerrarSesion()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    // Las dos pestañas del administrador; el dashboard es la raíz del historial.
    val irADashboard = {
        navController.navigate(Rutas.ESTADISTICAS) {
            popUpTo(Rutas.ESTADISTICAS)
            launchSingleTop = true
        }
    }
    val irAInventario = {
        navController.navigate(Rutas.CATALOGO) {
            popUpTo(Rutas.ESTADISTICAS)
            launchSingleTop = true
        }
    }
    val irAReservas = {
        navController.navigate(Rutas.RESERVAS) {
            popUpTo(Rutas.ESTADISTICAS)
            launchSingleTop = true
        }
    }
    // [HILDA] Pestañas del cliente; el catálogo es la raíz de su historial.
    val irAMisReservas = {
        navController.navigate(Rutas.MIS_RESERVAS) {
            popUpTo(Rutas.CATALOGO)
            launchSingleTop = true
        }
    }
    val irACatalogoCliente = {
        navController.navigate(Rutas.CATALOGO) {
            popUpTo(Rutas.CATALOGO)
            launchSingleTop = true
        }
    }
    val barraInferior: @Composable (String) -> Unit = { ruta ->
        if (esAdministrador) {
            BarraInferior(
                rutaActual = ruta,
                onDashboard = irADashboard,
                onInventario = irAInventario,
                onReservas = irAReservas
            )
        } else if (estado.autenticado) {
            BarraInferiorCliente(
                rutaActual = ruta,
                onCatalogo = irACatalogoCliente,
                onMisReservas = irAMisReservas
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
    ) {
        composable(Rutas.LOGIN) {
            val contexto = LocalContext.current

            LoginScreen(
                estado = estado,
                onIniciarSesion = authViewModel::iniciarSesion,
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrARecuperar = {
                    authViewModel.limpiarMensajes()
                    navController.navigate(Rutas.RECUPERAR)
                },
                onIniciarConGoogle = {
                    authViewModel.iniciarSesionConGoogle { ClienteGoogle(contexto).obtenerIdToken() }
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                estado = estado,
                onRegistrar = authViewModel::registrar,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.RECUPERAR) {
            RecuperarContrasenaScreen(
                estado = estado,
                onRecuperar = { correo -> authViewModel.recuperarContrasena(correo) },
                onVolver = {
                    authViewModel.limpiarMensajes()
                    navController.popBackStack()
                }
            )
        }
        composable(Rutas.CATALOGO) {
            CatalogoScreen(
                prendas = prendas,
                onPrendaClick = { id -> navController.navigate(Rutas.detalle(id)) },
                onAgregarClick = { navController.navigate(Rutas.formulario()) },
                esAdministrador = esAdministrador,
                onCerrarSesion = cerrarSesion,
                bottomBar = { barraInferior(Rutas.CATALOGO) },
                cargando = cargandoPrendas,
                error = errorPrendas,
                onReintentar = prendaViewModel::cargarPrendas
            )
        }

        composable(Rutas.ESTADISTICAS) {
            if (!esAdministrador) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            EstadisticasScreen(
                prendas = prendas,
                onCerrarSesion = cerrarSesion,
                bottomBar = { barraInferior(Rutas.ESTADISTICAS) },
                cargando = cargandoPrendas,
                error = errorPrendas,
                onReintentar = prendaViewModel::cargarPrendas
            )
        }

        composable(Rutas.RESERVAS) {
            if (!esAdministrador) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            // [ADMIN] Escucha todas las reservas mientras el administrador está en sesión.
            LaunchedEffect(uidCliente) { reservasAdminViewModel.escuchar() }
            ReservasScreen(
                reservas = reservasAdmin,
                onAprobar = reservasAdminViewModel::aprobar,
                onCancelar = reservasAdminViewModel::cancelar,
                cargando = cargandoReservasAdmin,
                error = errorReservasAdmin,
                onReintentar = reservasAdminViewModel::escuchar,
                aviso = mensajeReservasAdmin,
                onAvisoMostrado = reservasAdminViewModel::limpiarMensaje,
                onCerrarSesion = cerrarSesion,
                bottomBar = { barraInferior(Rutas.RESERVAS) }
            )
        }

        // [HILDA] Historial de reservas del cliente (en tiempo real).
        composable(Rutas.MIS_RESERVAS) {
            if (esAdministrador || uidCliente == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            LaunchedEffect(uidCliente) { reservaViewModel.cargarMisReservas(uidCliente) }
            MisReservasScreen(
                reservas = misReservas,
                // La transacción de Firestore ya devuelve la prenda a DISPONIBLE.
                onCancelar = { reserva -> reservaViewModel.cancelar(reserva) },
                cargando = cargandoReservas,
                error = errorReservas,
                onReintentar = { reservaViewModel.cargarMisReservas(uidCliente) },
                aviso = mensajeReservas,
                onAvisoMostrado = reservaViewModel::limpiarMensaje,
                onCerrarSesion = cerrarSesion,
                bottomBar = { barraInferior(Rutas.MIS_RESERVAS) }
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
                esAdministrador = esAdministrador,
                // [HILDA] Solo el cliente autenticado puede reservar. La transacción marca
                // la prenda RESERVADA y el catálogo se actualiza solo por el listener.
                onReservar = if (!esAdministrador && uidCliente != null) {
                    { idPrenda ->
                        prendas.find { it.id == idPrenda }?.let { prenda ->
                            reservaViewModel.reservar(prenda, uidCliente, nombreCliente) {
                                navController.navigate(Rutas.MIS_RESERVAS) {
                                    popUpTo(Rutas.CATALOGO)
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                } else null
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
