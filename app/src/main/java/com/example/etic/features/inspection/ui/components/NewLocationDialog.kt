package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.DialogProperties
import com.example.etic.R
import com.example.etic.data.local.entities.EstatusInspeccionDet
import com.example.etic.data.local.entities.Fabricante
import com.example.etic.data.local.entities.TipoPrioridad
import com.example.etic.features.inspection.ui.state.LocationFormState

@Composable
fun NewLocationDialog(
    show: Boolean,
    formState: LocationFormState,
    statusOptions: List<EstatusInspeccionDet>,
    prioridadOptions: List<TipoPrioridad>,
    fabricanteOptions: List<Fabricante>,
    previewRoute: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onAddManufacturer: () -> Unit = {}
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        shape = RoundedCornerShape(12.dp),
        title = { Text(stringResource(R.string.dlg_nueva_ubicacion)) },
        text = {
            Box(Modifier.fillMaxWidth().widthIn(min = 520.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownSelector(
                        label = stringResource(R.string.label_estatus_inspeccion),
                        options = statusOptions.map { it.idStatusInspeccionDet to (it.estatusInspeccionDet ?: it.idStatusInspeccionDet) },
                        selectedId = formState.statusId,
                        selectedLabelOverride = formState.statusLabel,
                        placeholder = "Seleccionar estatus",
                        onExpandedChange = { formState.statusExpanded = it },
                        expanded = formState.statusExpanded,
                        onSelected = { id, label ->
                            formState.statusId = id
                            formState.statusLabel = label
                            formState.statusExpanded = false
                        }
                    )

                    DropdownSelector(
                        label = "Tipo de prioridad",
                        options = prioridadOptions.map { it.idTipoPrioridad to (it.tipoPrioridad ?: it.idTipoPrioridad) },
                        selectedId = formState.prioridadId,
                        selectedLabelOverride = formState.prioridadLabel,
                        placeholder = "Seleccionar prioridad",
                        onExpandedChange = { formState.prioridadExpanded = it },
                        expanded = formState.prioridadExpanded,
                        onSelected = { id, label ->
                            formState.prioridadId = id
                            formState.prioridadLabel = label
                            formState.prioridadExpanded = false
                        }
                    )

                    FilterableSelector(
                        label = "Fabricante",
                        options = fabricanteOptions.map { it.idFabricante to (it.fabricante ?: it.idFabricante) },
                        selectedId = formState.fabricanteId,
                        selectedLabelOverride = formState.fabricanteLabel,
                        onExpandedChange = { formState.fabricanteExpanded = it },
                        expanded = formState.fabricanteExpanded,
                        placeholder = "Seleccionar fabricante",
                        onSelected = { id, label ->
                            formState.fabricanteId = id
                            formState.fabricanteLabel = label
                            formState.fabricanteExpanded = false
                        },
                        onAddClick = onAddManufacturer
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Es equipo")
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = formState.isEquipment, onCheckedChange = { formState.isEquipment = it })
                    }

                    LabeledInputField(
                        value = formState.name,
                        onValueChange = { formState.name = it },
                        label = stringResource(R.string.label_nombre_ubicacion),
                        required = true,
                        isError = formState.error != null,
                        singleLine = true
                    )

                    LabeledInputField(
                        value = formState.description,
                        onValueChange = { formState.description = it },
                        label = stringResource(R.string.label_descripcion),
                        singleLine = false,
                        fieldHeight = 64.dp
                    )

                    LabeledInputField(
                        value = formState.barcode,
                        onValueChange = { formState.barcode = it },
                        label = stringResource(R.string.label_codigo_barras),
                        singleLine = true
                    )

                    LabeledInputField(
                        value = previewRoute,
                        onValueChange = {},
                        label = "Ruta destino",
                        readOnly = true,
                        singleLine = true
                    )

                    if (formState.error != null) {
                        Text(formState.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = formState.name.isNotBlank() && !isSaving,
                onClick = onConfirm
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isSaving) { Text("Cerrar") }
        }
    )
}

private val FIELD_HEIGHT = 25.dp
private val FIELD_RADIUS = 4.dp
private val FIELD_BORDER = 1.dp
private val FIELD_PADDING = PaddingValues(horizontal = 4.dp, vertical = 2.dp)

@Composable
private fun LabeledInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    required: Boolean = false,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    fieldHeight: Dp = FIELD_HEIGHT
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            if (required) {
                Text(" *", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(
                    FIELD_BORDER,
                    if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(FIELD_RADIUS)
                )
                .padding(FIELD_PADDING),
            contentAlignment = Alignment.CenterStart
        ) {
            if (readOnly) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    selectedLabelOverride: String,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String, String) -> Unit
) {
    val selectedLabel = selectedLabelOverride.ifBlank {
        options.firstOrNull { it.first == selectedId }?.second.orEmpty()
    }
    Column(Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FIELD_HEIGHT)
                .border(
                    FIELD_BORDER,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(FIELD_RADIUS)
                )
                .padding(FIELD_PADDING)
                .clickable { onExpandedChange(!expanded) },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedLabel.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { (id, text) ->
                    DropdownMenuItem(
                        text = { Text(text = text.ifBlank { id }) },
                        onClick = { onSelected(id, text.ifBlank { id }) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterableSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    selectedLabelOverride: String,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String, String) -> Unit,
    onAddClick: () -> Unit
) {
    var query by remember(selectedId, options, selectedLabelOverride) {
        mutableStateOf(
            selectedLabelOverride.ifBlank {
                options.firstOrNull { it.first == selectedId }?.second.orEmpty()
            }
        )
    }
    val filtered = remember(query, options) {
        val q = query.trim()
        if (q.isBlank()) options else options.filter { (_, text) -> text.contains(q, ignoreCase = true) }
    }

    Column(Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FIELD_HEIGHT)
                        .border(
                            FIELD_BORDER,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(FIELD_RADIUS)
                        )
                        .padding(FIELD_PADDING)
                        .clickable(enabled = options.isNotEmpty()) { onExpandedChange(true) },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                onExpandedChange(true)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "▼",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (query.isBlank() && selectedId == null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 5.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    properties = PopupProperties(focusable = false)
                ) {
                    filtered.forEach { (id, text) ->
                        DropdownMenuItem(
                            text = { Text(text = text.ifBlank { id }) },
                            onClick = {
                                query = text.ifBlank { id }
                                onExpandedChange(false)
                                onSelected(id, text.ifBlank { id })
                            }
                        )
                    }
                }
            }
            AddInlineButton(onClick = onAddClick)
        }
    }
}

@Composable
private fun AddInlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(FIELD_HEIGHT)
            .border(
                FIELD_BORDER,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(FIELD_RADIUS)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


