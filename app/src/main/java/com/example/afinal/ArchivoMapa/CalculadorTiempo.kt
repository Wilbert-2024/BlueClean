package com.example.afinal.ArchivoMapa

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

/**
 * Clase encargada de realizar cálculos matemáticos de distancia y tiempo
 * para estimar la llegada del camión a la posición del usuario.
 */
object CalculadorTiempo {

    private const val TAG = "CalculadorTiempo"
    
    // Velocidad mínima de resguardo (12 km/h por defecto si nunca ha avanzado)
    private var ultimaVelocidadValida = 12.0 

    /**
     * Calcula los minutos que le faltan al camión para llegar a la ubicación del usuario,
     * utilizando la velocidad actual de Firebase o la última velocidad válida registrada.
     */
    fun estimarMinutosLlegada(
        posCamion: LatLng,
        posUsuario: LatLng,
        puntosRuta: List<LatLng>,
        velocidadActualKmH: Double = 0.0
    ): Int {
        if (puntosRuta.isEmpty()) return -2

        // 1. Si la velocidad actual de Firebase es mayor a 0.5 km/h, la guardamos como la "última válida"
        if (velocidadActualKmH > 0.5) {
            ultimaVelocidadValida = velocidadActualKmH
            Log.d(TAG, "Nueva velocidad válida registrada: $ultimaVelocidadValida km/h")
        } else {
            Log.d(TAG, "Camión detenido ($velocidadActualKmH km/h). Usando última velocidad: $ultimaVelocidadValida km/h")
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
        // Ejemplo: 12 km/h = 12000 metros / 60 min = 200 metros/minuto
        val metrosPorMinuto = (ultimaVelocidadValida * 1000.0) / 60.0
        val minutos = (distanciaTotalMetros / metrosPorMinuto).roundToInt()
        val resultadoFinal = if (minutos < 1) 1 else minutos

        Log.d(TAG, "Distancia: ${distanciaTotalMetros.toInt()}m | Vel Usada: $ultimaVelocidadValida km/h | Tiempo: $resultadoFinal min")
        
        return resultadoFinal
    }

    private fun buscarIndiceMasCercano(etiqueta: String, posicion: LatLng, ruta: List<LatLng>): Int {
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
