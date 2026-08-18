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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoConfigurarRecordatorio(onDismiss: () -> Unit, onGuardar: () -> Unit) {
    var recordatorioActivo by remember { mutableStateOf(true) }
    var nocheAnterior by remember { mutableStateOf(false) }
    var diaRecoleccion by remember { mutableStateOf(true) }
    var predeterminado by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth().background(Colores.BlancoTarjeta, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {

        Box(modifier = Modifier.padding(vertical = 12.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Color.LightGray).align(Alignment.CenterHorizontally))


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Configurar recordatorio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Colores.TextoGrisSecundario) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opción para activar o desactivar todos los avisos
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

        // Ajuste para avisar la noche antes de la recolección
        OpcionRecordatorio(icono = Icons.Outlined.Nightlight, titulo = "La noche anterior", descripcion = "para ir preparando las bolsas", hora = "17:30", seleccionado = nocheAnterior, onSeleccionChange = { nocheAnterior = it })
        Spacer(modifier = Modifier.height(12.dp))

        // Ajuste para avisar el mismo día del paso del camión
        OpcionRecordatorio(icono = Icons.Default.WbSunny, titulo = "El día de la recolección", descripcion = "Justo antes de que pase el camión recolector", hora = "07:00", seleccionado = diaRecoleccion, onSeleccionChange = { diaRecoleccion = it })
        Spacer(modifier = Modifier.height(24.dp))

        // Botón para confirmar los cambios
        Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdeBosque)) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = Colores.BlancoTarjeta)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Guardar recordatorios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.BlancoTarjeta)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opción para aplicar esta configuración a todas las fechas futuras
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
private fun OpcionRecordatorio(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, descripcion: String, hora: String, seleccionado: Boolean, onSeleccionChange: (Boolean) -> Unit) {
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
            Surface(color = if (seleccionado) Colores.VerdeFondoSuave else Color(0xFFF5F5F5), shape = RoundedCornerShape(50), modifier = Modifier.clip(RoundedCornerShape(50)).clickable { /* Abrir seleccion de hora */ }) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = hora, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.VerdeBosque)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Colores.VerdeBosque, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAlarma() { DialogoConfigurarRecordatorio(onDismiss = {}, onGuardar = {}) }
