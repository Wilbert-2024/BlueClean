package com.example.afinal.ArchivoMapa

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

/**
 * Clase encargada de realizar cálculos matemáticos de distancia, tiempo y porcentaje de progreso
 * para estimar la llegada del camión y su avance en la ruta.
 */
object CalculadorTiempo {

    private const val TAG = "CalculadorTiempo"
    
    // Velocidad promedio de resguardo para tránsito urbano (15 km/h por defecto si el camión se detiene)
    private var ultimaVelocidadValida = 15.0 

    /**
     * Calcula los minutos que le faltan al camión para llegar a la ubicación del usuario,
     * utilizando la velocidad actual de Firebase/Firestore o la última velocidad válida registrada.
     */
    fun estimarMinutosLlegada(
        posCamion: LatLng,
        posUsuario: LatLng,
        puntosRuta: List<LatLng>,
        velocidadActualKmH: Double = 0.0
    ): Int {
        if (puntosRuta.isEmpty()) return -2

        // 1. Si la velocidad actual de Firestore es mayor a 1.0 km/h, la guardamos como la "última válida"
        if (velocidadActualKmH > 1.0) {
            ultimaVelocidadValida = velocidadActualKmH
            Log.d(TAG, "Nueva velocidad válida registrada: $ultimaVelocidadValida km/h")
        } else {
            if (ultimaVelocidadValida < 5.0) ultimaVelocidadValida = 15.0
            Log.d(TAG, "Camión detenido ($velocidadActualKmH km/h). Usando velocidad promedio: $ultimaVelocidadValida km/h")
        }

        // 2. Encontrar el índice de la ruta donde está el camión y el usuario
        val indiceCamion = buscarIndiceMasCercano("CAMIÓN", posCamion, puntosRuta)
        val indiceUsuario = buscarIndiceMasCercano("USUARIO", posUsuario, puntosRuta)

        // 3. Si el camión está en el mismo punto
        if (indiceCamion == indiceUsuario) {
            return 0
        }

        // 4. Si el camión ya avanzó más allá del usuario
        if (indiceCamion > indiceUsuario) {
            return -1 
        }

        // 5. Calcular distancia acumulada por las calles
        var distanciaTotalMetros = 0f
        val resultado = FloatArray(1)
        
        for (i in indiceCamion until indiceUsuario) {
            val puntoA = puntosRuta[i]
            val puntoB = puntosRuta[i + 1]
            Location.distanceBetween(puntoA.latitude, puntoA.longitude, puntoB.latitude, puntoB.longitude, resultado)
            distanciaTotalMetros += resultado[0]
        }

        if (distanciaTotalMetros < 100) {
            return 0
        }

        // 6. Convertir km/h a Metros Por Minuto
        val metrosPorMinuto = (ultimaVelocidadValida * 1000.0) / 60.0
        val minutos = (distanciaTotalMetros / metrosPorMinuto).roundToInt()
        val resultadoFinal = if (minutos < 1) 1 else minutos

        Log.d(TAG, "Distancia: ${distanciaTotalMetros.toInt()}m | Vel Usada: $ultimaVelocidadValida km/h | Tiempo: $resultadoFinal min")
        
        return resultadoFinal
    }

    /**
     * Calcula el porcentaje de avance del camión en la ruta (valor de 0.0f a 1.0f).
     */
    fun calcularPorcentajeProgreso(posCamion: LatLng, puntosRuta: List<LatLng>): Float {
        if (puntosRuta.isEmpty()) return 0.0f
        
        val indiceCamion = buscarIndiceMasCercano("PROGRESO", posCamion, puntosRuta)
        val maxIndice = (puntosRuta.size - 1).coerceAtLeast(1)
        
        val porcentaje = (indiceCamion.toFloat() / maxIndice.toFloat()).coerceIn(0.0f, 1.0f)
        Log.d(TAG, "Progreso de la ruta: ${(porcentaje * 100).toInt()}% (Índice $indiceCamion de $maxIndice)")
        
        return porcentaje
    }

    /**
     * Busca el índice del punto de la ruta más cercano a una posición dada.
     */
    fun buscarIndiceMasCercano(etiqueta: String = "BUSQUEDA", posicion: LatLng, ruta: List<LatLng>): Int {
        if (ruta.isEmpty()) return 0
        var indiceMinimo = 0
        var distanciaMinima = Float.MAX_VALUE
        val resultado = FloatArray(1)

        ruta.forEachIndexed { index, punto ->
            Location.distanceBetween(posicion.latitude, posicion.longitude, punto.latitude, punto.longitude, resultado)
            if (resultado[0] < distanciaMinima) {
                distanciaMinima = resultado[0]
                indiceMinimo = index
            }
        }

        return indiceMinimo
    }
}
