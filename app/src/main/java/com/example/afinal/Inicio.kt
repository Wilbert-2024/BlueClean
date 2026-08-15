package com.example.afinal

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

val VerdeOscuroGrad = Color(0xFF00381F)
val VerdeClaroGrad = Color(0xFF006B3C)
val VerdeLive = Color(0xFF4ADE80)
val GrisSutil = Color(0xFF8E8E93)
val NegroElegante = Color(0xFF1C1C1E)

@Composable
fun Inicio() {
    val estaEnServicio = true

    Box(
        modifier = Modifier.fillMaxSize().background(Colores.GrisFondo).verticalScroll(rememberScrollState())
    ) {

        Box(
            modifier = Modifier.fillMaxWidth().height(275.dp).clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(VerdeOscuroGrad, Colores.VerdePrincipal, VerdeClaroGrad)
                    )
                )
        )

        Column(modifier = Modifier .fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp).padding(bottom = 100.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text( text = "¡Hola, Martin!", fontSize = 22.sp, fontWeight = FontWeight.Bold,color = Color.White,letterSpacing = (-0.8).sp)

                }
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, "Notificaciones", tint = Color.White, modifier = Modifier.size(20.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(VerdeLive)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Camión Asignado",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrisSutil,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Colores.GrisFondo),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.camion),
                            contentDescription = "Camion",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Camión #01",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegroElegante,
                        letterSpacing = (-0.8).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Colores.VerdePrincipal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ruta Santa Rosa - Loma Fresca",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrisSutil
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    EstadoServicio(estaActivo = estaEnServicio)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de hora de llegadas (Con borde verde izquierdo)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    // Borde verde izquierdo
                    Box(
                        modifier = Modifier.width(6.dp).fillMaxHeight() .background(Colores.VerdePrincipal)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono de reloj a la izquierda en cuadro gris
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Colores.GrisFondo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Colores.VerdePrincipal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Textos a la derecha
                        Column {
                            Text(
                                "Llegada estimada",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = GrisSutil
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "10:15 AM",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.VerdePrincipal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Accesos rápidos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NegroElegante,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), clip = false)
                    .clickable { /* Navegar a reporte de incidencia */ },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Colores.GrisFondo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Report, "Reporte", tint = Colores.VerdePrincipal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reportar incidencia", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NegroElegante)
                        Text("Ayuda a mantener limpio tu barrio", fontSize = 12.sp, color = GrisSutil)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = GrisSutil, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun EstadoServicio(estaActivo: Boolean) {
    val colorFondo = if (estaActivo) Colores.VerdeClaro else Color(0xFFFFEBEE)
    val colorTexto = if (estaActivo) Colores.VerdePrincipal else Color(0xFFD32F2F)
    val texto = if (estaActivo) "En servicio" else "Inactivo"

    val transicionInfinita = rememberInfiniteTransition(label = "pulso")
    val alfaPulso by transicionInfinita.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Restart),
        label = "alfa"
    )
    val escalaPulso by transicionInfinita.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Restart),
        label = "escala"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (estaActivo) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .scale(escalaPulso)
                        .alpha(alfaPulso)
                        .clip(CircleShape)
                        .background(colorTexto)
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colorTexto)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorTexto)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewInicio() {
    Inicio()
}