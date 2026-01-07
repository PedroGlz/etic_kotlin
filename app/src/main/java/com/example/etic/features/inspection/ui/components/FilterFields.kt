package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val FILTER_FIELD_HEIGHT = 28.dp
internal val FILTER_FIELD_RADIUS = 4.dp
internal val FILTER_FIELD_BORDER = 1.dp
internal val FILTER_FIELD_HORIZONTAL_PADDING = 6.dp
internal val FILTER_FIELD_VERTICAL_PADDING = 4.dp
internal val FILTER_FIELD_ROW_SPACING = 8.dp

@Composable
fun FilterFieldContainer(
    label: String,
    modifier: Modifier = Modifier,
    minWidth: Dp = 150.dp,
    maxWidth: Dp = 220.dp,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier.widthIn(min = minWidth, max = maxWidth),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FILTER_FIELD_HEIGHT)
                .border(
                    FILTER_FIELD_BORDER,
                    MaterialTheme.colorScheme.outline,
                    androidx.compose.foundation.shape.RoundedCornerShape(FILTER_FIELD_RADIUS)
                )
                .padding(
                    horizontal = FILTER_FIELD_HORIZONTAL_PADDING,
                    vertical = FILTER_FIELD_VERTICAL_PADDING
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun FilterDropdownField(
    label: String,
    options: List<Pair<String?, String>>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 150.dp,
    maxWidth: Dp = 220.dp,
    placeholder: String = "Seleccionar"
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    Column(modifier = modifier) {
        FilterFieldContainer(
            label = label,
            minWidth = minWidth,
            maxWidth = maxWidth
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setExpanded(true) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedLabel.isNotBlank()) selectedLabel else placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text = text.ifBlank { id.orEmpty() }, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        setExpanded(false)
                        onSelected(id)
                    }
                )
            }
        }
    }
}

@Composable
fun FilterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 180.dp,
    maxWidth: Dp = 320.dp,
    placeholder: String = "Buscar",
    leadingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    FilterFieldContainer(
        label = label,
        modifier = modifier,
        minWidth = minWidth,
        maxWidth = maxWidth
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = leadingIconTint,
            modifier = Modifier
                .width(14.dp)
                .height(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        )
    }
}
