package com.example.futbolmatch.login

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.futbolmatch.R
import android.content.Intent
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.futbolmatch.MainActivity
import com.example.futbolmatch.dao.DaoUsuario
import com.google.firebase.auth.FirebaseAuth
import com.example.futbolmatch.login.Login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class Perfil : AppCompatActivity() {

    private lateinit var userID: String
    private lateinit var nombreUsu: TextView
    private lateinit var rolUsuario: TextView
    private val daoUsuario: DaoUsuario = DaoUsuario()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Obtener el userID desde el MainActivity
        userID = intent.getStringExtra("userID").toString()

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            back()
        }

        val cambiarNombre = findViewById<TextView>(R.id.cambio_nombre)
        cambiarNombre.setOnClickListener {
            abrirDialogo()
        }

        val cambioRol = findViewById<Button>(R.id.cambio_rol)
        cambioRol.setOnClickListener {
            mandarMensaje()
        }

        obtenerDatos()


    }

    private fun obtenerDatos() {
        daoUsuario.obtenerDatos(userID) { nombre, rol ->
            runOnUiThread {
                if (nombre != null && rol != null) {
                    nombreUsu = findViewById(R.id.Usuario_nombre)
                    nombreUsu.text = nombre
                    rolUsuario = findViewById(R.id.rol)
                    rolUsuario.text = rol
                }
            }
        }
    }

    /**
     * Cambiar nombre de usuario
     */

    private fun abrirDialogo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar nombre")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            val nuevoNombre = input.text.toString()
            cambiarNombreUsuario(nuevoNombre)
        })

        builder.setNegativeButton(
            "Cancelar",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun cambiarNombreUsuario(nuevoNombre: String) {
        daoUsuario.cambiarNombreUsuario(userID, nuevoNombre) { success ->
            runOnUiThread {
                if (success) {
                    nombreUsu.text = nuevoNombre
                    Toast.makeText(
                        this,
                        "Nombre de usuario actualizado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error al actualizar el nombre de usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Mandar mensaje
     */

    private fun mandarMensaje() {
        daoUsuario.mandarMensaje(userID) { canSend ->
            runOnUiThread {
                if (canSend) {
                    mostrarDialogoMensaje()
                } else {
                    Toast.makeText(this, "No puedes enviar mensajes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDialogoMensaje() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enviar Mensaje")
        builder.setMessage("Mensaje")

        val input = EditText(this)
        input.maxLines = 3
        val maxLength = 100
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(maxLength)
        input.filters = filterArray

        builder.setView(input)

        builder.setPositiveButton("Enviar") { dialog, which ->
            val mensaje = input.text.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val currentUserID = currentUser.uid
                val userRol =
                    FirebaseFirestore.getInstance().collection("usuarios").document(currentUserID)

                userRol.get().addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre")
                        val rol = document.getString("rol")
                        if (nombre != null && rol != null) {
                            daoUsuario.guardarMensaje(
                                currentUserID,
                                nombre,
                                mensaje,
                                rol
                            ) { success ->
                                runOnUiThread {
                                    if (success) {
                                        Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Error al enviar el mensaje",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }


    /**
     * Metodos funcionales
     */
    private fun back() {
        onBackPressed()
    }

    fun logOut(view: View) {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.revokeAccess()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
