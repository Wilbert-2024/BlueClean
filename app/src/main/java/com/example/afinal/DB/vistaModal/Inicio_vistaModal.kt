package com.example.afinal.DB.vistaModal

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.componentes.EstadoPuntoRecorrido
import com.example.afinal.componentes.PuntoRecorrido
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.DB.repositorio.camion_repositprio
import com.example.afinal.DB.repositorio.Feriado_Repositorio
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
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
    
    // --- FLUJO TIEMPO REAL: Registro para cancelar la vigilancia al cerrar la pantalla ---
    private var rutaListener: ListenerRegistration? = null

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

            // --- FLUJO TIEMPO REAL: Iniciar la vigilancia del Estado ---
            iniciarVigilanciaEstado(datos.RutaId)
            
            // --- SINCRONIZACIÓN: Actualizar feriados en segundo plano ---
            sincronizarFeriados(context)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sincronizarFeriados(context: Context) {
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
                val nuevoJson = arrayJson.toString()
                val actualJson = datosEnMemoria.obtener(context)?.FeriadosJson ?: "[]"
                
                // Solo guardamos si hay cambios reales para ahorrar escritura en disco
                if (nuevoJson != actualJson) {
                    Log.d("Inicio_vistaModal", "Feriados actualizados desde Firebase")
                    datosEnMemoria.guardarFeriadosLocal(context, nuevoJson)
                }
            } catch (e: Exception) {
                Log.e("Inicio_vistaModal", "Error sincronizando feriados: ${e.message}")
            }
        }
    }

    private fun iniciarVigilanciaEstado(rutaId: String) {
        rutaListener?.remove()
        
        // Ahora vigilamos la colección "Camiones" buscando el que tenga el ruta_id correspondiente
        rutaListener = com.example.afinal.DB.repositorio.camion_repositprio.observarCamionPorRuta(rutaId) { nuevoEstado ->
            Log.d("Inicio_vistaModal", "¡Cambio en el camión detectado! Estado: $nuevoEstado")
            estadoServicio = nuevoEstado
        }
    }

    // --- FLUJO TIEMPO REAL: Función para limpiar recursos al salir de la pantalla ---
    override fun onCleared() {
        super.onCleared()
        rutaListener?.remove()
    }
}
