package com.example.afinal

import android.Manifest
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.afinal.ArchivoMapa.ConexionMapasBootstrap
import com.example.afinal.ArchivoMapa.DibujanteElementosMapa
import com.example.afinal.ArchivoMapa.GestorPermisosMapa
import com.example.afinal.ArchivoMapa.recordarVistaMapaConCicloVida
import com.example.afinal.DB.vistaModal.Mapa_vistaModal
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

@Composable
fun Mapa(onAtras: () -> Unit) {
    val contexto = LocalContext.current
    val vm = remember { Mapa_vistaModal() }
    val vistaMapa = recordarVistaMapaConCicloVida()
    
    var camaraInicializada by remember { mutableStateOf(false) }
    var circuloPulso by remember { mutableStateOf<Circle?>(null) }
    var puntoCentro by remember { mutableStateOf<Circle?>(null) }
    
    val transicionInfinita = rememberInfiniteTransition(label = "pulso")
    val escalaPulso by transicionInfinita.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "escala"
    )

    var permisoConcedido by remember { mutableStateOf(GestorPermisosMapa.tienePermisoUbicacion(contexto)) }
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoConcedido = concedido
        if (concedido) vm.iniciarGpsUsuario(contexto)
    }

    LaunchedEffect(Unit) {
        ConexionMapasBootstrap.asegurarInicializacion(contexto)
        vm.cargarDatos(contexto)
        if (!permisoConcedido) lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else vm.iniciarGpsUsuario(contexto)
    }

    // DIBUJO ESTABLE (Línea de avance y Camión)
    LaunchedEffect(vm.rutaTrazada, vm.ubicacionCamion) {
        vistaMapa.getMapAsync { googleMap ->
            googleMap.clear()
            circuloPulso = null
            puntoCentro = null
            
            val puntosTotales = vm.rutaTrazada
            val posCamion = vm.ubicacionCamion

            if (puntosTotales.isNotEmpty()) {
                if (posCamion != null) {
                    // Calculamos el punto más cercano para dividir la ruta
                    val indiceCercano = buscarPuntoMasCercano(posCamion, puntosTotales)
                    
                    // Parte 1: Recorrido (Verde)
                    val partePasada = puntosTotales.subList(0, indiceCercano + 1)
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, partePasada, "#4CAF50") // Verde
                    
                    // Parte 2: Por recorrer (Celeste)
                    val parteFutura = puntosTotales.subList(indiceCercano, puntosTotales.size)
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, parteFutura, "#00BCD4") // Celeste
                } else {
                    // Si no hay camión, toda la ruta es celeste
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, puntosTotales, "#00BCD4")
                }
            }

            if (permisoConcedido) DibujanteElementosMapa.mostrarMiUbicacionReal(googleMap)
            
            posCamion?.let { pos ->
                if (pos.latitude != 0.0) DibujanteElementosMapa.dibujarMarcadorCamion(googleMap, contexto, pos)
            }

            if (!camaraInicializada) {
                val posicionEnfoque = vm.ubicacionUsuario ?: posCamion ?: LatLng(12.0131, -83.7635)
                if (puntosTotales.isNotEmpty()) {
                    try {
                        val limites = LatLngBounds.builder().apply { puntosTotales.forEach { include(it) } }.build()
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 100))
                        camaraInicializada = true
                    } catch (_: Exception) {}
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionEnfoque, 15f))
                    if (vm.ubicacionUsuario != null || posCamion != null) camaraInicializada = true
                }
            }
        }
    }

    // ANIMACIÓN Y PUNTO DE CENTRO (Usuario)
    LaunchedEffect(escalaPulso, vm.ubicacionUsuario) {
        vm.ubicacionUsuario?.let { pos ->
            if (pos.latitude != 0.0) {
                vistaMapa.getMapAsync { googleMap ->
                    if (circuloPulso == null) {
                        circuloPulso = DibujanteElementosMapa.crearPulsoInicial(googleMap, pos, "#2196F3")
                    } else {
                        DibujanteElementosMapa.actualizarPulso(circuloPulso!!, pos, escalaPulso, "#2196F3")
                    }

                    if (puntoCentro == null) {
                        puntoCentro = DibujanteElementosMapa.crearPuntoCentro(googleMap, pos, "#2196F3")
                    } else {
                        puntoCentro!!.center = pos
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AndroidView(factory = { vistaMapa }, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = Color.White.copy(alpha = 0.9f), shadowElevation = 4.dp) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAtras) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF004527)) }
                    Text("Mapa en tiempo real", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004527), textAlign = TextAlign.Center)
                    IconButton(onClick = { }) { Icon(Icons.Default.Notifications, null, tint = Color(0xFF004527)) }
                }
            }

            Surface(modifier = Modifier.padding(16.dp).clip(RoundedCornerShape(50)).shadow(4.dp), color = Color.White) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tu Barrio: ", fontSize = 14.sp, color = Color.Gray)
                    Text(vm.barrioUsuario, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!vm.estaCargando) {
                Card(modifier = Modifier.padding(24.dp).fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Próxima parada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Barrio ${vm.barrioUsuario}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        vm.proximasParadas.forEachIndexed { index, parada ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(imageVector = if (index == 0) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, tint = if (index == 0) Color(0xFF2E7D32) else Color.LightGray, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(parada, fontSize = 15.sp, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal, color = if (index == 0) Color.Black else Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 30.dp), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(onClick = { }, containerColor = Color(0xFF1A237E), contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Route, null)
            }
        }
    }
}

/**
 * Función matemática para encontrar qué punto de la ruta es el más cercano al camión.
 */
private fun buscarPuntoMasCercano(camion: LatLng, ruta: List<LatLng>): Int {
    var indiceMinimo = 0
    var distanciaMinima = Float.MAX_VALUE
    val resultado = FloatArray(1)

    ruta.forEachIndexed { index, punto ->
        Location.distanceBetween(camion.latitude, camion.longitude, punto.latitude, punto.longitude, resultado)
        if (resultado[0] < distanciaMinima) {
            distanciaMinima = resultado[0]
            indiceMinimo = index
        }
    }
    return indiceMinimo
}
