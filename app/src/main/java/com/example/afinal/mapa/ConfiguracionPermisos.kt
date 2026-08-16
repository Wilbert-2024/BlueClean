package com.example.afinal.mapa

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class ConfiguracionPermisos(private val activity: Activity) {

    fun verificarPermisosYGPS(
        lanzadorPermiso: ActivityResultLauncher<String>,
        onSuccess: () -> Unit
    ) {
        if (tienePermisoUbicacion()) {
            if (verificarGPSActivo()) {
                onSuccess()
            } else {
                mostrarMensaje("Por favor active el GPS")
                abrirConfiguracionGPS()
            }
        } else {
            lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun tienePermisoUbicacion(): Boolean {
        val permiso = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        return permiso == PermissionChecker.PERMISSION_GRANTED
    }

    fun verificarGPSActivo(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun abrirConfiguracionGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivity(intent)
    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun verificarConexionInternet(): Boolean {
        val manejoConexion = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val redes = manejoConexion.activeNetwork ?: return false
        val actNetwork = manejoConexion.getNetworkCapabilities(redes) ?: return false

        return actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
