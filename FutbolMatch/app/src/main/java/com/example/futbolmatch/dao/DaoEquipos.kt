package com.example.futbolmatch.dao

import com.example.futbolmatch.modelo.dto.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class DaoEquipos {
    fun obtenerEquipos(callback: (List<Equipo>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("equipos")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val equipos = ArrayList<Equipo>()
                for (document in result) {
                    val equipo = document.toObject(Equipo::class.java)
                    equipos.add(equipo)
                }

                // Llamar al callback y pasar la lista de equipos
                callback(equipos)
            }
            .addOnFailureListener { exception ->
                // Manejar el error en caso de que la obtención de equipos falle
                callback(emptyList()) // Enviar una lista vacía en caso de error
            }
    }

    fun obtenerURLImagenEquipo(nombreEquipo: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val coleccionEquipos = db.collection("equipos")

        coleccionEquipos.get()
            .addOnSuccessListener { documentos ->
                val urlImagenEquipo = documentos.firstOrNull {
                    it.getString("nombre")?.equals(nombreEquipo, ignoreCase = true) == true
                }?.getString("foto")
                if (urlImagenEquipo != null) {
                    callback(urlImagenEquipo)
                } else {
                    // Manejar el caso de error si no se encuentra la URL de la imagen
                }
            }
            .addOnFailureListener { exception ->
                // Manejar el caso de error al obtener los documentos
            }
    }
}