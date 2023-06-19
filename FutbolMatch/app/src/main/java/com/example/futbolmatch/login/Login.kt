package com.example.futbolmatch.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.example.futbolmatch.MainActivity
import com.example.futbolmatch.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa las variables auth y db
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configura el inicio de sesión con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configura los TextWatchers en los campos de texto
        setupTextWatchers()

        val emailLoginButton = findViewById<Button>(R.id.login_btn)
        emailLoginButton.setOnClickListener {
            signInWithEmail()
        }

        val googleImageButton = findViewById<ImageButton>(R.id.googleImage)
        googleImageButton.setOnClickListener {
            signInWithGoogle()
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            back()
        }

        val register = findViewById<Button>(R.id.register_btn)
        register.setOnClickListener {
            registerActivity()
        }
    }

    /**
     * Registro
     */
    private fun registerActivity() {
        // Redirige al usuario a RegisterActivity
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    /**
     * Inicio de sesion con Google
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Autenticación exitosa. Credenciales de Google para autenticar con Firebase.
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: ApiException) {
                // Autenticación fallida.

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autenticación exitosa con Firebase.
                    val user = auth.currentUser
                    val userID = user?.uid
                    val email = user?.email
                    checkUserInFirestore(userID, email)
                } else {
                    // Autenticación fallida con Firebase.
                }
            }
    }

    /**
     * Iniciar sesion con email
     */
    private fun signInWithEmail() {
        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Inicio de sesión exitoso con correo electrónico y contraseña
                        val user = auth.currentUser
                        val userID = user?.uid
                        checkUserInFirestore(userID, user?.email)
                    } else {
                        // Inicio de sesión fallido con correo electrónico y contraseña
                    }
                }
        } else {
            // Mostrar un mensaje de error indicando que se deben completar ambos campos.
        }
    }

    private fun checkUserInFirestore(userID: String?, email: String?) {
        if (userID != null) {
            db.collection("usuarios").document(userID)
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val userRole = documentSnapshot.getString("rol")

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userID", userID)
                        intent.putExtra("userRole", userRole)
                        startActivity(intent)
                    } else {
                        // El usuario no existe en Firestore, se crea un nuevo documento
                        val user = auth.currentUser
                        val userData = HashMap<String, Any>()
                        userData["nombre"] = ""
                        userData["rol"] = "usuario"
                        userData["email"] = email ?: ""
                        db.collection("usuarios").document(userID)
                            .set(userData)
                            .addOnSuccessListener {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("userID", userID)
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception: Exception? ->
                                // Maneja la excepción
                            }
                    }
                }
                .addOnFailureListener { exception: Exception? ->
                    // Maneja la excepción
                }
        }
    }

    /**
     * Correo deshabilitado hasta que se rellenen los campos
     */
    private fun setupTextWatchers() {
        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val emailLoginButton = findViewById<Button>(R.id.login_btn)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Habilita o deshabilita el botón según el contenido de los campos de texto.
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                emailLoginButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        // Agrega el TextWatcher a los campos de texto.
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun back() {
        onBackPressed()
    }
}
