package com.example.futbolmatch.dao

import com.example.futbolmatch.modelo.dto.Partido
import com.google.firebase.firestore.FirebaseFirestore

class DaoPartidos {
    private val db = FirebaseFirestore.getInstance()

    fun obtenerPartidos(jornada: Int, callback: (List<Partido>?) -> Unit) {
        val coleccionPartidos = db.collection("partidos")

        coleccionPartidos.whereEqualTo("jornada", jornada)
            .get()
            .addOnSuccessListener { documentos ->
                val listaPartidos = mutableListOf<Partido>()

                for (documento in documentos) {
                    val id = documento.id
                    val equipoLocal = documento.getString("equipoLocal")
                    val equipoVisitante = documento.getString("equipoVisitante")
                    val marcadorLocal = documento.getLong("marcadorLocal")
                    val marcadorVisitante = documento.getLong("marcadorVisitante")
                    val fecha = documento.getString("fecha")
                    val hora = documento.getString("hora")
                    val jornada = documento.getLong("jornada")

                    val partido = Partido(
                        id,
                        equipoLocal.toString(),
                        equipoVisitante.toString(),
                        fecha.toString(),
                        hora.toString(),
                        jornada,
                        marcadorLocal,
                        marcadorVisitante
                    )
                    listaPartidos.add(partido)
                }

                callback(listaPartidos)
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener los partidos
                callback(null) // Enviar null en caso de error
            }
    }

    fun guardarResultado(partidoId: String, resultadoLocal: Int, resultadoVisitante: Int, callback: (Boolean) -> Unit, onResultSaved: () -> Unit) {
        val coleccionPartidos = db.collection("partidos")
        val partidoRef = coleccionPartidos.document(partidoId)

        val datosActualizados = hashMapOf(
            "marcadorLocal" to resultadoLocal,
            "marcadorVisitante" to resultadoVisitante
        )

        partidoRef.update(datosActualizados as Map<String, Any>)
            .addOnSuccessListener {
                callback(true)
                onResultSaved()
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }

    fun borrarPartido(partido: Partido, callback: (Boolean) -> Unit, onResultSaved: () -> Unit) {
        val coleccionPartidos = db.collection("partidos")
        val partidoRef = coleccionPartidos.document(partido.id)

        partidoRef.delete()
            .addOnSuccessListener {
                callback(true)
                onResultSaved()
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }
}