package com.example.afinal.datos.Calendario

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

object GeneradorRecorridos {

    fun generar(
        diasRuta: Set<DayOfWeek>,
        horaRecoleccion: LocalTime,
        cantidad: Int = 3,
        fechasFeriadas: Set<LocalDate> = emptySet()
    ): List<DiaRecorrido> {

        val ahora = FechaHora.obtenerFechaHoraLocal()
        val hoy = ahora.toLocalDate()

        // Lógica de corte: Sábado a la 1:00 PM (13:00)
        val esDespuesDelCorte = (hoy.dayOfWeek == DayOfWeek.SATURDAY && ahora.toLocalTime().isAfter(LocalTime.of(13, 0))) ||
                hoy.dayOfWeek == DayOfWeek.SUNDAY

        // Determinar el lunes de la semana que queremos mostrar
        val lunesReferencia = if (esDespuesDelCorte) {
            hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        // Generamos los días de la ruta para esa semana específica
        // Ordenamos por día de la semana para que siempre salgan en orden cronológico
        return diasRuta.sortedBy { it.value }
            .map { diaSemana ->
                // Calculamos la fecha sumando los días desde el lunes de referencia
                val fecha = lunesReferencia.plusDays((diaSemana.value - DayOfWeek.MONDAY.value).toLong())
                DiaRecorrido(
                    fecha = fecha,
                    hora = horaRecoleccion,
                    esFeriado = fecha in fechasFeriadas
                )
            }
            .take(cantidad)
    }
}