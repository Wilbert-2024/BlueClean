package com.example.afinal.DB.repositorio

import android.util.Log
import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.datos.mensajeria.Mensajeria
import org.json.JSONArray
import org.json.JSONObject

object ExportadorDatos {
    private const val TAG = "DEBUG_DB_EXPORT"

    fun exportarTodoALogcat() {
        Mensajeria.advertencia("Iniciando exportación de base de datos...")
        val backupCompleto = JSONObject()

        // 1. Exportar Puntos_referencia
        Conexion.db.collection("Puntos_referencia").get().addOnSuccessListener { puntosResult ->
            val puntosJson = JSONObject()
            puntosResult.documents.forEach { doc ->
                val data = doc.data
                if (data != null) puntosJson.put(doc.id, JSONObject(data))
            }
            backupCompleto.put("Puntos_referencia", puntosJson)

            // 2. Exportar Rutas
            Conexion.db.collection("Rutas").get().addOnSuccessListener { rutasResult ->
                val rutasJson = JSONObject()
                rutasResult.documents.forEach { doc ->
                    val data = doc.data
                    if (data != null) rutasJson.put(doc.id, JSONObject(data))
                }
                backupCompleto.put("Rutas", rutasJson)

                // 3. Exportar Camion
                Conexion.db.collection("camion").get().addOnSuccessListener { camionResult ->
                    val camionJson = JSONObject()
                    camionResult.documents.forEach { doc ->
                        val data = doc.data
                        if (data != null) camionJson.put(doc.id, JSONObject(data))
                    }
                    backupCompleto.put("camion", camionJson)

                    // FINAL: Imprimir en Logcat
                    Log.d(TAG, "--- INICIO BACKUP JSON ---")
                    Log.d(TAG, backupCompleto.toString(4))
                    Log.d(TAG, "--- FIN BACKUP JSON ---")
                    Mensajeria.exito("¡Datos exportados al Logcat con éxito!")
                }
            }
        }.addOnFailureListener {
            Mensajeria.error("Error al exportar: ${it.message}")
        }
    }
}
