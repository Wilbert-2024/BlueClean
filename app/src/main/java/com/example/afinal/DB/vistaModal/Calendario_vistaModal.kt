package com.example.afinal.DB.vistaModal

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.DB.repositorio.Feriado_Repositorio
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class Calendario_vistaModal : ViewModel() {
    var barrio by mutableStateOf("")
    var diasRuta by mutableStateOf<Set<DayOfWeek>>(emptySet())
    var horaInicio by mutableStateOf("06:00")
    var horaFin by mutableStateOf("12:00")
    var estaCargando by mutableStateOf(true)
    var fechasFeriadas by mutableStateOf<Map<LocalDate, String>>(emptyMap())
    var mesActual by mutableStateOf(YearMonth.now())
    var diaSeleccionado by mutableStateOf<LocalDate>(LocalDate.now())

    fun mesAnterior() { mesActual = mesActual.minusMonths(1) }
    fun mesSiguiente() { mesActual = mesActual.plusMonths(1) }
    fun seleccionarDia(fecha: LocalDate) { diaSeleccionado = fecha }

    fun cargarDatos(context: Context) {
        val datos = datosEnMemoria.obtener(context)
        if (datos != null) {
            barrio = datos.Barrio
            try {
                val detalles = JSONObject(datos.DetallesRutaJson)
                val horarioObj = detalles.optJSONObject("Horario")
                if (horarioObj != null) {
                    horaInicio = horarioObj.optString("inicio", "06:00")
                    horaFin = horarioObj.optString("fin", "12:00")
                }
                val diasArray = detalles.optJSONArray("Dias")
                val conjuntoDias = mutableSetOf<DayOfWeek>()
                if (diasArray != null) {
                    for (i in 0 until diasArray.length()) {
                        val diaStr = diasArray.getString(i)
                        convertirDia(diaStr)?.let { conjuntoDias.add(it) }
                    }
                }
                diasRuta = conjuntoDias

                if (datos.FeriadosJson != "[]") {
                    procesarFeriadosJson(datos.FeriadosJson)
                    estaCargando = false
                } else {
                    descargarFeriadosFirebase(context)
                }

            } catch (e: Exception) {
                Log.e("CalendarioVM", "Error cargando datos: ${e.message}")
                estaCargando = false
            }
        } else {
            estaCargando = false
        }
    }

    private fun descargarFeriadosFirebase(context: Context) {
        Feriado_Repositorio.obtenerTodos { lista ->
            try {
                val arrayJson = JSONArray()
                lista.forEach { f ->
                    val obj = JSONObject().apply {
                        put("nombre", f.nombre); put("dia", f.dia)
                        put("mes", f.mes); put("anio", f.anio)
                    }
                    arrayJson.put(obj)
                }
                val jsonStr = arrayJson.toString()
                datosEnMemoria.guardarFeriadosLocal(context, jsonStr)
                procesarFeriadosJson(jsonStr)
            } catch (e: Exception) {
                Log.e("CalendarioVM", "Error procesando Firebase: ${e.message}")
            }
            estaCargando = false
        }
    }

    private fun procesarFeriadosJson(json: String) {
        try {
            val array = JSONArray(json)
            val hoy = LocalDate.now()
            val anioActual = hoy.year
            val feriadosMap = mutableMapOf<LocalDate, String>()

            for (i in 0 until array.length()) {
                val f = array.getJSONObject(i)
                val dia = f.getInt("dia")
                val mes = f.getInt("mes")
                val anio = f.getInt("anio")
                val nombre = f.getString("nombre")

                if (dia in 1..31 && mes in 1..12) {
                    if (anio == 0) {
                        feriadosMap[LocalDate.of(anioActual, mes, dia)] = nombre
                        feriadosMap[LocalDate.of(anioActual + 1, mes, dia)] = nombre
                    } else if (anio >= anioActual) {
                        feriadosMap[LocalDate.of(anio, mes, dia)] = nombre
                    }
                }
            }
            fechasFeriadas = feriadosMap
        } catch (e: Exception) {
            Log.e("CalendarioVM", "Error procesando JSON: ${e.message}")
        }
    }

    private fun convertirDia(dia: String): DayOfWeek? {
        return when (dia.lowercase().trim()) {
            "lunes" -> DayOfWeek.MONDAY; "martes" -> DayOfWeek.TUESDAY
            "miercoles", "miércoles" -> DayOfWeek.WEDNESDAY
            "jueves" -> DayOfWeek.THURSDAY; "viernes" -> DayOfWeek.FRIDAY
            "sabado", "sábado" -> DayOfWeek.SATURDAY; "domingo" -> DayOfWeek.SUNDAY
            else -> null
        }
    }
}
