package com.example.afinal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ArchivoMapa.GestorPermisosMapa
import com.example.afinal.datos.Colores
import kotlinx.coroutines.delay

data class ItemMenu(val ruta: String, val icono: ImageVector, val titulo: String)

@Composable
fun MenuPrincipal(onRegresarAlInicio: () -> Unit) {
    val contexto = LocalContext.current
    val controladorNav = rememberNavController()
    var mostrarAvisoSinInternet by remember { mutableStateOf(false) }

    // Temporizador para ocultar la píldora flotante a los 3.5 segundos
    LaunchedEffect(mostrarAvisoSinInternet) {
        if (mostrarAvisoSinInternet) {
            delay(3500L)
            mostrarAvisoSinInternet = false
        }
    }

    // Lista de opciones del menú inferior
    val itemsMenu = listOf(
        ItemMenu("inicio", Icons.Default.Home, "Inicio"),
        ItemMenu("mapa", Icons.Default.LocationOn, "Mapa"),
        ItemMenu("avisos", Icons.Default.ErrorOutline, "Avisos"),
        ItemMenu("calendario", Icons.Default.CalendarToday, "calendario"),
        ItemMenu("perfil", Icons.Default.MoreHoriz, "Perfil")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by controladorNav.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route
                
                // Solo mostrar la barra de navegación si NO estamos en la pantalla de denuncia
                if (rutaActual != "denuncia") {
                    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                        itemsMenu.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icono, contentDescription = item.titulo) },
                                label = { Text(text = item.titulo, fontSize = 11.sp) },
                                selected = rutaActual == item.ruta,
                                onClick = {
                                    if (item.ruta == "mapa") {
                                        val hayInternet = GestorPermisosMapa.verificarConexionInternet(contexto)
                                        if (!hayInternet) {
                                            mostrarAvisoSinInternet = true
                                            return@NavigationBarItem
                                        }
                                    }
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
            }
        ) { paddingInterno ->
            NavHost(navController = controladorNav, startDestination = "inicio", modifier = Modifier.padding(paddingInterno)) {
                composable("inicio") { 
                    Inicio(onNavegarADenuncia = { controladorNav.navigate("denuncia") }) 
                }
                composable("mapa") { 
                    Mapa(
                        onAtras = { 
                            controladorNav.navigate("inicio") {
                                popUpTo(controladorNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
                composable("avisos") { PantallaNotificaciones() }
                composable("calendario") { Calendario() }
                composable("perfil") { Prueba(onRegresarAlInicio) }
                composable("denuncia") { 
                    Denuncia(onRegresar = { controladorNav.popBackStack() }) 
                }
            }
        }

        // --- PÍLDORA FLOTANTE VIBRANTE Y LLAMATIVA AL PRESIONAR MAPA SIN INTERNET ---
        AnimatedVisibility(
            visible = mostrarAvisoSinInternet,
            enter = fadeIn(tween(500)) + slideInVertically(animationSpec = tween(500), initialOffsetY = { -it }),
            exit = fadeOut(tween(500)) + slideOutVertically(animationSpec = tween(500), targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .clickable { mostrarAvisoSinInternet = false },
                color = Color(0xFFC02323) // Rojo Carmesí vibrante de alto impacto
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text( text = "No hay conexión a Internet", color = Color.White, fontSize = 14.sp,  fontWeight = FontWeight.ExtraBold  )
                }
            }
        }
    }
}
