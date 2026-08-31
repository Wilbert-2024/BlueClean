package com.example.afinal.DB.vistaModal

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import org.json.JSONObject
import java.time.DayOfWeek
import java.util.EnumSet

class Calendario_vistaModal : ViewModel() {
    var barrio by mutableStateOf("")
    var diasRuta by mutableStateOf<Set<DayOfWeek>>(emptySet())
    var horaInicio by mutableStateOf("06:00")
    var horaFin by mutableStateOf("12:00")
    var estaCargando by mutableStateOf(true)

    fun cargarDatos(context: Context) {
        val datos = datosEnMemoria.obtener(context)
        if (datos != null) {
            barrio = datos.Barrio
            try {
                val detalles = JSONObject(datos.DetallesRutaJson)
                
                // Extraer Horarios
                val horarioObj = detalles.optJSONObject("Horario")
                if (horarioObj != null) {
                    horaInicio = horarioObj.optString("inicio", "06:00")
                    horaFin = horarioObj.optString("fin", "12:00")
                }

                // Extraer y convertir Días
                val diasArray = detalles.optJSONArray("Dias")
                val conjuntoDias = mutableSetOf<DayOfWeek>()
                if (diasArray != null) {
                    for (i in 0 until diasArray.length()) {
                        val diaStr = diasArray.getString(i)
                        val diaEnum = convertirDia(diaStr)
                        if (diaEnum != null) conjuntoDias.add(diaEnum)
                    }
                }
                diasRuta = conjuntoDias

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        estaCargando = false
    }

    private fun convertirDia(dia: String): DayOfWeek? {
        return when (dia.lowercase().trim()) {
            "lunes" -> DayOfWeek.MONDAY
            "martes" -> DayOfWeek.TUESDAY
            "miercoles", "miércoles" -> DayOfWeek.WEDNESDAY
            "jueves" -> DayOfWeek.THURSDAY
            "viernes" -> DayOfWeek.FRIDAY
            "sabado", "sábado" -> DayOfWeek.SATURDAY
            "domingo" -> DayOfWeek.SUNDAY
            else -> null
        }
    }
}
