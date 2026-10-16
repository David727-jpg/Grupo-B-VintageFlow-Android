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

    // CODIGO INTEGRANTE J: Estados para gestionar las validaciones de los tres campos de registro.
    var errorNombreLocal by rememberSaveable { mutableStateOf<String?>(null) }
    var errorCorreoLocal by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasenaLocal by rememberSaveable { mutableStateOf<String?>(null) }
    // ______

    // CODIGO INTEGRANTE J: Lógica de validación que bloquea el registro si no se cumplen
    // las longitudes mínimas requeridas por Firebase Auth (contraseñas de 6 caracteres).
    fun validarYRegistrar() {
        var esValido = true
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-z]{2,}\$".toRegex()

        if (nombre.trim().length < 3) {
            errorNombreLocal = "El nombre debe tener al menos 3 letras"
            esValido = false
        } else {
            errorNombreLocal = null
        }

        if (correo.isBlank()) {
            errorCorreoLocal = "El correo es obligatorio"
            esValido = false
        } else if (!correo.matches(emailRegex)) {
            errorCorreoLocal = "Ingresa un formato de correo válido"
            esValido = false
        } else {
            errorCorreoLocal = null
        }

        if (contrasena.length < 6) {
            errorContrasenaLocal = "Debe tener un mínimo de 6 caracteres"
            esValido = false
        } else {
            errorContrasenaLocal = null
        }

        if (esValido) {
            onRegistrar(nombre.trim(), correo.trim(), contrasena)
        }
    }
    // ______

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
            onValorChange = { 
                nombre = it
                // CODIGO INTEGRANTE J: Limpia el error al modificar el nombre.
                errorNombreLocal = null 
                // ______
            },
            etiqueta = "Nombre completo",
            // CODIGO INTEGRANTE J: Prioriza el mensaje de error local.
            error = errorNombreLocal ?: estado.errorNombre,
            // ______
            habilitado = !estado.cargando
        )

        Spacer(Modifier.height(12.dp))

        CampoValidado(
            valor = correo,
            onValorChange = { 
                correo = it
                // CODIGO INTEGRANTE J: Limpia el error local del correo
                errorCorreoLocal = null 
                // ______
            },
            etiqueta = "Correo electrónico",
            // CODIGO INTEGRANTE J: Prioriza error local
            error = errorCorreoLocal ?: estado.errorCorreo,
            // ______
            tipoTeclado = KeyboardType.Email,
            habilitado = !estado.cargando
        )

        Spacer(Modifier.height(12.dp))

        CampoValidado(
            valor = contrasena,
            onValorChange = { 
                contrasena = it
                // CODIGO INTEGRANTE J: Limpia error local de contraseña
                errorContrasenaLocal = null 
                // ______
            },
            etiqueta = "Contraseña (mínimo 6 caracteres)",
            // CODIGO INTEGRANTE J: Prioriza error local
            error = errorContrasenaLocal ?: estado.errorContrasena,
            // ______
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
            // CODIGO INTEGRANTE J: Ejecuta la función de validación antes de continuar.
            onClick = { validarYRegistrar() }, 
            // ______
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