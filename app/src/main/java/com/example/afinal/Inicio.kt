package com.example.afinal

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.afinal.componentes.IndicadorRuta
import com.example.afinal.componentes.VisualizadorRuta
import com.example.afinal.datos.Colores
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.DB.vistaModal.Inicio_vistaModal
import com.example.afinal.DB.repositorio.ExportadorDatos

val VerdeOscuroGrad = Color(0xFF00381F)
val VerdeLive = Color(0xFF4ADE80)
val GrisSutil = Color(0xFF8E8E93)
val NegroElegante = Color(0xFF1C1C1E)

@Composable
fun Inicio(onNavegarADenuncia: () -> Unit) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val altoCabecera = (config.screenHeightDp * 0.25).dp
    
    val vm = remember { Inicio_vistaModal() }
    var estaCargando by remember { mutableStateOf(true) }
    var mostrarRuta by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.cargarDatos(context)
        estaCargando = false
    }

    // --- FLUJO TIEMPO REAL: Los datos de vm.estadoServicio ahora se actualizan solos gracias al VistaModal ---

    // Usamos la lista real de puntos que viene de la memoria
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
                    Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(0.15f)).border(1.dp, Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Box(modifier = Modifier.size(8.dp).align(Alignment.TopEnd).padding(2.dp).clip(CircleShape).background(VerdeLive))
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
                        
                        // Indicador de ruta con datos reales de origen y destino
                        IndicadorRuta(
                            origen = vm.origenDestino.first, 
                            destino = vm.origenDestino.second, 
                            progreso = 0.5f, 
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
                                Text("Próxima llegada", fontSize = 12.sp, color = GrisSutil)
                                Text(vm.horarioRuta, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NegroElegante)
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
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                
                // BOTÓN DE DEBUG TEMPORAL
                Button(
                    onClick = { ExportadorDatos.exportarTodoALogcat() },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DEBUG: EXPORTAR BASE DE DATOS", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
