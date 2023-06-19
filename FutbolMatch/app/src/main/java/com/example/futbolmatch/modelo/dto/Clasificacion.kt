package com.example.futbolmatch.modelo.dto

data class Clasificacion(
    val equipo: String,
    val foto: String,
    var pts: Long,
    var gFavor: Long,
    var gContra: Long,
    var pGanados: Long,
    var pPerdidos: Long,
    var pEmpatados: Long,
    var difGoles: Long
) {
    constructor() : this("", "", 0, 0, 0, 0, 0, 0, 0)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "equipo" to equipo,
            "foto" to foto,
            "pts" to pts,
            "gFavor" to gFavor,
            "gContra" to gContra,
            "pGanados" to pGanados,
            "pPerdidos" to pPerdidos,
            "pEmpatados" to pEmpatados,
            "difGoles" to difGoles
        )
    }
}