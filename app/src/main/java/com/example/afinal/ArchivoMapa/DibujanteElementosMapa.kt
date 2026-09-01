package com.example.afinal.ArchivoMapa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.afinal.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap

/**
 * Este objeto contiene las "herramientas de dibujo" para el mapa.
 * Se encarga de trazar las líneas de la ruta y posicionar el icono del camión.
 */
object DibujanteElementosMapa {

    /**
     * Dibuja la línea de la ruta en el mapa con un color específico.
     */
    fun trazarLineaRuta(mapa: GoogleMap, puntos: List<LatLng>, colorHex: String = "#00BCD4") {
        if (puntos.isEmpty()) return
        
        mapa.addPolyline(
            PolylineOptions()
                .addAll(puntos)
                .color(Color.parseColor(colorHex))
                .width(12f)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * Coloca o actualiza el marcador del camión recolector en el mapa.
     */
    fun dibujarMarcadorCamion(mapa: GoogleMap, contexto: Context, posicion: LatLng) {
        val imagenBase = BitmapFactory.decodeResource(contexto.resources, R.drawable.camion)
        val iconoRedimensionado = Bitmap.createScaledBitmap(imagenBase, 120, 80, false)
        
        mapa.addMarker(
            MarkerOptions()
                .position(posicion)
                .icon(BitmapDescriptorFactory.fromBitmap(iconoRedimensionado))
                .anchor(0.5f, 0.5f)
                .flat(true)
        )
    }

    /**
     * Activa la capa de "Mi ubicación" de Google Maps.
     */
    @SuppressLint("MissingPermission")
    fun mostrarMiUbicacionReal(mapa: GoogleMap) {
        mapa.isMyLocationEnabled = true
        mapa.uiSettings.isMyLocationButtonEnabled = false
    }

    /**
     * Crea un efecto de pulso circular inicial.
     */
    fun crearPulsoInicial(mapa: GoogleMap, posicion: LatLng, colorHex: String): Circle {
        val colorInt = Color.parseColor(colorHex)
        val colorInicial = Color.argb(100, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        
        return mapa.addCircle(
            CircleOptions()
                .center(posicion)
                .radius(0.0)
                .strokeWidth(0f)
                .fillColor(colorInicial)
        )
    }

    /**
     * Actualiza un pulso existente de forma fluida sin borrarlo.
     */
    fun actualizarPulso(circulo: Circle, posicion: LatLng, escala: Float, colorHex: String) {
        val colorInt = Color.parseColor(colorHex)
        val alpha = (100 * (1 - escala)).toInt()
        val nuevoColor = Color.argb(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        
        circulo.center = posicion
        circulo.radius = (escala * 200).toDouble()
        circulo.fillColor = nuevoColor
    }

    /**
     * Crea un punto sólido para marcar la ubicación exacta del usuario.
     */
    fun crearPuntoCentro(mapa: GoogleMap, posicion: LatLng, colorHex: String): Circle {
        return mapa.addCircle(
            CircleOptions()
                .center(posicion)
                .radius(12.0)
                .fillColor(Color.parseColor(colorHex))
                .strokeColor(Color.WHITE)
                .strokeWidth(6f)
                .zIndex(5.0f)
        )
    }
}
