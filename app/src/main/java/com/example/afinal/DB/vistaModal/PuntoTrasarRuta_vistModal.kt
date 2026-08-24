package com.example.afinal.DB.vistaModal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.DB.repositorio.PuntoTrasarRuta_Repositorio
import com.example.afinal.datosTemporal.TrasarLineas
import com.google.firebase.firestore.GeoPoint

class PuntoTrasarRuta_vistModal : ViewModel() {

    var coordenadas by mutableStateOf<List<GeoPoint>>(emptyList())
        private set


    fun obtenerCoordenadasPorRuta(rutaId: String) {

        PuntoTrasarRuta_Repositorio.obtenerCoordenadasPorRutas(
            rutaId = rutaId,

            onSuccess = { puntos ->
                coordenadas = puntos
            },

            onError = { exception ->
                exception.printStackTrace()
            }
        )
    }


    fun insertarPuntos( rutaId: String,  onSuccess: () -> Unit, onError: (Exception)-> Unit ) {

        val puntos = TrasarLineas.coordenadas()
        PuntoTrasarRuta_Repositorio.insertarPuntos(rutaId,puntos,
            onSuppress = {  onSuccess()},

            onError = { exception ->
                onError(exception)
            }
        )
    }
}