package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Rutas_Modal

object Ruta_repositorio {
    private const val Coleccion = "Rutas"

    fun obtener(onSuccess: (List<Rutas_Modal.Datos>) -> Unit) {
        Conexion.db.collection(Coleccion).get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Rutas_Modal.Datos::class.java)
                onSuccess(lista)
            }
    }

    fun agregar(ruta: Rutas_Modal.Datos) {
        Conexion.db.collection(Coleccion).add(ruta)
    }
}