package com.example.afinal.DB.repositorio

import android.util.Log
import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.PuntoTrasarRuta_Modal
import com.google.firebase.firestore.GeoPoint

object PuntoTrasarRuta_Repositorio {
    private const val Coleeccion = "Punto_trasar_ruta"

    fun insertarPuntos(
        rutaId: String,
        Puntos: List<GeoPoint>,
        onSuppress: () -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {

        val datos = PuntoTrasarRuta_Modal.Datos(Puntos)

        Conexion.db
            .collection(Coleeccion)
            .document(rutaId)
            .set(datos)

            .addOnSuccessListener {

                Log.d(
                    "FIRESTORE_PRUEBA",
                    "GUARDADO CORRECTAMENTE: ${Puntos.size} coordenadas"
                )

                onSuppress()
            }

            .addOnFailureListener { exception ->

                Log.e(
                    "FIRESTORE_PRUEBA",
                    "ERROR AL GUARDAR",
                    exception
                )

                onError?.invoke(exception)
            }
    }

/*
    fun insertarPuntos(rutaId: String, Puntos: List<GeoPoint>, onSuppress: ()-> Unit, onError: ((Exception) -> Unit)? = null){
        val datos = PuntoTrasarRuta_Modal.Datos(Puntos)

        Conexion.db.collection(Coleeccion).document(rutaId).set(datos)
            .addOnSuccessListener { onSuppress }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }
*/

    fun obtenerCoordenadasPorRutas(rutaId: String, onSuccess: (List<GeoPoint>) -> Unit, onError: ((Exception)-> Unit)? = null){
        Conexion.db.collection(Coleeccion).document(rutaId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()){
                    val datos = doc.toObject(PuntoTrasarRuta_Modal.Datos::class.java)
                    onSuccess(datos?.coordenadas?: emptyList())

                } else{ onSuccess(emptyList()) }
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }

    }



}