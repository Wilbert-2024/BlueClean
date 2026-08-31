package com.example.afinal.DB.vistaModal

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.DB.modal.PuntoReferencia_Modal
import com.example.afinal.DB.repositorio.PuntoReferencia_Repositorio
import com.example.afinal.DB.repositorio.Ruta_repositorio
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.datos.mensajeria.Mensajeria
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ConfiguracionInicial_vistaModal : ViewModel() {
    var nombreUsuario by mutableStateOf("")
    var barrioSeleccionado by mutableStateOf("")
    var lugarSeleccionado by mutableStateOf("")
    var rutaIdSeleccionada by mutableStateOf("")
    
    var listaLugares by mutableStateOf<List<PuntoReferencia_Modal.Lugar>>(emptyList())
    var estaCargando by mutableStateOf(false)
    var mostrarSelectorLugares by mutableStateOf(false)

    fun cargarDatosExistentes(context: Context) {
        val datos = datosEnMemoria.obtener(context)
        if (datos != null) {
            nombreUsuario = datos.NomUsuario
            barrioSeleccionado = datos.Barrio
            lugarSeleccionado = datos.LugarReferencia
            rutaIdSeleccionada = datos.RutaId
        }
    }

    fun seleccionarBarrio(barrio: String) {
        barrioSeleccionado = barrio
        estaCargando = true
        PuntoReferencia_Repositorio.obtenerLugaresPorBarrio(barrio) { lugares ->
            listaLugares = lugares
            estaCargando = false
            if (lugares.isNotEmpty()) {
                mostrarSelectorLugares = true
            } else {
                Mensajeria.error("No hay puntos registrados en este barrio")
            }
        }
    }

    fun seleccionarLugar(nombre: String) {
        lugarSeleccionado = nombre
        rutaIdSeleccionada = listaLugares.find { it.Nombre == nombre }?.ruta_id ?: ""
        mostrarSelectorLugares = false
    }

    fun finalizarConfiguracion(context: Context, onExito: () -> Unit) {
        if (nombreUsuario.isBlank() || barrioSeleccionado.isBlank() || lugarSeleccionado.isBlank()) {
            Mensajeria.error("Completa tu nombre, barrio y lugar de referencia")
            return
        }

        estaCargando = true
        viewModelScope.launch {
            // Paso 1: Obtener todos los lugares de la ruta
            PuntoReferencia_Repositorio.obtenerTodosLosLugaresDeUnaRuta(rutaIdSeleccionada) { todosLosPuntos ->
                // Paso 2: Obtener detalles del documento de la colección Rutas
                Ruta_repositorio.obtenerPorId(rutaIdSeleccionada) { detallesRuta ->
                    if (detallesRuta != null) {
                        viewModelScope.launch {
                            // Paso 3: Serializar a JSON
                            val puntosJson = JSONArray().apply {
                                todosLosPuntos.forEach { p ->
                                    put(JSONObject().apply {
                                        put("Nombre", p.Nombre)
                                        put("ruta_id", p.ruta_id)
                                        put("lat", p.Coordenadas.latitude)
                                        put("lng", p.Coordenadas.longitude)
                                    })
                                }
                            }.toString()

                            val detallesJson = JSONObject().apply {
                                put("Nombre", detallesRuta.Nombre)
                                put("Estado", detallesRuta.Estado)
                                put("Barrios", JSONArray(detallesRuta.Barrios))
                                put("Dias", JSONArray(detallesRuta.Dias))
                                put("Horario", JSONObject(detallesRuta.Horario))
                                put("Recorrido", JSONObject(detallesRuta.Recorrido))
                            }.toString()

                            // Paso 4: Guardar en memoria persistente
                            val exito = datosEnMemoria.guardaDatos(
                                context, nombreUsuario, barrioSeleccionado,
                                lugarSeleccionado, rutaIdSeleccionada, puntosJson, detallesJson
                            )

                            estaCargando = false
                            if (exito) onExito()
                            else Mensajeria.error("Error al guardar la configuración")
                        }
                    } else {
                        estaCargando = false
                        Mensajeria.error("No se pudo obtener la información de la ruta")
                    }
                }
            }
        }
    }
}
