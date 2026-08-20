package com.example.afinal.DB.modal

import com.google.firebase.firestore.GeoPoint

object PuntoTrasarRuta_Modal {
    data class Datos (
        val Coordenadas: List<GeoPoint> = emptyList()
    )
}