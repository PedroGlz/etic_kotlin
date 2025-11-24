package com.example.etic.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Grupo estándar de entrada para imágenes utilizado en baseline y problemas.
 * Estilo tipo input-group:
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
    // Altura base del TextField
    val textFieldHeight = 56.dp  // estándar aproximado de Material3

    // Para que ↑ y ↓ juntos midan lo mismo que el TextField:
    val arrowsSpacing = 0.dp   // SIN ESPACIO ENTRE ↑ Y ↓
    val arrowButtonSize = textFieldHeight / 2   // 2 botones = altura total

    // Otros tamaños
    val smallIconSize = 16.dp
    val dotsButtonWidth = 32.dp

    // Colores botones
    val arrowBgColor: Color = MaterialTheme.colorScheme.primary
    val arrowContentColor: Color = MaterialTheme.colorScheme.onPrimary

    val dotsBgColor: Color = MaterialTheme.colorScheme.tertiary
    val dotsContentColor: Color = MaterialTheme.colorScheme.onTertiary

    val folderBgColor: Color = MaterialTheme.colorScheme.secondary
    val folderContentColor: Color = MaterialTheme.colorScheme.onSecondary

    val cameraBgColor: Color = MaterialTheme.colorScheme.error
    val cameraContentColor: Color = MaterialTheme.colorScheme.onError

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)  // pegado todo
    ) {
        // ------------------ TEXTFIELD ------------------
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = textFieldMinWidth)
                .height(textFieldHeight), // mismo alto que el grupo de botones
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 12.sp)
                    if (isRequired) {
                        Text(" *", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            singleLine = true
        )

        // Para hacer botones compactos (sin min 48dp)
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false
        ) {
            // ------------------ FLECHAS ↑ ↓ (MISMO COLOR, MISMO ALTO TOTAL) ------------------
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),   // SIN ESPACIO
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.height(textFieldHeight)         // MISMO ALTO QUE EL TEXTFIELD
            ) {
            SmallSquareButton(
                    enabled = enabled && onMoveUp != null,
                    size = arrowButtonSize,
                    onClick = onMoveUp,
                    bgColor = arrowBgColor,
                    contentColor = arrowContentColor
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Imagen anterior",
                        modifier = Modifier.size(smallIconSize)
                    )
                }

                SmallSquareButton(
                    enabled = enabled && onMoveDown != null,
                    size = arrowButtonSize,
                    onClick = onMoveDown,
                    bgColor = arrowBgColor,
                    contentColor = arrowContentColor
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Imagen siguiente",
                        modifier = Modifier.size(smallIconSize)
                    )
                }
            }

            // ------------------ GRUPO [...] [folder] [camera] ------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)  // botones pegados entre sí
            ) {
                // Botón "..." – mismo alto que el TextField, color propio
                GroupButton(
                    enabled = enabled && onDotsClick != null,
                    width = dotsButtonWidth,
                    height = textFieldHeight,
                    onClick = onDotsClick,
                    bgColor = dotsBgColor,
                    contentColor = dotsContentColor
                ) {
                    Text(
                        "...",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Botón carpeta – cuadrado, mismo alto que el TextField
                GroupButton(
                    enabled = enabled && onFolderClick != null,
                    width = textFieldHeight,
                    height = textFieldHeight,
                    onClick = onFolderClick,
                    bgColor = folderBgColor,
                    contentColor = folderContentColor
                ) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = "Abrir carpeta",
                        modifier = Modifier.size(smallIconSize)
                    )
                }

                // Botón cámara – cuadrado, mismo alto que el TextField
                GroupButton(
                    enabled = enabled && onCameraClick != null,
                    width = textFieldHeight,
                    height = textFieldHeight,
                    onClick = onCameraClick,
                    bgColor = cameraBgColor,
                    contentColor = cameraContentColor
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
}

/**
 * Botón pequeño cuadrado (para ↑ / ↓).
 * Dos de estos con spacing = arrowsSpacing suman la altura del TextField.
 */
@Composable
private fun SmallSquareButton(
    enabled: Boolean,
    size: Dp,
    onClick: (() -> Unit)?,
    bgColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Button(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        modifier = Modifier
            .width(size)
            .height(size),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(0.dp), // esquinas cuadradas
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor,
            disabledContainerColor = bgColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        content()
    }
}

/**
 * Botón rectangular de grupo para [...], carpeta y cámara,
 * con el mismo alto que el TextField y sin bordes redondos.
 */
@Composable
private fun GroupButton(
    enabled: Boolean,
    width: Dp,
    height: Dp,
    onClick: (() -> Unit)?,
    bgColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Button(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        modifier = Modifier
            .width(width)
            .height(height),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(0.dp), // esquinas cuadradas
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor,
            disabledContainerColor = bgColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        content()
    }
}
