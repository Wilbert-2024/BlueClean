package com.example.afinal.datos.Calendario

import java.util.Locale
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object FechaHora {

    private val idiomaEspanol = Locale("es", "NI")

    fun obtenerHoraActual(): String {
        val formato = DateTimeFormatter.ofPattern( "HH:mm:ss", Locale.getDefault() )

        return LocalTime.now().format(formato)
    }

    fun obtenerFechaActual(): String {
        val formato = DateTimeFormatter.ofPattern( "yyyy-MM-dd", Locale.getDefault() )

        return LocalDate.now().format(formato)
    }

    fun obtenerFechaCorta(): String {

        val formato = DateTimeFormatter.ofPattern(  "d MMM", idiomaEspanol )

        return LocalDate.now().format(formato).lowercase(idiomaEspanol)
    }


    fun obtenerFechaLocal(): LocalDate { return LocalDate.now()  }

    fun obtenerHoraLocal(): LocalTime {   return LocalTime.now()  }

    fun obtenerFechaHoraLocal(): LocalDateTime { return LocalDateTime.now() }

}