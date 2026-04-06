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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
                .fillMaxWidth(0.86f)
                .fillMaxHeight(0.86f),
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
                        text = "No hay acceso a la carpeta de imagenes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (imageFiles.isEmpty()) {
                    Text(
                        text = "No hay imagenes disponibles en esta carpeta.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        columns = GridCells.Adaptive(minSize = 170.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(imageFiles, key = { it.uri.toString() }) { file ->
                            ImageGridItem(
                                file = file,
                                selected = file.name.orEmpty().equals(selectedName, ignoreCase = true),
                                onClick = { selectedName = file.name.orEmpty() }
                            )
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

@Composable
private fun ImageGridItem(
    file: DocumentFile,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val fileName = file.name.orEmpty()
    val thumbnail by produceState<Bitmap?>(initialValue = null, file) {
        value = loadBitmapFromDocument(context, file)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.18f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = fileName,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
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
