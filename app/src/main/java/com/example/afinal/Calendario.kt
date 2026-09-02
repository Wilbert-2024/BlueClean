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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
    onVerAvisos: () -> Unit = {},
    cantidadNoLeidos: Int = 0
) {
    val context = LocalContext.current
    val vm = remember { Calendario_vistaModal() }
    var mostrarAlarma by remember { mutableStateOf(false) }
    var mostrarModalDetalle by remember { mutableStateOf(false) }
    var vistaSeleccionada by remember { mutableStateOf("Mensual") }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) { vm.cargarDatos(context) }

    if (vm.estaCargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Colores.VerdePrincipal) }
    } else {
        val horaRuta = try { LocalTime.parse(vm.horaInicio) } catch (_: Exception) { LocalTime.of(6, 0) }
        val horaFinRuta = try { LocalTime.parse(vm.horaFin) } catch (_: Exception) { LocalTime.of(12, 0) }
        val fechas = GeneradorFechasCalendario.generar(vm.diasRuta, horaRuta, 3, vm.fechasFeriadas)

        Box(modifier = Modifier.fillMaxSize().background(Colores.VerdeBosque)) {
            Column(modifier = Modifier.fillMaxSize()) {
                EncabezadoCalendario(horaInicio = horaRuta, horaFin = horaFinRuta, onNotificaciones = onNotificaciones, cantidadNoLeidos = cantidadNoLeidos)
                
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
                    Column(modifier = Modifier.fillMaxSize().background(color = Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp)) {
                            SelectorVistaCalendario(vistaSeleccionada = vistaSeleccionada, onVistaCambiada = { vistaSeleccionada = it })
                        }
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 90.dp)) {
                            if (vistaSeleccionada == "Mensual") {
                                CalendarioVistaMensual(
                                    mesActual = vm.mesActual,
                                    diaSeleccionado = vm.diaSeleccionado,
                                    diasRuta = vm.diasRuta,
                                    fechasFeriadas = vm.fechasFeriadas,
                                    onMesAnterior = { vm.mesAnterior() },
                                    onMesSiguiente = { vm.mesSiguiente() },
                                    onDiaSeleccionado = {
                                        vm.seleccionarDia(it)
                                        mostrarModalDetalle = true
                                    }
                                )
                            } else {
                                TituloSeccion(icono = Icons.Default.DateRange, titulo = "Próximas recolecciones", onVerTodas = onVerAvisos)
                                Spacer(modifier = Modifier.height(12.dp))
                                fechas.forEachIndexed { index, fecha ->
                                    TarjetaRecoleccionCompacta(recorrido = fecha, onRecordar = { mostrarAlarma = true; onRecordar(fecha) })
                                    if (index < fechas.lastIndex) { Spacer(modifier = Modifier.height(12.dp)) }
                                }
                            }
                        }
                    }
                }
            }
            
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                PastillaEstadoFlotante(
                    diasRuta = vm.diasRuta,
                    fechasFeriadas = vm.fechasFeriadas,
                    horaInicio = horaRuta,
                    horaFin = horaFinRuta,
                    onClick = {
                        vm.seleccionarDia(LocalDate.now())
                        mostrarModalDetalle = true
                    }
                )
            }

            if (mostrarModalDetalle) {
                ModalBottomSheetDetalleDia(
                    fecha = vm.diaSeleccionado,
                    diasRuta = vm.diasRuta,
                    fechasFeriadas = vm.fechasFeriadas,
                    horaInicio = horaRuta,
                    horaFin = horaFinRuta,
                    onDismiss = { mostrarModalDetalle = false },
                    onRecordar = {
                        mostrarModalDetalle = false
                        mostrarAlarma = true
                        onRecordar(it)
                    }
                )
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
private fun EncabezadoCalendario(horaInicio: LocalTime, horaFin: LocalTime, onNotificaciones: () -> Unit, cantidadNoLeidos: Int = 0) {
    val fmt = DateTimeFormatter.ofPattern("H:mm")
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp).offset(y = (-8).dp)) {
            Text(text = "Calendario", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.align(Alignment.Center))
            BadgedBox(
                modifier = Modifier.align(Alignment.CenterEnd),
                badge = {
                    if (cantidadNoLeidos > 0) {
                        Badge(containerColor = Color(0xFFD32F2F), contentColor = Color.White) {
                            Text(text = if (cantidadNoLeidos > 99) "99+" else cantidadNoLeidos.toString())
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificaciones) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(24.dp)) }
            }
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
private fun TituloSeccion(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, onVerTodas: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icono, contentDescription = null, tint = Colores.VerdeSecundario, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
        }

    }
}

