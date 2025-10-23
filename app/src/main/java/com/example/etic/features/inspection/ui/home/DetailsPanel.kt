package com.example.etic.features.inspection.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.etic.features.inspection.domain.model.Location
import com.example.etic.features.inspection.home.BaselinesState
import com.example.etic.features.inspection.home.CenterTab
import com.example.etic.features.inspection.ui.components.Breadcrumb
import com.example.etic.features.inspection.ui.components.KpiCard

@Composable
fun DetailsPanel(
    location: Location?,
    baselines: BaselinesState,
    centerTab: CenterTab,
    onCenterTab: (CenterTab) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(8.dp).animateContentSize()) {
        Header(location)
        Divider(Modifier.padding(vertical = 6.dp))
        if (location == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Seleccione una ubicación del árbol")
            }
            return
        }
        KpiGrid(location, baselines)
        Spacer(Modifier.height(8.dp))
        MapPlaceholder(location)

        Spacer(Modifier.height(8.dp))
        val tabs = CenterTab.values()
        TabRow(selectedTabIndex = tabs.indexOf(centerTab)) {
            tabs.forEach { t ->
                Tab(selected = t == centerTab, onClick = { onCenterTab(t) }, text = { Text(t.name) })
            }
        }
        Crossfade(targetState = centerTab, label = "centerTabs") { tab ->
            when (tab) {
                CenterTab.Overview -> OverviewTab(location, baselines)
                CenterTab.Assets -> AssetsTab(location)
                CenterTab.Timeline -> TimelineTab(location)
            }
        }
    }
}

@Composable
private fun Header(location: Location?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(location?.name ?: "Sin selección", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (location != null) Breadcrumb(location.path)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { /* edit */ }) { Icon(Icons.Default.Edit, contentDescription = null) }
            IconButton(onClick = { /* refresh */ }) { Icon(Icons.Default.Refresh, contentDescription = null) }
        }
    }
}

@Composable
private fun KpiGrid(location: Location, baselines: BaselinesState) {
    val diffsByKey = baselines.diffs.associateBy { it.metric }
    LazyVerticalGrid(columns = GridCells.Adaptive(140.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(140.dp)) {
        items(location.kpis.toList()) { (k, v) ->
            val diff = diffsByKey[k]?.delta
            val valueText = if (k == "risk") "%.1f".format(v) else "%.1f%%".format(v)
            KpiCard(title = k.replaceFirstChar { it.titlecase() }, value = valueText, delta = diff)
        }
    }
}

@Composable
private fun MapPlaceholder(location: Location) {
    Row(Modifier.fillMaxWidth().height(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.padding(8.dp)) {
            Text("Mapa (placeholder)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Lat: ${location.coords?.lat?.let { String.format("%.4f", it) }}, Lng: ${location.coords?.lng?.let { String.format("%.4f", it) }}")
        }
        Button(onClick = { /* center map */ }) {
            Icon(Icons.Default.CenterFocusStrong, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Centrar")
        }
    }
}

@Composable
private fun OverviewTab(location: Location, baselines: BaselinesState) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Metadata", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AssistChip(onClick = {}, label = { Text(location.type.name) })
            AssistChip(onClick = {}, label = { Text("Status: ${location.status}") })
        }
        Divider()
        Text("KPIs y últimas alertas", style = MaterialTheme.typography.titleMedium)
        Text("Comparando con: ${baselines.all.find { it.id == baselines.selectedId }?.name ?: "N/A"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AssetsTab(location: Location) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Activos relacionados de ${location.name}")
    }
}

@Composable
private fun TimelineTab(location: Location) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Timeline de eventos de ${location.name}")
    }
}
