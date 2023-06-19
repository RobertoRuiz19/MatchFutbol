package com.example.futbolmatch.dao

import com.example.futbolmatch.login.AdminActivity
import com.example.futbolmatch.modelo.dto.Clasificacion
import com.example.futbolmatch.modelo.dto.Equipo
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DaoClasificacion {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Obtener las tablas de clasificacion
     */
    fun obtenerTablaClasificacion(jornada: Int, callback: (List<Clasificacion>?) -> Unit) {
        val coleccionJornadas = db.collection("jornadas")

        coleccionJornadas.document(jornada.toString()).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val tablaClasificacion = documento.toObject(TablaClasificacion::class.java)
                    callback(tablaClasificacion?.tabla)
                } else {
                    callback(null) // El documento de la jornada seleccionada no existe
                }
            }.addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener la tabla de clasificación
                callback(null) // Enviar null en caso de error
            }
    }

    /**
     * Crear tabla de clasificacion
     */
    fun crearTablaClasificacion(jornadaId: Int) {
        val equiposRef = db.collection("equipos")
        val clasificacionRef = db.collection("jornadas")

        equiposRef.get().addOnSuccessListener { snapshot ->
            val equipos = mutableListOf<Equipo>()

            for (document in snapshot.documents) {
                val equipo = document.toObject(Equipo::class.java)
                if (equipo != null) {
                    equipos.add(equipo)
                }
            }

            val tablaClasificacion = mutableListOf<Clasificacion>()

            for (equipo in equipos) {
                val clasificacion = Clasificacion(
                    equipo.nombre.toUpperCase(),
                    equipo.foto,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                )
                tablaClasificacion.add(clasificacion)
            }

            // Crear la tabla de clasificación para la jornada especificada
            val nuevaTablaClasificacion = hashMapOf(
                "jornadaId" to jornadaId,
                "tabla" to tablaClasificacion
            )

            clasificacionRef.document(jornadaId.toString())
                .set(nuevaTablaClasificacion)

        }.addOnFailureListener { exception ->
            // Manejar el error en la obtención de los datos de los equipos
        }
    }

    /**
     * Actualizar tablas de clasificacion
     */
    fun actualizarTablaClasificacion(callback: (Boolean) -> Unit) {
        val partidosRef = db.collection("partidos")
        val jornadasRef = db.collection("jornadas")

        val jornadasQuery = jornadasRef.orderBy("jornadaId")
        actualizarJornada(jornadasQuery, partidosRef, jornadasRef, 0, callback)

    }

    private fun actualizarJornada(
        jornadasQuery: Query,
        partidosRef: CollectionReference,
        jornadasRef: CollectionReference,
        index: Int,
        callback: (Boolean) -> Unit
    ) {
        jornadasQuery.get().addOnSuccessListener { jornadasSnapshot ->
            if (index < jornadasSnapshot.size()) {
                val jornadaDoc = jornadasSnapshot.documents[index]
                val jornadaId = jornadaDoc.getLong("jornadaId")
                if (jornadaId != null) {
                    val jornadaActual = jornadaId.toInt()
                    crearTablaClasificacion(jornadaActual)

                    actualizarClasificacionJornada(
                        partidosRef,
                        jornadasRef,
                        jornadaDoc.reference,
                        jornadaActual.toLong()
                    ) { success ->
                        if (success) {
                            // Llamada recursiva para pasar a la siguiente jornada
                            actualizarJornada(jornadasQuery, partidosRef, jornadasRef, index + 1, callback)

                        } else {
                            // Manejar el caso de error en la actualización de la jornada
                            callback(false)
                        }
                    }
                }
            } else {
                // Se han actualizado todas las jornadas
                callback(true)
            }
        }
    }

    private fun actualizarClasificacionJornada(
        partidosRef: CollectionReference,
        jornadasRef: CollectionReference,
        jornadaRef: DocumentReference,
        jornadaId: Long,
        callback: (Boolean) -> Unit
    ) {
        val partidosQuery = partidosRef.whereLessThanOrEqualTo("jornada", jornadaId)

        partidosQuery.get().addOnSuccessListener { partidosSnapshot ->
            jornadaRef.get().addOnSuccessListener { jornadaSnapshot ->
                val tablaClasificacion = jornadaSnapshot.toObject(AdminActivity.TablaClasificacion::class.java)

                if (tablaClasificacion != null) {
                    for (partidoDoc in partidosSnapshot.documents) {
                        val equipoLocal = partidoDoc.getString("equipoLocal")
                        val equipoVisitante = partidoDoc.getString("equipoVisitante")
                        val marcadorLocal = partidoDoc.getLong("marcadorLocal")
                        val marcadorVisitante = partidoDoc.getLong("marcadorVisitante")

                        if (equipoLocal != null && equipoVisitante != null &&
                            marcadorLocal != null && marcadorVisitante != null
                        ) {
                            actualizarPuntosEquipos(
                                tablaClasificacion.tabla,
                                equipoLocal,
                                equipoVisitante,
                                marcadorLocal.toInt(),
                                marcadorVisitante.toInt()
                            )
                        }
                    }

                    ordenarTablaClasificacion(tablaClasificacion.tabla)

                    jornadaRef.update("tabla", tablaClasificacion.tabla)
                        .addOnSuccessListener {
                            // Indicar éxito en la actualización de la jornada
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            // Indicar fallo en la actualización de la jornada
                            callback(false)
                        }
                } else {
                    // La tabla de clasificación no existe
                    callback(false)
                }
            }
        }
    }


    private fun actualizarPuntosEquipos(
        tablaClasificacion: MutableList<Clasificacion>,
        equipoLocal: String,
        equipoVisitante: String,
        marcadorLocal: Int,
        marcadorVisitante: Int
    ) {
        val clasificacionLocal = tablaClasificacion.find { it.equipo == equipoLocal }
        val clasificacionVisitante = tablaClasificacion.find { it.equipo == equipoVisitante }

        if (clasificacionLocal != null && clasificacionVisitante != null) {
            if (marcadorLocal > marcadorVisitante) {
                clasificacionLocal.pts += 3
                clasificacionLocal.pGanados += 1
                clasificacionVisitante.pPerdidos += 1
            } else if (marcadorLocal < marcadorVisitante) {
                clasificacionVisitante.pts += 3
                clasificacionVisitante.pGanados += 1
                clasificacionLocal.pPerdidos += 1
            } else {
                clasificacionLocal.pts += 1
                clasificacionLocal.pEmpatados += 1
                clasificacionVisitante.pts += 1
                clasificacionVisitante.pEmpatados += 1
            }

            clasificacionLocal.gFavor += marcadorLocal
            clasificacionLocal.gContra += marcadorVisitante
            clasificacionLocal.difGoles = clasificacionLocal.gFavor - clasificacionLocal.gContra

            clasificacionVisitante.gFavor += marcadorVisitante
            clasificacionVisitante.gContra += marcadorLocal
            clasificacionVisitante.difGoles =
                clasificacionVisitante.gFavor - clasificacionVisitante.gContra
        }
    }


    private fun ordenarTablaClasificacion(tablaClasificacion: MutableList<Clasificacion>) {
        tablaClasificacion.sortWith(compareByDescending<Clasificacion> { it.pts }
            .thenByDescending { it.difGoles })
    }

    data class TablaClasificacion(
        val tabla: List<Clasificacion>
    ) {
        constructor() : this(emptyList())
    }
}