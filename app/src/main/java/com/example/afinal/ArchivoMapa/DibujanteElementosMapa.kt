package com.example.afinal.ArchivoMapa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.afinal.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
     * Dibuja la línea de la ruta en el mapa con color cian/celeste.
     */
    fun trazarLineaRuta(mapa: GoogleMap, puntos: List<LatLng>) {
        if (puntos.isEmpty()) return
        
        mapa.addPolyline(
            PolylineOptions()
                .addAll(puntos)
                .color(Color.parseColor("#00BCD4")) // Color Celeste/Cian profesional
                .width(12f) // Grosor ideal para visibilidad
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    /**
     * Coloca o actualiza el marcador del camión recolector en el mapa.
     */
    fun dibujarMarcadorCamion(mapa: GoogleMap, contexto: Context, posicion: LatLng) {
        val imagenBase = BitmapFactory.decodeResource(contexto.resources, R.drawable.camion)
        
        // Redimensionamos el icono para que se vea bien en el mapa
        val iconoRedimensionado = Bitmap.createScaledBitmap(imagenBase, 120, 80, false)
        
        mapa.addMarker(
            MarkerOptions()
                .position(posicion)
                .icon(BitmapDescriptorFactory.fromBitmap(iconoRedimensionado))
                .anchor(0.5f, 0.5f) // Centramos el icono en la coordenada
                .flat(true) // Hace que el camión rote junto con el mapa
        )
    }
}
