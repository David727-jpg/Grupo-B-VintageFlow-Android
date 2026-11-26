package com.vintageflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vintageflow.app.data.repository.AuthRepositoryFirebase
import com.vintageflow.app.ui.navigation.AppNavHost
import com.vintageflow.app.ui.theme.VintageFlowTheme
import com.vintageflow.app.ui.viewmodel.AuthViewModel
import com.vintageflow.app.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VintageFlowTheme {
                // El Surface pinta el fondo con los colores del tema. Sin él, las pantallas
                // que no usan Scaffold (login, registro) quedan sobre el fondo claro de la
                // ventana y, con el teléfono en modo oscuro, el texto claro no se ve.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(AuthRepositoryFirebase())
                    )
                    AppNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}
