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
 * Pantalla de recuperación de contraseña.
 *
 * Sin estado propio más allá del campo de texto, igual que LoginScreen:
 * recibe el estado y emite eventos. Tiene dos vistas según el formulario y la confirmación.
 *
 * La app no cambia la contraseña. Firebase envía un enlace y el cambio
 * ocurre fuera de la aplicación, de modo que la contraseña nueva nunca pasa
 * por este código.
 */
@Composable
fun RecuperarContrasenaScreen(
    estado: AuthUiState,
    onRecuperar: (correo: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var correo by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (estado.correoRecuperacionEnviado) {

            // ---------- Confirmación ----------

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Revisa tu correo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Si existe una cuenta con ese correo, recibirás " +
                                "un enlace para crear una contraseña nueva. " +
                                "Revisa también la carpeta de correo no deseado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Volver a iniciar sesión")
            }

        } else {

            // ---------- Formulario ----------

            Text(
                text = "Escribe el correo con el que te registraste y te " +
                        "enviaremos un enlace para restablecerla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            CampoValidado(
                valor = correo,
                onValorChange = { correo = it },
                etiqueta = "Correo electrónico",
                error = estado.errorCorreo,
                tipoTeclado = KeyboardType.Email,
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
                onClick = { onRecuperar(correo) },
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
                    Text("Enviar enlace")
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onVolver, enabled = !estado.cargando) {
                Text("Volver a iniciar sesión")
            }
        }
    }
}

@Preview(showBackground = true, name = "Recuperar")
@Composable
private fun RecuperarPreview() {
    MaterialTheme {
        RecuperarContrasenaScreen(
            estado = AuthUiState(),
            onRecuperar = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Recuperar con error")
@Composable
private fun RecuperarErrorPreview() {
    MaterialTheme {
        RecuperarContrasenaScreen(
            estado = AuthUiState(errorCorreo = "El formato del correo no es válido."),
            onRecuperar = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Recuperar enviado")
@Composable
private fun RecuperarEnviadoPreview() {
    MaterialTheme {
        RecuperarContrasenaScreen(
            estado = AuthUiState(correoRecuperacionEnviado = true),
            onRecuperar = {},
            onVolver = {}
        )
    }
}