package com.example.afinal.datos.guardarDatosTelefono

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object datosEnMemoria {
    private const val nombreArchivo = "datosGuardados"

    data class DatosUsuario(
        val NomUsuario: String,
        val Barrio: String,
        val LugarReferencia: String,
        val RutaId: String,
        val PuntosRutaJson: String = "[]",
        val DetallesRutaJson: String = "{}",
        val FeriadosJson: String = "[]"
    )

    suspend fun guardaDatos(
        context: Context, 
        NombreUser: String, 
        barrio: String,
        lugar: String,
        rutaId: String,
        puntosJson: String,
        detallesJson: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val guardar = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit()
                // Nota: No usamos clear() aquí para no borrar los feriados si ya existen
                guardar.putString("NomUser", NombreUser)
                guardar.putString("Barrio", barrio)
                guardar.putString("Lugar", lugar)
                guardar.putString("RutaId", rutaId)
                guardar.putString("PuntosRuta", puntosJson)
                guardar.putString("DetallesRuta", detallesJson)
                guardar.apply()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun guardarFeriadosLocal(context: Context, feriadosJson: String) {
        val guardar = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit()
        guardar.putString("Feriados", feriadosJson)
        guardar.apply()
    }

    // --- NOTIFICACIONES: Guardar IDs de mensajes leídos ---
    fun marcarComoVisto(context: Context, idNotificacion: String) {
        val prefs = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)
        val vistos = prefs.getStringSet("NotificacionesVistas", mutableSetOf()) ?: mutableSetOf()
        val nuevaLista = vistos.toMutableSet()
        nuevaLista.add(idNotificacion)
        prefs.edit().putStringSet("NotificacionesVistas", nuevaLista).apply()
    }

    fun obtenerVistos(context: Context): Set<String> {
        return context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)
            .getStringSet("NotificacionesVistas", emptySet()) ?: emptySet()
    }

    fun existe(context: Context): Boolean {
        return context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).contains("NomUser")
    }

    fun obtener(context: Context): DatosUsuario? {
        val prefs = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)
        val NomUSER = prefs.getString("NomUser", null)
        val BarriosSelec = prefs.getString("Barrio", null)
        val LugarSelec = prefs.getString("Lugar", "") ?: ""
        val RutaIdSelec = prefs.getString("RutaId", "") ?: ""
        val Puntos = prefs.getString("PuntosRuta", "[]") ?: "[]"
        val Detalles = prefs.getString("DetallesRuta", "{}") ?: "{}"
        val Feriados = prefs.getString("Feriados", "[]") ?: "[]"

        return if (NomUSER != null && BarriosSelec != null) {
            DatosUsuario(NomUSER, BarriosSelec, LugarSelec, RutaIdSelec, Puntos, Detalles, Feriados)
        } else null
    }

    fun eliminar(context: Context) {
        context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
