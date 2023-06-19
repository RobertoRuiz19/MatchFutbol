package com.example.futbolmatch.modelo.dto

data class Equipo(
    val nombre: String = "",
    val campo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val tecnico: String = "",
    val telefono: Long = 0,
    val foto: String = "",
    val jugadores: ArrayList<String> = ArrayList(),
    val equipacion: ArrayList<String> = ArrayList(),
) {
    constructor() : this("", "", 0.0, 0.0, "", 0, "", ArrayList(), ArrayList())
}