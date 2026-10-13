package com.vintageflow.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.ui.viewmodel.AuthUiState

/**
 * Pantalla de inicio de sesión.
 */
@Composable
fun LoginScreen(
    estado: AuthUiState,
    onIniciarSesion: (correo: String, contrasena: String) -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Vintage Flow",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Inventario de ropa de segunda mano",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        CampoValidado(
            valor = correo,
            onValorChange = { correo = it },
            etiqueta = "Correo electrónico",
            error = estado.errorCorreo,
            tipoTeclado = KeyboardType.Email,
            habilitado = !estado.cargando
        )

        Spacer(Modifier.height(12.dp))

        CampoValidado(
            valor = contrasena,
            onValorChange = { contrasena = it },
            etiqueta = "Contraseña",
            error = estado.errorContrasena,
            esContrasena = true,
            imeAction = ImeAction.Done,
            habilitado = !estado.cargando
        )

        if (estado.errorGeneral != null) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = estado.errorGeneral,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onIniciarSesion(correo, contrasena) },
            enabled = !estado.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión")
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onIrARegistro, enabled = !estado.cargando) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

@Preview(showBackground = true, name = "Login")
@Composable
private fun LoginPreview() {
    MaterialTheme {
        LoginScreen(
            estado = AuthUiState(),
            onIniciarSesion = { _, _ -> },
            onIrARegistro = {}
        )
    }
}

@Preview(showBackground = true, name = "Login con error")
@Composable
private fun LoginErrorPreview() {
    MaterialTheme {
        LoginScreen(
            estado = AuthUiState(
                errorCorreo = "El formato del correo no es válido.",
                errorGeneral = "El correo o la contraseña son incorrectos."
            ),
            onIniciarSesion = { _, _ -> },
            onIrARegistro = {}
        )
    }
}