@Composable
private fun CalendarioVistaMensual(
    mesActual: YearMonth,
    diaSeleccionado: LocalDate,
    diasRuta: Set<DayOfWeek>,
    fechasFeriadas: Map<LocalDate, String>,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onDiaSeleccionado: (LocalDate) -> Unit
) {
    val hoy = LocalDate.now()
    val primerDiaMes = mesActual.atDay(1)
    val primerDiaSemana = primerDiaMes.dayOfWeek
    val diasPrevios = primerDiaSemana.value - 1
    val primerDiaGrid = primerDiaMes.minusDays(diasPrevios.toLong())
    val diasGrid = (0 until 35).map { primerDiaGrid.plusDays(it.toLong()) }
    
    val nombreMes = mesActual.month.getDisplayName(TextStyle.FULL, idiomaEspanol).replaceFirstChar { it.uppercase() }
    val tituloMes = "$nombreMes ${mesActual.year}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMesAnterior, modifier = Modifier.size(32.dp).background(Colores.VerdeFondoSuave, CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior", tint = Colores.VerdeBosque, modifier = Modifier.size(16.dp))
                }
                Text(text = tituloMes, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                IconButton(onClick = onMesSiguiente, modifier = Modifier.size(32.dp).background(Colores.VerdeFondoSuave, CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente", tint = Colores.VerdeBosque, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM").forEach { dia ->
                    Text(text = dia, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Colores.TextoGrisSecundario, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            diasGrid.chunked(7).forEach { semana ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp)) {
                    semana.forEach { fecha ->
                        val esDelMes = fecha.month == mesActual.month
                        val esHoy = fecha == hoy
                        val esSeleccionado = fecha == diaSeleccionado
                        val esDiaRuta = diasRuta.contains(fecha.dayOfWeek) && esDelMes
                        val esFeriado = fechasFeriadas.containsKey(fecha) && esDelMes

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CeldaDiaGrid(fecha = fecha, esDelMes = esDelMes, esHoy = esHoy, esSeleccionado = esSeleccionado, esDiaRuta = esDiaRuta, esFeriado = esFeriado, onClick = { onDiaSeleccionado(fecha) })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LeyendaEstados()
        }
    }
}

@Composable
private fun CeldaDiaGrid(fecha: LocalDate, esDelMes: Boolean, esHoy: Boolean, esSeleccionado: Boolean, esDiaRuta: Boolean, esFeriado: Boolean, onClick: () -> Unit) {
    val colorTexto = when {
        !esDelMes -> Color.LightGray
        esHoy -> Color.White
        esSeleccionado -> Colores.VerdeBosque
        esFeriado -> Colores.RojoFeriado
        esDiaRuta -> Colores.VerdeBosque
        else -> Color.DarkGray
    }
    val colorFondo = when {
        !esDelMes -> Color.Transparent
        esHoy -> Colores.VerdeBosque
        esSeleccionado -> Colores.VerdeFondoSuave
        esDiaRuta -> Colores.VerdePastel
        else -> Color.Transparent
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(colorFondo).then(if (esSeleccionado && !esHoy) Modifier.clip(CircleShape) else Modifier), contentAlignment = Alignment.Center) {
            Text(text = fecha.dayOfMonth.toString(), fontSize = 12.5.sp, fontWeight = if (esHoy || esSeleccionado || esDiaRuta || esFeriado) FontWeight.Bold else FontWeight.Normal, color = colorTexto)
        }
        Spacer(modifier = Modifier.height(2.dp))
        if (esDelMes && (esDiaRuta || esFeriado)) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (esFeriado) Colores.RojoFeriado else Colores.VerdeSecundario))
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun LeyendaEstados() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50), color = Color(0xFFF9F9F9), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            ItemLeyenda(color = Colores.VerdeSecundario, texto = "Programado")
            ItemLeyenda(color = Colores.RojoFeriado, texto = "Feriado")
            ItemLeyenda(color = Color.LightGray, texto = "Sin servicio")
        }
    }
}

