package com.example.futbolmatch.dao

import com.google.firebase.firestore.FirebaseFirestore

class DaoJornadas {
    private val db = FirebaseFirestore.getInstance()

    fun obtenerJornadas(callback: (List<String>) -> Unit) {
        val coleccionJornadas = db.collection("jornadas")

        coleccionJornadas.get()
            .addOnSuccessListener { documentos ->
                val listaJornadas = mutableListOf<String>()

                for (documento in documentos) {
                    val idJornada = documento.id
                    val jornada = "Jornada $idJornada"
                    listaJornadas.add(jornada)
                }

                val listaJornadasOrdenadas = listaJornadas.sortedBy { it.replace("Jornada ", "").toInt() }
                callback(listaJornadasOrdenadas)

            }.addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener las jornadas
                callback(emptyList()) // Enviar una lista vacía en caso de error
            }
    }

    fun crearJornada(callback: (Boolean, String?) -> Unit) {
        val coleccionJornadas = db.collection("jornadas")

        coleccionJornadas.get()
            .addOnSuccessListener { documentos ->
                val ultimaJornadaId = documentos.size() + 1
                val nuevaJornada = hashMapOf(
                    "jornadaId" to ultimaJornadaId
                )

                coleccionJornadas.document(ultimaJornadaId.toString())
                    .set(nuevaJornada)
                    .addOnSuccessListener {
                        callback(true, ultimaJornadaId.toString())
                    }
                    .addOnFailureListener { exception ->
                        // Manejar cualquier error que ocurra al crear la jornada
                        callback(false, null)
                    }
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener las jornadas
                callback(false, null)
            }
    }
}