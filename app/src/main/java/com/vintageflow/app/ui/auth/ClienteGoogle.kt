package com.vintageflow.app.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.vintageflow.app.R
import com.vintageflow.app.domain.GoogleCanceladoException
import com.vintageflow.app.domain.GoogleSinCuentaException
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Obtiene el token de identidad de Google mediante Credential Manager.
 *
 *
 * Esta clase solo consigue el token. No habla con Firebase ni conoce el
 * perfil del usuario: eso es trabajo de `AuthRepositoryFirebase`. Separarlo
 * así mantiene el `Context` fuera del ViewModel.
 *
 */
class ClienteGoogle(private val context: Context) {

    suspend fun obtenerIdToken(): Result<String> = try {

        // El "server client ID" es el de tipo *aplicación web*, no el de
        // Android. Es el error más frecuente de esta integración. No se
        // escribe a mano: el plugin de google-services genera esta cadena a
        // partir de google-services.json, así que sigue al archivo y no
        // queda un identificador suelto en el repositorio.
        val idClienteWeb = context.getString(R.string.default_web_client_id)

        val opcion = GetSignInWithGoogleOption
            .Builder(serverClientId = idClienteWeb)
            .setNonce(generarNonce())
            .build()

        val peticion = GetCredentialRequest.Builder()
            .addCredentialOption(opcion)
            .build()

        val respuesta = CredentialManager.create(context).getCredential(context, peticion)
        val credencial = respuesta.credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Result.success(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            Result.failure(
                IllegalStateException("Google devolvió una credencial de tipo inesperado.")
            )
        }

    } catch (e: GetCredentialCancellationException) {
        Result.failure(GoogleCanceladoException())
    } catch (e: NoCredentialException) {
        Result.failure(GoogleSinCuentaException())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Número usado una sola vez, para que el token pedido no pueda
     * reutilizarse en otra petición.
     */
    private fun generarNonce(): String {
        val aleatorio = ByteArray(32)
        SecureRandom().nextBytes(aleatorio)
        return MessageDigest.getInstance("SHA-256")
            .digest(aleatorio)
            .joinToString("") { "%02x".format(it) }
    }
}