@Composable
private fun ItemLeyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Colores.TextoGrisSecundario)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalBottomSheetDetalleDia(
    fecha: LocalDate,
    diasRuta: Set<DayOfWeek>,
    fechasFeriadas: Map<LocalDate, String>,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    onDismiss: () -> Unit,
    onRecordar: (DiaRecorrido) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val esDiaRuta = diasRuta.contains(fecha.dayOfWeek)
    val esFeriado = fechasFeriadas.containsKey(fecha)
    val nombreFeriado = fechasFeriadas[fecha]
    val fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", idiomaEspanol)).replaceFirstChar { it.uppercase() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Información del día", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeSecundario)
                    Text(text = fechaFormateada, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Colores.TextoGrisSecundario) }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (esDiaRuta || esFeriado) {
                val recorrido = DiaRecorrido(fecha, horaInicio, esFeriado, nombreFeriado)
                TarjetaRecoleccionCompacta(recorrido = recorrido, onRecordar = { onRecordar(recorrido) })
            } else {
                TarjetaSinRecoleccion(diasRuta = diasRuta)
            }
        }
    }
}

@Composable
private fun TarjetaDetalleDiaSeleccionado(
    fecha: LocalDate,
    diasRuta: Set<DayOfWeek>,
    fechasFeriadas: Map<LocalDate, String>,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    onRecordar: (DiaRecorrido) -> Unit
) {
    val esDiaRuta = diasRuta.contains(fecha.dayOfWeek)
    val esFeriado = fechasFeriadas.containsKey(fecha)
    val nombreFeriado = fechasFeriadas[fecha]

    if (esDiaRuta || esFeriado) {
        val recorrido = DiaRecorrido(fecha, horaInicio, esFeriado, nombreFeriado)
        TarjetaRecoleccionCompacta(recorrido = recorrido, onRecordar = { onRecordar(recorrido) })
    } else {
        TarjetaSinRecoleccion(diasRuta = diasRuta)
    }
}

@Composable
private fun TarjetaSinRecoleccion(diasRuta: Set<DayOfWeek>) {
    val diasAtencionStr = if (diasRuta.isEmpty()) "Lunes, Miércoles y Viernes" else diasRuta.sortedBy { it.value }.joinToString(", ") {
        when (it) {
            DayOfWeek.MONDAY -> "Lunes"; DayOfWeek.TUESDAY -> "Martes"; DayOfWeek.WEDNESDAY -> "Miércoles"
            DayOfWeek.THURSDAY -> "Jueves"; DayOfWeek.FRIDAY -> "Viernes"; DayOfWeek.SATURDAY -> "Sábado"; DayOfWeek.SUNDAY -> "Domingo"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, Color(0xFFEFEFEF))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EventBusy, contentDescription = null, tint = Colores.TextoGrisSecundario, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Sin recolección este día", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.AzulOscuroTitulo)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Días habituales: $diasAtencionStr.", fontSize = 12.sp, color = Colores.TextoGrisSecundario, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SelectorVistaCalendario(vistaSeleccionada: String, onVistaCambiada: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(50), color = Color(0xFFF2F4F3)) {
        Row(modifier = Modifier.fillMaxSize().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(50)).background(if (vistaSeleccionada == "Mensual") Colores.VerdeBosque else Color.Transparent).clickable { onVistaCambiada("Mensual") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = if (vistaSeleccionada == "Mensual") Color.White else Colores.TextoGrisSecundario, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Vista Mensual", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (vistaSeleccionada == "Mensual") Color.White else Colores.TextoGrisSecundario)
                }
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(50)).background(if (vistaSeleccionada == "Lista") Colores.VerdeBosque else Color.Transparent).clickable { onVistaCambiada("Lista") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = null, tint = if (vistaSeleccionada == "Lista") Color.White else Colores.TextoGrisSecundario, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Lista Semanal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (vistaSeleccionada == "Lista") Color.White else Colores.TextoGrisSecundario)
                }
            }
        }
    }
}

