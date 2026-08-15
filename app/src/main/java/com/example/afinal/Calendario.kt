package com.example.afinal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.afinal.datos.Calendario.GeneradorRecorridos
import com.example.afinal.datos.Colores
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TextoPrincipal = Color(0xFF101114)
private val TextoSecundario = Color(0xFF5F6368)
private val ColorBorde = Color(0xFFE5E7EB)
private val FondoTarjetaRuta = Color(0xFFF3F8F4)
private val FondoTarjetaInfo = Color(0xFFF4F8FF)
private val BordeTarjetaInfo = Color(0xFFBED3FF)
private val VerdePastel = Color(0xFFE7F5E8)
private val FondoBotonSuave = Color(0xFFF3F7F3)

private val RojoFeriado = Color(0xFFC62828)
private val FondoFeriado = Color(0xFFFFEBEE)

private val idiomaEspanol = Locale("es", "NI")

private val formatoMes = DateTimeFormatter.ofPattern("MMMM", idiomaEspanol)

private fun obtenerDiaCorto(diaSemana: DayOfWeek): String {
    return when (diaSemana) {
        DayOfWeek.MONDAY -> "LUN"
        DayOfWeek.TUESDAY -> "MAR"
        DayOfWeek.WEDNESDAY -> "MIÉ"
        DayOfWeek.THURSDAY -> "JUE"
        DayOfWeek.FRIDAY -> "VIE"
        DayOfWeek.SATURDAY -> "SÁB"
        DayOfWeek.SUNDAY -> "DOM"
    }
}

private fun formatearHora(hora: LocalTime): String {
    val horaFormato12 = when (val resultado = hora.hour % 12) {
        0 -> 12
        else -> resultado
    }

    val periodo = if (hora.hour < 12) "a. m." else "p. m."

    return String.format(idiomaEspanol, "%d:%02d %s", horaFormato12, hora.minute, periodo)
}

@Composable
fun Calendario(
    barrio: String,
    diasRuta: Set<DayOfWeek>,
    horaRuta: LocalTime,
    fechasFeriadas: Set<LocalDate> = emptySet(),
    onVolver: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onRecordar: (DiaRecorrido) -> Unit = {},
    nombreRuta: String = "Ruta Santa Rosa",
    horaFinRuta: LocalTime = LocalTime.of(12, 0),
    onVerBarrios: () -> Unit = {},
    onVerAvisos: () -> Unit = {}
) {
    val fechas = GeneradorRecorridos.generar(diasRuta, horaRuta, 3, fechasFeriadas)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.GrisFondo)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        EncabezadoCalendario(
            onVolver = onVolver,
            onNotificaciones = onNotificaciones
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val horizontal = if (maxWidth < 340.dp) 12.dp else 16.dp
            val spacingGrande = if (maxWidth < 340.dp) 14.dp else 18.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontal)
            ) {
                TarjetaBarrio(barrio = barrio)

                Spacer(modifier = Modifier.height(spacingGrande))

                TarjetaResumenRuta(
                    nombreRuta = nombreRuta,
                    horaInicio = horaRuta,
                    horaFin = horaFinRuta,
                    onVerBarrios = onVerBarrios
                )

                Spacer(modifier = Modifier.height(12.dp))

                TarjetaAclaracionRuta()

                Spacer(modifier = Modifier.height(spacingGrande))

                TituloSeccion(
                    icono = Icons.Default.DateRange,
                    titulo = "Próximas recolecciones"
                )

                Spacer(modifier = Modifier.height(12.dp))

                fechas.forEachIndexed { index, fecha ->
                    TarjetaRecoleccionCompacta(
                        recorrido = fecha,
                        onRecordar = { onRecordar(fecha) }
                    )

                    if (index < fechas.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TarjetaInformacion(
                    onVerAvisos = onVerAvisos
                )
            }
        }
    }
}

