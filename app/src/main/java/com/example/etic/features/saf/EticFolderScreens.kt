package com.example.etic.features.saf

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import com.example.etic.core.saf.SafEticManager
import kotlinx.coroutines.launch

enum class EticFolderType(val label: String) {
    Images("Carpeta Imagenes"),
    Reports("Carpeta Archivos")
}

@Composable
fun FolderPickerScreen(
    onTreeUriPicked: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
        scope.launch { onTreeUriPicked(uri) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Seleccionar carpeta ETIC", style = MaterialTheme.typography.titleMedium)
        Text(
            "Selecciona una carpeta ra\u00edz para crear ETIC/Inspecciones y mantener la estructura SAF.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = { launcher.launch(null) }) {
            Text("Seleccionar carpeta ETIC")
        }
    }
}

@Composable
fun EticFolderShortcutScreen(
    folderType: EticFolderType,
    rootTreeUri: Uri?,
    inspectionNumero: String?,
    onPickRoot: (Uri) -> Unit,
    manager: SafEticManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshTick by remember { mutableStateOf(0) }
    var renameTarget by remember { mutableStateOf<DocumentFile?>(null) }
    var deleteTarget by remember { mutableStateOf<DocumentFile?>(null) }
    var newName by remember { mutableStateOf("") }

    if (rootTreeUri == null) {
        FolderPickerScreen(onTreeUriPicked = onPickRoot, modifier = modifier)
        return
    }

    if (inspectionNumero.isNullOrBlank()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay inspecci\u00f3n activa.")
        }
        return
    }

    val files by produceState(
        initialValue = emptyList<DocumentFile>(),
        rootTreeUri,
        inspectionNumero,
        folderType,
        refreshTick
    ) {
        val dir = when (folderType) {
            EticFolderType.Images -> manager.getImagesDir(context, rootTreeUri, inspectionNumero)
            EticFolderType.Reports -> manager.getReportsDir(context, rootTreeUri, inspectionNumero)
        }
        value = manager.listFiles(dir)
    }

    val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(folderType.label, style = MaterialTheme.typography.titleMedium)
            Row {
                IconButton(onClick = { refreshTick++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refrescar")
                }
                IconButton(
                    onClick = {
                        val dir = when (folderType) {
                            EticFolderType.Images -> manager.getImagesDir(context, rootTreeUri, inspectionNumero)
                            EticFolderType.Reports -> manager.getReportsDir(context, rootTreeUri, inspectionNumero)
                        }
                        val uri = dir?.uri
                        if (uri != null) {
                            runCatching { context.startActivity(manager.openFolderIntent(uri)) }
                                .onFailure {
                                    Toast.makeText(context, "No se pudo abrir la carpeta.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = "Abrir carpeta")
                }
            }
        }

        Divider()
        Spacer(Modifier.height(8.dp))

        if (files.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin archivos")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(files, key = { _, item -> item.uri.toString() }) { index, file ->
                    val rowColor = if (index % 2 == 1) zebraColor else Color.Transparent
                    val name = file.name ?: "(Sin nombre)"
                    val mime = context.contentResolver.getType(file.uri)
                    val isImage = mime?.startsWith("image/") == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowColor)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isImage) {
                            AsyncImage(
                                model = file.uri,
                                contentDescription = name,
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    runCatching {
                                        context.startActivity(manager.openFileIntent(file.uri, mime))
                                    }.onFailure {
                                        Toast.makeText(context, "No se pudo abrir el archivo.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            Text(mime ?: "archivo", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            renameTarget = file
                            newName = name
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Renombrar")
                        }
                        IconButton(onClick = { deleteTarget = file }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                        IconButton(onClick = {
                            runCatching {
                                context.startActivity(manager.openFileIntent(file.uri, mime))
                            }.onFailure {
                                Toast.makeText(context, "No se pudo abrir el archivo.", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = "Abrir")
                        }
                    }
                    Divider()
                }
            }
        }
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        val target = renameTarget ?: return@Button
                        val ok = manager.rename(target, newName.trim())
                        if (!ok) {
                            Toast.makeText(context, "No se pudo renombrar.", Toast.LENGTH_SHORT).show()
                        }
                        renameTarget = null
                        refreshTick++
                    },
                    enabled = newName.trim().isNotEmpty()
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancelar") }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Renombrar archivo")
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true
                    )
                }
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(onClick = {
                    val target = deleteTarget ?: return@Button
                    val ok = manager.delete(target)
                    if (!ok) {
                        Toast.makeText(context, "No se pudo eliminar.", Toast.LENGTH_SHORT).show()
                    }
                    deleteTarget = null
                    refreshTick++
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            },
            text = { Text("Eliminar archivo seleccionado?") }
        )
    }
}
