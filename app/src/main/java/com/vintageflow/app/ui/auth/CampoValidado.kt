package com.vintageflow.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Componente reutilizable: lo usan las dos pantallas de autenticación y
 * está disponible para el formulario de prendas.
 */
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    esContrasena: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    habilitado: Boolean = true
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            label = { Text(etiqueta) },
            singleLine = true,
            enabled = habilitado,
            isError = error != null,
            visualTransformation =
                if (!esContrasena || visible) VisualTransformation.None
                else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado,
                imeAction = imeAction
            ),
            trailingIcon = {
                if (esContrasena) {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "Ocultar" else "Ver")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoValidadoPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CampoValidado(
                valor = "correo@ejemplo",
                onValorChange = {},
                etiqueta = "Correo electrónico",
                error = "El formato del correo no es válido."
            )
        }
    }
}