package com.example.afinal.ArchivoMapa

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

/**
 * Clase encargada de realizar cálculos matemáticos de distancia y tiempo
 * para estimar la llegada del camión a la posición del usuario.
 */
object CalculadorTiempo {

    // Velocidad promedio de un camión recolector en zona urbana (aprox 12 km/h con paradas)
    // 12 km/h ≈ 200 metros por minuto
    private const val METROS_POR_MINUTO = 200

    /**
     * Calcula los minutos que le faltan al camión para llegar al punto de la ruta
     * más cercano al usuario.
     */
    fun estimarMinutosLlegada(
        posCamion: LatLng,
        posUsuario: LatLng,
        puntosRuta: List<LatLng>
    ): Int {
        if (puntosRuta.isEmpty()) return 0

        // 1. Encontrar el índice de la ruta donde está el camión
        val indiceCamion = buscarIndiceMasCercano(posCamion, puntosRuta)
        
        // 2. Encontrar el índice de la ruta donde está el usuario (punto de encuentro)
        val indiceUsuario = buscarIndiceMasCercano(posUsuario, puntosRuta)

        // 3. Si el camión ya pasó al usuario, devolvemos 0 o un valor que indique "Ya pasó"
        if (indiceCamion >= indiceUsuario) return 1 // Está muy cerca o ya pasó

        // 4. Calcular distancia acumulada por la calle (no en línea recta)
        var distanciaTotalMetros = 0f
        val resultado = FloatArray(1)
        
        for (i in indiceCamion until indiceUsuario) {
            val puntoA = puntosRuta[i]
            val puntoB = puntosRuta[i + 1]
            Location.distanceBetween(puntoA.latitude, puntoA.longitude, puntoB.latitude, puntoB.longitude, resultado)
            distanciaTotalMetros += resultado[0]
        }

        // 5. Convertir distancia a minutos
        val minutos = (distanciaTotalMetros / METROS_POR_MINUTO).roundToInt()
        
        return if (minutos < 1) 1 else minutos
    }

    private fun buscarIndiceMasCercano(posicion: LatLng, ruta: List<LatLng>): Int {
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
