package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.etic.features.inspection.home.BaselinesState

@Composable
fun BaselinePanel(
    state: BaselinesState,
    onSelect: (String?) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BaselineDropdown(state, onSelect)
            Button(onClick = onApply) { Text("Aplicar baseline") }
            Button(onClick = onReset) { Text("Reset comparación") }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.diffs, key = { it.metric }) { diff ->
                val color = when {
                    diff.delta == null -> Color.Unspecified
                    diff.delta > 0 -> Color(0xFFF44336)
                    diff.delta < 0 -> Color(0xFF4CAF50)
                    else -> Color.Unspecified
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(diff.metric)
                    Text("Actual: ${diff.current?.let { String.format("%.2f", it) } ?: "-"}")
                    Text("Base: ${diff.baseline?.let { String.format("%.2f", it) } ?: "-"}")
                    Text("Δ ${diff.delta?.let { String.format("%.2f", it) } ?: "-"}", color = color)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaselineDropdown(state: BaselinesState, onSelect: (String?) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    val selected = state.all.find { it.id == state.selectedId }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = !expanded.value }) {
        TextField(
            readOnly = true,
            value = selected?.name ?: "Selecciona baseline",
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor()
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            state.all.forEach { b ->
                DropdownMenuItem(text = { Text(b.name) }, onClick = { onSelect(b.id); expanded.value = false })
            }
            DropdownMenuItem(text = { Text("Ninguna") }, onClick = { onSelect(null); expanded.value = false })
        }
    }
}
