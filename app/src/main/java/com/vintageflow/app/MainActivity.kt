package com.vintageflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(AuthRepositoryFirebase())
                )
                AppNavHost(authViewModel = authViewModel)
            }
        }
    }
}