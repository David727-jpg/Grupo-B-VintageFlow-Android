package com.vintageflow.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vintageflow.app.data.model.Usuario
import com.vintageflow.app.domain.PerfilUsuario
import com.vintageflow.app.domain.Roles
import com.vintageflow.app.domain.aPerfil
import kotlinx.coroutines.tasks.await

/**
 * Implementación con Firebase. Es la única clase del proyecto que
 * conoce FirebaseAuth y Firestore; ni el ViewModel ni las pantallas los importan.
 *
 * La autenticación son dos pasos: Firebase Auth valida las credenciales
 * y Firestore aporta el rol.
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
     * Se creara la cuenta y escribe su documento en Firestore en el mismo flujo.
     * Si la escritura falla, el usuario quedaría autenticado pero sin rol,
     * así que ese caso presentado se reporta como error en lugar de ignorarse.
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

    override suspend fun perfilActual(): Result<PerfilUsuario>? =
        auth.currentUser?.uid?.let { cargarPerfil(it) }

    override fun cerrarSesion() = auth.signOut()

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

/** Rol con el que se crearan las cuentas nuevas desde la app. */
val ROL_POR_DEFECTO: String = Roles.CLIENTE