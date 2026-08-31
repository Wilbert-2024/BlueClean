package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Notificacion_Modal
import com.google.firebase.firestore.Query

object Notificacion_Repositorio {
    private const val Coleccion = "Notificaciones"

    fun obtenerPorRuta(rutaId: String, onSuccess: (List<Notificacion_Modal.Datos>) -> Unit) {
        Conexion.db.collection(Coleccion)
            .whereEqualTo("Destino", rutaId)
            .orderBy("Fecha_Hora", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    doc.toObject(Notificacion_Modal.Datos::class.java)?.copy(id = doc.id)
                }
                onSuccess(lista)
            }
            .addOnFailureListener {
                onSuccess(emptyList())
            }
    }
}