@Composable
private fun EncabezadoCalendario(
    onVolver: () -> Unit,
    onNotificaciones: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Regresar",
                tint = Colores.VerdePrincipal,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Calendario",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.VerdePrincipal,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onNotificaciones,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = Colores.VerdePrincipal,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TarjetaBarrio(barrio: String) {
    Surface(
        modifier = Modifier.widthIn(max = 260.dp),
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Colores.VerdePrincipal,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Mi barrio: ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.VerdePrincipal
            )

            Text(
                text = barrio,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaResumenRuta(
    nombreRuta: String,
    horaInicio: LocalTime,
    horaFin: LocalTime,
    onVerBarrios: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjetaRuta),
        border = BorderStroke(1.dp, Color(0xFFE7EEE8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val estrecho = maxWidth < 320.dp

            if (estrecho) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {


                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = nombreRuta,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Colores.VerdePrincipal,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Horario general",
                                fontSize = 15.sp,
                                color = TextoSecundario
                            )
                            Text(
                                text = "${formatearHora(horaInicio)} – ${formatearHora(horaFin)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoPrincipal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFDCE7DD), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onVerBarrios)
                            .background(Color.White)
                            .border(1.dp, Colores.VerdePrincipal, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Colores.VerdePrincipal,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Ver barrios",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Colores.VerdePrincipal,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver barrios",
                            tint = Colores.VerdePrincipal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Colores.VerdePrincipal,
                                    modifier = Modifier.size(22.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Horario general",
                                        fontSize = 16.sp,
                                        color = TextoSecundario
                                    )
                                    Text(
                                        text = "${formatearHora(horaInicio)} – ${formatearHora(horaFin)}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoPrincipal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFDCE7DD), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = onVerBarrios)
                                .background(Color.White)
                                .border(1.dp, Colores.VerdePrincipal, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Colores.VerdePrincipal,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Ver barrios",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Colores.VerdePrincipal
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Ver barrios",
                                tint = Colores.VerdePrincipal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAclaracionRuta() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FondoBotonSuave,
        border = BorderStroke(1.dp, Color(0xFFDDE4DE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Colores.VerdePrincipal,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Este es el horario general de la ruta. La hora de paso por tu barrio puede variar.",
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = TextoSecundario
            )
        }
    }
}

@Composable
private fun TituloSeccion(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Colores.VerdePrincipal,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F2042)
        )
    }
}

@Composable
private fun TarjetaRecoleccionCompacta(
    recorrido: DiaRecorrido,
    onRecordar: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ColorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val estrecho = maxWidth < 255.dp
            val alturaLinea = if (estrecho) 72.dp else 82.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CajaFecha(recorrido = recorrido, compacta = estrecho)

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(alturaLinea)
                        .clip(RoundedCornerShape(50))
                        .background(if (recorrido.esFeriado) RojoFeriado else Colores.VerdePrincipal)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (estrecho) {
                        Text(
                            text = if (recorrido.esFeriado) "Día feriado" else "Recolección general",
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        EstadoRecorrido(esFeriado = recorrido.esFeriado)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (recorrido.esFeriado) "Día feriado" else "Recolección general",
                                    fontSize = 16.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoPrincipal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                EstadoRecorrido(esFeriado = recorrido.esFeriado)
                            }

                            IconButton(
                                onClick = onRecordar,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Configurar recordatorio",
                                    tint = Colores.VerdePrincipal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (estrecho) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onRecordar,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Configurar recordatorio",
                                    tint = Colores.VerdePrincipal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    if (recorrido.esFeriado) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MensajeFeriadoCompacto()
                    }
                }
            }
        }
    }
}

@Composable
private fun CajaFecha(
    recorrido: DiaRecorrido,
    compacta: Boolean
) {
    val colorPrincipal = if (recorrido.esFeriado) RojoFeriado else Colores.VerdePrincipal
    val colorFondo = if (recorrido.esFeriado) FondoFeriado else VerdePastel

    val semana = obtenerDiaCorto(recorrido.fecha.dayOfWeek)
    val numeroDia = recorrido.fecha.dayOfMonth.toString()
    val nombreMes = recorrido.fecha.format(formatoMes).lowercase(idiomaEspanol)

    val ancho = if (compacta) 66.dp else 78.dp
    val alto = if (compacta) 92.dp else 104.dp
    val fontDia = if (compacta) 15.sp else 16.sp
    val fontNumero = if (compacta) 30.sp else 34.sp

    Box(
        modifier = Modifier
            .width(ancho)
            .height(alto)
            .clip(RoundedCornerShape(18.dp))
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = semana,
                fontSize = fontDia,
                fontWeight = FontWeight.SemiBold,
                color = colorPrincipal
            )

            Text(
                text = numeroDia,
                fontSize = fontNumero,
                lineHeight = fontNumero,
                fontWeight = FontWeight.ExtraBold,
                color = colorPrincipal
            )

            Text(
                text = "de $nombreMes",
                fontSize = if (compacta) 12.sp else 13.sp,
                color = TextoSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EstadoRecorrido(esFeriado: Boolean) {
    val texto = if (esFeriado) "Feriado" else "Programado"
    val colorPrincipal = if (esFeriado) RojoFeriado else Colores.VerdePrincipal
    val colorFondo = if (esFeriado) FondoFeriado else Colores.VerdeClaro

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colorPrincipal)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorPrincipal
        )
    }
}

@Composable
private fun MensajeFeriadoCompacto() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FondoFeriado)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = RojoFeriado,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "No habrá recolección",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = RojoFeriado
        )
    }
}

@Composable
private fun TarjetaInformacion(
    onVerAvisos: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = FondoTarjetaInfo,
        border = BorderStroke(1.dp, BordeTarjetaInfo)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val estrecho = maxWidth < 320.dp

            if (estrecho) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Información",
                            tint = Colores.AzulIcono,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mantente informado.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.AzulOscuroTitulo
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Los horarios pueden cambiar por clima o por mantenimiento del servicio.",
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = Colores.AzulMedioTexto
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onVerAvisos)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ver avisos",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.AzulIcono
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Ver avisos",
                                tint = Colores.AzulIcono,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Colores.AzulIcono,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mantente informado.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.AzulOscuroTitulo
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Los horarios pueden cambiar por clima o por mantenimiento del servicio.",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = Colores.AzulMedioTexto
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onVerAvisos)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ver avisos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.AzulIcono
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver avisos",
                            tint = Colores.AzulIcono,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Celular normal", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Celular pequeño", showBackground = true, widthDp = 320, heightDp = 700, fontScale = 1f)
@Composable
private fun PreviewCalendarioResponsive() {
    Calendario(
        barrio = "Santa Rosa",
        diasRuta = setOf(
            DayOfWeek.TUESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SATURDAY
        ),
        horaRuta = LocalTime.of(6, 0),
        nombreRuta = "Ruta Santa Rosa",
        horaFinRuta = LocalTime.of(12, 0)
    )
}
