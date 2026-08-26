package com.example.afinal.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

@Composable
fun IndicadorRuta(origen: String, destino: String, progreso: Float, estaActivo: Boolean = true) {
    val colorRecorrido = if (estaActivo) Colores.VerdePrincipal else Color.LightGray
    val colorRestante = Color.LightGray
    val opacidadIcono = if (estaActivo) 1f else 0.5f

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(origen, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (estaActivo) Color(0xFF1C1C1E) else Color.Gray)
            Text(destino, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (estaActivo) Color(0xFF1C1C1E) else Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                val width = size.width
                val progressX = width * progreso

                if (estaActivo) {
                    drawLine(color = colorRecorrido, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(progressX, 0f), strokeWidth = 6f)
                    drawLine(color = colorRestante, start = androidx.compose.ui.geometry.Offset(progressX, 0f), end = androidx.compose.ui.geometry.Offset(width, 0f), strokeWidth = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                } else {
                    drawLine(color = colorRestante, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(width, 0f), strokeWidth = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(colorRecorrido, CircleShape).border(2.dp, Color.White, CircleShape))
                Box(Modifier.weight(1f)) {
                    Box(modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(if(estaActivo) progreso else 0f).alpha(opacidadIcono)) {
                        Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = (6).dp)) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White).border(1.dp, colorRecorrido, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalShipping, null, tint = colorRecorrido, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Box(Modifier.size(12.dp).background(Color.White, CircleShape).border(2.dp, colorRestante, CircleShape))
            }
        }
    }
}