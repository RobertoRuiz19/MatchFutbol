import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futbolmatch.R
import com.example.futbolmatch.dao.DaoJornadas
import com.example.futbolmatch.dao.DaoPartidos
import com.example.futbolmatch.modelo.dto.Partido

class PartidosFragment : Fragment() {
    private lateinit var spinnerJornadas: Spinner
    private lateinit var daoPartidos: DaoPartidos

    private lateinit var partidos: ArrayList<Partido>
    private lateinit var recyclerView: RecyclerView
    private var jornadaSeleccionada: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resultados_partidos, container, false)
        spinnerJornadas = view.findViewById(R.id.jornada_spinner)
        recyclerView = view.findViewById(R.id.list)
        partidos = ArrayList()

        daoPartidos = DaoPartidos()

        // Obtener la lista de jornadas desde Firebase
        obtenerJornadas()

        // Agregar un OnItemSelectedListener al Spinner para obtener los partidos cuando se seleccione una jornada
        spinnerJornadas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Obtener los partidos de la jornada seleccionada
                val jornada = obtenerNumeroJornada(spinnerJornadas.selectedItem.toString())
                obtenerPartidos(jornada)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se hace nada si no se selecciona ninguna jornada
            }
        }

        // Configurar el RecyclerView y su adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyPartidosRecyclerViewAdapter(
            partidos,
            requireContext(),
            parentFragmentManager,
            onResultSaved = {
                obtenerPartidos(jornadaSeleccionada) // Actualiza los partidos en el fragmento
            }
        )

        return view
    }

    private fun obtenerJornadas() {
        val daoJornadas = DaoJornadas()
        daoJornadas.obtenerJornadas { jornadas ->
            val jornadasOrdenadas = jornadas.sortedBy { it.replace("Jornada ", "").toInt() }

            val adaptadorSpinner = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                jornadasOrdenadas
            )
            adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerJornadas.adapter = adaptadorSpinner
        }
    }

    private fun obtenerPartidos(jornada: Int) {
        daoPartidos.obtenerPartidos(jornada) { listaPartidos ->
            if (listaPartidos != null) {
                partidos.clear()
                partidos.addAll(listaPartidos)
                recyclerView.adapter?.notifyDataSetChanged()
            } else {
                // Manejar el caso de error al obtener los partidos
            }
        }
    }

    private fun obtenerNumeroJornada(jornada: String): Int {
        return jornada.replace("Jornada ", "").toInt()
    }
}