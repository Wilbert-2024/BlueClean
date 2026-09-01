package com.example.afinal.DB.vistaModal

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.ArchivoMapa.CalculadorTiempo
import com.example.afinal.ArchivoMapa.SeguimientoGPS
import com.example.afinal.DB.repositorio.camion_repositprio
import com.example.afinal.DB.repositorio.PuntoTrasarRuta_Repositorio
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONArray

class Mapa_vistaModal : ViewModel() {
    var barrioUsuario by mutableStateOf("")
    var ubicacionCamion by mutableStateOf<LatLng?>(null)
    var ubicacionUsuario by mutableStateOf<LatLng?>(null)
    var rutaTrazada by mutableStateOf<List<LatLng>>(emptyList())
    var proximasParadas by mutableStateOf<List<String>>(emptyList())
    var estaCargando by mutableStateOf(true)
    var minutosRestantes by mutableIntStateOf(0)
    var velocidadCamion by mutableDoubleStateOf(0.0)

    private var ubicacionListener: ListenerRegistration? = null
    private var gestorGPS: SeguimientoGPS? = null

    fun cargarDatos(context: Context) {
        val datos = datosEnMemoria.obtener(context) ?: return
        barrioUsuario = datos.Barrio
        val rutaId = datos.RutaId

        PuntoTrasarRuta_Repositorio.obtenerCoordenadasPorRutas(rutaId, onSuccess = { puntos ->
            rutaTrazada = puntos.map { LatLng(it.latitude, it.longitude) }
            intentarCalcularTiempo()
            verificarCargaCompleta()
        }, onError = { err ->
            Log.e("Mapa_vistaModal", "Error cargando ruta: ${err.message}")
        })

        try {
            val puntosArray = JSONArray(datos.PuntosRutaJson)
            val paradas = mutableListOf<String>()
            for (i in 0 until puntosArray.length()) {
                paradas.add(puntosArray.getJSONObject(i).getString("Nombre"))
            }
            proximasParadas = paradas
        } catch (e: Exception) { e.printStackTrace() }

        iniciarSeguimientoCamion(rutaId)
    }

    fun iniciarGpsUsuario(context: Context) {
        gestorGPS?.detener()
        gestorGPS = SeguimientoGPS(context)
        gestorGPS?.iniciar { nuevaUbicacion ->
            ubicacionUsuario = nuevaUbicacion
            intentarCalcularTiempo()
        }
    }

    fun detenerGpsUsuario() {
        gestorGPS?.detener()
        gestorGPS = null
        ubicacionUsuario = null
    }

    private fun iniciarSeguimientoCamion(rutaId: String) {
        ubicacionListener?.remove()
        ubicacionListener = camion_repositprio.observarUbicacionYVelocidad(rutaId) { punto, velocidad ->
            if (punto != null) {
                ubicacionCamion = LatLng(punto.latitude, punto.longitude)
                velocidadCamion = velocidad
                intentarCalcularTiempo()
            }
            verificarCargaCompleta()
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

    private fun verificarCargaCompleta() {
        if (rutaTrazada.isNotEmpty()) estaCargando = false
    }

    override fun onCleared() {
        super.onCleared()
        ubicacionListener?.remove()
        gestorGPS?.detener()
    }
}
