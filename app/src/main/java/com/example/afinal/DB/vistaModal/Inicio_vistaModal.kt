package com.example.afinal.DB.vistaModal

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.componentes.EstadoPuntoRecorrido
import com.example.afinal.componentes.PuntoRecorrido
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import org.json.JSONArray
import org.json.JSONObject

class Inicio_vistaModal : ViewModel() {
    var nombreUsuario by mutableStateOf("")
    var barrioUsuario by mutableStateOf("")
    var nombreUnidad by mutableStateOf("Unidad #01")
    var estadoServicio by mutableStateOf(false)
    var horarioRuta by mutableStateOf("")
    var puntosRutaParaVisualizador by mutableStateOf<List<PuntoRecorrido>>(emptyList())
    var origenDestino by mutableStateOf(Pair("Origen", "Destino"))

    fun cargarDatos(context: Context) {
        val datos = datosEnMemoria.obtener(context) ?: return
        
        nombreUsuario = datos.NomUsuario
        barrioUsuario = datos.Barrio
        
        try {
            // Desempaquetar detalles de la ruta
            val detalles = JSONObject(datos.DetallesRutaJson)
            nombreUnidad = detalles.optString("Nombre", "Unidad #01")
            estadoServicio = detalles.optBoolean("Estado", false)
            
            // Construir string de horario (ej: "06:00 - 12:00")
            val horarioObj = detalles.optJSONObject("Horario")
            if (horarioObj != null) {
                horarioRuta = "${horarioObj.optString("inicio", "06:00")} - ${horarioObj.optString("fin", "12:00")}"
            }

            // Desempaquetar puntos de la ruta para el Visualizador
            val puntosArray = JSONArray(datos.PuntosRutaJson)
            val listaTemporal = mutableListOf<PuntoRecorrido>()
            
            for (i in 0 until puntosArray.length()) {
                val p = puntosArray.getJSONObject(i)
                val nombre = p.getString("Nombre")
                
                // Por ahora marcamos como COMPLETADO o PROXIMO basado en la posición 
                // Esto se hará real con el seguimiento GPS más adelante
                val estado = if (i < puntosArray.length() / 2) EstadoPuntoRecorrido.COMPLETADO 
                             else if (i == puntosArray.length() / 2) EstadoPuntoRecorrido.ACTUAL
                             else EstadoPuntoRecorrido.PROXIMO
                
                listaTemporal.add(PuntoRecorrido(nombre, estado))
            }
            puntosRutaParaVisualizador = listaTemporal
            
            // Establecer origen y destino para el indicador
            if (listaTemporal.isNotEmpty()) {
                origenDestino = Pair(listaTemporal.first().nombre, listaTemporal.last().nombre)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
