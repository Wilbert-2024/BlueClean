package com.example.afinal.ArchivoMapa

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.view.animation.LinearInterpolator
import com.example.afinal.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.atan2

/**
 * Este objeto contiene las "herramientas de dibujo" para el mapa.
 * Se encarga de trazar las líneas de la ruta y posicionar/animar el icono del camión.
 */
object DibujanteElementosMapa {

    private var animadorActual: ValueAnimator? = null

    /**
     * Dibuja la línea de la ruta en el mapa con un color específico.
     */
    fun trazarLineaRuta(mapa: GoogleMap, puntos: List<LatLng>, colorHex: String = "#00BCD4"): Polyline? {
        if (puntos.isEmpty()) return null
        return mapa.addPolyline(
            PolylineOptions().addAll(puntos).color(Color.parseColor(colorHex)).width(12f).startCap(RoundCap()).endCap(RoundCap())
        )
    }

    /**
     * Obtiene o crea el marcador del camión recolector en el mapa sin borrar los existentes.
     */
    fun obtenerOCrearMarcadorCamion(mapa: GoogleMap, contexto: Context, posicion: LatLng, marcadorExistente: Marker?): Marker {
        if (marcadorExistente != null) {
            marcadorExistente.position = posicion
            return marcadorExistente
        }
        val imagenBase = BitmapFactory.decodeResource(contexto.resources, R.drawable.camion)
        val iconoRedimensionado = Bitmap.createScaledBitmap(imagenBase, 120, 80, false)
        return mapa.addMarker(
            MarkerOptions().position(posicion).icon(BitmapDescriptorFactory.fromBitmap(iconoRedimensionado)).anchor(0.5f, 0.5f).flat(true)
        )!!
    }

    /**
     * Anima el movimiento y rotación del camión de forma fluida a lo largo de los puntos de la ruta.
     */
    fun animarCamionSobreRuta(marcador: Marker, puntosSubRuta: List<LatLng>, duracionMs: Long = 4500L, alAvanzar: ((LatLng) -> Unit)? = null) {
        animadorActual?.cancel()
        if (puntosSubRuta.isEmpty()) return

        if (puntosSubRuta.size == 1) {
            marcador.position = puntosSubRuta[0]
            alAvanzar?.invoke(puntosSubRuta[0])
            return
        }

        // Calcular distancias acumuladas por tramo
        val distancias = FloatArray(puntosSubRuta.size - 1)
        var distanciaTotal = 0f
        val res = FloatArray(1)

        for (i in 0 until puntosSubRuta.size - 1) {
            val pA = puntosSubRuta[i]
            val pB = puntosSubRuta[i + 1]
            Location.distanceBetween(pA.latitude, pA.longitude, pB.latitude, pB.longitude, res)
            distancias[i] = res[0]
            distanciaTotal += res[0]
        }

        if (distanciaTotal <= 0f) {
            marcador.position = puntosSubRuta.last()
            alAvanzar?.invoke(puntosSubRuta.last())
            return
        }

        animadorActual = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = duracionMs
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val fraccion = anim.animatedValue as Float
                val distanciaObjetivo = fraccion * distanciaTotal

                var distanciaAcumulada = 0f
                for (i in distancias.indices) {
                    val distTramo = distancias[i]
                    if (distanciaAcumulada + distTramo >= distanciaObjetivo || i == distancias.lastIndex) {
                        val pA = puntosSubRuta[i]
                        val pB = puntosSubRuta[i + 1]
                        val avanceEnTramo = (distanciaObjetivo - distanciaAcumulada).coerceAtLeast(0f)
                        val tSegmento = if (distTramo > 0) (avanceEnTramo / distTramo).coerceIn(0f, 1f) else 1f

                        val lat = pA.latitude + (pB.latitude - pA.latitude) * tSegmento
                        val lng = pA.longitude + (pB.longitude - pA.longitude) * tSegmento
                        val posActual = LatLng(lat, lng)

                        marcador.position = posActual
                        marcador.rotation = calcularRotacion(pA, pB)
                        alAvanzar?.invoke(posActual)
                        break
                    }
                    distanciaAcumulada += distTramo
                }
            }
            start()
        }
    }

    /**
     * Calcula el ángulo de rotación entre dos coordenadas para orientar el vehículo hacia la calle.
     */
    fun calcularRotacion(origen: LatLng, destino: LatLng): Float {
        val deltaLat = destino.latitude - origen.latitude
        val deltaLng = destino.longitude - origen.longitude
        val anguloRad = atan2(deltaLng, deltaLat)
        return (anguloRad * 180.0 / Math.PI).toFloat()
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
            CircleOptions().center(posicion).radius(0.0).strokeWidth(0f).fillColor(colorInicial)
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
            CircleOptions().center(posicion).radius(12.0).fillColor(Color.parseColor(colorHex)).strokeColor(Color.WHITE).strokeWidth(6f).zIndex(5.0f)
        )
    }
}
