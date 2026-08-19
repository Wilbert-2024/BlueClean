package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

@Composable
fun Prueba(onRegresar: () -> Unit) {
    Box(
        modifier = Modifier .fillMaxSize().background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Título fijo ahora que es una pantalla directa
            Text( text = "Pantalla de Prueba", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Colores.TextoOscuro )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // --- BOTÓN PARA REGRESAR A SELECCIÓN DE BARRIO ---
            Button(
                onClick = { onRegresar() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdePrincipal)
            ) {
                Text( text = "REGRESAR A SELECCIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPrueba() {
    Prueba(onRegresar = {})
}
