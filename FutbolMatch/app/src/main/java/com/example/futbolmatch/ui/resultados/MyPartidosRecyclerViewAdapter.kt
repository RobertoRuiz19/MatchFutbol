import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.futbolmatch.R
import com.example.futbolmatch.dao.DaoEquipos
import com.example.futbolmatch.dao.DaoPartidos
import com.example.futbolmatch.dao.DaoUsuario
import com.example.futbolmatch.modelo.dto.Partido
import com.example.futbolmatch.ui.resultados.InputFilterMinMax
import com.google.firebase.auth.FirebaseAuth

class MyPartidosRecyclerViewAdapter(
    private val partidos: List<Partido>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val onResultSaved: () -> Unit
) : RecyclerView.Adapter<MyPartidosRecyclerViewAdapter.ViewHolder>() {

    private val daoPartidos: DaoPartidos = DaoPartidos()
    private val daoEquipos: DaoEquipos = DaoEquipos()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.fragment_item_partidos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partido = partidos[position]
        holder.resuLocal.text = partido.marcadorLocal.toString()
        holder.resuVisitante.text = partido.marcadorVisitante.toString()
        holder.fecha.text = partido.fecha + " " + partido.hora

        daoEquipos.obtenerURLImagenEquipo(partido.equipoLocal) { urlEquipoLocal ->
            Glide.with(context)
                .load(urlEquipoLocal)
                .override(200, 200)
                .into(holder.equipoLocal)
        }

        daoEquipos.obtenerURLImagenEquipo(partido.equipoVisitante) { urlEquipoVisitante ->
            Glide.with(context)
                .load(urlEquipoVisitante)
                .override(200, 200)
                .into(holder.equipoVisitante)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userID = currentUser.uid
            val daoUsuario = DaoUsuario()
            daoUsuario.obtenerRolUsuario(userID) { rol ->
                if (rol != null) {
                    if (rol == "administrador") {
                        holder.itemView.setOnClickListener {
                            mostrarAlertDialogAdministrador(partido)
                        }
                    } else if (rol == "arbitro") {
                        holder.itemView.setOnClickListener {
                            mostrarAlertDialog(partido)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = partidos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val equipoLocal: ImageView = view.findViewById(R.id.equipoLocal)
        val equipoVisitante: ImageView = view.findViewById(R.id.equipoVisitante)
        val resuLocal: TextView = view.findViewById(R.id.resuLocal)
        val resuVisitante: TextView = view.findViewById(R.id.resuVisit)
        val fecha: TextView = view.findViewById(R.id.textViewDateTime)
    }

    private fun mostrarAlertDialog(partido: Partido) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ingresa el resultado")

        val inputLayout = LinearLayout(context)
        inputLayout.orientation = LinearLayout.HORIZONTAL

        val editTextLocal = EditText(context)
        editTextLocal.hint = "Local"
        editTextLocal.inputType = InputType.TYPE_CLASS_NUMBER
        editTextLocal.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "20"))

        val editTextVisitante = EditText(context)
        editTextVisitante.hint = "Visitante"
        editTextVisitante.inputType = InputType.TYPE_CLASS_NUMBER
        editTextVisitante.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "20"))

        inputLayout.addView(editTextLocal)
        inputLayout.addView(editTextVisitante)

        builder.setView(inputLayout)

        builder.setPositiveButton("Aceptar") { dialog, _ ->
            val resultadoLocal = editTextLocal.text.toString().toIntOrNull()
            val resultadoVisitante = editTextVisitante.text.toString().toIntOrNull()

            if (resultadoLocal != null && resultadoVisitante != null) {
                daoPartidos.guardarResultado(partido.id, resultadoLocal, resultadoVisitante, { success ->
                    if (success) {
                        Toast.makeText(context, "Resultado guardado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al guardar el resultado", Toast.LENGTH_SHORT).show()
                    }
                }, onResultSaved)
            } else {
                Toast.makeText(context, "Ingresa resultados válidos", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun mostrarAlertDialogAdministrador(partido: Partido) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Opciones del partido")

        val options = arrayOf("Borrar partido", "Modificar partido", "Cancelar")

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> mostrarConfirmacionBorrado(partido)
                1 -> mostrarAlertDialog(partido)
                2 -> {
                    // Opción cancelar, no hacer nada
                }
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun mostrarConfirmacionBorrado(partido: Partido) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar borrado")
        builder.setMessage("¿Estás seguro de que deseas borrar este partido?")

        builder.setPositiveButton("Sí") { _, _ ->
            daoPartidos.borrarPartido(partido, { success ->
                if (success) {
                    Toast.makeText(context, "Partido borrado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al borrar el partido", Toast.LENGTH_SHORT).show()
                }
            }, onResultSaved)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}