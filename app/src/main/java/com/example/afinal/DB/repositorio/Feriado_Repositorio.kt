package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.Feriado_Modal
import com.example.afinal.datosTemporal.DiasFeriados

object Feriado_Repositorio {
    private const val Coleccion = "Feriados"

    fun obtenerTodos(onSuccess: (List<Feriado_Modal.Datos>) -> Unit) {
        Conexion.db.collection(Coleccion).get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Feriado_Modal.Datos::class.java)
                onSuccess(lista)
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }

    // Función para subir la lista inicial a Firebase
    fun inicializarFeriados() {
        DiasFeriados.lista.forEach { f ->
            val datos = Feriado_Modal.Datos(f.nombre, f.dia, f.mes, f.anio)
            Conexion.db.collection(Coleccion).add(datos)
        }
    }
}
