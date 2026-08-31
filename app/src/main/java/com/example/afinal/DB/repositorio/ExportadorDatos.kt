package com.example.afinal.DB.repositorio

import android.util.Log
import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.datos.mensajeria.Mensajeria
import org.json.JSONObject

object ExportadorDatos {
    private const val TAG = "DEBUG_DB_EXPORT"

    fun exportarTodoALogcat() {
        Mensajeria.advertencia("Iniciando extracción TOTAL (7 colecciones)...")
        val backupCompleto = JSONObject()
        // Lista completa basada en tu captura de pantalla de la consola Firebase
        val colecciones = listOf(
            "Camiones",
            "Denuncia",
            "Historial_Incidencia",
            "Notificaciones",
            "Punto_trasar_ruta",
            "Puntos_referencia",
            "Rutas"
        )
        var procesadas = 0

        colecciones.forEach { nombreColeccion ->
            Conexion.db.collection(nombreColeccion).get().addOnSuccessListener { result ->
                val coleccionJson = JSONObject()
                result.documents.forEach { doc ->
                    val data = doc.data
                    if (data != null) {
                        coleccionJson.put(doc.id, procesarMapa(data))
                    }
                }
                backupCompleto.put(nombreColeccion, coleccionJson)
                verificarProgreso(++procesadas, colecciones.size, backupCompleto)
            }.addOnFailureListener {
                Log.e(TAG, "Error en coleccion $nombreColeccion: ${it.message}")
                verificarProgreso(++procesadas, colecciones.size, backupCompleto)
            }
        }
    }

    private fun verificarProgreso(actual: Int, total: Int, json: JSONObject) {
        if (actual == total) {
            finalizarExportacion(json)
        }
    }

    private fun procesarMapa(mapa: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        mapa.forEach { (llave, valor) ->
            when (valor) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    json.put(llave, procesarMapa(valor as Map<String, Any?>))
                }
                is List<*> -> {
                    val array = org.json.JSONArray()
                    valor.forEach { item ->
                        when(item) {
                            is com.google.firebase.firestore.GeoPoint -> {
                                val geo = JSONObject().apply { 
                                    put("lat", item.latitude)
                                    put("lng", item.longitude)
                                }
                                array.put(geo)
                            }
                            is Map<*, *> -> {
                                @Suppress("UNCHECKED_CAST")
                                array.put(procesarMapa(item as Map<String, Any?>))
                            }
                            else -> array.put(item)
                        }
                    }
                    json.put(llave, array)
                }
                is com.google.firebase.firestore.GeoPoint -> {
                    val geo = JSONObject()
                    geo.put("lat", valor.latitude)
                    geo.put("lng", valor.longitude)
                    json.put(llave, geo)
                }
                is com.google.firebase.Timestamp -> json.put(llave, valor.toDate().toString())
                else -> json.put(llave, valor)
            }
        }
        return json
    }

    private fun finalizarExportacion(json: JSONObject) {
        Log.d(TAG, "--- INICIO BACKUP TOTAL JSON (7 COLECCIONES) ---")
        val fullRes = json.toString(4)
        val maxLogSize = 3000
        for (i in 0..fullRes.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > fullRes.length) fullRes.length else end
            Log.d(TAG, fullRes.substring(start, end))
        }
        Log.d(TAG, "--- FIN BACKUP TOTAL JSON ---")
        Mensajeria.exito("¡Base de datos completa (7/7) exportada con éxito!")
    }
}
