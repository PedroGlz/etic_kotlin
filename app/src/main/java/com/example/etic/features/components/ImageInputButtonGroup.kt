package com.example.etic.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Grupo estándar de entrada para imágenes utilizado en baseline y problemas.
 * Incluye campo de texto más botones de navegación (↑/↓), acción “...”,
 * explorador de carpetas y acceso rápido a la cámara del dispositivo.
 */
@Composable
fun ImageInputButtonGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDotsClick: (() -> Unit)? = null,
    onFolderClick: (() -> Unit)? = null,
    onCameraClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label)
                    if (isRequired) {
                        Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
        IconButton(
            onClick = { onMoveUp?.invoke() },
            enabled = enabled && onMoveUp != null
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Imagen anterior")
        }
        IconButton(
            onClick = { onMoveDown?.invoke() },
            enabled = enabled && onMoveDown != null
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Imagen siguiente")
        }
        OutlinedButton(
            onClick = { onDotsClick?.invoke() },
            enabled = enabled && onDotsClick != null,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("...")
        }
        IconButton(
            onClick = { onFolderClick?.invoke() },
            enabled = enabled && onFolderClick != null
        ) {
            Icon(Icons.Outlined.FolderOpen, contentDescription = "Abrir carpeta")
        }
        IconButton(
            onClick = { onCameraClick?.invoke() },
            enabled = enabled && onCameraClick != null
        ) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = "Abrir cámara")
        }
    }
}
