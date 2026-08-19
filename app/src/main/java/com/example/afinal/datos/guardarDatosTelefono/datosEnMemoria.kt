package com.example.afinal.datos.guardarDatosTelefono

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object datosEnMemoria {
    private const val nombreArchivo = "datosGuardados"

    data class DatosUsuario(
        val NomUsuario: String,
        val Barrio: String,
    )


    suspend fun guardaDatos(context: Context, NombreUser: String, barrio: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val guardar = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit()

                guardar.clear()

                guardar.putString("NomUser", NombreUser)
                guardar.putString("Barrio", barrio)

                val resultado = guardar.commit()
                
                resultado

            } catch (e: Exception) {
                false
            }
        }
    }

    // VER SI HAY SESIÓN
    fun existe(context: Context): Boolean {
        return context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).contains("NomUser")
    }

    // OBTENER DATOS GUARDADOS
    fun obtener(context: Context): DatosUsuario? {
        val prefs = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)
        val NomUSER = prefs.getString("NomUser", null)
        val BarriosSelec = prefs.getString("Barrio", null)

        return if (NomUSER != null && BarriosSelec != null) {
            DatosUsuario(NomUSER, BarriosSelec)
        } else null
    }


    // ELIMINAR SESIÓN
    fun eliminar(context: Context) {
        context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
