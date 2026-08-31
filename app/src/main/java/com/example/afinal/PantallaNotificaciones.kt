package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.DB.modal.Notificacion_Modal
import com.example.afinal.DB.vistaModal.Notificacion_vistaModal
import com.example.afinal.datos.Colores
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaNotificaciones() {
    val context = LocalContext.current
    val vm = remember { Notificacion_vistaModal() }

    LaunchedEffect(Unit) {
        vm.cargarNotificaciones(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(Colores.GrisFondo)) {
        // Encabezado sólido
        Box(modifier = Modifier.fillMaxWidth().background(Colores.VerdePrincipal).statusBarsPadding().padding(vertical = 20.dp)) {
            Text(text = "Avisos y Notificaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        if (vm.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colores.VerdePrincipal)
            }
        } else if (vm.listaNotificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes avisos nuevos", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.listaNotificaciones) { notificacion ->
                    TarjetaNotificacion(notificacion)
                }
            }
        }
    }
}

@Composable
fun TarjetaNotificacion(datos: Notificacion_Modal.Datos) {
    val formato = SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "NI"))
    val fechaStr = datos.Fecha_Hora?.toDate()?.let { formato.format(it) } ?: "Reciente"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(40.dp).background(Colores.VerdeClaro, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.NotificationsActive, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = datos.Titulo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Colores.TextoOscuro)
                }
                Text(text = fechaStr, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                Text(text = datos.Mensaje, fontSize = 14.sp, color = Colores.TextoGris, lineHeight = 20.sp)
            }
        }
    }
}
