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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vintageflow.app.ui.viewmodel.AuthUiState

/**
 * Pantalla de registro.
 *
 * Las cuentas creadas desde la app recibiran el rol CLIENTE. El rol ADMINISTRADOR se asigna solo por Firestore, para que nadie
 * pueda darse permisos de inventario desde la app.
 */
@Composable
fun RegistroScreen(
    estado: AuthUiState,
    onRegistrar: (nombre: String, correo: String, contrasena: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by rememberSaveable { mutableStateOf("") }
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
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CampoValidado(
            valor = nombre,
            onValorChange = { nombre = it },
            etiqueta = "Nombre completo",
            error = estado.errorNombre,
            habilitado = !estado.cargando
        )

        Spacer(Modifier.height(12.dp))

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
            etiqueta = "Contraseña (mínimo 6 caracteres)",
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
            onClick = { onRegistrar(nombre, correo, contrasena) },
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
                Text("Registrarme")
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onVolver, enabled = !estado.cargando) {
            Text("Ya tengo cuenta")
        }
    }
}

@Preview(showBackground = true, name = "Registro")
@Composable
private fun RegistroPreview() {
    MaterialTheme {
        RegistroScreen(
            estado = AuthUiState(),
            onRegistrar = { _, _, _ -> },
            onVolver = {}
        )
    }
}