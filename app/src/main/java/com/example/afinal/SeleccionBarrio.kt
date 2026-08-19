package com.example.afinal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place // <--- Icono de ubicación importado
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores
import com.example.afinal.datos.ListadoBarrios
import com.example.afinal.datos.mensajeria.MensajesFlotantes
import com.example.afinal.datos.guardarDatosTelefono.datosEnMemoria
import com.example.afinal.datos.mensajeria.Mensajeria
import kotlinx.coroutines.launch

// Colores del tema


@Composable
fun PantallaSeleccionBarrio( onContinuarClick: () -> Unit ) {
    // Estados
    var nombreUsuario by remember { mutableStateOf("") }
    var barrioSeleccionado by remember { mutableStateOf("") }
    val controladorTeclado = LocalSoftwareKeyboardController.current
    val corrutina = rememberCoroutineScope()
    val context = LocalContext.current
    var mostrarMensaje by remember { mutableStateOf(false) }

    // --- CAMBIO PARA PROGRAMADOR: Estado para el círculo de carga ---
    var estaCargando by remember { mutableStateOf(false) }

    // Lista de barrios
    val listaBarrios = ListadoBarrios.lista

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier .fillMaxSize() .background(Colores.GrisFondo).statusBarsPadding() .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
        // Título principal
        Text(  text = "Selecciona tu barrio", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,  color = Colores.VerdePrincipal )

        Text( text = "Ingresa tu nombre y elige el barrio donde vives para mostrarte el servicio de recolección.",
            fontSize = 14.sp, color = Colores.TextoGris, lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Text(text = "Tu nombre", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // donde se ingresa el nombre del user
        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = { nombreUsuario = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
            placeholder = { Text("Escribe tu nombre aquí", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.BlancoTarjeta,
                unfocusedContainerColor = Colores.BlancoTarjeta,
                focusedIndicatorColor = Colores.VerdePrincipal,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Colores.VerdePrincipal,
                focusedTextColor = Colores.TextoOscuro,
                unfocusedTextColor = Colores.TextoOscuro
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { controladorTeclado?.hide() })
        )


        Text( text = "Elige tu barrio",  fontSize = 13.sp,  fontWeight = FontWeight.SemiBold,
            color = Colores.TextoOscuro,  modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Contenedor de la Lista
        Box(
            modifier = Modifier.fillMaxWidth() .weight(1f) .clip(RoundedCornerShape(16.dp))
                .background(Colores.BlancoTarjeta).padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listaBarrios) { barrio ->
                    ItemBarrio(
                        nombre = barrio,
                        estaSeleccionado = barrio == barrioSeleccionado,
                        alHacerClic = { barrioSeleccionado = barrio }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

// Botón Continuar
        Button(
            onClick = {
                val verificar = validarDatosObtenidos(nombreUsuario, barrioSeleccionado)

                if (verificar) {
                    corrutina.launch {
                        // --- CAMBIO PARA PROGRAMADOR: Activamos carga antes de guardar ---
                        estaCargando = true
                        
                        val guardadoExitoso = datosEnMemoria.guardaDatos(context, nombreUsuario, barrioSeleccionado)
                        
                        if (guardadoExitoso) {
                            mostrarMensaje = true
                            onContinuarClick()
                        } else {
                            Mensajeria.error("Error técnico: No se pudieron guardar los datos en el dispositivo")
                        }
                        
                        // --- CAMBIO PARA PROGRAMADOR: Desactivamos carga al finalizar ---
                        estaCargando = false
                    }
                }
            },
            modifier = Modifier .fillMaxWidth() .height(56.dp),
            enabled = !estaCargando, // Bloqueamos el botón si está cargando
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdePrincipal),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (estaCargando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text( text = "CONTINUAR", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White )
            }
        }
    }
    
    // --- CAMBIO PARA PROGRAMADOR: Overlay de carga que bloquea la pantalla completa si es necesario ---
    if (estaCargando) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Colores.VerdePrincipal)
        }
    }

    MensajesFlotantes.Dialogo(mostrarMensaje, nombreUsuario, barrioSeleccionado) { mostrarMensaje = false }
    }
}

fun validarDatosObtenidos(NombreUser: String, BarrioSeleccionado: String): Boolean {
    return try {
        if (NombreUser.isBlank() || BarrioSeleccionado.isBlank()) {
            Mensajeria.error("Debes poner tu nombre y seleccionar tu barrio")
            false
        } else {
            true
        }
    } catch (e: Exception) {
        false
    }
}


@Composable
fun ItemBarrio( nombre: String, estaSeleccionado: Boolean, alHacerClic: () -> Unit ) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (estaSeleccionado) Colores.VerdeClaro else Color.Transparent)
            .clickable { alHacerClic() } .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lado izquierdo: Icono de ubicación + Nombre del barrio
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place, contentDescription = "Ubicación",
                // El icono se pone verde si está seleccionado, gris si no
                tint = if (estaSeleccionado) Colores.VerdeSecundario else Colores.TextoGris,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp)) // Espacio entre el icono y el texto
            Text(  text = nombre, fontSize = 16.sp,fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal,
                color = if (estaSeleccionado) Colores.VerdeSecundario else Colores.TextoOscuro
            )
        }

        if (estaSeleccionado) {
            Box(
                modifier = Modifier.size(24.dp) .clip(RoundedCornerShape(50)).background(Colores.VerdeSecundario),
                contentAlignment = Alignment.Center
            ) {
                Icon(  imageVector = Icons.Default.Check, contentDescription = "Seleccionado",
                    tint = Color.White, modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaSeleccionBarrio() {
    PantallaSeleccionBarrio( onContinuarClick = {})
}

