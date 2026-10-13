package com.vintageflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vintageflow.app.data.repository.AuthRepositoryFirebase
import com.vintageflow.app.ui.auth.LoginScreen
import com.vintageflow.app.ui.auth.RegistroScreen
import com.vintageflow.app.ui.theme.VintageFlowTheme
import com.vintageflow.app.ui.viewmodel.AuthViewModel
import com.vintageflow.app.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VintageFlowTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(AuthRepositoryFirebase())
                )
                val estado by authViewModel.estado.collectAsStateWithLifecycle()
                var enRegistro by rememberSaveable { mutableStateOf(false) }

                when {
                    estado.autenticado -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(estado.perfil?.descripcion() ?: "")
                    }

                    enRegistro -> RegistroScreen(
                        estado = estado,
                        onRegistrar = authViewModel::registrar,
                        onVolver = { enRegistro = false }
                    )

                    else -> LoginScreen(
                        estado = estado,
                        onIniciarSesion = authViewModel::iniciarSesion,
                        onIrARegistro = { enRegistro = true }
                    )
                }
            }
        }
    }
}