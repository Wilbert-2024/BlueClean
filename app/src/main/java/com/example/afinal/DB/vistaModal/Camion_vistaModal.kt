package com.example.afinal.DB.vistaModal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.afinal.DB.repositorio.camion_repositprio
import com.google.firebase.firestore.GeoPoint

class camion_visModal : ViewModel() {
    var ubicacion by mutableStateOf<GeoPoint?>(null)
        private set

    fun obtenerUbicacion(id: String) {
        camion_repositprio.obtenerUbicacion(id) { resultado ->
            ubicacion = resultado
        }
    }
}