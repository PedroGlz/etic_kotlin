package com.example.etic.features.inspection.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KpiCard(
    title: String,
    value: String,
    delta: Double? = null,
) {
    val color = when {
        delta == null -> MaterialTheme.colorScheme.onSurfaceVariant
        delta > 0 -> MaterialTheme.colorScheme.error
        delta < 0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(12.dp).animateContentSize()) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
            if (delta != null) {
                val s = if (delta >= 0) "+%.2f".format(delta) else "%.2f".format(delta)
                Text(s, style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
    }
}
