package com.example.etic.features.inspection.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.etic.features.inspection.domain.model.LocationType
import com.example.etic.features.inspection.domain.model.Status
import com.example.etic.features.inspection.home.TreeRow

@Composable
fun TreePanel(
    query: String,
    rows: List<TreeRow>,
    selectedId: String?,
    onQuery: (String) -> Unit,
    onToggle: (String) -> Unit,
    onSelect: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Buscar por nombre o path") }
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(rows, key = { it.id }) { row ->
                TreeRowItem(
                    row = row,
                    selected = row.id == selectedId,
                    onToggle = { onToggle(row.id) },
                    onClick = { onSelect(row.id) }
                )
            }
        }
    }
}

@Composable
private fun TreeRowItem(
    row: TreeRow,
    selected: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val (menuOpen, setMenuOpen) = remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(start = (row.level * 12).dp, top = 6.dp, bottom = 6.dp, end = 8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (row.hasChildren) {
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(if (row.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
        } else {
            Spacer(Modifier.size(24.dp))
        }

        val icon = when (row.type) {
            LocationType.Site -> Icons.Default.Place
            LocationType.Area -> Icons.Default.Storage
            LocationType.Asset -> Icons.Default.LocationOn
        }
        Icon(icon, contentDescription = null, tint = when (row.status) {
            Status.Good -> Color(0xFF4CAF50)
            Status.Warning -> Color(0xFFFFC107)
            Status.Critical -> Color(0xFFF44336)
            Status.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
        })
        Text(row.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

        IconButton(onClick = { setMenuOpen(true) }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { setMenuOpen(false) }) {
            DropdownMenuItem(text = { Text("Ver detalles") }, onClick = { setMenuOpen(false); onClick() })
            DropdownMenuItem(text = { Text("Centrar en mapa") }, onClick = { setMenuOpen(false) })
            DropdownMenuItem(text = { Text("Favorito") }, onClick = { setMenuOpen(false) })
        }
    }
}
