package com.example.afinal.datos.Calendario

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object GeneradorRecorridos {

    fun generar( diasRuta: Set<DayOfWeek>,   horaRecoleccion: LocalTime,
        cantidad: Int = 3,     fechasFeriadas: Set<LocalDate> = emptySet()
    ): List<DiaRecorrido> {

        val ahora = FechaHora.obtenerFechaHoraLocal()
        val hoy = ahora.toLocalDate()
        val horaActual = ahora.toLocalTime()

        return generateSequence(hoy) { fecha ->
            fecha.plusDays(1)
        }
            .filter { fecha ->

                val correspondeARuta =  fecha.dayOfWeek in diasRuta

                val recorridoPendiente = fecha != hoy || !horaActual.isAfter(horaRecoleccion)

                correspondeARuta && recorridoPendiente
            }
            .take(cantidad).map { fecha ->
                DiaRecorrido( fecha,horaRecoleccion, fecha in fechasFeriadas )
            }
            .toList()
    }
}