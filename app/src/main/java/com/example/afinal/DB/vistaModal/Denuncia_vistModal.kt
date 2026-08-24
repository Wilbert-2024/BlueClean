package com.example.afinal.DB.vistaModal

import androidx.lifecycle.ViewModel
import com.example.afinal.DB.modal.Denuncia_Modal
import com.example.afinal.DB.repositorio.Denuncia_repositorio
import com.example.afinal.datos.Calendario.FechaHora

class Denuncia_vistModal : ViewModel() {

    fun insertarDenuncia(   barrio: String, descripcion: String,direccion: String, imagen: String,tipo: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val nuevaDenuncia = Denuncia_Modal.Datos(barrio,descripcion,direccion,Imagen = imagen, Tipo = tipo)

        Denuncia_repositorio.insertar(nuevaDenuncia, onSuccess, onError)
    }
}