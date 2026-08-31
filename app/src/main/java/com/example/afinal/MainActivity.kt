package com.example.afinal

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import com.google.firebase.FirebaseApp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Iniciando app, SDK Version: ${Build.VERSION.SDK_INT}")
        
        // Forzar inicialización de Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "Firebase inicializado correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error inicializando Firebase: ${e.message}")
        }

        crearCanalNotificaciones()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("MainActivity", "Solicitando permiso de notificaciones...")
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            
            // Usamos un nuevo ID (v3) para forzar a Android a aplicar los cambios de sonido
            val channelId = "canal_recoleccion_v3"
            val channel = NotificationChannel(channelId, "Recordatorios de Recolección", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos importantes con sonido de alarma"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
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

