package com.example.afinal.DB.modal

import com.google.firebase.Timestamp

object Notificacion_Modal {
    data class Datos(
        val id: String = "", // Para rastrear si ya se leyó
        val Titulo: String = "",
        val Mensaje: String = "",
        val Fecha_Hora: Timestamp? = null,
        val Destino: String = "",
        val Tipo: String = "NOVEDAD" // EMERGENCIA, ALERTA, NOVEDAD
    )
}
