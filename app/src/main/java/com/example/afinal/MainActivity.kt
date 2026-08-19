package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.datos.mensajeria.PantallaMensajeGlobal
import com.example.afinal.ui.theme.FinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalTheme {
                val controladorNav = rememberNavController()
                
                // --- CAMBIO PARA PROGRAMADOR: Verificar si ya existen datos guardados ---
                val existeSesion = datosEnMemoria.existe(this)
                val destinoInicial = if (existeSesion) "menu_principal" else "seleccion_barrio"

                Box(Modifier.fillMaxSize()){
                   NavHost(
                       navController= controladorNav,
                       startDestination = destinoInicial
                   ){
                       composable("seleccion_barrio"){
                           PantallaSeleccionBarrio(
                               onContinuarClick = {
                                   controladorNav.navigate("menu_principal") {
                                       popUpTo("seleccion_barrio") { inclusive = true }
                                   }
                               },
                               // --- CAMBIO: Si el usuario pulsa atrás, lo mandamos al menú principal (Inicio) ---
                               onAtrasClick = if (controladorNav.previousBackStackEntry != null) {
                                   {
                                       controladorNav.navigate("menu_principal") {
                                           // Limpiamos el historial para que el menú sea la base de nuevo
                                           popUpTo("menu_principal") { inclusive = true }
                                       }
                                   }
                               } else null
                           )
                       }
                       composable("menu_principal"){ 
                           MenuPrincipal(
                               onRegresarAlInicio = {
                                   // --- CAMBIO: Navegamos sin borrar el menú para poder volver atrás si se arrepiente ---
                                   controladorNav.navigate("seleccion_barrio")
                               }
                           )
                       }


                   }


                    PantallaMensajeGlobal()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinalTheme {
        Greeting("Android")
    }
}

