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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

enum class FilterLabelPosition { Top, Start, End, None }

internal val FILTER_FIELD_HEIGHT = 40.dp
internal val FILTER_FIELD_RADIUS = 4.dp
internal val FILTER_FIELD_BORDER = 1.dp
internal val FILTER_FIELD_HORIZONTAL_PADDING = 4.dp
internal val FILTER_FIELD_VERTICAL_PADDING = 2.dp
internal val FILTER_FIELD_ROW_SPACING = 8.dp

@Composable
fun FilterFieldContainer(
    label: String,
    modifier: Modifier = Modifier,
    minWidth: Dp = 150.dp,
    maxWidth: Dp = 220.dp,
    labelPosition: FilterLabelPosition = FilterLabelPosition.Top,
    labelWidth: Dp = 90.dp,
    content: @Composable RowScope.() -> Unit
) {
    val fieldContent: @Composable () -> Unit = {
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

    when (labelPosition) {
        FilterLabelPosition.Top -> {
            Column(
                modifier = modifier.widthIn(min = minWidth, max = maxWidth),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                fieldContent()
            }
        }
        FilterLabelPosition.Start -> {
            Row(
                modifier = modifier.widthIn(min = minWidth, max = maxWidth),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.widthIn(min = labelWidth)
                )
                Box(modifier = Modifier.weight(1f)) {
                    fieldContent()
                }
            }
        }
        FilterLabelPosition.End -> {
            Row(
                modifier = modifier.widthIn(min = minWidth, max = maxWidth),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    fieldContent()
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.widthIn(min = labelWidth)
                )
            }
        }
        FilterLabelPosition.None -> {
            Box(modifier = modifier.widthIn(min = minWidth, max = maxWidth)) {
                fieldContent()
            }
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
    placeholder: String = "Seleccionar",
    enabled: Boolean = true,
    labelPosition: FilterLabelPosition = FilterLabelPosition.Top,
    labelWidth: Dp = 90.dp
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    Box(modifier = modifier.widthIn(min = minWidth, max = maxWidth)) {
        FilterFieldContainer(
            label = label,
            modifier = Modifier,
            minWidth = minWidth,
            maxWidth = maxWidth,
            labelPosition = labelPosition,
            labelWidth = labelWidth
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = enabled
                    ) { setExpanded(true) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedLabel.isNotBlank()) selectedLabel else placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Desplegar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text = text.ifBlank { id.orEmpty() }, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        if (enabled) {
                            setExpanded(false)
                            onSelected(id)
                        }
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
    searchIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    labelPosition: FilterLabelPosition = FilterLabelPosition.Top,
    labelWidth: Dp = 90.dp
) {
    FilterFieldContainer(
        label = label,
        modifier = modifier,
        minWidth = minWidth,
        maxWidth = maxWidth,
        labelPosition = labelPosition,
        labelWidth = labelWidth
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() },
                onDone = { onSearch() }
            ),
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
        Spacer(Modifier.width(2.dp))
        IconButton(
            onClick = onSearch,
            enabled = enabled,
            modifier = Modifier
                .width(28.dp)
                .height(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = searchIconTint,
                modifier = Modifier
                    .width(26.dp)
                    .height(26.dp)
            )
        }
    }
}
