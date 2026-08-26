package com.example.afinal.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.afinal.datos.Colores

data class CategoriaIncidencia(val nombre: String, val icono: ImageVector)

object ListadoCategorias {
    val lista = listOf(
        CategoriaIncidencia("Camión ya pasó", Icons.Default.LocalShipping),
        CategoriaIncidencia("Basura acumulada", Icons.Default.Delete),
        CategoriaIncidencia("Contenedor dañado", Icons.Default.DeleteForever),
        CategoriaIncidencia("Contenedor lleno", Icons.Default.DeleteSweep),
        CategoriaIncidencia("Animal muerto", Icons.Default.Pets),
        CategoriaIncidencia("Escombros", Icons.Default.Foundation),
        CategoriaIncidencia("Quema de basura", Icons.Default.Whatshot),
        CategoriaIncidencia("Otro", Icons.Default.MoreHoriz)
    )
}

@Composable
fun SelectorCategorias(
    mostrar: Boolean,
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
                    Text("¿Qué deseas reportar?", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdePrincipal, modifier = Modifier.padding(bottom = 20.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 450.dp)
                    ) {
                        items(ListadoCategorias.lista) { categoria ->
                            CardCategoria(categoria) {
                                onSeleccion(categoria.nombre)
                                onDismiss()
                            }
                        }
                    }
                    
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(top = 16.dp)) {
                        Text("CANCELAR", color = Colores.TextoGris, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CardCategoria(categoria: CategoriaIncidencia, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(20.dp))
            .background(Colores.VerdeFondoSuave)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(categoria.icono, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(categoria.nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePillBarrio, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}