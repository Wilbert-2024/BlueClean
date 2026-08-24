package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Denuncia_Modal

object Denuncia_repositorio {
    private const val Coleccion = "Denuncia"

    fun insertar(denuncia: Denuncia_Modal.Datos, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        Conexion.db.collection(Coleccion).add(denuncia)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

}