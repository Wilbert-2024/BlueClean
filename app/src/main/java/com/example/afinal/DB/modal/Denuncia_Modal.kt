package com.example.afinal.DB.modal

import com.google.firebase.Timestamp

object Denuncia_Modal {
    data class Datos(
        val Barrio: String,
        val Descripcion: String,
        val Direccion: String,
        val Estado : Boolean = false,
        val Fecha_Hora: Timestamp = Timestamp.now(),
        val Imagen: String,
        val Tipo: String
    )
}