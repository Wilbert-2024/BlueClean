package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
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
    var recordatorioActivo by remember { mutableStateOf(value = true) }
    var nocheAnterior by remember { mutableStateOf(value = false) }
    var diaRecoleccion by remember { mutableStateOf(value = true) }
    var predeterminado by remember { mutableStateOf(value = true) }

    var horaNoche by remember { mutableStateOf("17:30") }
    var horaDia by remember { mutableStateOf("07:00") }
    var mostrarRelojNoche by remember { mutableStateOf(false) }
    var mostrarRelojDia by remember { mutableStateOf(false) }

    if (mostrarRelojNoche) SeleccionadorHora(horaNoche, { mostrarRelojNoche = false }) { horaNoche = it; mostrarRelojNoche = false }
    if (mostrarRelojDia) SeleccionadorHora(horaDia, { mostrarRelojDia = false }) { horaDia = it; mostrarRelojDia = false }

    Column(modifier = Modifier.fillMaxWidth().background(Colores.BlancoTarjeta, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {

        Box(modifier = Modifier.padding(vertical = 12.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Color.LightGray).align(Alignment.CenterHorizontally))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Configurar recordatorio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Colores.TextoGrisSecundario) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (recordatorioActivo) Colores.VerdeFondoSuave else Colores.BlancoTarjeta), border = androidx.compose.foundation.BorderStroke(1.dp, if (recordatorioActivo) Color(0xFFE0EBE2) else Color(0xFFEEEEEE))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(Colores.BlancoTarjeta, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Colores.VerdeSecundario, modifier = Modifier.size(24.dp)) }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Activar recordatorio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                    Text(text = "Te avisaremos antes de que pase el camión recolector", fontSize = 12.sp, color = Colores.TextoGrisSecundario, lineHeight = 16.sp)
                }
                Switch(checked = recordatorioActivo, onCheckedChange = { recordatorioActivo = it }, colors = SwitchDefaults.colors(checkedThumbColor = Colores.BlancoTarjeta, checkedTrackColor = Colores.VerdeSecundario))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "¿CUÁNDO Y A QUÉ HORA TE AVISAMOS?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeSecundario, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(12.dp))

        OpcionRecordatorio(Icons.Outlined.Nightlight, "La noche anterior", "para ir preparando las bolsas", horaNoche, nocheAnterior, { nocheAnterior = it }) { mostrarRelojNoche = true }
        Spacer(modifier = Modifier.height(12.dp))

        OpcionRecordatorio(Icons.Default.WbSunny, "El día de la recolección", "Justo antes de que pase el camión recolector", horaDia, diaRecoleccion, { diaRecoleccion = it }) { mostrarRelojDia = true }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            GestionAlarmas.programarRecordatorios(context, recordatorioActivo, horaNoche, nocheAnterior, horaDia, diaRecoleccion)
            onGuardar()
            Mensajeria.exito("¡Recordatorios guardados correctamente!")
        }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdeBosque)) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = Colores.BlancoTarjeta)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Guardar recordatorios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.BlancoTarjeta)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = predeterminado, onCheckedChange = { predeterminado = it }, colors = CheckboxDefaults.colors(checkedColor = Colores.VerdeSecundario, checkmarkColor = Colores.BlancoTarjeta))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Guardar como predeterminado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                    Text(text = "Aplicar esta configuración a todas mis próximas recolecciones", fontSize = 12.sp, color = Colores.TextoGrisSecundario)
                }
            }
        }
    }
}

@Composable
private fun OpcionRecordatorio(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, descripcion: String, hora: String, seleccionado: Boolean, onSeleccionChange: (Boolean) -> Unit, onClickHora: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = Colores.VerdeBosque, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                    Text(text = descripcion, fontSize = 12.sp, color = Colores.TextoGrisSecundario)
                }
                Checkbox(checked = seleccionado, onCheckedChange = onSeleccionChange, colors = CheckboxDefaults.colors(checkedColor = Colores.VerdeSecundario, checkmarkColor = Colores.BlancoTarjeta))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = if (seleccionado) Colores.VerdeFondoSuave else Color(0xFFF5F5F5), shape = RoundedCornerShape(50), modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onClickHora() }) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = hora, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Colores.VerdeBosque, modifier = Modifier.size(18.dp))
                }
            }
        }
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
