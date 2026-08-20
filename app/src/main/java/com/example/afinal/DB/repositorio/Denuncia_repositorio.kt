package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Denuncia_Modal

object Denuncia_repositorio {
    private const val Coleccion = "Denuncia"

    fun insertar (denucia: Denuncia_Modal.Datos){
        Conexion.db.collection(Coleccion).add(denucia)
    }

}