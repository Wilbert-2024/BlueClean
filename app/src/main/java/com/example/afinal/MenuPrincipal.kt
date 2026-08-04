package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.afinal.datos.Colores
import java.time.DayOfWeek
import java.time.LocalTime


// Modelo para los items del menú
data class ItemMenu( val ruta: String,  val icono: ImageVector,  val titulo: String )

@Composable
fun MenuPrincipal() {
    val controladorNav = rememberNavController()

    // Lista de opciones del menú inferior
    val itemsMenu = listOf(
        ItemMenu("inicio", Icons.Default.Home, "Inicio"),
        ItemMenu("mapa", Icons.Default.LocationOn, "Mapa"),
        ItemMenu("avisos", Icons.Default.ErrorOutline, "Avisos"),
        ItemMenu("calendario", Icons.Default.CalendarToday, "calendario"),
        ItemMenu("perfil", Icons.Default.MoreHoriz, "Perfil")
    )

    Scaffold(
        bottomBar = {

            NavigationBar(  containerColor = Color.White, tonalElevation = 8.dp ) {
                val navBackStackEntry by controladorNav.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route

                itemsMenu.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icono, contentDescription = item.titulo) },
                        label = { Text(text = item.titulo, fontSize = 11.sp) },
                        selected = rutaActual == item.ruta,

                        onClick = {
                            controladorNav.navigate(item.ruta) {

                                popUpTo(controladorNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Colores.VerdePrincipal,
                            selectedTextColor = Colores.VerdePrincipal,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Colores.VerdeClaro
                        )
                    )
                }
            }
        }
    ) { paddingInterno ->
        // Aquí es donde cambian las pantallas
        NavHost(
            navController = controladorNav,
            startDestination = "inicio",
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable("inicio") { Inicio()}
            composable("mapa") { PantallaDePrueba("Pantalla de Mapa") }
            composable("avisos") { PantallaDePrueba("Pantalla de Avisos") }
            composable("calendario"){Calendario( barrio = "Santa Rosa",
                diasRuta = setOf( DayOfWeek.TUESDAY, DayOfWeek.THURSDAY,  DayOfWeek.SATURDAY  ),
                horaRuta = LocalTime.of(8, 0))}

            composable("perfil") { PantallaDePrueba("Pantalla de Perfil") }
        }
    }
}

// Una pantalla temporal sencilla para probar que el menú funciona
@Composable
fun PantallaDePrueba(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoOscuro
            )
            Text( text = "¡El menú funciona correctamente!", color = Colores.TextoOscuro,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}