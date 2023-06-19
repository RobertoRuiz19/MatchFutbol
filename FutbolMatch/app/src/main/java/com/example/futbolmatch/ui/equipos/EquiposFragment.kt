package com.example.futbolmatch.ui.equipos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futbolmatch.R
import com.example.futbolmatch.dao.DaoEquipos
import com.example.futbolmatch.modelo.dto.Equipo

class EquiposFragment : Fragment() {
    private var columnCount = 1
    private lateinit var recyclerView: RecyclerView
    private lateinit var daoEquipos: DaoEquipos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_equipos_list, container, false)
        recyclerView = view.findViewById(R.id.list)
        return view
    }

    // Se ejecuta al iniciar el fragment
    override fun onStart() {
        super.onStart()

        // Mostrar un indicador de carga mientras se espera la respuesta de Firestore
        recyclerView.visibility = View.GONE
        view?.findViewById<View>(R.id.nombre_equipo)?.visibility = View.VISIBLE

        // Crear una instancia de DaoEquipos
        daoEquipos = DaoEquipos()

        // Llamar al método obtenerEquipos() del DAO y recibir la lista de equipos en el callback
        daoEquipos.obtenerEquipos { equipos ->
            // Mostrar los equipos en el RecyclerView
            mostrarEquipos(equipos)
        }
    }

    private fun mostrarEquipos(equipos: List<Equipo>) {
        // Ocultar el indicador de carga y mostrar los datos en el RecyclerView
        recyclerView.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.nombre_equipo)?.visibility = View.GONE
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        recyclerView.adapter = context?.let {
            MyEquiposRecyclerViewAdapter(
                equipos,
                it,
                parentFragmentManager
            )
        }
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            EquiposFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}