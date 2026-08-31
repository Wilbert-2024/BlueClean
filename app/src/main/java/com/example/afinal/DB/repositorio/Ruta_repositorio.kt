package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Rutas_Modal
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log

object Ruta_repositorio {
    private const val Coleccion = "Rutas"

    fun obtener(onSuccess: (List<Rutas_Modal.Datos>) -> Unit) {
        Conexion.db.collection(Coleccion).get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Rutas_Modal.Datos::class.java)
                onSuccess(lista)
            }
    }

    // Nueva: Obtiene los detalles de una ruta específica mediante su ID
    fun obtenerPorId(rutaId: String, onSuccess: (Rutas_Modal.Datos?) -> Unit) {
        Conexion.db.collection(Coleccion).document(rutaId).get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.toObject(Rutas_Modal.Datos::class.java))
            }
            .addOnFailureListener { onSuccess(null) }
    }

    // --- FLUJO TIEMPO REAL: Función para vigilar cambios en el documento de la ruta ---
    fun observarRuta(rutaId: String, onUpdate: (Rutas_Modal.Datos?) -> Unit): ListenerRegistration {
        return Conexion.db.collection(Coleccion).document(rutaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Ruta_repositorio", "Error en tiempo real: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    onUpdate(snapshot.toObject(Rutas_Modal.Datos::class.java))
                }
            }
    }

    fun agregar(ruta: Rutas_Modal.Datos) {
        Conexion.db.collection(Coleccion).add(ruta)
    }
}