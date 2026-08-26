package com.example.afinal.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.afinal.datos.Colores

@Composable
fun SelectorOpciones(  titulo: String,lista: List<String>, seleccionado: String = "",   mostrar: Boolean,
    onDismiss: () -> Unit,
    onSeleccion: (String) -> Unit
) {
    if (mostrar) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdePrincipal, modifier = Modifier.padding(bottom = 16.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                        items(lista) { opcion ->
                            val esSeleccionado = opcion == seleccionado
                            val colorFondo = if (esSeleccionado) Colores.VerdePrincipal else Colores.VerdeFondoSuave
                            val colorTexto = if (esSeleccionado) Color.White else Colores.VerdePillBarrio

                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                    .background(colorFondo)
                                    .clickable { onSeleccion(opcion); onDismiss() }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(opcion, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorTexto)
                                if (esSeleccionado) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                        Text("CANCELAR", color = Colores.TextoGris, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}