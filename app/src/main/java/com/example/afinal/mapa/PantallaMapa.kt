package com.example.afinal.mapa

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.afinal.datos.Colores
import com.google.android.gms.maps.MapView

@Composable
fun PantallaMapa() {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    if (activity == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se pudo iniciar el mapa")
        }
        return
    }

    val permisos = remember(activity) { ConfiguracionPermisos(activity) }
    val baseDato = remember {
        FirebaseBootstrap.ensureInitialized(context)
        ConsultaBaseDato()
    }
    val mensajes = remember { VentEmergentesAlert() }
    val manejadorMapa = remember(activity) { ManejadorMapa(activity) }
    val mapView = rememberMapViewWithLifecycle()
    val handler = remember { Handler(Looper.getMainLooper()) }
    val tiempoInicialPorRuta = remember { mutableMapOf<String, Long?>() }
    val estadoRutas = remember { mutableStateMapOf<String, Boolean>() }

    var rutaEnPantalla by remember { mutableStateOf<String?>(null) }
    var mapaExpandido by remember { mutableStateOf(false) }
    var mapaListo by remember { mutableStateOf(false) }
    var permisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoUbicacion = concedido
        if (!concedido) {
            permisos.mostrarMensaje("Permiso de ubicacion denegado")
        }
    }

    LaunchedEffect(Unit) {
        if (!permisoUbicacion) {
            lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (!permisos.verificarGPSActivo()) {
            permisos.mostrarMensaje("Por favor active el GPS")
            permisos.abrirConfiguracionGPS()
        }
    }

    DisposableEffect(mapView, permisoUbicacion) {
        mapView.getMapAsync { googleMap ->
            manejadorMapa.setGoogleMap(googleMap)
            mapaListo = true
            asignarRutaDisponibleUnaVez(
                context = context,
                baseDato = baseDato,
                manejadorMapa = manejadorMapa,
                mensajes = mensajes
            )
        }

        onDispose {
            manejadorMapa.detener()
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(contexto: Context?, intent: Intent?) {
                val hayInternet = permisos.verificarConexionInternet()
                val gpsActivo = permisos.verificarGPSActivo()

                contexto?.let {
                    if (!hayInternet && !gpsActivo) {
                        mensajes.noHayConexion(it, "Problemas GPS e Internet", "Verifique la conexion a internet y la ubicacion GPS")
                    } else if (!hayInternet) {
                        mensajes.noHayConexion(it, "Sin conexion", "No hay conexion a Internet")
                    } else if (!gpsActivo) {
                        mensajes.noHayConexion(it, "Ubicacion Inactiva", "Active la ubicacion GPS")
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    DisposableEffect(Unit) {
        val runnable = object : Runnable {
            override fun run() {
                baseDato.obtenerLasRutas(context) { listaDeRutas, error ->
                    if (error != null) {
                        Toast.makeText(context, "Error al obtener rutas: $error", Toast.LENGTH_SHORT).show()
                    } else {
                        listaDeRutas.forEach { ruta ->
                            baseDato.obtenerTiempoRuta(context, ruta) { tiempoNuevo ->
                                val tiempoInicial = tiempoInicialPorRuta[ruta]

                                if (tiempoInicial != null && tiempoNuevo != null) {
                                    if (tiempoInicial == tiempoNuevo) {
                                        estadoRutas[ruta] = false
                                        if (ruta == rutaEnPantalla) {
                                            mensajes.rutaNoExiste(context, "Esta ruta no esta disponible")
                                            rutaEnPantalla = null
                                        }
                                        manejadorMapa.ocultarRutaPorId(ruta)
                                        manejadorMapa.ocultarRutaYMarcadorPorId(ruta)
                                    } else {
                                        estadoRutas[ruta] = true
                                    }
                                }

                                tiempoInicialPorRuta[ruta] = tiempoNuevo
                            }
                        }
                    }
                    handler.postDelayed(this, 5000)
                }
            }
        }

        handler.post(runnable)

        onDispose {
            handler.removeCallbacks(runnable)
            tiempoInicialPorRuta.clear()
        }
    }

    val pesoMapa by animateFloatAsState(
        targetValue = if (mapaExpandido) 0.95f else 0.70f,
        label = "pesoMapa"
    )
    val pesoControles by animateFloatAsState(
        targetValue = if (mapaExpandido) 0.05f else 0.30f,
        label = "pesoControles"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(pesoMapa)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            if (!mapaListo) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Colores.VerdePrincipal
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(pesoControles)
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = { mapaExpandido = !mapaExpandido },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopCenter)
            ) {
                Icon(
                    imageVector = if (mapaExpandido) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (mapaExpandido) "Ver rutas" else "Expandir mapa",
                    tint = Colores.VerdePrincipal,
                    modifier = Modifier.size(34.dp)
                )
            }

            if (!mapaExpandido) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonRuta(texto = "Ruta1") {
                        val estado = estadoRutas["ruta1"]
                        if (estado != false) {
                            verificacionEstadoRuta(
                                context = context,
                                rutaAsignada = "ruta1",
                                opcionIcon = 1,
                                baseDato = baseDato,
                                manejadorMapa = manejadorMapa
                            )
                            rutaEnPantalla = "ruta1"
                        } else {
                            mensajes.rutaNoExiste(context, "Ruta uno No disponible")
                        }
                    }

                    BotonRuta(texto = "Ruta2") {
                        val estado = estadoRutas["ruta2"]
                        if (estado != false) {
                            verificacionEstadoRuta(
                                context = context,
                                rutaAsignada = "ruta2",
                                opcionIcon = 2,
                                baseDato = baseDato,
                                manejadorMapa = manejadorMapa
                            )
                            rutaEnPantalla = "ruta2"
                        } else {
                            mensajes.rutaNoExiste(context, "Ruta Dos No disponible")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonRuta(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White
        )
    ) {
        Text(
            text = texto,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun verificacionEstadoRuta(
    context: Context,
    rutaAsignada: String,
    opcionIcon: Int,
    baseDato: ConsultaBaseDato,
    manejadorMapa: ManejadorMapa
) {
    baseDato.obtenerEstadoActivo(
        context,
        rutaAsignada,
        "Activo",
        onResultado = { activo ->
            if (activo) {
                baseDato.obtenerEstadoActivo(
                    context,
                    rutaAsignada,
                    "Usando",
                    onResultado = { usando ->
                        if (usando) {
                            manejadorMapa.iniciarLogicaMapa(rutaAsignada, opcionIcon)
                        } else {
                            Toast.makeText(context, "Ruta no esta activa", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, "Error base de dato Usando: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(context, "Ruta no esta activa", Toast.LENGTH_SHORT).show()
            }
        },
        onError = { errorMsg ->
            Toast.makeText(context, "Error base de dato Activa: $errorMsg", Toast.LENGTH_LONG).show()
        }
    )
}

private fun asignarRutaDisponibleUnaVez(
    context: Context,
    baseDato: ConsultaBaseDato,
    manejadorMapa: ManejadorMapa,
    mensajes: VentEmergentesAlert
) {
    baseDato.obtenerTodasLasRutas(context) { listaDeRutas, _ ->
        baseDato.rutaDisponible(context) { rutaDisponible, _ ->
            if (rutaDisponible != null) {
                for (i in listaDeRutas.indices) {
                    if (listaDeRutas[i] == rutaDisponible) {
                        manejadorMapa.iniciarLogicaMapa(rutaDisponible, i + 1)
                    }
                }
            } else {
                mensajes.siNoHayRutas(context)
            }
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycle, mapView) {
        mapView.onStart()
        mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
