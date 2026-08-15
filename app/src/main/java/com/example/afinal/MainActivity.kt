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
import com.example.afinal.datos.mensajeria.PantallaMensajeGlobal
import com.example.afinal.ui.theme.FinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalTheme {
                val controladorNav = rememberNavController()
                Box(Modifier.fillMaxSize()){
                   NavHost(
                       navController= controladorNav,
                       startDestination = "seleccion_barrio"
                   ){
                       composable("seleccion_barrio"){
                           PantallaSeleccionBarrio(
                               onContinuarClick = {
                                   controladorNav.navigate("menu_principal") {
                                       popUpTo("seleccion_barrio") { inclusive = true }
                                   }
                               }
                           )
                       }
                       composable("menu_principal"){ MenuPrincipal()}


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
