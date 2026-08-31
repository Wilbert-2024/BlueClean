package com.example.afinal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.afinal.DB.vistaModal.ConfiguracionInicial_vistaModal
import com.example.afinal.componentes.SelectorOpciones
import com.example.afinal.datos.Colores
import com.example.afinal.datos.ListadoBarrios
import com.example.afinal.datos.mensajeria.MensajesFlotantes

@Composable
fun PantallaSeleccionBarrio( 
    onContinuarClick: () -> Unit,
    onAtrasClick: (() -> Unit)? = null
) {
    if (onAtrasClick != null) { BackHandler { onAtrasClick() } }

    val context = LocalContext.current
    val vm = remember { ConfiguracionInicial_vistaModal() }
    val controladorTeclado = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        vm.cargarDatosExistentes(context)
    }

    // Selector de lugares de referencia flotante
    SelectorOpciones(
        titulo = "Lugar más cercano",
        lista = vm.listaLugares.map { it.Nombre },
        seleccionado = vm.lugarSeleccionado,
        mostrar = vm.mostrarSelectorLugares,
        onDismiss = { vm.mostrarSelectorLugares = false },
        onSeleccion = { vm.seleccionarLugar(it) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Colores.GrisFondo).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(text = "Selecciona tu barrio", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Colores.VerdePrincipal)
            Text(text = "Ingresa tu nombre y elige el barrio donde vives para mostrarte el servicio de recolección.", fontSize = 14.sp, color = Colores.TextoGris, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

            Text(text = "Tu nombre", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            OutlinedTextField(
                value = vm.nombreUsuario,
                onValueChange = { vm.nombreUsuario = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                placeholder = { Text("Escribe tu nombre aquí", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
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

            Text(text = "Elige tu barrio", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Colores.TextoOscuro, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp)).background(Colores.BlancoTarjeta).padding(8.dp)) {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ListadoBarrios.lista) { barrio ->
                        ItemBarrio(
                            nombre = barrio,
                            estaSeleccionado = barrio == vm.barrioSeleccionado,
                            alHacerClic = { vm.seleccionarBarrio(barrio) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { vm.finalizarConfiguracion(context, onContinuarClick) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !vm.estaCargando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colores.VerdePrincipal),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (vm.estaCargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "CONTINUAR", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (vm.estaCargando) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colores.VerdePrincipal)
            }
        }
    }
}

@Composable
fun ItemBarrio(nombre: String, estaSeleccionado: Boolean, alHacerClic: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (estaSeleccionado) Colores.VerdeClaro else Color.Transparent)
            .clickable { alHacerClic() }.padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, null, tint = if (estaSeleccionado) Colores.VerdeSecundario else Colores.TextoGris, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = nombre, fontSize = 16.sp, fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal, color = if (estaSeleccionado) Colores.VerdeSecundario else Colores.TextoOscuro)
        }
        if (estaSeleccionado) {
            Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50)).background(Colores.VerdeSecundario), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaSeleccionBarrio() {
    PantallaSeleccionBarrio(onContinuarClick = {})
}
