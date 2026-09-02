package com.example.afinal.ArchivoMapa

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.core.content.ContextCompat

object GestorPermisosMapa {

    /**pide permiso al usuario de acceder al la opcion del gps del telefono   */
    fun tienePermisoUbicacion(contexto: Context): Boolean {
        return ContextCompat.checkSelfPermission(  contexto,  Manifest.permission.ACCESS_FINE_LOCATION  ) == PackageManager.PERMISSION_GRANTED
    }

    /** Verifica si el GPS del telefono está encendido    */
    fun verificarGpsActivo(contexto: Context): Boolean {
        val administradorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return administradorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /** Abre la pantalla de ajustes para activaer la ubicación del GPS en el teléfono. */
    fun abrirAjustesGps(contexto: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }


    fun verificarConexionInternet(contexto: Context): Boolean {
        val manejoConexion = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val redes = manejoConexion.activeNetwork ?: return false
        val actNetwork = manejoConexion.getNetworkCapabilities(redes) ?: return false

        return actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
