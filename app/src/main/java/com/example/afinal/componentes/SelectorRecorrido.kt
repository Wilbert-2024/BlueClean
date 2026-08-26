package com.example.afinal.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

enum class EstadoPuntoRecorrido { COMPLETADO, ACTUAL, PROXIMO }
data class PuntoRecorrido(val nombre: String, val estado: EstadoPuntoRecorrido)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorRecorrido(
    mostrar: Boolean,
    onDismiss: () -> Unit
) {
    val puntos = listOf(
        PuntoRecorrido("San Pedro", EstadoPuntoRecorrido.COMPLETADO),
        PuntoRecorrido("La Morenita", EstadoPuntoRecorrido.COMPLETADO),
        PuntoRecorrido("19 de Julio", EstadoPuntoRecorrido.ACTUAL),
        PuntoRecorrido("San Pedro Sur", EstadoPuntoRecorrido.PROXIMO),
        PuntoRecorrido("Sector La Ceiba", EstadoPuntoRecorrido.PROXIMO),
        PuntoRecorrido("Loma Fresca", EstadoPuntoRecorrido.PROXIMO)
    )

    if (mostrar) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(Colores.GrisBorde, CircleShape)) }
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("RECORRIDO", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdeSecundario)
                Text("Seguimiento de la Unidad #01", fontSize = 13.sp, color = Colores.TextoGris, modifier = Modifier.padding(bottom = 20.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                    itemsIndexed(puntos) { index, punto ->
                        FilaLineaTiempo(
                            punto = punto,
                            esPrimero = index == 0,
                            esUltimo = index == puntos.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilaLineaTiempo(punto: PuntoRecorrido, esPrimero: Boolean, esUltimo: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Columna de la Línea y el Icono
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            val colorEje = if (punto.estado == EstadoPuntoRecorrido.COMPLETADO) Colores.VerdePrincipal else Colores.GrisBorde
            
            // Icono de Estado
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                when (punto.estado) {
                    EstadoPuntoRecorrido.COMPLETADO -> Icon(Icons.Default.Check, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(20.dp))
                    EstadoPuntoRecorrido.ACTUAL -> Icon(Icons.Default.LocalShipping, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(24.dp))
                    else -> Icon(Icons.Default.MyLocation, null, tint = Colores.GrisBorde, modifier = Modifier.size(20.dp))
                }
            }

            // Línea Vertical
            if (!esUltimo) {
                val esPunteada = punto.estado != EstadoPuntoRecorrido.COMPLETADO
                Canvas(Modifier.weight(1f).width(2.dp)) {
                    drawLine(
                        color = if (punto.estado == EstadoPuntoRecorrido.COMPLETADO) Colores.VerdePrincipal else Colores.GrisBorde,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 4f,
                        pathEffect = if (esPunteada) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Contenido del Texto
        Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            Text(
                text = punto.nombre,
                fontSize = 16.sp,
                fontWeight = if (punto.estado == EstadoPuntoRecorrido.ACTUAL) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (punto.estado == EstadoPuntoRecorrido.ACTUAL) Colores.VerdePrincipal else Colores.TextoOscuro
            )
            val subtitulo = when {
                esPrimero -> "Punto de partida"
                punto.estado == EstadoPuntoRecorrido.ACTUAL -> "En recorrido"
                esUltimo -> "Punto final"
                else -> ""
            }
            if (subtitulo.isNotEmpty()) {
                Text(subtitulo, fontSize = 13.sp, color = if(punto.estado == EstadoPuntoRecorrido.ACTUAL) Colores.VerdePrincipal else Colores.TextoGris)
            }
            
            if (!esUltimo) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Colores.GrisSeparador.copy(0.5f))
            }
        }
    }
}