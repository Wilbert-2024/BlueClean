package com.example.afinal.ArchivoMapa

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Este objeto se encarga de inicializar la conexión especial necesaria
 * para que los servicios de Google Maps funcionen correctamente.
 * Utiliza una configuración secundaria de Firebase dedicada a mapas.
 */
object ConexionMapasBootstrap {
    private const val URL_BASE_DATOS = "https://ubicacion-a4f61-default-rtdb.firebaseio.com"
    private const val ID_APLICACION = "1:990833293006:android:6710876df704e6989d829f"
    private const val LLAVE_API = "AIzaSyDct9LJPAXfMdpHDH2fPl7fXnCNT4S2erQ"
    private const val ID_PROYECTO = "ubicacion-a4f61"

    fun asegurarInicializacion(contexto: Context) {
        val contextoApp = contexto.applicationContext
        // Verificamos si ya existe una instancia de Firebase con estos datos para no duplicar
        if (FirebaseApp.getApps(contextoApp).any { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
            return
        }

        val opciones = FirebaseOptions.Builder()
            .setApplicationId(ID_APLICACION)
            .setApiKey(LLAVE_API)
            .setDatabaseUrl(URL_BASE_DATOS)
            .setProjectId(ID_PROYECTO)
            .build()

        FirebaseApp.initializeApp(contextoApp, opciones)
    }
}
