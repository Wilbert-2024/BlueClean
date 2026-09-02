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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores
import com.example.afinal.datos.mensajeria.Mensajeria
import com.example.afinal.servicios.GestionAlarmas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoConfigurarRecordatorio(onDismiss: () -> Unit, onGuardar: () -> Unit) {
    val context = LocalContext.current
    var modoSeleccionado by remember { mutableStateOf("AMBOS") }
    var predeterminado by remember { mutableStateOf(true) }

    var horaNoche by remember { mutableStateOf("17:30") }
    var horaDia by remember { mutableStateOf("07:00") }
    var mostrarRelojNoche by remember { mutableStateOf(false) }
    var mostrarRelojDia by remember { mutableStateOf(false) }

    if (mostrarRelojNoche) SeleccionadorHora(horaNoche, { mostrarRelojNoche = false }) { horaNoche = it; mostrarRelojNoche = false }
    if (mostrarRelojDia) SeleccionadorHora(horaDia, { mostrarRelojDia = false }) { horaDia = it; mostrarRelojDia = false }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Colores.BlancoTarjeta, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Box(modifier = Modifier.padding(bottom = 14.dp).size(width = 44.dp, height = 4.dp).clip(CircleShape).background(Color.LightGray).align(Alignment.CenterHorizontally))

        // Clean Header with Official App Typography
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "Recordatorios", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdeBosque)
                Text(text = "Avisos automáticos de recolección", fontSize = 12.sp, color = Colores.TextoGrisSecundario)
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Colores.VerdeFondoSuave)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Colores.VerdeBosque)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Mode Selector Pills (Official Palette)
        Surface(
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(50),
            color = Color(0xFFF2F4F3)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                ModoChip(
                    titulo = "🌙 NOCHE",
                    activo = modoSeleccionado == "NOCHE",
                    onClick = { modoSeleccionado = "NOCHE" },
                    modifier = Modifier.weight(1f)
                )
                ModoChip(
                    titulo = "☀️ MAÑANA",
                    activo = modoSeleccionado == "MAÑANA",
                    onClick = { modoSeleccionado = "MAÑANA" },
                    modifier = Modifier.weight(1f)
                )
                ModoChip(
                    titulo = "⚡ AMBOS",
                    activo = modoSeleccionado == "AMBOS",
                    onClick = { modoSeleccionado = "AMBOS" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Giant Digital Clock Display Card (VerdeBosque + VerdeVibrante)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.VerdeBosque),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (modoSeleccionado == "NOCHE") Icons.Outlined.Nightlight else if (modoSeleccionado == "MAÑANA") Icons.Default.WbSunny else Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Colores.VerdeVibrante,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (modoSeleccionado == "NOCHE") "NOCHE ANTERIOR" else if (modoSeleccionado == "MAÑANA") "DÍA DE RECOLECCIÓN" else "MODO DOBLE ACTIVADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Colores.VerdeVibrante
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val horaAMostrar = if (modoSeleccionado == "NOCHE") horaNoche else if (modoSeleccionado == "MAÑANA") horaDia else "$horaNoche / $horaDia"
                Text(
                    text = horaAMostrar,
                    fontSize = if (modoSeleccionado == "AMBOS") 34.sp else 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Colores.VerdeVibrante,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tuning Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (modoSeleccionado == "NOCHE" || modoSeleccionado == "AMBOS") {
                        Button(
                            onClick = { mostrarRelojNoche = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f))
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, tint = Colores.VerdeVibrante, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Ajustar Noche ($horaNoche)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (modoSeleccionado == "MAÑANA" || modoSeleccionado == "AMBOS") {
                        Button(
                            onClick = { mostrarRelojDia = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f))
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, tint = Colores.VerdeVibrante, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Ajustar Día ($horaDia)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Pulse Bar (VerdePastel + VerdeBosque)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Colores.VerdePastel,
            border = BorderStroke(1.dp, Color(0xFFC8E6C9))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Colores.VerdeSecundario)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "STATUS: RECORDATORIO ACTIVADO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Colores.VerdeBosque,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Predeterminado Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { predeterminado = !predeterminado }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = predeterminado,
                onCheckedChange = { predeterminado = it },
                colors = CheckboxDefaults.colors(checkedColor = Colores.VerdeSecundario, checkmarkColor = Colores.BlancoTarjeta)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Guardar como preferido predeterminado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                Text(text = "Aplicar a todas las recolecciones de mi barrio", fontSize = 11.sp, color = Colores.TextoGrisSecundario)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary CTA Button
        Button(
            onClick = {
                val nocheActiva = modoSeleccionado == "NOCHE" || modoSeleccionado == "AMBOS"
                val diaActivo = modoSeleccionado == "MAÑANA" || modoSeleccionado == "AMBOS"
                GestionAlarmas.programarRecordatorios(context, true, horaNoche, nocheActiva, horaDia, diaActivo)
                onGuardar()
                Mensajeria.exito("¡Recordatorios guardados correctamente!")
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdeBosque)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = Colores.BlancoTarjeta)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Guardar recordatorio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.BlancoTarjeta)
        }
    }
}

@Composable
private fun ModoChip(
    titulo: String,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .background(if (activo) Colores.VerdeBosque else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = titulo,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (activo) Color.White else Colores.TextoGrisSecundario
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeleccionadorHora(horaInicial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val partes = horaInicial.split(":")
    val state = rememberTimePickerState(initialHour = partes[0].toInt(), initialMinute = partes[1].toInt(), is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm("${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}") }) { Text("ACEPTAR", color = Colores.VerdeBosque) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Colores.TextoGrisSecundario) } },
        text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state, colors = TimePickerDefaults.colors(selectorColor = Colores.VerdeBosque)) } },
        containerColor = Colores.BlancoTarjeta
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAlarma() { DialogoConfigurarRecordatorio(onDismiss = {}, onGuardar = {}) }
