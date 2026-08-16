package com.example.afinal.mapa

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper

class VentEmergentesAlert {

    fun siNoHayRutas(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Sin Rutas")
            .setMessage("Por los momentos no hay rutas disponibles. Deseas intentar nuevamente?")
            .setCancelable(false)
            .setPositiveButton("Refrescar") { _, _ ->
                if (context is Activity) context.recreate()
            }
            .setNegativeButton("Salir") { _, _ ->
                if (context is Activity) context.finish()
            }
            .show()
    }

    fun noHayConexion(context: Context, titulo: String, mensaje: String) {
        AlertDialog.Builder(context)
            .setTitle(titulo)
            .setMessage("$mensaje. Deseas intentar nuevamente?")
            .setCancelable(false)
            .setPositiveButton("Actualizar") { _, _ ->
                if (context is Activity) context.recreate()
            }
            .setNegativeButton("Salir") { _, _ ->
                if (context is Activity) context.finish()
            }
            .show()
    }

    fun rutaNoExiste(context: Context, nombreRuta: String) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Ruta no encontrada")
            .setMessage("\"$nombreRuta\"")
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }
}
