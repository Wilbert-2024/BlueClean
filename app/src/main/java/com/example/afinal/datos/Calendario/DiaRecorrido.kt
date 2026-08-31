package com.example.afinal.datos.Calendario

import java.time.LocalDate
import java.time.LocalTime

data class DiaRecorrido (
    val fecha: LocalDate,
    val hora: LocalTime,
    val esFeriado: Boolean = false,
    val nombreFeriado: String? = null
)
