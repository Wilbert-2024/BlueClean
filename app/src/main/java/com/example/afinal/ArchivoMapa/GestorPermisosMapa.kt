package com.example.afinal.ArchivoMapa

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Este objeto se encarga de gestionar los permisos de ubicación y el estado del GPS.
 * Permite que el usuario vea su posición real en el mapa de forma segura.
 */
object GestorPermisosMapa {

    /**
     * Verifica si el usuario ya concedió el permiso de ubicación precisa.
     */
    fun tienePermisoUbicacion(contexto: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si el GPS (Ubicación) está encendido en los ajustes del teléfono.
     */
    fun verificarGpsActivo(contexto: Context): Boolean {
        val administradorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return administradorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Abre la pantalla de ajustes de ubicación del sistema Android.
     */
    fun abrirAjustesGps(contexto: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }
}
