package com.example.afinal

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.afinal.ArchivoMapa.CalculadorTiempo
import com.example.afinal.ArchivoMapa.ConexionMapasBootstrap
import com.example.afinal.ArchivoMapa.DibujanteElementosMapa
import com.example.afinal.ArchivoMapa.GestorPermisosMapa
import com.example.afinal.ArchivoMapa.recordarVistaMapaConCicloVida
import com.example.afinal.DB.vistaModal.Mapa_vistaModal
import com.example.afinal.ui.theme.FinalTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

@Composable
fun Mapa(onAtras: () -> Unit) {
    val contexto = LocalContext.current
    val cicloVidaOwner = LocalLifecycleOwner.current
    val vm = remember { Mapa_vistaModal() }
    val vistaMapa = recordarVistaMapaConCicloVida()
    
    var camaraInicializada by remember { mutableStateOf(false) }
    var circuloPulso by remember { mutableStateOf<Circle?>(null) }
    var puntoCentro by remember { mutableStateOf<Circle?>(null) }
    var expandido by remember { mutableStateOf(false) }
    
    // --- ESTADOS DE CONTROL DE GPS ---
    var permisoConcedido by remember { mutableStateOf(GestorPermisosMapa.tienePermisoUbicacion(contexto)) }
    var gpsActivo by remember { mutableStateOf(GestorPermisosMapa.verificarGpsActivo(contexto)) }
    var mostrarDialogoGps by remember { mutableStateOf(!permisoConcedido || !gpsActivo) }

    val transicionInfinita = rememberInfiniteTransition(label = "pulso")
    val escalaPulso by transicionInfinita.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "escala"
    )

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoConcedido = concedido
        gpsActivo = GestorPermisosMapa.verificarGpsActivo(contexto)
        mostrarDialogoGps = !concedido || !gpsActivo
        if (concedido && gpsActivo) {
            vm.iniciarGpsUsuario(contexto)
        }
    }

    // --- REACCIÓN AL VOLVER A LA PANTALLA ---
    DisposableEffect(cicloVidaOwner.lifecycle) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = GestorPermisosMapa.tienePermisoUbicacion(contexto)
                gpsActivo = GestorPermisosMapa.verificarGpsActivo(contexto)
                mostrarDialogoGps = !permisoConcedido || !gpsActivo
                if (permisoConcedido && gpsActivo) {
                    vm.iniciarGpsUsuario(contexto)
                }
            }
        }
        cicloVidaOwner.lifecycle.addObserver(observador)
        onDispose { cicloVidaOwner.lifecycle.removeObserver(observador) }
    }

    // --- RECEPTOR NATIVO DE EVENTOS DEL SISTEMA (INSTANTÁNEO AL APAGAR GPS) ---
    DisposableEffect(contexto) {
        val receptorGps = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    val tienePermiso = GestorPermisosMapa.tienePermisoUbicacion(contexto)
                    val tieneGps = GestorPermisosMapa.verificarGpsActivo(contexto)
                    permisoConcedido = tienePermiso
                    gpsActivo = tieneGps
                    mostrarDialogoGps = !tienePermiso || !tieneGps
                    if (tienePermiso && tieneGps) {
                        vm.iniciarGpsUsuario(contexto)
                    }
                }
            }
        }
        val filtro = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        contexto.registerReceiver(receptorGps, filtro)

        onDispose {
            try {
                contexto.unregisterReceiver(receptorGps)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        ConexionMapasBootstrap.asegurarInicializacion(contexto)
        vm.cargarDatos(contexto)
    }

    // --- REACCIÓN INSTANTÁNEA A LA ACTIVACIÓN / DESACTIVACIÓN DEL GPS ---
    LaunchedEffect(permisoConcedido, gpsActivo) {
        circuloPulso?.remove()
        puntoCentro?.remove()
        circuloPulso = null
        puntoCentro = null
        
        if (!permisoConcedido || !gpsActivo) {
            vm.detenerGpsUsuario()
        } else {
            camaraInicializada = false // Forzamos el re-encuadre completo como al entrar desde la barra
            vm.iniciarGpsUsuario(contexto)
        }
    }

    // DIBUJO ESTABLE (Línea de avance y Camión)
    LaunchedEffect(vm.rutaTrazada, vm.ubicacionCamion, gpsActivo, permisoConcedido) {
        vistaMapa.getMapAsync { googleMap ->
            googleMap.clear()
            circuloPulso = null
            puntoCentro = null
            
            val puntosTotales = vm.rutaTrazada
            val posCamion = vm.ubicacionCamion

            if (puntosTotales.isNotEmpty()) {
                if (posCamion != null) {
                    val indiceCercano = CalculadorTiempo.buscarIndiceMasCercano("CAMIÓN", posCamion, puntosTotales)
                    
                    val partePasada = puntosTotales.subList(0, indiceCercano + 1)
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, partePasada, "#4CAF50")
                    
                    val parteFutura = puntosTotales.subList(indiceCercano, puntosTotales.size)
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, parteFutura, "#00BCD4")
                } else {
                    DibujanteElementosMapa.trazarLineaRuta(googleMap, puntosTotales, "#00BCD4")
                }
            }

            if (permisoConcedido && gpsActivo) {
                DibujanteElementosMapa.mostrarMiUbicacionReal(googleMap)
            }
            
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
    LaunchedEffect(escalaPulso, vm.ubicacionUsuario, gpsActivo, permisoConcedido) {
        if (permisoConcedido && gpsActivo) {
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
        } else {
            circuloPulso?.remove()
            puntoCentro?.remove()
            circuloPulso = null
            puntoCentro = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        AndroidView(factory = { vistaMapa }, modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier .clip(RoundedCornerShape(50)).shadow(4.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tu Barrio: ", fontSize = 13.sp, color = Color.Gray)
                    Text(vm.barrioUsuario, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Surface(
                modifier = Modifier.size(44.dp).clip(CircleShape).shadow(4.dp) .clickable { },
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFF004527), modifier = Modifier.size(22.dp))
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (!vm.estaCargando) {
                Card(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        .fillMaxWidth() .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clickable { expandido = !expandido },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFF2E7D32))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text( text = "PRÓXIMA PARADA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp )
                                
                                val proximaParadaTexto = vm.proximasParadas.firstOrNull() ?: vm.barrioUsuario
                                Text( text = proximaParadaTexto, fontSize = 14.sp,fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E) )
                            }

                            IconButton(
                                onClick = { expandido = !expandido },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ver paradas",
                                    tint = Color.Gray
                                )
                            }
                        }

                        if (expandido && vm.proximasParadas.isNotEmpty()) {
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(text = "Recorrido de paradas:",fontSize = 11.sp, fontWeight = FontWeight.Bold,color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp) )
                                
                                vm.proximasParadas.forEachIndexed { index, parada ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (index == 0) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (index == 0) Color(0xFF2E7D32) else Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = parada,fontSize = 13.sp,
                                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (index == 0) Color.Black else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- CUADRO DE DIÁLOGO OBLIGATORIO DE BLOQUEO DE GPS ---
        if (mostrarDialogoGps) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text( text = "Activar ubicación",  fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,color = Color(0xFF004527)
                    )
                },
                text = {
                    Text( text = "Para acceder al mapa en tiempo real es necesario activar la ubicación GPS de tu teléfono.",
                        fontSize = 14.sp, color = Color(0xFF333333), lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!permisoConcedido) {
                                lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else if (!gpsActivo) {
                                GestorPermisosMapa.abrirAjustesGps(contexto)
                            }
                        },
                        colors = ButtonDefaults.buttonColors( containerColor = Color(0xFF004527), contentColor = Color.White ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text( text = "Activar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoGps = false
                            onAtras()
                        }
                    ) {
                        Text(  text = "Cancelar", color = Color(0xFF666666), fontWeight = FontWeight.Bold,  fontSize = 14.sp )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewMapa() = FinalTheme { Mapa(onAtras = {}) }
