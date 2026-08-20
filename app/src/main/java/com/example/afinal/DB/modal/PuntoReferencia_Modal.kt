package com.example.afinal.DB.modal

import com.google.firebase.firestore.GeoPoint

object PuntoReferencia_Modal {

    data class Lugar(
        val Coordenadas: GeoPoint = GeoPoint(0.0, 0.0),
        val Nombre: String = "",
        val ruta_id: String = ""
    )

    data class Datos (
        val Lugares: List<Lugar> = emptyList()
    )
}