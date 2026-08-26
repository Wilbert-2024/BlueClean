package com.example.afinal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        PuntoRuta(2, "La Morenita", EstadoPunto.COMPLETADO),
        PuntoRuta(3, "19 de Julio", EstadoPunto.PROXIMO),
        PuntoRuta(4, "Loma Fresca", EstadoPunto.DESTINO)
    )

    BackHandler { onRegresar() }

    Scaffold(
        topBar = {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                IconButton(onClick = onRegresar, Modifier.background(Colores.VerdeFondoSuave, CircleShape).size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Colores.VerdePrincipal)
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
            Text( text = "RECORRIDO", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdeSecundario,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(puntos) { index, punto ->
                    FilaPuntoLimpia( punto = punto, esPrimero = index == 0, esUltimo = index == puntos.size - 1  )
                }
            }
        }
    }
}

@Composable
fun FilaPuntoLimpia(punto: PuntoRuta, esPrimero: Boolean, esUltimo: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de Mira/Ubicación
            Icon(
                imageVector = Icons.Default.MyLocation, contentDescription = null,
                tint = Colores.VerdeVibrante.copy(0.7f), modifier = Modifier.size(24.dp)
            )
            
            Spacer(Modifier.width(20.dp))
            
            Column {
                Text(text = punto.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = Colores.TextoOscuro
                )
                
                val subtitulo = when {
                    esPrimero -> "Punto de partida"
                    esUltimo -> "Punto final"
                    else -> ""
                }
                
                if (subtitulo.isNotEmpty()) {
                    Text( text = subtitulo, fontSize = 14.sp,color = Colores.TextoGris )
                }
            }
        }
        
        // Línea de separación (excepto después del último)
        if (!esUltimo) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 44.dp),
                thickness = 0.5.dp,
                color = Colores.GrisSeparador
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLugaresRuta() = FinalTheme { LugaresRuta {} }
