package com.example.afinal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.afinal.DB.vistaModal.Notificacion_vistaModal
import com.example.afinal.datos.Colores
import kotlinx.coroutines.delay

data class ItemMenu(val ruta: String, val icono: ImageVector, val titulo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(onRegresarAlInicio: () -> Unit) {
    val contexto = LocalContext.current
    val controladorNav = rememberNavController()
    var mostrarAvisoSinInternet by remember { mutableStateOf(false) }

    val notifVm = remember { Notificacion_vistaModal() }
    LaunchedEffect(Unit) { notifVm.cargarNotificaciones(contexto) }
    val cantidadNoLeidos = notifVm.cantidadNoLeidos

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
        ItemMenu("avisos", Icons.Default.Notifications, "Avisos"),
        ItemMenu("calendario", Icons.Default.CalendarToday, "calendario"),
        ItemMenu("Ajuste", Icons.Default.MoreHoriz, "Ajuste")
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
                                icon = {
                                    if (item.ruta == "avisos" && cantidadNoLeidos > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Color(0xFFD32F2F), contentColor = Color.White) {
                                                    Text(text = if (cantidadNoLeidos > 99) "99+" else cantidadNoLeidos.toString())
                                                }
                                            }
                                        ) {
                                            Icon(item.icono, contentDescription = item.titulo)
                                        }
                                    } else {
                                        Icon(item.icono, contentDescription = item.titulo)
                                    }
                                },
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
                val navegarAAvisos: () -> Unit = {
                    controladorNav.navigate("avisos") {
                        popUpTo(controladorNav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                composable("inicio") { 
                    Inicio(
                        onNavegarADenuncia = { controladorNav.navigate("denuncia") },
                        onNavegarAAvisos = navegarAAvisos,
                        cantidadNoLeidos = cantidadNoLeidos
                    ) 
                }
                composable("mapa") { 
                    Mapa(
                        onAtras = { 
                            controladorNav.navigate("inicio") {
                                popUpTo(controladorNav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavegarAAvisos = navegarAAvisos,
                        cantidadNoLeidos = cantidadNoLeidos
                    ) 
                }
                composable("avisos") { PantallaNotificaciones(vm = notifVm) }
                composable("calendario") { 
                    Calendario(
                        onNotificaciones = navegarAAvisos,
                        onVerAvisos = navegarAAvisos,
                        cantidadNoLeidos = cantidadNoLeidos
                    ) 
                }
                composable("Ajuste") { PantallaAjustes(onRegresarAlInicio) }
                composable("denuncia") { 
                    Denuncia(
                        onRegresar = { controladorNav.popBackStack() },
                        onNavegarAAvisos = navegarAAvisos,
                        cantidadNoLeidos = cantidadNoLeidos
                    ) 
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
