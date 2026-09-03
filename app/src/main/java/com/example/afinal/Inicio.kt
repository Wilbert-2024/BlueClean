package com.example.afinal

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.afinal.R
import com.example.afinal.ArchivoMapa.GestorPermisosMapa
import com.example.afinal.DB.repositorio.ExportadorDatos
import com.example.afinal.DB.vistaModal.Inicio_vistaModal
import com.example.afinal.componentes.IndicadorRuta
import com.example.afinal.componentes.VisualizadorRuta
import com.example.afinal.datos.Colores
import com.example.afinal.ui.theme.*

// --- COLORES LOCALES PARA LA INTERFAZ ---
private val VerdeOscuroGrad = Color(0xFF004527)
private val VerdeLive = Color(0xFF4CAF50)
private val GrisSutil = Color(0xFF757575)
private val NegroElegante = Color(0xFF212121)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(onNavegarADenuncia: () -> Unit = {}, onNavegarAAvisos: () -> Unit = {}, cantidadNoLeidos: Int = 0) {
    val context = LocalContext.current
    val cicloVidaOwner = LocalLifecycleOwner.current
    val config = LocalConfiguration.current
    val altoCabecera = (config.screenHeightDp * 0.25).dp

    val vm = remember { Inicio_vistaModal() }
    var estaCargando by remember { mutableStateOf(true) }
    var mostrarRuta by remember { mutableStateOf(false) }

    // --- ESTADOS DE CONTROL DE GPS Y CÁLCULO ---
    var permisoConcedido by remember { mutableStateOf(GestorPermisosMapa.tienePermisoUbicacion(context)) }
    var gpsActivo by remember { mutableStateOf(GestorPermisosMapa.verificarGpsActivo(context)) }
    val ubicaActiva = permisoConcedido && gpsActivo
    val estaCalculando = ubicaActiva && vm.estadoServicio && vm.calculandoTiempo
    var puntosCargando by remember { mutableIntStateOf(1) }

    LaunchedEffect(estaCalculando) {
        if (estaCalculando) {
            while (true) {
                kotlinx.coroutines.delay(400L)
                puntosCargando = (puntosCargando % 3) + 1
            }
        }
    }

    // --- ANIMACIÓN DE PARPADEO Y PULSO CONTINUO MIENTRAS EL GPS ESTÉ APAGADO ---
    val transicionParpadeo = rememberInfiniteTransition(label = "pulsoAvisoGps")
    val alfaAviso by transicionParpadeo.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alfaAviso"
    )
    val escalaAviso by transicionParpadeo.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaAviso"
    )

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoConcedido = concedido
        gpsActivo = GestorPermisosMapa.verificarGpsActivo(context)
        if (concedido && gpsActivo) {
            vm.iniciarGpsUsuario(context)
        } else {
            vm.detenerGpsUsuario()
        }
    }

    // Observar ciclo de vida para verificar GPS
    DisposableEffect(cicloVidaOwner.lifecycle) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = GestorPermisosMapa.tienePermisoUbicacion(context)
                gpsActivo = GestorPermisosMapa.verificarGpsActivo(context)
                if (permisoConcedido && gpsActivo) {
                    vm.iniciarGpsUsuario(context)
                } else {
                    vm.detenerGpsUsuario()
                }
            }
        }
        cicloVidaOwner.lifecycle.addObserver(observador)
        onDispose { cicloVidaOwner.lifecycle.removeObserver(observador) }
    }

    // Receptor nativo de cambios de GPS en tiempo real
    DisposableEffect(context) {
        val receptorGps = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    val tienePermiso = GestorPermisosMapa.tienePermisoUbicacion(context)
                    val tieneGps = GestorPermisosMapa.verificarGpsActivo(context)
                    permisoConcedido = tienePermiso
                    gpsActivo = tieneGps
                    if (tienePermiso && tieneGps) {
                        vm.iniciarGpsUsuario(context)
                    } else {
                        vm.detenerGpsUsuario()
                    }
                }
            }
        }
        val filtro = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(receptorGps, filtro)
        onDispose {
            try { context.unregisterReceiver(receptorGps) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        vm.cargarDatos(context)
        if (GestorPermisosMapa.tienePermisoUbicacion(context) && GestorPermisosMapa.verificarGpsActivo(context)) {
            vm.iniciarGpsUsuario(context)
        }
        estaCargando = false
    }

    VisualizadorRuta(
        mostrar = mostrarRuta,
        onDismiss = { mostrarRuta = false },
        puntosCargados = vm.puntosRutaParaVisualizador
    )

    if (estaCargando) {
        Box(modifier = Modifier.fillMaxSize().background(Colores.GrisFondo), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Colores.VerdePrincipal, strokeWidth = 3.dp)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Colores.GrisFondo).verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(altoCabecera).clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(Brush.verticalGradient(listOf(VerdeOscuroGrad, Colores.VerdePrincipal)))
                )

                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("¡Hola, ${vm.nombreUsuario}!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
                            Text("Bienvenido de nuevo a ${vm.barrioUsuario}", fontSize = 13.sp, color = Color.White.copy(0.7f))
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
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)).border(1.dp, Color.White.copy(0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Notifications, "Avisos", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.1f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CAMIÓN ASIGNADO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrisSutil, letterSpacing = 1.sp)
                            Image(painter = painterResource(id = R.drawable.camion), null, Modifier.size(100.dp, 60.dp).padding(vertical = 4.dp), contentScale = ContentScale.Fit)
                            Text(vm.nombreUnidad, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NegroElegante)

                            IndicadorRuta(
                                origen = vm.origenDestino.first,
                                destino = vm.origenDestino.second,
                                progreso = vm.progresoRuta,
                                estaActivo = vm.estadoServicio
                            )

                            Spacer(Modifier.height(8.dp))
                            EstadoServicio(estaActivo = vm.estadoServicio)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Seguimiento en tiempo real", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NegroElegante, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.05f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)), Alignment.Center) {
                                    Icon(Icons.Default.Schedule, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Próxima llegada estimada", fontSize = 12.sp, color = GrisSutil)

                                    if (estaCalculando) {
                                        Text("Calculando" + ".".repeat(puntosCargando), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                    } else {
                                        val textoLlegada = if (!ubicaActiva) {
                                            vm.horarioRuta
                                        } else if (vm.estadoServicio) {
                                            when (vm.minutosRestantes) {
                                                -1 -> "Ya pasó por tu casa"
                                                0 -> "¡Llegando a tu casa!"
                                                else -> if (vm.minutosRestantes > 0) "En ${vm.minutosRestantes} min aprox." else vm.horarioRuta
                                            }
                                        } else {
                                            vm.horarioRuta
                                        }

                                        Text(textoLlegada, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NegroElegante)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.05f)).clickable { mostrarRuta = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(VerdeOscuroGrad, RoundedCornerShape(12.dp)), Alignment.Center) {
                                Icon(Icons.Default.Route, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Ver lugares de la ruta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NegroElegante)
                                Text("Mira los puntos que recorre el camión", fontSize = 11.sp, color = GrisSutil)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = GrisSutil.copy(0.5f), modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.05f)).clickable { onNavegarADenuncia() },
                        shape = RoundedCornerShape(24.dp),colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp)), Alignment.Center) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Reportar incidencia", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NegroElegante)
                                Text("Ayúdanos a mantener tu barrio limpio", fontSize = 11.sp, color = GrisSutil)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = GrisSutil.copy(0.5f), modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                  /*  Button(
                        onClick = { ExportadorDatos.exportarTodoALogcat() },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DEBUG: EXPORTAR BASE DE DATOS", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }*/
                }
            }

            // --- CÁPSULA / PÍLDORA FLOTANTE MINIMALISTA OSCURA FLOTANDO ABAJO (SOBRE LA BARRA DE MENÚ) ---
            AnimatedVisibility(
                visible = !ubicaActiva,
                enter = fadeIn(tween(600)) + slideInVertically(animationSpec = tween(600), initialOffsetY = { it }),
                exit = fadeOut(tween(600)) + slideOutVertically(animationSpec = tween(600), targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.shadow(8.dp, CircleShape).clip(CircleShape),
                    color = Color(0xFF1E293B) // Color oscuro noche Slate M3
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text( text = "Activa GPS para ver en tiempo real", color = Color.White, fontSize = 11.sp,fontWeight = FontWeight.Bold )
                        }

                        Spacer(Modifier.width(10.dp))

                        // ÚNICAMENTE EL BOTÓN "ACTIVAR" TIENE EL PARPADEO Y PULSO DE ATENCIÓN
                        Surface(
                            modifier = Modifier.scale(escalaAviso).alpha(alfaAviso).clip(CircleShape)
                                .clickable {
                                    if (!permisoConcedido) {
                                        lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    } else if (!gpsActivo) {
                                        GestorPermisosMapa.abrirAjustesGps(context)
                                    }
                                },
                            color = Color(0xFFF59E0B) // Botón Ámbar / Naranja de atención
                        ) {
                            Text(text = "Activar",color = Color.White,fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp) )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoServicio(estaActivo: Boolean) {
    val colorBase = if (estaActivo) Colores.VerdePrincipal else Color(0xFFD32F2F)
    val transicion = rememberInfiniteTransition()
    val escala by transicion.animateFloat(0.8f, 1.4f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))
    val alfa by transicion.animateFloat(1f, 0.3f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))

    Row(modifier = Modifier.clip(CircleShape).background(if (estaActivo) Colores.VerdeClaro else Color(0xFFFFEBEE)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(10.dp).scale(escala).alpha(alfa).clip(CircleShape).background(colorBase))
            Box(Modifier.size(6.dp).clip(CircleShape).background(colorBase))
        }
        Spacer(Modifier.width(6.dp))
        Text(if (estaActivo) "En servicio" else "Inactivo", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colorBase)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewInicio() = FinalTheme { Inicio({}) }
