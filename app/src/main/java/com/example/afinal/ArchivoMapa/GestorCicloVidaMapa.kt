package com.example.afinal.ArchivoMapa

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.MapView

/**
 * Función encargada de gestionar la "vida" del mapa en la pantalla.
 * Asegura que el mapa se pause, se reanude o se destruya cuando el usuario
 * sale de la aplicación o cambia de pantalla, ahorrando batería y memoria.
 */
@Composable
fun recordarVistaMapaConCicloVida(): MapView {
    val contexto = LocalContext.current
    val cicloVidaOwner = LocalLifecycleOwner.current
    
    // Creamos la vista del mapa una sola vez y la recordamos
    val vistaMapa = remember { 
        MapView(contexto).apply { 
            onCreate(Bundle()) 
        } 
    }

    // Escuchamos los cambios del teléfono (pausa, cierre, etc.)
    DisposableEffect(cicloVidaOwner.lifecycle, vistaMapa) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> vistaMapa.onStart()
                Lifecycle.Event.ON_RESUME -> vistaMapa.onResume()
                Lifecycle.Event.ON_PAUSE -> vistaMapa.onPause()
                Lifecycle.Event.ON_STOP -> vistaMapa.onStop()
                else -> Unit
            }
        }
        
        cicloVidaOwner.lifecycle.addObserver(observador)

        onDispose {
            cicloVidaOwner.lifecycle.removeObserver(observador)
            vistaMapa.onDestroy()
        }
    }
    
    return vistaMapa
}
