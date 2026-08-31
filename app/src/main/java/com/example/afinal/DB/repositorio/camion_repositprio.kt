package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.camion_Modal
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log


object camion_repositprio {
    private const val Coleccion = "Camiones"


    fun insertar(Camion: camion_Modal.DatosGenerales){
        Conexion.db.collection(Coleccion).add(Camion)
    }

    fun insertarConID(id: String, Camion: camion_Modal.DatosGenerales){
        Conexion.db.collection(Coleccion).document(id).set(Camion)
    }

    fun obtener (id: String, resultado:(camion_Modal.DatosGenerales?)-> Unit){
        Conexion.db.collection(Coleccion).document(id).get().addOnSuccessListener { documento ->
            resultado(documento.toObject(camion_Modal.DatosGenerales::class.java))
        }
    }

    fun obtenerDatosEscogidos(id: String, result: (camion_Modal.datosEscogidos?)-> Unit){
        Conexion.db.collection(Coleccion).document(id).get().addOnSuccessListener { documento ->
            result(documento.toObject(camion_Modal.datosEscogidos::class.java))
        }
    }

    fun obtenerUbicacion(id: String, result: (GeoPoint?) -> Unit){
        Conexion.db.collection(Coleccion).document(id).get().addOnSuccessListener {
            documento -> result(documento.getGeoPoint("Ubicacion_actual"))
        }
    }


    fun actualizar(id: String, camion: camion_Modal){
        Conexion.db.collection(Coleccion).document(id).set(camion)
    }

    fun eliminar (id: String){
        Conexion.db.collection(Coleccion).document(id).delete()
    }

    /**
     * Vigila el estado del camión asignado a una ruta específica.
     */
    fun observarCamionPorRuta(rutaId: String, onUpdate: (Boolean) -> Unit): ListenerRegistration {
        return Conexion.db.collection(Coleccion)
            .whereEqualTo("ruta_id", rutaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("camion_repositprio", "Error vigilando camión: ${error.message}")
                    return@addSnapshotListener
                }
                
                val camionDoc = snapshot?.documents?.firstOrNull()
                if (camionDoc != null) {
                    val estado = camionDoc.getBoolean("Estado") ?: false
                    onUpdate(estado)
                }
            }
    }

    // --- FLUJO MAPA: Vigilar la ubicación del camión en tiempo real ---
    fun observarUbicacionReal(rutaId: String, onUpdate: (GeoPoint?) -> Unit): ListenerRegistration {
        return Conexion.db.collection(Coleccion)
            .whereEqualTo("ruta_id", rutaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val camionDoc = snapshot?.documents?.firstOrNull()
                if (camionDoc != null) {
                    onUpdate(camionDoc.getGeoPoint("Ubicacion_actual"))
                }
            }
    }







}