package com.example.afinal

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.afinal.componentes.SelectorImagen
import com.example.afinal.datos.Colores

@Composable
fun Denuncia() {
    var tipo by remember { mutableStateOf("") }
    var barrio by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarMenuImagen by remember { mutableStateOf(false) }

    SelectorImagen(
        mostrarDialogo = mostrarMenuImagen,
        onDismiss = { mostrarMenuImagen = false },
        onImagenSeleccionada = { imagenUri = it }
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Reportar Incidencia", Modifier.weight(1f), color = Colores.VerdePrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Icon(Icons.Default.Notifications, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(24.dp))
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text("Ayuda a reportar puntos con basura para que la alcaldía tome acciones", fontSize = 14.sp, color = Colores.TextoGris, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))

            // Cuadro de imagen
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp).clickable { mostrarMenuImagen = true },
                contentAlignment = Alignment.Center
            ) {
                if (imagenUri == null) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawRoundRect(color = Color(0xFF4CAF50), style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = CornerRadius(16.dp.toPx()))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(80.dp).background(Colores.VerdeClaro.copy(0.2f), CircleShape), Alignment.Center) { 
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(40.dp), tint = Colores.VerdePrincipal) 
                        }
                        Text("Agrega una foto", Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold, color = Colores.TextoOscuro)
                        Text("Toma o selecciona una imagen del lugar", fontSize = 12.sp, color = Colores.TextoGris)
                    }
                } else {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Botón para cambiar foto
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopEnd) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.background(Colores.VerdePrincipal, CircleShape).padding(4.dp).size(20.dp))
                    }
                }
            }

            CampoSeleccion("¿Que deseas reportar?", tipo, "Camión ya pasó")
            CampoSeleccion("Selecciona el Barrio", barrio, "Santa Rosa")
            CampoTexto("Dirección Exacta", direccion, "Ej: frente al Colegio primaria") { direccion = it }
            CampoTexto("Descripción", descripcion, "Agrega mas detalle del problema.......", true) { descripcion = it }

            Button(onClick = {}, Modifier.fillMaxWidth().padding(vertical = 24.dp).height(54.dp), shape = RoundedCornerShape(27.dp), colors = ButtonDefaults.buttonColors(Colores.VerdePrincipal)) {
                Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Enviar Reporte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CampoSeleccion(label: String, valor: String, hint: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TextoOscuro, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(valor, {}, Modifier.fillMaxWidth(), placeholder = { Text(hint, color = Color.Gray) }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Colores.GrisBorde), readOnly = true)
    }
}

@Composable
fun CampoTexto(label: String, valor: String, hint: String, largo: Boolean = false, onCambio: (String) -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TextoOscuro, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(valor, onCambio, Modifier.fillMaxWidth().then(if(largo) Modifier.height(110.dp) else Modifier), placeholder = { Text(hint, color = Color.Gray) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Colores.GrisBorde))
    }
}

@Preview(showBackground = true) @Composable fun DenunciaPreview() = Denuncia()
