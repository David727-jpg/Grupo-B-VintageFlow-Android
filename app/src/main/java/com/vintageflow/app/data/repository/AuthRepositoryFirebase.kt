package com.vintageflow.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.vintageflow.app.data.model.Usuario
import com.vintageflow.app.domain.CorreoNoRegistradoException
import com.vintageflow.app.domain.PerfilUsuario
import com.vintageflow.app.domain.Roles
import com.vintageflow.app.domain.aPerfil
import kotlinx.coroutines.tasks.await

/**
 * Implementación con Firebase. Es la única clase del proyecto que
 * conoce FirebaseAuth y Firestore
 *
 * La autenticación son dos pasos: Firebase Auth valida las credenciales
 * y Firestore aporta el rol, porque Auth no almacena roles.
 */
class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
    }

    override fun haySesionActiva(): Boolean = auth.currentUser != null

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Result<PerfilUsuario> = try {
        val credenciales = auth.signInWithEmailAndPassword(correo.trim(), contrasena).await()
        val uid = credenciales.user?.uid
        if (uid == null) {
            Result.failure(IllegalStateException("No se obtuvo el identificador del usuario."))
        } else {
            cargarPerfil(uid)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Crea la cuenta y escribe su documento en Firestore en el mismo flujo.
     * Si la escritura falla, el usuario quedaría autenticado pero sin rol así que ese caso se reporta como error en lugar de ignorarse.
     */
    override suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: String
    ): Result<PerfilUsuario> = try {
        val credenciales = auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await()
        val uid = credenciales.user?.uid
        if (uid == null) {
            Result.failure(IllegalStateException("No se obtuvo el identificador del usuario."))
        } else {
            val usuario = Usuario(
                id = uid,
                nombre = nombre.trim(),
                correo = correo.trim(),
                rol = rol.trim().uppercase()
            )
            db.collection(COLECCION_USUARIOS).document(uid).set(usuario).await()
            Result.success(usuario.aPerfil())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Autentica con Google y garantiza que el perfil exista en Firestore.
     *
     * Hay una diferencia con el registro por correo que no es obvia: Google
     * crea la cuenta en Firebase Auth, pero no escribe nada en Firestore. La
     * primera vez que alguien entra con Google no tiene documento en
     * `usuarios`, y sin documento no tiene rol. Por eso se comprueba y se
     * crea en ese momento.
     *
     * Si el documento ya existe no se toca, para no sobrescribir el rol de
     * un administrador que además entre con Google.
     */
    override suspend fun iniciarSesionConGoogle(idToken: String): Result<PerfilUsuario> = try {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val usuarioFirebase = auth.signInWithCredential(credencial).await().user

        if (usuarioFirebase == null) {
            Result.failure(IllegalStateException("No se obtuvo el identificador del usuario."))
        } else {
            val uid = usuarioFirebase.uid
            val documento = db.collection(COLECCION_USUARIOS).document(uid).get().await()

            if (documento.exists()) {
                cargarPerfil(uid)
            } else {
                val usuario = Usuario(
                    id = uid,
                    // Google puede no entregar nombre visible; el correo sí.
                    nombre = usuarioFirebase.displayName?.trim()?.takeIf { it.isNotEmpty() }
                        ?: usuarioFirebase.email?.substringBefore('@').orEmpty(),
                    correo = usuarioFirebase.email.orEmpty(),
                    rol = Roles.CLIENTE
                )
                db.collection(COLECCION_USUARIOS).document(uid).set(usuario).await()
                Result.success(usuario.aPerfil())
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun perfilActual(): Result<PerfilUsuario>? =
        auth.currentUser?.uid?.let { cargarPerfil(it) }

    /**
     * El correo lo compone y envía Firebase; la app no maneja plantillas ni
     * tokens. El enlace que recibe el usuario caduca por sí solo.
     */
    override suspend fun recuperarContrasena(correo: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(correo.trim()).await()
        Result.success(Unit)
    } catch (e: FirebaseAuthInvalidUserException) {
        // Se traduce al tipo del dominio: el ViewModel decide qué mostrar sin
        // tener que conocer las excepciones de Firebase.
        Result.failure(CorreoNoRegistradoException())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun cerrarSesion() = auth.signOut()

    /**
     * Lee el documento y lo convierte al perfil correspondiente.
     * toObject aprovecha que Usuario es data class con valores por
     */
    private suspend fun cargarPerfil(uid: String): Result<PerfilUsuario> = try {
        val doc = db.collection(COLECCION_USUARIOS).document(uid).get().await()
        val usuario = doc.toObject(Usuario::class.java)
        if (usuario == null) {
            Result.failure(IllegalStateException("El usuario no tiene perfil registrado."))
        } else {
            Result.success(usuario.copy(id = uid).aPerfil())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/** Rol con el que se crean las cuentas nuevas desde la app. */
val ROL_POR_DEFECTO: String = Roles.CLIENTE