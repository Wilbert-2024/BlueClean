package com.example.afinal

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.widget.Toast
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
import com.google.android.gms.maps.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mapa(onAtras: () -> Unit, onNavegarAAvisos: () -> Unit = {}, cantidadNoLeidos: Int = 0) {
    val contexto = LocalContext.current
    val cicloVidaOwner = LocalLifecycleOwner.current
    val vm = remember { Mapa_vistaModal() }
    val vistaMapa = recordarVistaMapaConCicloVida()
    
    var camaraInicializada by remember { mutableStateOf(false) }
    var circuloPulso by remember { mutableStateOf<Circle?>(null) }
    var puntoCentro by remember { mutableStateOf<Circle?>(null) }
    var expandido by remember { mutableStateOf(false) }
    var mostrarLeyenda by remember { mutableStateOf(false) }

    // --- ELEMENTOS PERSISTENTES EN EL MAPA ---
    var marcadorCamion by remember { mutableStateOf<Marker?>(null) }
    var polilineaPasada by remember { mutableStateOf<Polyline?>(null) }
    var polilineaFutura by remember { mutableStateOf<Polyline?>(null) }
    var posCamionAnterior by remember { mutableStateOf<LatLng?>(null) }
    
    // --- ESTADOS DE CONTROL DE GPS ---
    var permisoConcedido by remember { mutableStateOf(GestorPermisosMapa.tienePermisoUbicacion(contexto)) }
    var gpsActivo by remember { mutableStateOf(GestorPermisosMapa.verificarGpsActivo(contexto)) }
    var mostrarDialogoGps by remember { mutableStateOf(!permisoConcedido || !gpsActivo) }

    // --- ESTADOS DE CONTROL DE INTERNET ---
    var hayInternet by remember { mutableStateOf(GestorPermisosMapa.verificarConexionInternet(contexto)) }
    var mostrarDialogoSinInternet by remember { mutableStateOf(!hayInternet) }

    // Evaluación para mantener la rueda de carga hasta que el mapa y el punto azul del usuario estén listos
    val faltaUbicacionUsuario = permisoConcedido && gpsActivo && (vm.ubicacionUsuario == null)
    val estaPreparandoMapa = vm.estaCargando || !camaraInicializada || faltaUbicacionUsuario

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

                val conectado = GestorPermisosMapa.verificarConexionInternet(contexto)
                hayInternet = conectado
                if (!conectado) {
                    mostrarDialogoSinInternet = true
                }
            }
        }
        cicloVidaOwner.lifecycle.addObserver(observador)
        onDispose { cicloVidaOwner.lifecycle.removeObserver(observador) }
    }

    // --- RECEPTOR NATIVO DE EVENTOS DEL SISTEMA (GPS E INTERNET) ---
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
                } else if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    val conectado = GestorPermisosMapa.verificarConexionInternet(contexto)
                    hayInternet = conectado
                    if (!conectado) {
                        mostrarDialogoSinInternet = true
                    }
                }
            }
        }
        val filtro = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
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
            camaraInicializada = false
            vm.ubicacionUsuario = null
            vm.iniciarGpsUsuario(contexto)
        }
    }

    // DIBUJO ESTABLE Y ANIMACIÓN FLUÍDA DEL CAMIÓN SOBRE LA RUTA
    LaunchedEffect(vm.rutaTrazada, vm.ubicacionCamion, gpsActivo, permisoConcedido) {
        vistaMapa.getMapAsync { googleMap ->
            val puntosTotales = vm.rutaTrazada
            val posCamion = vm.ubicacionCamion

            if (permisoConcedido && gpsActivo) {
                DibujanteElementosMapa.mostrarMiUbicacionReal(googleMap)
            }

            if (puntosTotales.isNotEmpty()) {
                if (posCamion != null && posCamion.latitude != 0.0) {
                    val indiceCercano = CalculadorTiempo.buscarIndiceMasCercano("CAMIÓN", posCamion, puntosTotales)
                    val partePasada = puntosTotales.subList(0, indiceCercano + 1)
                    val parteFutura = puntosTotales.subList(indiceCercano, puntosTotales.size)

                    if (polilineaPasada == null) {
                        polilineaPasada = DibujanteElementosMapa.trazarLineaRuta(googleMap, partePasada, "#4CAF50")
                    } else {
                        polilineaPasada!!.points = partePasada
                    }

                    if (polilineaFutura == null) {
                        polilineaFutura = DibujanteElementosMapa.trazarLineaRuta(googleMap, parteFutura, "#00BCD4")
                    } else {
                        polilineaFutura!!.points = parteFutura
                    }

                    if (marcadorCamion == null) {
                        marcadorCamion = DibujanteElementosMapa.obtenerOCrearMarcadorCamion(googleMap, contexto, posCamion, null)
                        posCamionAnterior = posCamion
                    } else if (posCamionAnterior != posCamion) {
                        val posAnt = posCamionAnterior ?: posCamion
                        val idxAnt = CalculadorTiempo.buscarIndiceMasCercano("ANT", posAnt, puntosTotales)
                        val idxNuevo = indiceCercano

                        val subRuta = if (idxAnt <= idxNuevo) {
                            puntosTotales.subList(idxAnt, (idxNuevo + 1).coerceAtMost(puntosTotales.size))
                        } else {
                            puntosTotales.subList(idxNuevo, (idxAnt + 1).coerceAtMost(puntosTotales.size)).reversed()
                        }

                        DibujanteElementosMapa.animarCamionSobreRuta(
                            marcador = marcadorCamion!!,
                            puntosSubRuta = subRuta,
                            duracionMs = 4500L,
                            alAvanzar = { posActual ->
                                val idxAct = CalculadorTiempo.buscarIndiceMasCercano("ACT", posActual, puntosTotales)
                                polilineaPasada?.points = puntosTotales.subList(0, (idxAct + 1).coerceAtMost(puntosTotales.size))
                                polilineaFutura?.points = puntosTotales.subList(idxAct, puntosTotales.size)
                            }
                        )
                        posCamionAnterior = posCamion
                    }
                } else {
                    if (polilineaFutura == null) {
                        polilineaFutura = DibujanteElementosMapa.trazarLineaRuta(googleMap, puntosTotales, "#00BCD4")
                    } else {
                        polilineaFutura!!.points = puntosTotales
                    }
                }
            }

            // ENCUADRE DINÁMICO INTELIGENTE (CAMIÓN + USUARIO)
            if (!camaraInicializada) {
                val uUser = vm.ubicacionUsuario
                val uCamion = posCamion

                if (uUser != null && uCamion != null && uUser.latitude != 0.0 && uCamion.latitude != 0.0) {
                    try {
                        val builder = LatLngBounds.builder()
                        builder.include(uUser)
                        builder.include(uCamion)

                        if (puntosTotales.isNotEmpty()) {
                            val idxCamion = CalculadorTiempo.buscarIndiceMasCercano("CAMION_FRAME", uCamion, puntosTotales)
                            val idxUser = CalculadorTiempo.buscarIndiceMasCercano("USER_FRAME", uUser, puntosTotales)
                            val inicio = minOf(idxCamion, idxUser)
                            val fin = maxOf(idxCamion, idxUser)
                            for (i in inicio..fin) {
                                builder.include(puntosTotales[i])
                            }
                        }

                        val limites = builder.build()
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 120))
                        camaraInicializada = true
                    } catch (_: Exception) {}
                } else if (uCamion != null && uCamion.latitude != 0.0) {
                    try {
                        if (puntosTotales.isNotEmpty()) {
                            val limites = LatLngBounds.builder().apply { puntosTotales.forEach { include(it) } }.build()
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(limites, 100))
                        } else {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uCamion, 15f))
                        }
                        camaraInicializada = true
                    } catch (_: Exception) {}
                } else if (uUser != null && uUser.latitude != 0.0) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uUser, 15f))
                    camaraInicializada = true
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
            IconButton(
                onClick = { mostrarLeyenda = true },
                modifier = Modifier.size(44.dp).clip(CircleShape).shadow(4.dp).background(Color.White)
            ) {
                Icon(Icons.Default.Info, "Leyenda", tint = Color(0xFF004527), modifier = Modifier.size(22.dp))
            }

            BadgedBox(
                badge = {
                    if (cantidadNoLeidos > 0) {
                        Badge(containerColor = Color(0xFFD32F2F), contentColor = Color.White) {
                            Text(text = if (cantidadNoLeidos > 99) "99+" else cantidadNoLeidos.toString())
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = onNavegarAAvisos,
                    modifier = Modifier.size(44.dp).clip(CircleShape).shadow(4.dp).background(Color.White)
                ) {
                    Icon(Icons.Default.Notifications, "Avisos", tint = Color(0xFF004527), modifier = Modifier.size(22.dp))
                }
            }
        }

        // --- LEYENDA COMPACTA EN FORMA DE PÍLDORA (IZQUIERDA) ---
        if (mostrarLeyenda) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable { mostrarLeyenda = false }
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = 64.dp, start = 16.dp)
                        .widthIn(max = 200.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(4.dp)
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Recorrido", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(4.dp)
                                    .background(Color(0xFF00BCD4), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sin recorrer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Tu ubicación", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Camión", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text( text = "PUNTO DE REFERENCIA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp )

                                    if (vm.minutosRestantes > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    text = "${vm.minutosRestantes} min",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }
                                        }
                                    }
                                }

                                val actual = vm.proximasParadas.getOrNull(0) ?: vm.barrioUsuario
                                val siguiente = vm.proximasParadas.getOrNull(1)

                                Text( text = actual, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E) )
                                if (siguiente != null) {
                                    Spacer(Modifier.height(1.dp))
                                    Text( text = "Siguiente: $siguiente", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray )
                                }
                            }

                            IconButton(
                                onClick = { expandido = !expandido },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ver puntos",
                                    tint = Color.Gray
                                )
                            }
                        }

                        if (expandido && vm.proximasParadas.isNotEmpty()) {
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(text = "Puntos de referencia:",fontSize = 11.sp, fontWeight = FontWeight.Bold,color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp) )
                                
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

        // --- PANTALLA / PANEL FLOTANTE DE CARGA MIENTRAS SE PREPARA EL MAPA Y EL PUNTERO AZUL ---
        if (estaPreparandoMapa && !mostrarDialogoGps && !mostrarDialogoSinInternet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF004527),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Preparando mapa...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }
        }

        // --- CUADRO DE DIÁLOGO OBLIGATORIO "SIN CONEXIÓN A INTERNET" ---
        if (mostrarDialogoSinInternet) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "Sin conexión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F)
                    )
                },
                text = {
                    Text(
                        text = "Para acceder al mapa en tiempo real es necesario contar con conexión a Internet.",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val conectado = GestorPermisosMapa.verificarConexionInternet(contexto)
                            hayInternet = conectado
                            if (conectado) {
                                // Limpieza y reinicio total de estados de cero
                                marcadorCamion?.remove()
                                marcadorCamion = null
                                polilineaPasada?.remove()
                                polilineaFutura?.remove()
                                polilineaPasada = null
                                polilineaFutura = null
                                posCamionAnterior = null
                                circuloPulso?.remove()
                                puntoCentro?.remove()
                                circuloPulso = null
                                puntoCentro = null

                                camaraInicializada = false
                                vm.estaCargando = true
                                vm.ubicacionUsuario = null
                                vm.ubicacionCamion = null
                                vm.rutaTrazada = emptyList()

                                vm.cargarDatos(contexto)
                                if (permisoConcedido && gpsActivo) {
                                    vm.iniciarGpsUsuario(contexto)
                                }
                                mostrarDialogoSinInternet = false
                            } else {
                                Toast.makeText(contexto, "Sigue sin conexión", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004527), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Actualizar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoSinInternet = false
                            onAtras()
                        }
                    ) {
                        Text(text = "Salir", color = Color(0xFF666666), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
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