@Composable
private fun PastillaEstadoFlotante(
    diasRuta: Set<DayOfWeek>,
    fechasFeriadas: Map<LocalDate, String>,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    onClick: () -> Unit
) {
    val hoy = LocalDate.now()
    val esDiaRuta = diasRuta.contains(hoy.dayOfWeek)
    val esFeriado = fechasFeriadas.containsKey(hoy)
    val nombreFeriado = fechasFeriadas[hoy]

    val colorIndicador = if (esFeriado) Colores.RojoFeriado else if (esDiaRuta) Colores.VerdeVibrante else Color.LightGray
    val fondoPastilla = if (esFeriado) Colores.RojoFeriado else if (esDiaRuta) Colores.VerdeBosque else Color(0xFF333333)
    val textoEstado = if (esFeriado) "Hoy: Feriado ($nombreFeriado)" else if (esDiaRuta) "Hoy: Recolección Normal Activa" else "Hoy: Sin recolección programada"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = fondoPastilla),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colorIndicador))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = textoEstado, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Toca para ver información completa", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Colores.VerdeVibrante, modifier = Modifier.size(22.dp))
        }
    }
}

@Preview(name = "Opcion 1 Preview", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewCalendarioOpcion1() {
    val hoy = LocalDate.now()
    var diaSeleccionado by remember { mutableStateOf(hoy) }
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    val diasRuta = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    val fechasFeriadas = mapOf(hoy.plusDays(2) to "Día Festivo")

    Box(modifier = Modifier.fillMaxSize().background(Colores.VerdeBosque)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EncabezadoCalendario(horaInicio = LocalTime.of(6, 0), horaFin = LocalTime.of(12, 0), onNotificaciones = {})
            
            Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Column(modifier = Modifier.fillMaxWidth().background(color = Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).padding(bottom = 100.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CalendarioVistaMensual(
                            mesActual = mesActual,
                            diaSeleccionado = diaSeleccionado,
                            diasRuta = diasRuta,
                            fechasFeriadas = fechasFeriadas,
                            onMesAnterior = { mesActual = mesActual.minusMonths(1) },
                            onMesSiguiente = { mesActual = mesActual.plusMonths(1) },
                            onDiaSeleccionado = { diaSeleccionado = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TituloSeccion(icono = Icons.Default.Event, titulo = "Día seleccionado")
                        Spacer(modifier = Modifier.height(12.dp))
                        TarjetaDetalleDiaSeleccionado(
                            fecha = diaSeleccionado,
                            diasRuta = diasRuta,
                            fechasFeriadas = fechasFeriadas,
                            horaInicio = LocalTime.of(6, 0),
                            horaFin = LocalTime.of(12, 0),
                            onRecordar = {}
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TarjetaInformacion(onVerAvisos = {})
                    }
                }
            }
        }
    }
}

@Preview(name = "Opcion A - Pastilla Flotante", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewCalendarioOpcion2() {
    val hoy = LocalDate.now()
    var vistaSeleccionada by remember { mutableStateOf("Mensual") }
    val diaSeleccionado = hoy.plusDays(2)
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    val diasRuta = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    val fechasFeriadas = mapOf(diaSeleccionado to "Día Festivo Nacional")
    val horaRuta = LocalTime.of(6, 0)
    val horaFinRuta = LocalTime.of(12, 0)

    Box(modifier = Modifier.fillMaxSize().background(Colores.VerdeBosque)) {
        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoCalendario(horaInicio = horaRuta, horaFin = horaFinRuta, onNotificaciones = {})
            
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
                Column(modifier = Modifier.fillMaxSize().background(color = Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp)) {
                        SelectorVistaCalendario(vistaSeleccionada = vistaSeleccionada, onVistaCambiada = { vistaSeleccionada = it })
                    }
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 80.dp)) {
                        CalendarioVistaMensual(
                            mesActual = mesActual,
                            diaSeleccionado = diaSeleccionado,
                            diasRuta = diasRuta,
                            fechasFeriadas = fechasFeriadas,
                            onMesAnterior = {},
                            onMesSiguiente = {},
                            onDiaSeleccionado = {}
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
            PastillaEstadoFlotante(
                diasRuta = diasRuta,
                fechasFeriadas = fechasFeriadas,
                horaInicio = horaRuta,
                horaFin = horaFinRuta,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Premium Design", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCalendarioResponsive() { Calendario() }
