package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria

@Composable
fun PantallaAjustes(onCambiarUbicacion: () -> Unit) {
    val contexto = LocalContext.current
    val datosUsuario = remember { datosEnMemoria.obtener(contexto) }
    var notificacionesActivadas by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(Colores.GrisFondo).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(text = "Ajustes y Configuración", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdePrincipal)
        Text(text = "Gestiona tus preferencias de ubicación y aplicación.", fontSize = 13.sp, color = Colores.TextoGris, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

        // --- TARJETA 1: DATOS DEL USUARIO Y UBICACIÓN ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Colores.VerdeClaro), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(text = datosUsuario?.NomUsuario ?: "Usuario", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.TextoOscuro)
                        Text(text = "Usuario registrado", fontSize = 12.sp, color = Colores.TextoGris)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Colores.GrisSeparador)

                ItemDetalleUbicacion(icono = Icons.Default.HomeWork, titulo = "Barrio actual", valor = datosUsuario?.Barrio ?: "No seleccionado")
                Spacer(Modifier.height(10.dp))
                ItemDetalleUbicacion(icono = Icons.Default.Place, titulo = "Lugar de referencia", valor = datosUsuario?.LugarReferencia?.takeIf { it.isNotEmpty() } ?: "No especificado")

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onCambiarUbicacion,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdePrincipal)
                ) {
                    Icon(Icons.Default.EditLocationAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = "CAMBIAR BARRIO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // --- TARJETA 2: PREFERENCIAS DE AVISOS ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Notificaciones y Avisos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePrincipal)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Colores.VerdeSecundario, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(text = "Alertas de Recolección", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro)
                            Text(text = "Avisos sonoros y recordatorios de camión", fontSize = 11.sp, color = Colores.TextoGris)
                        }
                    }
                    Switch(
                        checked = notificacionesActivadas,
                        onCheckedChange = { notificacionesActivadas = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Colores.VerdePrincipal)
                    )
                }
            }
        }

        // --- TARJETA 3: INFORMACIÓN DE LA APLICACIÓN ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.BlancoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Información", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePrincipal)
                Spacer(Modifier.height(12.dp))

                ItemInfoApp(icono = Icons.Default.Info, titulo = "Versión de la aplicación", valor = "1.0.0")
                Spacer(Modifier.height(10.dp))
                ItemInfoApp(icono = Icons.Default.LocalShipping, titulo = "Servicio", valor = "Recolección Municipal de Residuos")
            }
        }
    }
}

@Composable
private fun ItemDetalleUbicacion(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = Colores.TextoGris, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(text = titulo, fontSize = 11.sp, color = Colores.TextoGris)
            Text(text = valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro)
        }
    }
}

@Composable
private fun ItemInfoApp(icono: ImageVector, titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(icono, null, tint = Colores.TextoGris, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 11.sp, color = Colores.TextoGris)
            Text(text = valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaAjustes() {
    PantallaAjustes(onCambiarUbicacion = {})
}
