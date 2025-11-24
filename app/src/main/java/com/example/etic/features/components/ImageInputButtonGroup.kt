package com.example.etic.features.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Grupo estándar de entrada para imágenes utilizado en baseline y problemas.
 * Replica el input-group de HTML:
 *  [TextField] [↑/↓ (vertical)] [...] [folder] [camera]
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageInputButtonGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    textFieldMinWidth: Dp = 0.dp,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDotsClick: (() -> Unit)? = null,
    onFolderClick: (() -> Unit)? = null,
    onCameraClick: (() -> Unit)? = null
) {
    // Tamaños “compactos”
    val smallIconButtonSize = 26.dp
    val smallIconSize = 16.dp
    val dotsButtonWidth = 28.dp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Campo de texto
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = textFieldMinWidth),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        fontSize = 12.sp
                    )
                    if (isRequired) {
                        Text(" *", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            singleLine = true
        )

        // Botones ↑ / ↓ en columna, compactos
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onMoveUp?.invoke() },
                    enabled = enabled && onMoveUp != null,
                    modifier = Modifier.size(smallIconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Imagen anterior",
                        modifier = Modifier.size(smallIconSize)
                    )
                }
                IconButton(
                    onClick = { onMoveDown?.invoke() },
                    enabled = enabled && onMoveDown != null,
                    modifier = Modifier.size(smallIconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Imagen siguiente",
                        modifier = Modifier.size(smallIconSize)
                    )
                }
            }

            // Botón "..." tipo cuadradito pequeño
            OutlinedButton(
                onClick = { onDotsClick?.invoke() },
                enabled = enabled && onDotsClick != null,
                modifier = Modifier
                    .width(dotsButtonWidth)
                    .height(smallIconButtonSize),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "...",
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            // Botón carpeta (icono solo, compacto)
            IconButton(
                onClick = { onFolderClick?.invoke() },
                enabled = enabled && onFolderClick != null,
                modifier = Modifier.size(smallIconButtonSize)
            ) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = "Abrir carpeta",
                    modifier = Modifier.size(smallIconSize)
                )
            }

            // Botón cámara (icono solo, compacto)
            IconButton(
                onClick = { onCameraClick?.invoke() },
                enabled = enabled && onCameraClick != null,
                modifier = Modifier.size(smallIconButtonSize)
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = "Abrir cámara",
                    modifier = Modifier.size(smallIconSize)
                )
            }
        }
    }
}
