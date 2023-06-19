import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.bumptech.glide.Glide
import com.example.futbolmatch.R
import com.example.futbolmatch.dao.DaoClasificacion
import com.example.futbolmatch.dao.DaoJornadas
import com.example.futbolmatch.modelo.dto.Clasificacion

class ClasificacionFragment : Fragment() {
    private var columnCount = 1
    private lateinit var spinnerJornadas: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var daoJornadas: DaoJornadas
    private lateinit var daoClasificacion: DaoClasificacion
    private lateinit var clasificacionAdapter: MyClasificacionRecyclerViewAdapter

    private var listaJornadas: List<String> = emptyList()
    private var selectedJornada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        daoJornadas = DaoJornadas()
        daoClasificacion = DaoClasificacion()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clasificacion, container, false)

        spinnerJornadas = view.findViewById(R.id.spinner)
        recyclerView = view.findViewById(R.id.list_clasificacion)

        obtenerJornadas()

        spinnerJornadas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedJornada = listaJornadas[position]
                val numeroJornada = obtenerNumeroJornada(selectedJornada)
                obtenerTablaClasificacion(numeroJornada)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se ha seleccionado ninguna jornada
            }
        }

        return view
    }

    private fun obtenerJornadas() {
        daoJornadas.obtenerJornadas { jornadas ->
            listaJornadas = jornadas

            val adaptadorSpinner = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listaJornadas
            )
            adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerJornadas.adapter = adaptadorSpinner
        }
    }

    private fun obtenerTablaClasificacion(jornada: Int) {
        daoClasificacion.obtenerTablaClasificacion(jornada) { tablaClasificacion ->
            mostrarTablaClasificacion(tablaClasificacion)
        }
    }

    private fun obtenerNumeroJornada(jornada: String): Int {
        return jornada.replace("Jornada ", "").toInt()
    }

    private fun mostrarTablaClasificacion(tabla: List<Clasificacion>?) {
        tabla?.let {
            clasificacionAdapter = MyClasificacionRecyclerViewAdapter(it, Glide.with(this))
            recyclerView.adapter = clasificacionAdapter
        }
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ClasificacionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
