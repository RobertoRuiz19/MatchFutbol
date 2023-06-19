package com.example.futbolmatch.ui.equipos

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.futbolmatch.R
import com.example.futbolmatch.modelo.dto.Equipo
import com.example.futbolmatch.ui.detalles_equipo.DetallesEquipo


class MyEquiposRecyclerViewAdapter(
    private val equipos: List<Equipo>, // Lista de equipos a mostrar en el RecyclerView
    val context: Context,
    val fragmentManager: FragmentManager
) : RecyclerView.Adapter<MyEquiposRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.fragment_equipos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Mostrar los datos del equipo
        val equipo = equipos[position]
        holder.nombre.text = equipo.nombre.toUpperCase()
        holder.imagen_code.setText(equipo.foto)
        try {
            Glide.with(context)
                .load(equipo.foto)
                .override(200, 200)
                .fitCenter()
                .into(holder.imagen)
        } catch (e: Exception) {
            print(e)
        }

        // Agregar un listener para abrir la vista de detalles del equipo al hacer clic en la vista del equipo
        holder.itemView.setOnClickListener {
            val fragment = DetallesEquipo.newInstance(
                equipo.nombre,
                equipo.foto,
                equipo.campo,
                equipo.tecnico,
                equipo.equipacion,
                equipo.jugadores,
                equipo.latitud,
                equipo.longitud
            )
            fragmentManager.commit {
                replace(
                    R.id.nav_host_fragment_activity_main,
                    fragment
                ) // Reemplazar el fragmento actual con la vista de detalles del equipo
                addToBackStack(null) // Agregar la transacción al back stack para poder volver a la vista anterior con el botón de atrás
            }
        }
    }

    override fun getItemCount(): Int =
        equipos.size // Devolver la cantidad de elementos en la lista de equipos

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Obtener las vistas correspondientes para mostrar los datos del equipo
        val nombre: TextView = view.findViewById(R.id.nombre_equipo)
        val imagen_code: TextView = view.findViewById(R.id.imagen_code)
        val imagen: ImageView = view.findViewById(R.id.imagen_equipo)
    }

}