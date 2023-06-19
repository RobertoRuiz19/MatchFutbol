package com.example.futbolmatch.login

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.futbolmatch.MainActivity
import com.example.futbolmatch.R
import com.example.futbolmatch.dao.DaoClasificacion
import com.example.futbolmatch.dao.DaoJornadas
import com.example.futbolmatch.dao.DaoUsuario
import com.example.futbolmatch.modelo.dto.Clasificacion
import com.example.futbolmatch.modelo.dto.Equipo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class AdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userID: String
    private lateinit var nombreUsu: TextView
    private lateinit var rolUsuario: TextView

    private lateinit var jornadaActual: Number
    private lateinit var equipoLocalActual: String
    private lateinit var equipoVisitanteActual: String

    private val equipos = mutableListOf<Equipo>()

    private lateinit var daoJornadas: DaoJornadas
    private val daoUsuario: DaoUsuario = DaoUsuario()
    private val daoClasificacion: DaoClasificacion = DaoClasificacion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Obtener el userID desde el MainActivity
        userID = intent.getStringExtra("userID").toString()

        db = FirebaseFirestore.getInstance()

        obtenerDatos()

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            back()
        }

        val cambiarNombre = findViewById<TextView>(R.id.cambio_nombre)
        cambiarNombre.setOnClickListener {
            abrirDialogo()
        }

        obtenerDatos()

        val crearPartidoBtn = findViewById<Button>(R.id.crear_partido)
        crearPartidoBtn.setOnClickListener {
            crearPartido()
        }

        daoJornadas = DaoJornadas()
        val crearJornadaBtn = findViewById<Button>(R.id.crear_jornada)
        crearJornadaBtn.setOnClickListener {
            crearJornada()

        }

        val btnClasificacion = findViewById<Button>(R.id.clasificacion)
        btnClasificacion.setOnClickListener {
            actualizarTablaClasificacion()
        }

    }

    /**
     * Crear Jornada
     */

    fun crearJornada() {
        daoJornadas.obtenerJornadas { jornadas ->
            val ultimaJornada = if (jornadas.isNotEmpty()) {
                val ultimaJornadaString = jornadas.last()
                val numeroJornada = ultimaJornadaString.replace("Jornada ", "").toInt()
                numeroJornada
            } else {
                0
            }

            val siguienteJornada = ultimaJornada + 1

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Crear Jornada")
            alertDialogBuilder.setMessage("¿Deseas crear la jornada $siguienteJornada?")
            alertDialogBuilder.setPositiveButton("Crear") { dialog, _ ->
                dialog.dismiss()
                daoJornadas.crearJornada { exito, jornadaId ->
                    if (exito && jornadaId != null) {
                        Toast.makeText(this, "Se creó la jornada $jornadaId", Toast.LENGTH_SHORT)
                            .show()
                        crearTablaClasificacion(siguienteJornada)
                    } else {
                        Toast.makeText(this, "Error al crear la jornada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            alertDialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }


    /**
     * Obtener datos del usuario
     */

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
     * Crear partidos
     */

    fun crearPartido() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Jornada")

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_seleccion_jornada, null)
        builder.setView(dialogView)

        val spinnerJornada = dialogView.findViewById<Spinner>(R.id.spinnerJornada)

        val db = FirebaseFirestore.getInstance()
        val jornadasRef = db.collection("jornadas")

        jornadasRef.get().addOnSuccessListener { snapshot ->
            val jornadasDisponibles = ArrayList<String>()

            for (document in snapshot.documents) {
                val jornadaId = document.getLong("jornadaId")
                val jornadaNombre = "Jornada $jornadaId"
                jornadasDisponibles.add(jornadaNombre)
            }

            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, jornadasDisponibles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerJornada.adapter = adapter
        }.addOnFailureListener { exception ->
        }

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            val selectedJornada = spinnerJornada.selectedItem.toString()
            val jornadaId =
                selectedJornada.substring(selectedJornada.indexOf(' ') + 1).trim().toInt()
            obtenerEquiposDisponibles(jornadaId.toString())
        })

        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun obtenerEquiposDisponibles(jornada: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Equipos")

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_seleccion_equipos, null)
        builder.setView(dialogView)

        val spinnerEquipoLocal = dialogView.findViewById<Spinner>(R.id.spinnerEquipoLocal)
        val spinnerEquipoVisitante = dialogView.findViewById<Spinner>(R.id.spinnerEquipoVisitante)

        val db = FirebaseFirestore.getInstance()
        val equiposRef = db.collection("equipos")

        equiposRef.get().addOnSuccessListener { snapshot ->
            val equiposDisponibles = ArrayList<String>()

            for (document in snapshot.documents) {
                val nombreEquipo = document.getString("nombre")
                if (nombreEquipo != null) {
                    equiposDisponibles.add(nombreEquipo.toUpperCase())
                }
            }

            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, equiposDisponibles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEquipoLocal.adapter = adapter
            spinnerEquipoVisitante.adapter = adapter

            builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
                val equipoLocalSeleccionado = spinnerEquipoLocal.selectedItem.toString()
                val equipoVisitanteSeleccionado = spinnerEquipoVisitante.selectedItem.toString()

                if (equipoLocalSeleccionado == equipoVisitanteSeleccionado) {
                    // Los equipos seleccionados son iguales, mostrar un mensaje de error o realizar la acción correspondiente
                    Toast.makeText(
                        this,
                        "No puedes seleccionar el mismo equipo para local y visitante",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    equipoLocalActual = equipoLocalSeleccionado
                    equipoVisitanteActual = equipoVisitanteSeleccionado

                    // Inicializar jornadaActual con el número de jornada extraído
                    this.jornadaActual = jornada.substring(jornada.indexOf(' ') + 1).trim().toInt()

                    abrirDialogoFechaHora()
                }
            })

            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })

            val alertDialog = builder.create()
            alertDialog.show()
        }.addOnFailureListener { exception ->

        }
    }

    private fun abrirDialogoFechaHora() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Fecha y Hora")

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_seleccion_fecha, null)
        builder.setView(dialogView)

        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerFechaPartido)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePickerHoraPartido)

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            val diaSeleccionado = datePicker.dayOfMonth
            val mesSeleccionado =
                datePicker.month + 1 // Se suma 1 porque el índice de los meses comienza en 0
            val anoSeleccionado = datePicker.year

            val horaSeleccionada = timePicker.hour
            val minutoSeleccionado = timePicker.minute

            // Realizar las acciones con la fecha y hora seleccionadas
            // Crear un nuevo documento en la colección "partidos"
            val partido = hashMapOf(
                "jornada" to jornadaActual,
                "equipoLocal" to equipoLocalActual,
                "equipoVisitante" to equipoVisitanteActual,
                "fecha" to "$diaSeleccionado/$mesSeleccionado/$anoSeleccionado",
                "hora" to "$horaSeleccionada:$minutoSeleccionado",
                "marcadorLocal" to 0,
                "marcadorVisitante" to 0
            )

            val partidosRef = db.collection("partidos")
            partidosRef.add(partido)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        this,
                        "Partido guardado con ID: ${documentReference.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el partido", Toast.LENGTH_SHORT).show()
                }
        })

        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Cambiar nombre
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
     * Metodos para crear la tabla de clasificacion
     */

    private fun crearTablaClasificacion(jornadaId: Int) {
        daoClasificacion.crearTablaClasificacion(jornadaId)
    }


    /**
     * Actualizar la tabla de clasificacion
     */

    private fun actualizarTablaClasificacion() {
        daoClasificacion.actualizarTablaClasificacion { exito ->
            if (exito) {
                Toast.makeText(this, "Actualizadas las tablas de clasificación", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Error al actualizar tablas de clasificación", Toast.LENGTH_SHORT).show()
            }
        }
    }


    data class TablaClasificacion(
        val tabla: MutableList<Clasificacion>
    ) {
        constructor() : this(mutableListOf())
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