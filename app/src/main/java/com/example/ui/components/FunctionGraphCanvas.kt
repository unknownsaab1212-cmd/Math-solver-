package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun FunctionGraphCanvas(
    functionExpression: String,
    modifier: Modifier = Modifier,
    xMin: Float = -6f,
    xMax: Float = 6f,
    yMin: Float = -6f,
    yMax: Float = 6f
) {
    val evaluatedPoints = remember(functionExpression, xMin, xMax) {
        sampleFunction(functionExpression, xMin, xMax, steps = 240)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoGraph,
                contentDescription = "Graph icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Function Plot: f(x) = $functionExpression",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF0B0D17), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E2235), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                fun toScreenX(x: Float): Float = ((x - xMin) / (xMax - xMin)) * width
                fun toScreenY(y: Float): Float = height - (((y - yMin) / (yMax - yMin)) * height)

                // 1. Draw grid lines
                val gridColor = Color(0xFF1C223A)
                for (x in xMin.toInt()..xMax.toInt()) {
                    val sx = toScreenX(x.toFloat())
                    drawLine(
                        color = gridColor,
                        start = Offset(sx, 0f),
                        end = Offset(sx, height),
                        strokeWidth = 1f
                    )
                }
                for (y in yMin.toInt()..yMax.toInt()) {
                    val sy = toScreenY(y.toFloat())
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, sy),
                        end = Offset(width, sy),
                        strokeWidth = 1f
                    )
                }

                // 2. Draw X and Y Axes
                val axisColor = Color(0xFF64748B)
                val originX = toScreenX(0f)
                val originY = toScreenY(0f)

                if (originX in 0f..width) {
                    drawLine(
                        color = axisColor,
                        start = Offset(originX, 0f),
                        end = Offset(originX, height),
                        strokeWidth = 2f
                    )
                }
                if (originY in 0f..height) {
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, originY),
                        end = Offset(width, originY),
                        strokeWidth = 2f
                    )
                }

                // 3. Draw curve segments
                val curvePath = Path()
                var isStarted = false

                for ((x, y) in evaluatedPoints) {
                    if (y.isNaN() || y.isInfinite() || y < yMin - 10 || y > yMax + 10) {
                        isStarted = false
                        continue
                    }

                    val sx = toScreenX(x)
                    val sy = toScreenY(y)

                    if (!isStarted) {
                        curvePath.moveTo(sx, sy)
                        isStarted = true
                    } else {
                        curvePath.lineTo(sx, sy)
                    }
                }

                drawPath(
                    path = curvePath,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 3.5f)
                )

                // Draw origin dot
                drawCircle(
                    color = Color(0xFFF43F5E),
                    radius = 4f,
                    center = Offset(originX, originY)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "x ∈ [${xMin.toInt()}, ${xMax.toInt()}]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "y ∈ [${yMin.toInt()}, ${yMax.toInt()}]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// Simple robust mathematical expression evaluator for standard function shapes
fun sampleFunction(expr: String, xMin: Float, xMax: Float, steps: Int): List<Pair<Float, Float>> {
    val clean = expr.replace(" ", "").lowercase()
    val points = mutableListOf<Pair<Float, Float>>()
    val stepSize = (xMax - xMin) / steps

    for (i in 0..steps) {
        val x = xMin + i * stepSize
        val y = evalExpr(clean, x)
        points.add(Pair(x, y))
    }
    return points
}

fun evalExpr(expr: String, x: Float): Float {
    return try {
        when {
            expr.contains("sin(x)") -> {
                val coeff = extractCoeff(expr, "sin(x)")
                (coeff * sin(x.toDouble())).toFloat()
            }
            expr.contains("cos(x)") -> {
                val coeff = extractCoeff(expr, "cos(x)")
                (coeff * cos(x.toDouble())).toFloat()
            }
            expr.contains("tan(x)") -> {
                (tan(x.toDouble())).toFloat()
            }
            expr.contains("sqrt(x)") -> {
                if (x < 0) Float.NaN else sqrt(x)
            }
            expr.contains("1/x") -> {
                if (abs(x) < 0.05f) Float.NaN else (1f / x)
            }
            // Polynomials like x^2 - 4 or 2*x^2 + 3*x - 1
            expr.contains("x^2") || expr.contains("x^3") || expr.contains("x") -> {
                evalPolynomial(expr, x)
            }
            else -> {
                // Try simple linear/constant
                evalPolynomial(expr, x)
            }
        }
    } catch (_: Exception) {
        Float.NaN
    }
}

fun extractCoeff(expr: String, pattern: String): Float {
    val idx = expr.indexOf(pattern)
    if (idx <= 0) return 1f
    val prefix = expr.substring(0, idx).trimEnd('*')
    return prefix.toFloatOrNull() ?: 1f
}

fun evalPolynomial(expr: String, x: Float): Float {
    // Quick heuristic for typical school math functions: ax^3 + bx^2 + cx + d
    var value = 0.0
    // Clean spaces and normalize signs
    val clean = expr.replace(" ", "")
    val normalized = clean.replace("-", "+-")
    val tokens = normalized.split("+").filter { it.isNotBlank() }

    for (token in tokens) {
        val trimmed = token.trim()
        if (trimmed.contains("x^3")) {
            val c = trimmed.substringBefore("x^3").trimEnd('*').ifEmpty { "1" }
            val mult = if (c == "-") -1.0 else (c.toDoubleOrNull() ?: 1.0)
            value += mult * x.toDouble().pow(3.0)
        } else if (trimmed.contains("x^2")) {
            val c = trimmed.substringBefore("x^2").trimEnd('*').ifEmpty { "1" }
            val mult = if (c == "-") -1.0 else (c.toDoubleOrNull() ?: 1.0)
            value += mult * x.toDouble().pow(2.0)
        } else if (trimmed.contains("x")) {
            val c = trimmed.substringBefore("x").trimEnd('*').ifEmpty { "1" }
            val mult = if (c == "-") -1.0 else (c.toDoubleOrNull() ?: 1.0)
            value += mult * x.toDouble()
        } else {
            value += (trimmed.toDoubleOrNull() ?: 0.0)
        }
    }
    return value.toFloat()
}
