package com.example.afinal.DB.vistaModal

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.afinal.DB.modal.Notificacion_Modal
import com.example.afinal.DB.repositorio.Notificacion_Repositorio
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria

class Notificacion_vistaModal : ViewModel() {
    var listaNotificaciones by mutableStateOf<List<Notificacion_Modal.Datos>>(emptyList())
    var estaCargando by mutableStateOf(true)
    var idsVistos by mutableStateOf<Set<String>>(emptySet())

    fun cargarNotificaciones(context: Context) {
        val datos = datosEnMemoria.obtener(context)
        val rutaId = datos?.RutaId ?: ""
        idsVistos = datosEnMemoria.obtenerVistos(context)

        if (rutaId.isNotEmpty()) {
            Notificacion_Repositorio.obtenerPorRuta(rutaId) { lista ->
                listaNotificaciones = lista
                estaCargando = false
            }
        } else {
            estaCargando = false
        }
    }

    fun marcarVisto(context: Context, id: String) {
        datosEnMemoria.marcarComoVisto(context, id)
        idsVistos = idsVistos + id
    }
}
