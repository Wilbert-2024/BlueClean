package com.example.afinal.DB.modal

import com.google.firebase.firestore.GeoPoint

object camion_Modal {
    data class DatosGenerales(
        val Estado: Boolean = false,
        val Placa: String,
        val Ubicacion_actual: GeoPoint = GeoPoint(0.0, 0.0),
        val ruta_id: String = ""
    )

    data class datosEscogidos(
        val Placa: String="",
        val ruta_id: String = ""
    )




}