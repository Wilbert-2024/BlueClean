package com.example.afinal.componentes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorImagen(mostrarDialogo: Boolean, onDismiss: () -> Unit, onImagenSeleccionada: (Uri) -> Unit) {
    val context = LocalContext.current
    var uriTemporal by remember { mutableStateOf<Uri?>(null) }

    fun crearUriTemporal(): Uri {
        val file = File(context.cacheDir, "images").apply { mkdirs() }
        val imageFile = File(file, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { onImagenSeleccionada(it) }; onDismiss() }

    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { if (it) { uriTemporal?.let { uri -> onImagenSeleccionada(uri) } }; onDismiss() }

    val launcherPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) { val uri = crearUriTemporal(); uriTemporal = uri; launcherCamara.launch(uri) } }

    if (mostrarDialogo) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Seleccionar imagen", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Cámara") },
                    leadingContent = { Icon(Icons.Default.Camera, null) },
                    modifier = Modifier.clickable {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val uri = crearUriTemporal(); uriTemporal = uri; launcherCamara.launch(uri)
                        } else { launcherPermiso.launch(Manifest.permission.CAMERA) }
                    }
                )
                ListItem(
                    headlineContent = { Text("Galería") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable { launcherGaleria.launch("image/*") }
                )
            }
        }
    }
}