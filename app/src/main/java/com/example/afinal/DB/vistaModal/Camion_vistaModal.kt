package com.example.afinal.DB.vistaModal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.afinal.DB.repositorio.camion_repositprio
import com.google.firebase.firestore.GeoPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.afinal.DB.modal.camion_Modal

class Camion_vistaModal : ViewModel() {
    var ubicacion by mutableStateOf<GeoPoint?>(null)
        private set
    var datosEscogidos by mutableStateOf<camion_Modal.datosEscogidos?>(null)
        private set


    fun obtenerUbicacion(id: String) {
        camion_repositprio.obtenerUbicacion(id) { resultado ->
            ubicacion = resultado
        }
    }

    fun obtenerDatosEscogidos(id: String) {
        camion_repositprio.obtenerDatosEscogidos(id) { resultado ->
            datosEscogidos = resultado
        }
    }





}