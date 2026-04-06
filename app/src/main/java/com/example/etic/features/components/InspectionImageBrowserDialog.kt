package com.example.etic.features.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.example.etic.core.saf.SafEticManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InspectionImageBrowserDialog(
    title: String,
    rootTreeUri: Uri?,
    inspectionNumber: String?,
    initialSelection: String,
    useClientFolder: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val safManager = remember { SafEticManager() }
    val folder = remember(rootTreeUri, inspectionNumber, useClientFolder) {
        if (rootTreeUri == null) {
            null
        } else if (useClientFolder) {
            safManager.getClientesDir(context, rootTreeUri)
        } else {
            inspectionNumber?.takeIf { it.isNotBlank() }?.let {
                safManager.getImagesDir(context, rootTreeUri, it)
            }
        }
    }
    val imageFiles = remember(folder) {
        safManager.listFiles(folder).filter(::isImageDocument)
    }
    var selectedName by remember(initialSelection, imageFiles) {
        mutableStateOf(
            imageFiles.firstOrNull { it.name.equals(initialSelection.trim(), ignoreCase = true) }?.name
                ?: imageFiles.firstOrNull()?.name
                ?: ""
        )
    }
    val selectedFile = remember(selectedName, imageFiles) {
        imageFiles.firstOrNull { it.name.equals(selectedName, ignoreCase = true) }
    }
    val previewBitmap by produceState<Bitmap?>(initialValue = null, selectedFile) {
        value = loadBitmapFromDocument(context, selectedFile)
    }

    LaunchedEffect(imageFiles, selectedName) {
        if (imageFiles.isNotEmpty() && selectedName.isBlank()) {
            selectedName = imageFiles.firstNotNullOfOrNull { it.name }.orEmpty()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .fillMaxHeight(0.84f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (folder == null) {
                    Text(
                        text = "No hay acceso a la carpeta de imágenes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (imageFiles.isEmpty()) {
                    Text(
                        text = "No hay imágenes disponibles en esta carpeta.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.35f)
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (previewBitmap != null) {
                                    Image(
                                        bitmap = previewBitmap!!.asImageBitmap(),
                                        contentDescription = selectedName,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No se pudo cargar la vista previa.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = selectedName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            imageFiles.forEach { file ->
                                val fileName = file.name.orEmpty()
                                val selected = fileName.equals(selectedName, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedName = fileName }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = fileName,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = selectedName.isNotBlank(),
                        onClick = { onSelect(selectedName) }
                    ) {
                        Text("Seleccionar")
                    }
                }
            }
        }
    }
}

private fun isImageDocument(file: DocumentFile): Boolean {
    val name = file.name.orEmpty().lowercase()
    return file.isFile && (
        file.type?.startsWith("image/") == true ||
            name.endsWith(".jpg") ||
            name.endsWith(".jpeg") ||
            name.endsWith(".png") ||
            name.endsWith(".webp") ||
            name.endsWith(".bmp")
        )
}

private suspend fun loadBitmapFromDocument(
    context: Context,
    file: DocumentFile?
): Bitmap? = withContext(Dispatchers.IO) {
    if (file == null) return@withContext null
    context.contentResolver.openInputStream(file.uri)?.use(BitmapFactory::decodeStream)
}
