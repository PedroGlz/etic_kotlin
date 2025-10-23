package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun ResizableThreePanels(
    leftFraction: Float,
    rightFraction: Float,
    onLeftFraction: (Float) -> Unit,
    onRightFraction: (Float) -> Unit,
    dividerWidth: Dp = 8.dp,
    left: @Composable Modifier.() -> Unit,
    center: @Composable Modifier.() -> Unit,
    right: @Composable Modifier.() -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val dragSensitivity = remember { 1f }
        val dividerPx = with(density) { dividerWidth.toPx() }

        val leftW = leftFraction.coerceIn(0.15f, 0.4f)
        val rightW = rightFraction.coerceIn(0.2f, 0.5f)
        val centerW = 1f - leftW - rightW

        Box(Modifier.fillMaxHeight().fillMaxWidth(leftW)) { left(Modifier) }

        Box(
            Modifier
                .width(dividerWidth)
                .fillMaxHeight()
                .background(androidx.compose.ui.graphics.Color.Red)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = dragAmount / (size.width - 2 * dividerPx)
                        val newLeft = (leftW + delta * dragSensitivity)
                        onLeftFraction(newLeft)
                    }
                }
        ) { Divider(Modifier.fillMaxHeight().width(1.dp)) }

        Box(Modifier.fillMaxHeight().fillMaxWidth(centerW / (centerW + rightW))) { center(Modifier) }

        Box(
            Modifier
                .width(dividerWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val total = size.width - dividerPx
                        val delta = dragAmount / total
                        val newRight = (rightW - delta)
                        onRightFraction(newRight)
                    }
                }
        ) { Divider(Modifier.fillMaxHeight().width(1.dp)) }

        Box(Modifier.fillMaxHeight().fillMaxWidth()) { right(Modifier) }
    }
}
