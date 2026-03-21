package com.example.etic.features.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequenceInputButtonGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    textFieldMinWidth: Dp = 0.dp,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onCurrentValueClick: (() -> Unit)? = null
) {
    val fieldHeight = 38.dp
    val fieldRadius = 4.dp
    val fieldPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)

    val buttonHeight = 42.dp
    val arrowButtonSize = buttonHeight / 2
    val dotsButtonWidth = 34.dp
    val iconSize = 16.dp

    val arrowBgColor: Color = MaterialTheme.colorScheme.surfaceVariant
    val arrowContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    val dotsBgColor: Color = MaterialTheme.colorScheme.primaryContainer
    val dotsContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = textFieldMinWidth)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 11.sp)
                if (isRequired) {
                    Text(" *", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(fieldRadius))
                    .padding(fieldPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { if (enabled) onValueChange(it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.height(buttonHeight)
            ) {
                SmallSquareButton(
                    enabled = enabled && onMoveUp != null,
                    size = arrowButtonSize,
                    onClick = onMoveUp,
                    bgColor = arrowBgColor,
                    contentColor = arrowContentColor
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Valor anterior",
                        modifier = Modifier.size(iconSize)
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
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Valor siguiente",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            GroupButton(
                enabled = enabled && onCurrentValueClick != null,
                width = dotsButtonWidth,
                height = buttonHeight,
                onClick = onCurrentValueClick,
                bgColor = dotsBgColor,
                contentColor = dotsContentColor
            ) {
                Text("...", fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

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
        shape = RoundedCornerShape(0.dp),
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
        shape = RoundedCornerShape(0.dp),
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
