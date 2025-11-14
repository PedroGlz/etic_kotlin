package com.example.etic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Opción global de tamaño de fuente
enum class FontSizeOption { Small, Medium, Large }

// Tipografía base (equivale al tamaño "Grande" solicitado)
val BaseTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

private fun scaleUnit(value: TextUnit, scale: Float): TextUnit =
    if (value == TextUnit.Unspecified) value else (value.value * scale).sp

private fun scaleStyle(style: TextStyle, scale: Float): TextStyle = style.copy(
    fontSize = scaleUnit(style.fontSize, scale),
    lineHeight = scaleUnit(style.lineHeight, scale)
)

private fun scaledTypography(base: Typography, scale: Float): Typography = base.copy(
    displayLarge = scaleStyle(base.displayLarge, scale),
    displayMedium = scaleStyle(base.displayMedium, scale),
    displaySmall = scaleStyle(base.displaySmall, scale),
    headlineLarge = scaleStyle(base.headlineLarge, scale),
    headlineMedium = scaleStyle(base.headlineMedium, scale),
    headlineSmall = scaleStyle(base.headlineSmall, scale),
    titleLarge = scaleStyle(base.titleLarge, scale),
    titleMedium = scaleStyle(base.titleMedium, scale),
    titleSmall = scaleStyle(base.titleSmall, scale),
    bodyLarge = scaleStyle(base.bodyLarge, scale),
    bodyMedium = scaleStyle(base.bodyMedium, scale),
    bodySmall = scaleStyle(base.bodySmall, scale),
    labelLarge = scaleStyle(base.labelLarge, scale),
    labelMedium = scaleStyle(base.labelMedium, scale),
    labelSmall = scaleStyle(base.labelSmall, scale)
)

fun typographyFor(option: FontSizeOption): Typography {
    val base = BaseTypography // base definida arriba (equivale a "Grande")
    val scale = when (option) {
        FontSizeOption.Large -> 1.0f   // tamaño actual
        FontSizeOption.Medium -> 0.9f  // un poco más pequeño
        FontSizeOption.Small -> 0.8f   // aún más pequeño
    }
    return if (scale == 1.0f) base else scaledTypography(base, scale)
}
