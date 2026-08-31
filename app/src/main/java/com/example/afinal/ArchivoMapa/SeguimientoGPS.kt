package com.example.afinal.ArchivoMapa

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

/**
 * Clase encargada de obtener la ubicación GPS del usuario en tiempo real.
 */
class SeguimientoGPS(contexto: Context) {
    private val clienteUbicacion = LocationServices.getFusedLocationProviderClient(contexto)
    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun iniciar(alActualizar: (LatLng) -> Unit) {
        val peticion = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                resultado.lastLocation?.let {
                    alActualizar(LatLng(it.latitude, it.longitude))
                }
            }
        }

        clienteUbicacion.requestLocationUpdates(peticion, callback!!, Looper.getMainLooper())
    }

    fun detener() {
        callback?.let { clienteUbicacion.removeLocationUpdates(it) }
    }
}
