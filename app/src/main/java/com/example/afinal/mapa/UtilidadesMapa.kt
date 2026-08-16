package com.example.afinal.mapa

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.afinal.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2

object UtilidadesMapa {

    private fun radianesAGrados(radianes: Double): Double {
        return radianes * 180.0 / Math.PI
    }

    fun redimensionarIcono(
        resourceId: Int,
        ancho: Int,
        alto: Int,
        activity: Activity
    ): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(activity.resources, resourceId)
        val imagenEscalada = Bitmap.createScaledBitmap(imageBitmap, ancho, alto, false)
        return BitmapDescriptorFactory.fromBitmap(imagenEscalada)
    }

    fun calcularRotacion(ubicacionAntigua: LatLng, ubicacionNueva: LatLng): Float {
        val deltaLat = ubicacionNueva.latitude - ubicacionAntigua.latitude
        val deltaLng = ubicacionNueva.longitude - ubicacionAntigua.longitude
        val anguloRad = atan2(deltaLng, deltaLat)
        val anguloDeg = radianesAGrados(anguloRad)
        return anguloDeg.toFloat()
    }
}
