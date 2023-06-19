package com.example.futbolmatch.ui.detalles_equipo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.futbolmatch.R
import com.example.futbolmatch.ui.maps.MapsFragment

class DetallesEquipo : Fragment() {

    private lateinit var mapsFragment: MapsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsFragment = MapsFragment.newInstance(
            arguments?.getDouble("latitud") ?: 0.0,
            arguments?.getDouble("longitud") ?: 0.0,
            arguments?.getString("nombre") ?: ""
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles_equipo, container, false)

        // Botón de retroceso
        val button = view.findViewById<ImageButton>(R.id.button)
        button.setOnClickListener { view -> back(view) }

        // Escudo
        val escudo = view.findViewById<ImageView>(R.id.equipo_escudo)
        val foto_escudo = arguments?.getString("foto")
        try {
            Glide.with(view)
                .load(foto_escudo)
                .override(200, 200)
                .fitCenter()
                .into(escudo)
        } catch (e: Exception) {
            print(e)
        }

        // Nombre Equipo
        val nombreEq = view.findViewById<TextView>(R.id.nombre_equipo_detalle)
        val nombreEquipo = arguments?.getString("nombre")
        nombreEq.text = nombreEquipo!!.toUpperCase()

        // Campo
        val campo = view.findViewById<TextView>(R.id.campo_equipo)
        val campoPaso = arguments?.getString("campo")
        campo.text = campoPaso!!.toUpperCase()

        // Tecnico
        val tecnico = view.findViewById<TextView>(R.id.tecnico_equipo)
        val tecni = arguments?.getString("tecni")
        tecnico.text = tecni!!.toUpperCase()

        // Equipacion
        val equipacion_lista = arguments?.getStringArrayList("equipacion")
        val equipacionStringBuilder = StringBuilder()
        equipacion_lista?.let {
            equipacionStringBuilder.append("Camiseta: ${it[0]}\n")
            equipacionStringBuilder.append("Calzonas: ${it[1]}\n")
            equipacionStringBuilder.append("Medias: ${it[2]}")
        }
        val equipacion = view.findViewById<TextView>(R.id.equipacion_equipo)
        equipacion.text = equipacionStringBuilder.toString().toUpperCase()

        // Jugadores
        val jugad = view.findViewById<TextView>(R.id.lista_jugadores)
        val jugadores = arguments?.getStringArrayList("jugadores")
        val jugadoresTexto = jugadores?.joinToString("\n\n")
        jugad.text = "${jugadoresTexto!!.toUpperCase()}"

        // GPS
        val btn_gps = view.findViewById<ImageButton>(R.id.gps)
        btn_gps.setOnClickListener { abrirMapa() }

        return view
    }


    /**
     * Metodo para ir hacia atras
     */
    // Este método ejecuta la acción para retroceder a la pantalla anterior
    fun back(view: View) {
        requireActivity().supportFragmentManager.popBackStack()
    }

    /**
     * Mapa
      */
    private fun abrirMapa() {
        val navController = findNavController()
        val bundle = Bundle().apply {
            putDouble("latitud", arguments?.getDouble("latitud") ?: 0.0)
            putDouble("longitud", arguments?.getDouble("longitud") ?: 0.0)
            putString("equipo", arguments?.getString("nombre") ?: "")
        }
        navController.navigate(R.id.mapsFragment, bundle)
    }

    // Este método crea una instancia de la clase con los parámetros especificados y retorna una instancia de la misma
    companion object {
        @JvmStatic
        fun newInstance(
            nombre: String,
            foto: String,
            campo: String,
            tecni: String,
            equipacion: ArrayList<String>,
            jugadores: ArrayList<String>,
            latitud: Double,
            longitud: Double
        ) =
            DetallesEquipo().apply {
                arguments = Bundle().apply {
                    putString("nombre", nombre)
                    putString("foto", foto)
                    putString("campo", campo)
                    putString("tecni", tecni)
                    putStringArrayList("equipacion", equipacion)
                    putStringArrayList("jugadores", jugadores)
                    putDouble("latitud", latitud)
                    putDouble("longitud", longitud)
                }
            }
    }
}


