package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.DialogProperties
import com.example.etic.R
import com.example.etic.data.local.entities.EstatusInspeccionDet
import com.example.etic.data.local.entities.Fabricante
import com.example.etic.data.local.entities.TipoPrioridad
import com.example.etic.features.inspection.ui.state.LocationFormState

@OptIn(ExperimentalMaterial3Api::class)
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
    onConfirm: () -> Unit
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
                    ExposedDropdownMenuBox(
                        expanded = formState.statusExpanded,
                        onExpandedChange = { formState.statusExpanded = !formState.statusExpanded }
                    ) {
                        TextField(
                            value = if (formState.statusLabel.isNotBlank()) formState.statusLabel else "Seleccionar estatus",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_estatus_inspeccion)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.statusExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = formState.statusExpanded,
                            onDismissRequest = { formState.statusExpanded = false }
                        ) {
                            statusOptions.forEach { opt ->
                                val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        formState.statusLabel = label
                                        formState.statusId = opt.idStatusInspeccionDet
                                        formState.statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = formState.prioridadExpanded,
                        onExpandedChange = { formState.prioridadExpanded = !formState.prioridadExpanded }
                    ) {
                        TextField(
                            value = if (formState.prioridadLabel.isNotBlank()) formState.prioridadLabel else "Seleccionar prioridad",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de prioridad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.prioridadExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = formState.prioridadExpanded,
                            onDismissRequest = { formState.prioridadExpanded = false }
                        ) {
                            prioridadOptions.forEach { opt ->
                                val label = opt.tipoPrioridad ?: opt.idTipoPrioridad
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        formState.prioridadLabel = label
                                        formState.prioridadId = opt.idTipoPrioridad
                                        formState.prioridadExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = formState.fabricanteExpanded,
                        onExpandedChange = { formState.fabricanteExpanded = !formState.fabricanteExpanded }
                    ) {
                        TextField(
                            value = if (formState.fabricanteLabel.isNotBlank()) formState.fabricanteLabel else "Seleccionar fabricante",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fabricante") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.fabricanteExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = formState.fabricanteExpanded,
                            onDismissRequest = { formState.fabricanteExpanded = false }
                        ) {
                            fabricanteOptions.forEach { opt ->
                                val label = opt.fabricante ?: opt.idFabricante
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        formState.fabricanteLabel = label
                                        formState.fabricanteId = opt.idFabricante
                                        formState.fabricanteExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Es equipo")
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = formState.isEquipment, onCheckedChange = { formState.isEquipment = it })
                    }

                    TextField(
                        value = formState.name,
                        onValueChange = { formState.name = it },
                        singleLine = true,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.label_nombre_ubicacion))
                                Text(" *", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = formState.error != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = formState.description,
                        onValueChange = { formState.description = it },
                        singleLine = false,
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = formState.barcode,
                        onValueChange = { formState.barcode = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.label_codigo_barras)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = previewRoute,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ruta destino") },
                        modifier = Modifier.fillMaxWidth()
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
            Button(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}


