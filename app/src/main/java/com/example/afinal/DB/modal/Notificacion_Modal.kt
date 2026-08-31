package com.example.afinal.DB.modal

import com.google.firebase.Timestamp

object Notificacion_Modal {
    data class Datos(
        val Titulo: String = "",
        val Mensaje: String = "",
        val Fecha_Hora: Timestamp? = null,
        val Destino: String = ""
    )
}
