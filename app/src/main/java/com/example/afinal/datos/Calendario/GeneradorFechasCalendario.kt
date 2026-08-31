package com.example.afinal.datos.Calendario

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

object GeneradorFechasCalendario {

    fun generar(  diasRuta: Set<DayOfWeek>,  horaRecoleccion: LocalTime, cantidad: Int = 3,
         fechasFeriadas: Map<LocalDate, String> = emptyMap() ):
         List<DiaRecorrido> {

        val ahora = FechaHora.obtenerFechaHoraLocal()
        val hoy = ahora.toLocalDate()

        val esDespuesDelCorte = (hoy.dayOfWeek == DayOfWeek.SATURDAY && ahora.toLocalTime().isAfter(LocalTime.of(13, 0))) ||
                hoy.dayOfWeek == DayOfWeek.SUNDAY

        val lunesReferencia = if (esDespuesDelCorte) {
            hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        return diasRuta.sortedBy { it.value }
            .map { diaSemana ->
                val fecha = lunesReferencia.plusDays((diaSemana.value - DayOfWeek.MONDAY.value).toLong())

                DiaRecorrido(fecha, horaRecoleccion, fecha in fechasFeriadas, fechasFeriadas[fecha] )
            }
            .take(cantidad)
    }
}