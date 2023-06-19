package com.example.futbolmatch.dao

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class DaoUsuario {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun obtenerDatos(userID: String, callback: (String?, String?) -> Unit) {
        val user = db.collection("usuarios").document(userID)
        user.get().addOnSuccessListener { document ->
            // Obtener el campo del documento
            val nombre = document.getString("nombre")
            val rol = document.getString("rol")
            callback(nombre, rol)
        }
    }

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = auth.currentUser
                    val userID = user?.uid
                    val userData = HashMap<String, Any>()
                    userData["nombre"] = ""
                    userData["rol"] = "usuario"
                    userData["email"] = email

                    // Guardar datos del usuario en Firestore
                    if (userID != null) {
                        db.collection("usuarios").document(userID)
                            .set(userData)
                            .addOnSuccessListener {
                                callback(true, userID)
                            }
                            .addOnFailureListener { exception ->
                                callback(false, null)
                                // Eliminar el usuario registrado en Firebase Authentication en caso de fallo en Firestore
                                user.delete()
                            }
                    } else {
                        callback(false, null)
                    }
                } else {
                    // Error durante el registro
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthUserCollisionException -> {
                            callback(false, "El usuario ya está registrado")
                        }

                        is FirebaseAuthInvalidCredentialsException -> {
                            callback(false, "El correo electrónico no es válido")
                        }

                        else -> {
                            callback(false, "Error al registrar el usuario")
                        }
                    }
                }
            }
    }

    fun obtenerRolUsuario(userId: String, callback: (String?) -> Unit) {
        val usuariosRef = db.collection("usuarios").document(userId)

        usuariosRef.get()
            .addOnSuccessListener { document ->
                val rol = document.getString("rol")
                callback(rol)
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener el rol del usuario
                callback(null) // Enviar null en caso de error
            }
    }

    fun cambiarNombreUsuario(userID: String, nuevoNombre: String, callback: (Boolean) -> Unit) {
        val user = db.collection("usuarios").document(userID)

        user.update("nombre", nuevoNombre)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun mandarMensaje(userID: String, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserID = currentUser.uid
            val userRol = db.collection("usuarios").document(currentUserID)

            userRol.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val rol = document.getString("rol")
                        val nombre = document.getString("nombre")
                        if (rol != null && nombre != null) {
                            if (rol == "usuario" || rol == "arbitro") {
                                callback(true)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    callback(false)
                }
        }
    }

    fun guardarMensaje(userID: String, nombre: String, mensaje: String, rol: String, callback: (Boolean) -> Unit) {
        val mensajesCollection = db.collection("mensajes")
        val mensajeData = hashMapOf(
            "userID" to userID,
            "nombre" to nombre,
            "mensaje" to mensaje,
            "rol" to rol
        )

        mensajesCollection.add(mensajeData)
            .addOnSuccessListener { documentReference ->
                callback(true)
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }
}