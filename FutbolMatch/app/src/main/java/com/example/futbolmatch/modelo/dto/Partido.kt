package com.example.futbolmatch.modelo.dto

data class Partido(
    val id: String = "",
    val equipoLocal: String = "",
    val equipoVisitante: String = "",
    val fecha: String = "",
    val hora: String = "",
    val jornada: Long? = 1,
    val marcadorLocal: Long? = 0,
    val marcadorVisitante: Long? = 0
) {
    constructor() : this("", "", "", "", "", 1, 0, 0)
}