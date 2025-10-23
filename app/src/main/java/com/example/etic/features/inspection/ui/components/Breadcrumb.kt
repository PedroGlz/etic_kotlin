package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
//import androidx.compose.material3.icons.Icons
//import androidx.compose.material3.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Breadcrumb(items: List<String>) {
    Row(Modifier.wrapContentWidth()) {
        items.forEachIndexed { index, s ->
            Text(s, style = MaterialTheme.typography.labelLarge)
            /*if (index != items.lastIndex) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }*/
        }
    }
}
