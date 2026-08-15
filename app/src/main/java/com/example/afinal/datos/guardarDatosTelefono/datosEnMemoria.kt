package com.example.afinal.datos.guardarDatosTelefono

import android.content.Context


object datosEnMemoria {
    private const val nombreArchivo = "datosGuardados"

    data class DatosUsuario(
        val NomUsuario: String,
        val Barrio: String,
    )


    suspend fun guardaDatos(context: Context, NombreUser: String, barrio: String): Boolean {
        return try {

            context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE) .edit().clear()
                .putString("NomUser", NombreUser)
                .putString("Barrio", barrio)
                .apply()

            true

        } catch (e: Exception) {
            false
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

    // GUARDAR SOLO SI CAMBIA USUARIO
    suspend fun guardarSiCambio(context: Context, NombreUser: String, BarrioSel: String): Boolean {
        val prefs = context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)
        val BarriosSelec = prefs.getString("Barrio", null)

        if (BarriosSelec == BarrioSel) return false

        return guardaDatos(context, NombreUser,BarrioSel)
    }

    // ELIMINAR SESIÓN
    fun eliminar(context: Context) {
        context.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
