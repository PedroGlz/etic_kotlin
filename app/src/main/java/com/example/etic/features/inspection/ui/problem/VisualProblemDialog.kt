package com.example.etic.features.inspection.ui.problem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualProblemDialog(
    inspectionNumber: String,
    problemNumber: String,
    problemType: String,
    equipmentName: String,
    equipmentRoute: String,
    hazardIssues: List<Pair<String, String>>,
    severities: List<Pair<String, String>>,
    selectedHazardIssue: String?,
    selectedSeverity: String?,
    onHazardSelected: (String) -> Unit,
    onSeveritySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        shape = RoundedCornerShape(12.dp),
        confirmButton = {
            Button(onClick = onContinue) { Text("Continuar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Problema Visual") },
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .widthIn(min = 520.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoField(
                            label = "Inspeccion No.",
                            value = inspectionNumber,
                            modifier = Modifier.weight(1f)
                        )
                        InfoField(
                            label = "Problema No.",
                            value = problemNumber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoField(
                            label = "Tipo de problema",
                            value = problemType,
                            modifier = Modifier.weight(1f)
                        )
                        InfoField(
                            label = "Equipo",
                            value = equipmentName,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextField(
                        value = equipmentRoute,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ruta del equipo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Clasificacion visual", style = MaterialTheme.typography.bodyMedium)
                    DropdownField(
                        label = "Problema",
                        options = hazardIssues,
                        selectedId = selectedHazardIssue,
                        placeholder = "Selecciona una falla",
                        onSelected = onHazardSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField(
                        label = "Severidad",
                        options = severities,
                        selectedId = selectedSeverity,
                        placeholder = "Selecciona severidad",
                        onSelected = onSeveritySelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
private fun InfoField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    placeholder: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == selectedId }?.second ?: placeholder
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            if (options.isNotEmpty()) expanded.value = !expanded.value
        },
        modifier = modifier
    ) {
        TextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            enabled = options.isNotEmpty(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded.value = false
                        onSelected(id)
                    }
                )
            }
        }
    }
}
