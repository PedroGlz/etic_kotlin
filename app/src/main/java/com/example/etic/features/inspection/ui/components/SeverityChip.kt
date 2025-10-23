package com.example.etic.features.inspection.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.etic.features.inspection.domain.model.Severity

@Composable
fun SeverityChip(
    severity: Severity,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color: Color = when (severity) {
        Severity.Low -> Color(0xFF4CAF50)
        Severity.Medium -> Color(0xFFFFC107)
        Severity.High -> Color(0xFFFF5722)
        Severity.Critical -> Color(0xFFF44336)
    }
    AssistChip(
        label = { Text(severity.name) },
        onClick = onClick,
        colors = AssistChipDefaults.assistChipColors(
            labelColor = if (selected) color else MaterialTheme.colorScheme.onSurface,
            containerColor = if (selected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
