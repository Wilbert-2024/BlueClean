package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.camion_Modal
import com.google.firebase.firestore.GeoPoint


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







}