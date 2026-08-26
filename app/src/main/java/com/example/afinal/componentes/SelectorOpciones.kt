package com.example.afinal.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorOpciones(
    titulo: String,
    lista: List<String>,
    mostrar: Boolean,
    onDismiss: () -> Unit,
    onSeleccion: (String) -> Unit
) {
    if (mostrar) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn {
                    items(lista) { opcion ->
                        ListItem(
                            headlineContent = { Text(opcion) },
                            modifier = Modifier.clickable {
                                onSeleccion(opcion)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}