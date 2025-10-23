package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.etic.features.inspection.domain.model.Issue
import com.example.etic.features.inspection.domain.model.IssueStatus
import com.example.etic.features.inspection.domain.model.Severity
import com.example.etic.features.inspection.home.IssueSort
import com.example.etic.features.inspection.home.IssuesState
import com.example.etic.features.inspection.ui.components.SeverityChip

@Composable
fun IssuesPanel(
    state: IssuesState,
    onSeverityToggle: (Severity) -> Unit,
    onText: (String) -> Unit,
    onStatus: (IssueStatus?) -> Unit,
    onSort: (IssueSort) -> Unit,
    onIssueClick: (Issue) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SeverityChip(Severity.Critical, selected = Severity.Critical in state.severities) { onSeverityToggle(Severity.Critical) }
            SeverityChip(Severity.High, selected = Severity.High in state.severities) { onSeverityToggle(Severity.High) }
            SeverityChip(Severity.Medium, selected = Severity.Medium in state.severities) { onSeverityToggle(Severity.Medium) }
            SeverityChip(Severity.Low, selected = Severity.Low in state.severities) { onSeverityToggle(Severity.Low) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = state.text, onValueChange = onText, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Buscar…") })
            StatusDropdown(state.status, onStatus)
            SortDropdown(state.sortBy, onSort)
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            val grouped = state.filtered.groupBy { it.severity }.toSortedMap(compareByDescending { it.ordinal })
            grouped.forEach { (sev, list) ->
                item(key = "header-${sev.name}") { Text("${sev.name} (${list.size})", modifier = Modifier.padding(vertical = 6.dp)) }
                items(list, key = { it.id }) { issue ->
                    IssueItem(issue = issue, onClick = { onIssueClick(issue) })
                }
            }
        }
    }
}

@Composable
private fun IssueItem(issue: Issue, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(issue.title)
        Text("${issue.category} · ${issue.status} · ${issue.createdAt}")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StatusDropdown(value: IssueStatus?, onChange: (IssueStatus?) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = !expanded.value }) {
        TextField(
            readOnly = true,
            value = value?.name ?: "Estado",
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor()
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onChange(null); expanded.value = false })
            IssueStatus.values().forEach { s ->
                DropdownMenuItem(text = { Text(s.name) }, onClick = { onChange(s); expanded.value = false })
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SortDropdown(value: IssueSort, onChange: (IssueSort) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = !expanded.value }) {
        TextField(
            readOnly = true,
            value = when (value) {
                IssueSort.ByDateDesc -> "Fecha"
                IssueSort.BySeverityDesc -> "Severidad"
            },
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor()
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            DropdownMenuItem(text = { Text("Fecha") }, onClick = { onChange(IssueSort.ByDateDesc); expanded.value = false })
            DropdownMenuItem(text = { Text("Severidad") }, onClick = { onChange(IssueSort.BySeverityDesc); expanded.value = false })
        }
    }
}
