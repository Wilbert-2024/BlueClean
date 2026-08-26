package com.example.afinal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores
import com.example.afinal.ui.theme.FinalTheme

enum class EstadoPunto { COMPLETADO, ACTUAL, PROXIMO, DESTINO }

data class PuntoRuta(val numero: Int, val nombre: String, val estado: EstadoPunto)

@Composable
fun LugaresRuta(onRegresar: () -> Unit) {
    val puntos = listOf(
        PuntoRuta(1, "San Pedro", EstadoPunto.COMPLETADO),
        PuntoRuta(2, "Barrio El Centro", EstadoPunto.COMPLETADO),
        PuntoRuta(3, "Calle Principal", EstadoPunto.COMPLETADO),
        PuntoRuta(4, "San Pedro Sur", EstadoPunto.ACTUAL),
        PuntoRuta(5, "Urbanización Los Pinos", EstadoPunto.PROXIMO),
        PuntoRuta(6, "Barrio La Esperanza", EstadoPunto.PROXIMO),
        PuntoRuta(7, "Sector La Ceiba", EstadoPunto.PROXIMO),
        PuntoRuta(8, "Loma Fresca", EstadoPunto.DESTINO)
    )

    BackHandler { onRegresar() }

    Scaffold(
        topBar = {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                IconButton(onClick = onRegresar, Modifier.background(Colores.VerdeFondoSuave, CircleShape).size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Colores.VerdePrincipal)
                }
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lugares de la ruta", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Colores.NegroElegante)
                    Text("Camión #01", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePrincipal)
                }
            }
        },
        containerColor = Colores.GrisFondoPantalla
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            itemsIndexed(puntos) { index, punto ->
                FilaPuntoRuta(punto, esUltimo = index == puntos.size - 1)
            }
        }
    }
}

@Composable
fun FilaPuntoRuta(punto: PuntoRuta, esUltimo: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Indicador de Línea y Círculo
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            val colorEje = if (punto.estado == EstadoPunto.COMPLETADO || punto.estado == EstadoPunto.ACTUAL) Colores.VerdePrincipal else Colores.GrisBorde
            
            // Círculo/Icono del Estado
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                when (punto.estado) {
                    EstadoPunto.COMPLETADO -> Box(Modifier.size(24.dp).background(Colores.VerdePrincipal, CircleShape), Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    EstadoPunto.ACTUAL -> Box(Modifier.size(40.dp).border(2.dp, Colores.VerdePrincipal, CircleShape).background(Color.White, CircleShape), Alignment.Center) { Icon(Icons.Default.LocalShipping, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(20.dp)) }
                    else -> Box(Modifier.size(20.dp).border(2.dp, Colores.GrisBorde, CircleShape).background(Color.White, CircleShape))
                }
            }
            
            // Línea Conectora
            if (!esUltimo) {
                val esPunteada = punto.estado == EstadoPunto.ACTUAL || punto.estado == EstadoPunto.PROXIMO
                Canvas(Modifier.weight(1f).width(2.dp)) {
                    drawLine(
                        color = if (punto.estado == EstadoPunto.COMPLETADO) Colores.VerdePrincipal else Colores.GrisBorde,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 6f,
                        pathEffect = if (esPunteada) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Tarjeta Informativa
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // Número en cuadro suave
                Box(Modifier.size(36.dp).background(Colores.VerdeFondoSuave, RoundedCornerShape(8.dp)), Alignment.Center) {
                    Text(punto.numero.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePrincipal)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(punto.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.NegroElegante)
                    val textoEstado = when(punto.estado) {
                        EstadoPunto.COMPLETADO -> "Completado"
                        EstadoPunto.ACTUAL -> "Actual"
                        EstadoPunto.PROXIMO -> "Próximo"
                        EstadoPunto.DESTINO -> "Destino"
                    }
                    val colorEstado = if (punto.estado == EstadoPunto.ACTUAL || punto.estado == EstadoPunto.COMPLETADO) Colores.VerdePrincipal else Colores.TextoGris
                    Text(textoEstado, fontSize = 13.sp, color = colorEstado)
                }
                
                if (punto.estado == EstadoPunto.ACTUAL) {
                    Box(Modifier.clip(CircleShape).background(Colores.VerdeFondoSuave).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("En recorrido", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePrincipal)
                    }
                }
                
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Colores.GrisBorde)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLugaresRuta() = FinalTheme { LugaresRuta {} }
