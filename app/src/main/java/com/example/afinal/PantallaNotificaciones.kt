package com.example.afinal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.afinal.DB.modal.Notificacion_Modal
import com.example.afinal.DB.vistaModal.Notificacion_vistaModal
import com.example.afinal.datos.Colores
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaNotificaciones(vm: Notificacion_vistaModal = remember { Notificacion_vistaModal() }) {
    val context = LocalContext.current
    var mensajeSeleccionado by remember { mutableStateOf<Notificacion_Modal.Datos?>(null) }
    var filtroSeleccionado by remember { mutableStateOf("TODAS") }

    LaunchedEffect(Unit) { vm.cargarNotificaciones(context) }

    val listaFiltrada = when (filtroSeleccionado) {
        "EMERGENCIA" -> vm.listaNotificaciones.filter { it.Tipo.uppercase() == "EMERGENCIA" }
        "ALERTA" -> vm.listaNotificaciones.filter { it.Tipo.uppercase() == "ALERTA" }
        "NOVEDAD" -> vm.listaNotificaciones.filter { it.Tipo.uppercase() == "NOVEDAD" }
        else -> vm.listaNotificaciones
    }

    val hoy = LocalDate.now()
    val ayer = hoy.minusDays(1)
    val agrupadas = listaFiltrada.groupBy { aviso ->
        val fecha = aviso.Fecha_Hora?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.MIN
        when (fecha) {
            hoy -> "Hoy"
            ayer -> "Ayer"
            else -> fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "NI")))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB))) {
        // Título Estilo Moderno
        Text(text = "Centro de Avisos", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp))

        // Fila de Filtros (Chips con iconos)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChipIcon("Todas", Icons.Default.GridView, filtroSeleccionado == "TODAS") { filtroSeleccionado = "TODAS" }
            FilterChipIcon("Alertas", Icons.Default.NotificationsActive, filtroSeleccionado == "ALERTA") { filtroSeleccionado = "ALERTA" }
            FilterChipIcon("Novedades", Icons.AutoMirrored.Filled.VolumeUp, filtroSeleccionado == "NOVEDAD") { filtroSeleccionado = "NOVEDAD" }
            FilterChipIcon("Emergencias", Icons.Default.ErrorOutline, filtroSeleccionado == "EMERGENCIA") { filtroSeleccionado = "EMERGENCIA" }
        }

        if (vm.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF004527)) }
        } else if (listaFiltrada.isEmpty()) {
            CajaVacia()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                agrupadas.forEach { (fecha, avisos) ->
                    item { Text(text = fecha, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(vertical = 16.dp)) }
                    items(avisos) { aviso ->
                        val esVisto = vm.idsVistos.contains(aviso.id)
                        TarjetaAvisoDiseñoNuevo(aviso, esVisto) {
                            mensajeSeleccionado = aviso
                            if (!esVisto) vm.marcarVisto(context, aviso.id)
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    mensajeSeleccionado?.let { PopUpDetalleModerno(it) { mensajeSeleccionado = null } }
}

@Composable
fun FilterChipIcon(texto: String, icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    val colorBase = if (seleccionado) Color(0xFF004527) else Color.White
    val contenido = if (seleccionado) Color.White else Color.Black
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = colorBase,
        border = if (!seleccionado) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
        shadowElevation = if (seleccionado) 4.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = if(seleccionado) Color.White else obtenerColorPorTexto(texto), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, color = contenido, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TarjetaAvisoDiseñoNuevo(aviso: Notificacion_Modal.Datos, esVisto: Boolean, onClick: () -> Unit) {
    val config = obtenerConfiguracionTipo(aviso.Tipo)
    val horaStr = SimpleDateFormat("hh:mm a", Locale("es", "NI")).format(aviso.Fecha_Hora?.toDate() ?: java.util.Date())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icono en circulo grande y suave
            Box(modifier = Modifier.size(56.dp).background(config.color.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(config.icono, null, tint = config.color, modifier = Modifier.size(26.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = aviso.Titulo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1E), modifier = Modifier.weight(1f))
                    Text(text = horaStr, fontSize = 11.sp, color = Color.Gray)
                }
                
                Surface(color = config.color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = aviso.Tipo.lowercase().replaceFirstChar { it.uppercase() }, color = config.color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                Text(text = aviso.Mensaje, fontSize = 13.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                if (!esVisto) Box(modifier = Modifier.size(10.dp).background(config.color, CircleShape))
                Spacer(Modifier.height(16.dp))
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PopUpDetalleModerno(aviso: Notificacion_Modal.Datos, onCerrar: () -> Unit) {
    val config = obtenerConfiguracionTipo(aviso.Tipo)
    val fechaCompleta = SimpleDateFormat("dd 'de' MMMM 'de' yyyy  •  hh:mm a", Locale("es", "NI")).format(aviso.Fecha_Hora?.toDate() ?: java.util.Date())

    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(), contentAlignment = Alignment.TopCenter) {
            Card(
                modifier = Modifier.padding(top = 45.dp).fillMaxWidth().shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(28.dp).padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(color = config.color.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                        Text(text = aviso.Tipo, color = config.color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
                    }

                    Text(text = aviso.Titulo, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
                    Text(text = fechaCompleta, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    
                    Box(modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                    
                    Text(text = aviso.Mensaje, fontSize = 16.sp, color = Color(0xFF444444), lineHeight = 26.sp, textAlign = TextAlign.Center)
                    
                    Text(text = "Agradecemos su comprensión y colaboración.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 32.dp))
                    Text(text = "— Alcaldía de Ciudad Limpia", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 8.dp))

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onCerrar,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004527))
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            // Icono Circular que sobresale
            Surface(modifier = Modifier.size(90.dp), shape = CircleShape, color = config.color, border = BorderStroke(6.dp, Color.White), shadowElevation = 8.dp) {
                Box(contentAlignment = Alignment.Center) { Icon(config.icono, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
            }
        }
    }
}

@Composable
fun CajaVacia() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(80.dp), tint = Color(0xFFEEEEEE))
            Spacer(Modifier.height(16.dp))
            Text("No hay mensajes aquí", color = Color.LightGray, fontWeight = FontWeight.Bold)
        }
    }
}

private fun obtenerColorPorTexto(t: String) = when(t) {
    "Alertas" -> Color(0xFFFFA000)
    "Novedades" -> Color(0xFF2E7D32)
    "Emergencias" -> Color(0xFFD32F2F)
    else -> Color.Black
}

data class TipoConfig(val icono: ImageVector, val color: Color)
private fun obtenerConfiguracionTipo(tipo: String) = when (tipo.uppercase()) {
    "EMERGENCIA" -> TipoConfig(Icons.Default.ErrorOutline, Color(0xFFD32F2F))
    "ALERTA" -> TipoConfig(Icons.Default.NotificationsActive, Color(0xFFFFA000))
    "NOVEDAD" -> TipoConfig(Icons.AutoMirrored.Filled.VolumeUp, Color(0xFF2E7D32))
    else -> TipoConfig(Icons.Default.Notifications, Color(0xFF004527))
}
