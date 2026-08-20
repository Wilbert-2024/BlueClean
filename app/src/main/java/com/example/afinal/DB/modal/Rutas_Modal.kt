package com.example.afinal.DB.modal

object Rutas_Modal {
    data class Datos (
        val Barrios: List<String> = emptyList(),
        val Dias: List<String> = emptyList(),
        val Estado: Boolean = false,
        val Horario: Map<String, String> = emptyMap(),
        val Nombre: String = "",
        val Recorrido: Map<String, String> = emptyMap()
    )
}
