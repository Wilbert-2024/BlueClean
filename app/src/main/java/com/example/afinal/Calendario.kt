package com.example.afinal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.DB.vistaModal.Calendario_vistaModal
import com.example.afinal.datos.Calendario.DiaRecorrido
import com.example.afinal.datos.Calendario.FechaHora
import com.example.afinal.datos.Calendario.GeneradorFechasCalendario
import com.example.afinal.datos.Colores
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idiomaEspanol = Locale.forLanguageTag("es-NI")
private val formatoMes = DateTimeFormatter.ofPattern("MMM", idiomaEspanol)

private fun obtenerDiaCorto(diaSemana: DayOfWeek): String {
    return when (diaSemana) {
        DayOfWeek.MONDAY -> "LUN"; DayOfWeek.TUESDAY -> "MAR"; DayOfWeek.WEDNESDAY -> "MIÉ"
        DayOfWeek.THURSDAY -> "JUE"; DayOfWeek.FRIDAY -> "VIE"; DayOfWeek.SATURDAY -> "SÁB"
        DayOfWeek.SUNDAY -> "DOM"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendario(
    onNotificaciones: () -> Unit = {},
    onRecordar: (DiaRecorrido) -> Unit = {},
    onVerAvisos: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm = remember { Calendario_vistaModal() }
    var mostrarAlarma by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) { vm.cargarDatos(context) }

    if (vm.estaCargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Colores.VerdePrincipal) }
    } else {
        val horaRuta = try { LocalTime.parse(vm.horaInicio) } catch (_: Exception) { LocalTime.of(6, 0) }
        val horaFinRuta = try { LocalTime.parse(vm.horaFin) } catch (_: Exception) { LocalTime.of(12, 0) }
        val fechas = GeneradorFechasCalendario.generar(vm.diasRuta, horaRuta, 3, vm.fechasFeriadas)

        Box(modifier = Modifier.fillMaxSize().background(Colores.VerdeBosque)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                EncabezadoCalendario(horaInicio = horaRuta, horaFin = horaFinRuta, onNotificaciones = onNotificaciones)
                
                Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().background(color = Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).padding(bottom = 100.dp)) {
                        Spacer(modifier = Modifier.height(35.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            TituloSeccion(icono = Icons.Default.DateRange, titulo = "Próximas recolecciones")
                            Spacer(modifier = Modifier.height(16.dp))
                            fechas.forEachIndexed { index, fecha ->
                                TarjetaRecoleccionCompacta(recorrido = fecha, onRecordar = { mostrarAlarma = true; onRecordar(fecha) })
                                if (index < fechas.lastIndex) { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            TarjetaInformacion(onVerAvisos = onVerAvisos)
                        }
                    }
                }
            }
            if (mostrarAlarma) {
                ModalBottomSheet(onDismissRequest = { mostrarAlarma = false }, sheetState = sheetState, containerColor = Color.Transparent, dragHandle = null) {
                    DialogoConfigurarRecordatorio(onDismiss = { mostrarAlarma = false }, onGuardar = { mostrarAlarma = false })
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCalendario(horaInicio: LocalTime, horaFin: LocalTime, onNotificaciones: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("H:mm")
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp).offset(y = (-8).dp)) {
            Text(text = "Calendario", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onNotificaciones, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(24.dp)) }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(modifier = Modifier.align(Alignment.CenterHorizontally).background(Color.White.copy(0.12f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 5.dp).offset(y = (-4).dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, tint = Colores.VerdeVibrante, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Horario: ", fontSize = 13.sp, color = Color.White.copy(0.8f))
            Text(text = "${horaInicio.format(fmt)} - ${horaFin.format(fmt)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun TarjetaBarrio(barrio: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50), color = Colores.VerdeBosque.copy(alpha = 0.85f), border = BorderStroke(1.dp, Colores.BlancoTarjeta.copy(alpha = 0.15f)), shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Colores.VerdeVibrante, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Mi barrio: ", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Colores.BlancoTarjeta.copy(alpha = 0.8f))
            Text(text = barrio, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.BlancoTarjeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TarjetaResumenRuta(horaInicio: LocalTime, horaFin: LocalTime) {
    val fmt = DateTimeFormatter.ofPattern("H:mm")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Colores.VerdeBosque, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Horario General: ", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Colores.VerdeBosque)
        Text(text = "${horaInicio.format(fmt)} - ${horaFin.format(fmt)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
    }
}

@Composable
private fun TituloSeccion(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icono, contentDescription = null, tint = Colores.VerdeSecundario, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
    }
}

@Composable
private fun TarjetaRecoleccionCompacta(recorrido: DiaRecorrido, onRecordar: () -> Unit = {}) {
    val hoy = FechaHora.obtenerFechaLocal()
    val yaPaso = recorrido.fecha.isBefore(hoy)
    val colorEstado = if (recorrido.esFeriado) Colores.RojoFeriado else if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeSecundario

    Card(
        modifier = Modifier.fillMaxWidth().alpha(if(yaPaso) 0.6f else 1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if(!yaPaso && recorrido.esFeriado) BorderStroke(1.dp, Colores.RojoFeriado.copy(0.3f)) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            // Franja lateral indicadora
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(colorEstado))
            
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CajaFecha(recorrido = recorrido)
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    if (recorrido.esFeriado && recorrido.nombreFeriado != null) {
                        Text(text = recorrido.nombreFeriado!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Colores.RojoFeriado, modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Text(text = if (recorrido.esFeriado) "Día festivo" else "Recolección Normal", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeBosque)
                    Spacer(modifier = Modifier.height(6.dp))
                    EstadoRecorrido(recorrido = recorrido)
                }
                
                IconButton(onClick = onRecordar, modifier = Modifier.size(40.dp).background(if(yaPaso) Color.Transparent else Colores.VerdeFondoSuave, CircleShape), enabled = !yaPaso) {
                    Icon(imageVector = if(yaPaso) Icons.Default.CheckCircle else Icons.Default.NotificationsActive, contentDescription = "Aviso", tint = if (yaPaso) Colores.VerdeSecundario.copy(0.5f) else Colores.VerdeSecundario)
                }
            }
        }
    }
}

@Composable
private fun CajaFecha(recorrido: DiaRecorrido) {
    val hoy = FechaHora.obtenerFechaLocal(); val yaPaso = recorrido.fecha.isBefore(hoy)
    val colorP = if (recorrido.esFeriado) Color.White else if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeBosque
    val colorF = if (recorrido.esFeriado) Colores.RojoFeriado else if (yaPaso) Colores.GrisSeleccion else Colores.VerdePastel
    val semana = obtenerDiaCorto(recorrido.fecha.dayOfWeek)
    val num = recorrido.fecha.dayOfMonth.toString()
    val mes = recorrido.fecha.format(formatoMes).uppercase()
    
    Box(modifier = Modifier.width(64.dp).height(80.dp).clip(RoundedCornerShape(14.dp)).background(colorF), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = semana, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(recorrido.esFeriado) Color.White.copy(0.8f) else colorP.copy(alpha = 0.6f))
            Text(text = num, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colorP)
            Text(text = mes, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(recorrido.esFeriado) Color.White.copy(0.8f) else colorP.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun EstadoRecorrido(recorrido: DiaRecorrido) {
    val hoy = FechaHora.obtenerFechaLocal(); val esHoy = recorrido.fecha == hoy; val yaPaso = recorrido.fecha.isBefore(hoy)
    
    val texto = if (recorrido.esFeriado) "Feriado" else if (yaPaso) "Completado" else if (esHoy) "Hoy activo" else "Programado"
    val icon = if (recorrido.esFeriado) Icons.Default.EventBusy else if (yaPaso) Icons.Default.History else if (esHoy) Icons.Default.RunningWithErrors else Icons.Default.Schedule
    val colorT = if (recorrido.esFeriado) Colores.RojoFeriado else if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeSecundario

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(colorT.copy(0.1f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Icon(icon, null, tint = colorT, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colorT)
    }
}

@Composable
private fun TarjetaInformacion(onVerAvisos: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Colores.FondoTarjetaInfo, border = BorderStroke(1.dp, Colores.BordeTarjetaInfo)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Colores.AzulIcono, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Información importante", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                Text(text = "Los feriados nacionales podrían afectar el horario habitual.", fontSize = 13.sp, color = Colores.AzulMedioTexto)
            }
        }
    }
}

@Preview(name = "Premium Design", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCalendarioResponsive() { Calendario() }
