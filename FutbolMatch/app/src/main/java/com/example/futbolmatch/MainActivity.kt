package com.example.futbolmatch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.futbolmatch.databinding.ActivityMainBinding
import com.example.futbolmatch.login.AdminActivity
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.example.futbolmatch.login.Login
import com.example.futbolmatch.login.Perfil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Importaciones
    private lateinit var binding: ActivityMainBinding // Vista principal del activity
    private var notHome: Int = 0 // Contador de notificaciones de la pestaña "Equipos"
    private var notDash: Int = 0 // Contador de notificaciones de la pestaña "Clasificación"
    private var notNotif: Int = 0 // Contador de notificaciones de la pestaña "Resultados"
    private var notVacio: Int = 0 // Contador de notificaciones de la pestaña "Vacio"

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa la vista del activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView // Vista del menú de navegación inferior

        // Configuración del AppBar
        val appBar: BottomAppBar = binding.bottomAppBar // Vista del AppBar
        setSupportActionBar(appBar) // Configura el ActionBar con el AppBar
        val radius = resources.getDimension(R.dimen.cornerSize) // Radio de la esquina

        navView.background = null // Fondo del menú de navegación inferior

        // Configura la forma de la AppBar
        val shapeDrawable: MaterialShapeDrawable = appBar.background as MaterialShapeDrawable
        shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(
                CornerFamily.ROUNDED,
                radius
            ) // Configura la esquina superior izquierda redondeada
            .build()

        navView.menu.getItem(3).isEnabled =
            false // Deshabilita la opción "Vacio" del menú de navegación inferior

        // Configuración de la navegación del menú
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Se pasa cada ID del menú como un conjunto de IDs porque cada
        // menú debe considerarse como destinos de nivel superior.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_equipos,
                R.id.navigation_clasificacion,
                R.id.navigation_resultados,
                R.id.navigation_vacio,
                R.id.mapsFragment
            )
        )

       verificaUsuario()

        // Configuración del ActionBar con la navegación del menú
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun verificaUsuario() {
        // Verificar el estado de autenticación del usuario
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userID = currentUser.uid
            // Obtener referencia a la base de datos de Firestore
            val db = FirebaseFirestore.getInstance()
            val userRol = db.collection("usuarios").document(userID.toString())
            userRol.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtener el campo rol del documento
                    val rol = document.getString("rol")
                    if (rol != null) {
                        // Realizar acciones basadas en el rol del usuario
                        if (rol == "administrador") {
                            // Usuario con rol de administrador
                            // Permitir acciones de administrador
                        } else if (rol == "usuario" || rol == "arbitro") {
                            // Otro tipo de usuario
                        }
                        Toast.makeText(this, "Sesion iniciada", Toast.LENGTH_SHORT).show()


                        // Configurar el onClickListener del FloatingActionButton
                        val fab: FloatingActionButton = findViewById(R.id.fab)
                        fab.setImageResource(R.drawable.baseline_person_24)
                        fab.setOnClickListener {
                            if (rol == "administrador") {
                                // Usuario con rol de administrador
                                // Permitir acciones de administrador
                                val intent = Intent(this, AdminActivity::class.java)
                                intent.putExtra("userID", userID)
                                startActivity(intent)
                            } else if (rol == "usuario" || rol == "arbitro") {
                                val intent = Intent(this, Perfil::class.java)
                                intent.putExtra("userID", userID)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        } else {
            // El usuario no está logueado, establece la imagen correspondiente
            val fab: FloatingActionButton = findViewById(R.id.fab)
            fab.setImageResource(R.drawable.baseline_person_add_24)
            fab.setOnClickListener {
                // Abre el activity LoginFragment
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
    }
}
