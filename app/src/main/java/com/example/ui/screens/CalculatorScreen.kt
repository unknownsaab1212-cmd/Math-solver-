package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun CalculatorScreen(
    onSolveInAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var isRad by remember { mutableStateOf(false) }

    fun calculateResult(expr: String): String {
        return try {
            if (expr.isBlank()) return "0"
            // Simple calculator evaluation
            val clean = expr.replace("×", "*").replace("÷", "/")
            val res = evaluateSimpleMath(clean, isRad)
            if (res.isNaN()) "Error" else if (res == res.toLong().toDouble()) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            "..."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Screen
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { isRad = !isRad },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isRad) "RAD" else "DEG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (expression.isNotBlank()) {
                        Button(
                            onClick = { onSolveInAi(expression) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("calc_send_to_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Solve with Steps", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.End,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        // Keypad
        val rows = listOf(
            listOf("sin", "cos", "tan", "(", ")"),
            listOf("√", "^", "ln", "π", "÷"),
            listOf("7", "8", "9", "DEL", "AC"),
            listOf("4", "5", "6", "×", "-"),
            listOf("1", "2", "3", "+", "="),
            listOf("0", ".", "%", "ANS", "AI")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=", "^", "%")
                        val isAction = key in listOf("AC", "DEL", "AI")
                        val isFunc = key in listOf("sin", "cos", "tan", "√", "ln", "π", "(", ")", "ANS")

                        val btnColor = when {
                            key == "=" || key == "AI" -> MaterialTheme.colorScheme.primary
                            isAction -> MaterialTheme.colorScheme.errorContainer
                            isOp -> MaterialTheme.colorScheme.secondaryContainer
                            isFunc -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            key == "=" || key == "AI" -> MaterialTheme.colorScheme.onPrimary
                            isAction -> MaterialTheme.colorScheme.onErrorContainer
                            isOp -> MaterialTheme.colorScheme.onSecondaryContainer
                            isFunc -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            onClick = {
                                when (key) {
                                    "AC" -> {
                                        expression = ""
                                        resultText = "0"
                                    }
                                    "DEL" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                            resultText = calculateResult(expression)
                                        }
                                    }
                                    "=" -> {
                                        resultText = calculateResult(expression)
                                    }
                                    "AI" -> {
                                        if (expression.isNotBlank()) {
                                            onSolveInAi(expression)
                                        }
                                    }
                                    "ANS" -> {
                                        if (resultText != "Error" && resultText != "...") {
                                            expression += resultText
                                        }
                                    }
                                    "sin", "cos", "tan", "ln" -> {
                                        expression += "$key("
                                    }
                                    "√" -> {
                                        expression += "√("
                                    }
                                    "π" -> {
                                        expression += "3.14159"
                                        resultText = calculateResult(expression)
                                    }
                                    else -> {
                                        expression += key
                                        resultText = calculateResult(expression)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = btnColor,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("calc_key_$key")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = textColor,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isOp || key == "=") FontWeight.Bold else FontWeight.SemiBold,
                                            fontFamily = if (!isFunc && !isAction) FontFamily.Monospace else FontFamily.Default,
                                            fontSize = if (key.length > 2) 13.sp else 18.sp
                                        ),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple recursive descent expression evaluator
class SimpleMathParser(private val expr: String, private val isRad: Boolean) {
    private var pos = -1
    private var ch = -1

    private fun nextChar() {
        ch = if (++pos < expr.length) expr[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor()
            else if (eat('/'.code)) x /= parseFactor()
            else if (eat('%'.code)) x %= parseFactor()
            else return x
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('('.code)) {
            x = parseExpression()
            eat(')'.code)
        } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
            while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
            x = expr.substring(startPos, pos).toDouble()
        } else if (ch in 'a'.code..'z'.code || ch == '√'.code) {
            while (ch in 'a'.code..'z'.code || ch == '√'.code) nextChar()
            val func = expr.substring(startPos, pos)
            x = parseFactor()
            x = when (func) {
                "sqrt", "√" -> sqrt(x)
                "sin" -> if (isRad) sin(x) else sin(Math.toRadians(x))
                "cos" -> if (isRad) cos(x) else cos(Math.toRadians(x))
                "tan" -> if (isRad) tan(x) else tan(Math.toRadians(x))
                "ln" -> ln(x)
                "log" -> log10(x)
                else -> throw RuntimeException("Unknown function: $func")
            }
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }

        if (eat('%'.code)) x = x / 100.0
        if (eat('^'.code)) x = x.pow(parseFactor())
        return x
    }
}

fun evaluateSimpleMath(expression: String, isRad: Boolean): Double {
    return SimpleMathParser(expression.replace(" ", ""), isRad).parse()
}
