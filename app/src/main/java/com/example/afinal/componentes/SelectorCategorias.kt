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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorCategorias(
    mostrar: Boolean,
    onDismiss: () -> Unit,
    onSeleccion: (String) -> Unit
) {
    if (mostrar) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).clip(CircleShape).background(Colores.GrisBorde)) }
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("¿Qué deseas reportar?", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdePrincipal, modifier = Modifier.padding(bottom = 20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 500.dp)
                ) {
                    items(ListadoCategorias.lista) { categoria ->
                        CardCategoria(categoria) {
                            onSeleccion(categoria.nombre)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardCategoria(categoria: CategoriaIncidencia, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(20.dp))
            .background(Colores.VerdeFondoSuave)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(categoria.icono, null, tint = Colores.VerdePrincipal, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(categoria.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Colores.VerdePillBarrio, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}