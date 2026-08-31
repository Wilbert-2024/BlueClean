package com.example.afinal.DB.vistaModal

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.ArchivoMapa.CalculadorTiempo
import com.example.afinal.ArchivoMapa.SeguimientoGPS
import com.example.afinal.componentes.EstadoPuntoRecorrido
import com.example.afinal.componentes.PuntoRecorrido
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.DB.repositorio.camion_repositprio
import com.example.afinal.DB.repositorio.Feriado_Repositorio
import com.example.afinal.DB.repositorio.PuntoTrasarRuta_Repositorio
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ListenerRegistration
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
    
    // --- LÓGICA DE TIEMPO REAL ---
    var minutosRestantes by mutableIntStateOf(0)
    var velocidadCamion by mutableDoubleStateOf(0.0)
    var ubicacionUsuario by mutableStateOf<LatLng?>(null)
    var ubicacionCamion by mutableStateOf<LatLng?>(null)
    var rutaTrazada by mutableStateOf<List<LatLng>>(emptyList())

    private var rutaListener: ListenerRegistration? = null
    private var ubicacionCamionListener: ListenerRegistration? = null
    private var gestorGPS: SeguimientoGPS? = null

    fun cargarDatos(context: Context) {
        val datos = datosEnMemoria.obtener(context) ?: return
        nombreUsuario = datos.NomUsuario
        barrioUsuario = datos.Barrio
        val rutaId = datos.RutaId
        
        try {
            val detalles = JSONObject(datos.DetallesRutaJson)
            nombreUnidad = detalles.optString("Nombre", "Unidad #01")
            
            val horarioObj = detalles.optJSONObject("Horario")
            if (horarioObj != null) {
                horarioRuta = "${horarioObj.optString("inicio", "06:00")} - ${horarioObj.optString("fin", "12:00")}"
            }

            val puntosArray = JSONArray(datos.PuntosRutaJson)
            val listaTemporal = mutableListOf<PuntoRecorrido>()
            for (i in 0 until puntosArray.length()) {
                val p = puntosArray.getJSONObject(i)
                listaTemporal.add(PuntoRecorrido(p.getString("Nombre"), EstadoPuntoRecorrido.PROXIMO))
            }
            puntosRutaParaVisualizador = listaTemporal
            if (listaTemporal.isNotEmpty()) {
                origenDestino = Pair(listaTemporal.first().nombre, listaTemporal.last().nombre)
            }

            // 1. Cargar la ruta completa para el cálculo de tiempo
            PuntoTrasarRuta_Repositorio.obtenerCoordenadasPorRutas(rutaId, onSuccess = { puntos ->
                rutaTrazada = puntos.map { LatLng(it.latitude, it.longitude) }
                intentarCalcularTiempo()
            }, onError = {})

            // 2. Iniciar vigilancia del estado, ubicación y velocidad del camión
            iniciarSeguimientoReal(rutaId)
            
            // 3. Sincronización de feriados
            sincronizarFeriados(context)

        } catch (e: Exception) { e.printStackTrace() }
    }

    fun iniciarGpsUsuario(context: Context) {
        gestorGPS = SeguimientoGPS(context)
        gestorGPS?.iniciar { nuevaPos ->
            ubicacionUsuario = nuevaPos
            intentarCalcularTiempo()
        }
    }

    private fun iniciarSeguimientoReal(rutaId: String) {
        // Vigilar Estado
        rutaListener?.remove()
        rutaListener = camion_repositprio.observarCamionPorRuta(rutaId) { estadoServicio = it }

        // Vigilar Ubicación Y Velocidad del Camión
        ubicacionCamionListener?.remove()
        ubicacionCamionListener = camion_repositprio.observarUbicacionYVelocidad(rutaId) { punto, velocidad ->
            if (punto != null) {
                ubicacionCamion = LatLng(punto.latitude, punto.longitude)
                velocidadCamion = velocidad
                intentarCalcularTiempo()
            }
        }
    }

    private fun intentarCalcularTiempo() {
        val uUser = ubicacionUsuario
        val uCamion = ubicacionCamion
        val ruta = rutaTrazada

        if (uUser != null && uCamion != null && ruta.isNotEmpty()) {
            minutosRestantes = CalculadorTiempo.estimarMinutosLlegada(uCamion, uUser, ruta, velocidadCamion)
        }
    }

    private fun sincronizarFeriados(context: Context) {
        Feriado_Repositorio.obtenerTodos { lista ->
            try {
                val arrayJson = JSONArray()
                lista.forEach { f ->
                    arrayJson.put(JSONObject().apply {
                        put("nombre", f.nombre); put("dia", f.dia)
                        put("mes", f.mes); put("anio", f.anio)
                    })
                }
                datosEnMemoria.guardarFeriadosLocal(context, arrayJson.toString())
            } catch (e: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        rutaListener?.remove()
        ubicacionCamionListener?.remove()
        gestorGPS?.detener()
    }
}
