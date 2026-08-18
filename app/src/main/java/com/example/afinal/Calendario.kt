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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Calendario.DiaRecorrido
import com.example.afinal.datos.Calendario.FechaHora
import com.example.afinal.datos.Calendario.GeneradorRecorridos
import com.example.afinal.datos.Colores
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idiomaEspanol = Locale.forLanguageTag("es-NI")
private val formatoMes = DateTimeFormatter.ofPattern("MMM", idiomaEspanol)

// Función para obtener el nombre corto del día de la semana
private fun obtenerDiaCorto(diaSemana: DayOfWeek): String {
    return when (diaSemana) {
        DayOfWeek.MONDAY -> "LUN"; DayOfWeek.TUESDAY -> "MAR"; DayOfWeek.WEDNESDAY -> "MIÉ"
        DayOfWeek.THURSDAY -> "JUE"; DayOfWeek.FRIDAY -> "VIE"; DayOfWeek.SATURDAY -> "SÁB"
        DayOfWeek.SUNDAY -> "DOM"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendario(  barrio: String, diasRuta: Set<DayOfWeek>,
    horaRutaStr: String, fechasFeriadas: Set<LocalDate> = emptySet(),
    onNotificaciones: () -> Unit = {}, onRecordar: (DiaRecorrido) -> Unit = {},
    horaFinRutaStr: String, onVerAvisos: () -> Unit = {}
) {
    // Estado para la ventana de alarmas y cálculos de tiempo
    var mostrarAlarma by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val horaRuta = try { LocalTime.parse(horaRutaStr) } catch (_: Exception) { LocalTime.of(6, 0) }
    val horaFinRuta = try { LocalTime.parse(horaFinRutaStr) } catch (_: Exception) { LocalTime.of(12, 0) }
    val fechas = GeneradorRecorridos.generar(diasRuta, horaRuta, 3, fechasFeriadas)

    Box(modifier = Modifier.fillMaxSize().background(Colores.BlancoTarjeta).statusBarsPadding()) {
        // Franja verde superior
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Colores.VerdeBosque))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EncabezadoCalendario(onNotificaciones = onNotificaciones)
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                // Cuerpo con diseño de hoja blanca redondeada
                Column(modifier = Modifier.fillMaxWidth().padding(top = 30.dp).background(color = Colores.BlancoTarjeta, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).padding(bottom = 100.dp)) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TarjetaResumenRuta(horaRuta, horaFinRuta)
                        Spacer(modifier = Modifier.height(24.dp))
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
                // Píldora de barrio centrada y solapada
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) { TarjetaBarrio(barrio = barrio) }
            }
        }
        
        if (mostrarAlarma) {
            ModalBottomSheet(onDismissRequest = { mostrarAlarma = false }, sheetState = sheetState, containerColor = Color.Transparent, dragHandle = null) {
                DialogoConfigurarRecordatorio(onDismiss = { mostrarAlarma = false }, onGuardar = { mostrarAlarma = false })
            }
        }
    }
}

@Composable
private fun EncabezadoCalendario(onNotificaciones: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp)) {
        Text(text = "Calendario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Colores.BlancoTarjeta, modifier = Modifier.align(Alignment.Center))
        IconButton(onClick = onNotificaciones, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Colores.BlancoTarjeta, modifier = Modifier.size(28.dp)) }
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
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta), elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)) {
        val hoy = FechaHora.obtenerFechaLocal(); val yaPaso = recorrido.fecha.isBefore(hoy)
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            CajaFecha(recorrido = recorrido)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(if(yaPaso) Colores.GrisSeparador else Colores.VerdeBosque))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (recorrido.esFeriado) "Día feriado" else "Recolección general", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeBosque)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) { EstadoRecorrido(recorrido = recorrido) }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRecordar, modifier = Modifier.size(36.dp), enabled = !yaPaso) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Aviso", tint = if (yaPaso) Colores.TextoGrisSecundario.copy(alpha = 0.4f) else Colores.VerdeBosque, modifier = Modifier.size(24.dp)) }
        }
    }
}

@Composable
private fun CajaFecha(recorrido: DiaRecorrido) {
    val hoy = FechaHora.obtenerFechaLocal(); val yaPaso = recorrido.fecha.isBefore(hoy)
    val colorP = if (recorrido.esFeriado) Colores.RojoFeriado else if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeBosque
    val colorF = if (recorrido.esFeriado) Colores.FondoFeriado else if (yaPaso) Colores.GrisSeleccion else Colores.VerdePastel
    val semana = obtenerDiaCorto(recorrido.fecha.dayOfWeek)
    val num = recorrido.fecha.dayOfMonth.toString()
    val mes = recorrido.fecha.format(formatoMes).uppercase()
    Box(modifier = Modifier.width(60.dp).height(85.dp).clip(RoundedCornerShape(16.dp)).background(colorF), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = semana, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colorP.copy(alpha = 0.7f))
            Text(text = num, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, color = colorP)
            Text(text = mes, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorP.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EstadoRecorrido(recorrido: DiaRecorrido) {
    val hoy = FechaHora.obtenerFechaLocal(); val esHoy = recorrido.fecha == hoy; val yaPaso = recorrido.fecha.isBefore(hoy)
    val texto = if (recorrido.esFeriado) "Feriado" else if (esHoy) "Hoy: Recolección" else if (yaPaso) "Ya pasó" else "Programado"
    val colorT = if (recorrido.esFeriado) Colores.RojoFeriado else if (yaPaso) Colores.TextoGrisSecundario else Colores.VerdeBosque
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if(yaPaso) Colores.TextoGrisSecundario else Colores.VerdeSecundario))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorT)
    }
}

@Composable
private fun TarjetaInformacion(onVerAvisos: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Colores.FondoTarjetaInfo, border = BorderStroke(1.dp, Colores.BordeTarjetaInfo)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val estrecho = maxWidth < 320.dp
            if (estrecho) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Colores.AzulIcono, modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Mantente informado.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Los horarios pueden cambiar por clima o mantenimiento.", fontSize = 14.sp, lineHeight = 18.sp, color = Colores.AzulMedioTexto)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Row(modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onVerAvisos).padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Ver avisos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.AzulIcono)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Ver", tint = Colores.AzulIcono, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Colores.AzulIcono, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mantente informado.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = "Los horarios pueden cambiar por clima o mantenimiento.", fontSize = 14.sp, lineHeight = 18.sp, color = Colores.AzulMedioTexto)
                    }
                }
            }
        }
    }
}

@Preview(name = "Celular normal", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCalendarioResponsive() {
    Calendario(barrio = "Santa Rosa", diasRuta = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY), horaRutaStr = "06:00", horaFinRutaStr = "12:00")
}
