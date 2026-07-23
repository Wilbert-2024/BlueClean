package com.example.afinal.datos.mensajeria

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.datos.Colores

object MensajesFlotantes {

    @Composable
    fun Dialogo( mostrarMensaje: Boolean,   nombreUsuario: String,  barrioSeleccionado: String,  alCerrar: () -> Unit  )
    {

        if (!mostrarMensaje) return

        val nombreAUtilizar = if (nombreUsuario.isBlank()) "Vecino/a" else nombreUsuario


        AlertDialog(
            onDismissRequest = { alCerrar() },
            title = {
                Text(
                    text = "¡Información Guardada!",
                    fontWeight = FontWeight.Companion.Bold,
                    color = Colores.VerdePrincipal
                )
            },
            text = {
                Text(
                    text = "Hola $nombreAUtilizar.\n Has seleccionado el barrio $barrioSeleccionado .",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { alCerrar() }
                ) {
                    Text(
                        "Entendido",
                        color = Colores.VerdePrincipal,
                        fontWeight = FontWeight.Companion.Bold
                    )
                }
            },
            containerColor = Color.Companion.White,
            shape = RoundedCornerShape(16.dp)
        )
    }


}