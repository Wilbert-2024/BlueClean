package com.example.afinal.mapa

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class ConsultaBaseDato {

    private val database: FirebaseDatabase

    init {
        database = FirebaseDatabase.getInstance(FirebaseBootstrap.DATABASE_URL)
    }

    fun obtenerEstadoActivo(
        context: Context,
        rutaAsignada: String,
        estadoCampo: String,
        onResultado: (Boolean) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val rutaRef = database.getReference("ubicacion").child(rutaAsignada).child(estadoCampo)

        rutaRef.get()
            .addOnSuccessListener { dataSnapshot ->
                val activo = dataSnapshot.getValue(Boolean::class.java) ?: false
                onResultado(activo)
            }
            .addOnFailureListener { e ->
                onError?.invoke(e.message ?: "Error inesperado")
            }
    }

    fun rutaDisponible(context: Context, callback: (String?, DataSnapshot?) -> Unit) {
        val rutasRef = database.getReference("ubicacion")

        rutasRef.get()
            .addOnSuccessListener { snapshot ->
                val rutaDisponible = snapshot.children.firstOrNull { rutaSnapshot ->
                    val usando = rutaSnapshot.child("Usando").getValue(Boolean::class.java) ?: true
                    val activo = rutaSnapshot.child("Activo").getValue(Boolean::class.java) ?: true
                    usando && activo
                }

                if (rutaDisponible != null) {
                    val nombreRuta = rutaDisponible.key
                    callback(nombreRuta, snapshot)
                    Toast.makeText(context, "Mostrando la Ruta: $nombreRuta", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No hay rutas disponibles", Toast.LENGTH_SHORT).show()
                    callback(null, snapshot)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al obtener rutas: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(null, null)
            }
    }

    fun obtenerTodasLasRutas(context: Context, callback: (List<String>, DataSnapshot?) -> Unit) {
        val rutasRef = database.getReference("ubicacion")
        rutasRef.get()
            .addOnSuccessListener { snapshot ->
                val todasLasRutas = snapshot.children.mapNotNull { it.key }
                callback(todasLasRutas, snapshot)
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al obtener todas las rutas: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(emptyList(), null)
            }
    }

    fun obtenerLasRutas(context: Context, callback: (List<String>, String?) -> Unit) {
        val rutasRef = database.getReference("ubicacion")
        rutasRef.get()
            .addOnSuccessListener { snapshot ->
                val todasLasRutas = snapshot.children.mapNotNull { it.key }
                callback(todasLasRutas, null)
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Error al obtener todas las rutas: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(emptyList(), error.message)
            }
    }

    fun obtenerTiempoRuta(context: Context, ruta: String, onResultado: (Long?) -> Unit) {
        val ref = database.getReference("ubicacion/$ruta/coordenadas/timestamp")
        ref.get()
            .addOnSuccessListener { snapshot ->
                val tiempo = snapshot.getValue(Long::class.java)
                onResultado(tiempo)
            }
            .addOnFailureListener {
                onResultado(null)
            }
    }
}
