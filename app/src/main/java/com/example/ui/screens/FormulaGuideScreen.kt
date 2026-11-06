package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FormulaItem(
    val name: String,
    val formula: String,
    val description: String,
    val sampleProblem: String
)

@Composable
fun FormulaGuideScreen(
    onLoadProblem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Algebra", "Calculus", "Trigonometry", "Geometry", "Statistics")
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val algebraFormulas = listOf(
        FormulaItem(
            name = "Quadratic Formula",
            formula = "x = (-b ± √(b² - 4ac)) / (2a)",
            description = "Finds real and complex roots of any quadratic equation ax² + bx + c = 0",
            sampleProblem = "Solve for x: 2x^2 - 4x - 6 = 0"
        ),
        FormulaItem(
            name = "Difference of Squares",
            formula = "a² - b² = (a - b)(a + b)",
            description = "Fundamental algebraic factoring identity",
            sampleProblem = "Factorize: 9x^2 - 25"
        ),
        FormulaItem(
            name = "Logarithm Product Rule",
            formula = "log_b(xy) = log_b(x) + log_b(y)",
            description = "Separates log of products into sum of logarithms",
            sampleProblem = "Simplify: log2(8) + log2(4)"
        ),
        FormulaItem(
            name = "Binomial Expansion",
            formula = "(a + b)² = a² + 2ab + b²",
            description = "Standard square expansion",
            sampleProblem = "Expand: (3x + 4y)^2"
        )
    )

    val calculusFormulas = listOf(
        FormulaItem(
            name = "Power Rule (Derivative)",
            formula = "d/dx [xⁿ] = n · xⁿ⁻¹",
            description = "Standard derivative rule for polynomials",
            sampleProblem = "Differentiate f(x) = 5x^4 - 3x^2 + 7"
        ),
        FormulaItem(
            name = "Product Rule",
            formula = "d/dx [u · v] = u'v + uv'",
            description = "Derivative of the product of two functions",
            sampleProblem = "Find derivative of f(x) = x^2 * sin(x)"
        ),
        FormulaItem(
            name = "Chain Rule",
            formula = "d/dx [f(g(x))] = f'(g(x)) · g'(x)",
            description = "Derivative of composite functions",
            sampleProblem = "Differentiate f(x) = (2x + 1)^5"
        ),
        FormulaItem(
            name = "Definite Integral",
            formula = "∫ [a to b] f(x) dx = F(b) - F(a)",
            description = "Fundamental Theorem of Calculus",
            sampleProblem = "Evaluate: ∫ from 0 to 2 of (3x^2 + 2x) dx"
        )
    )

    val trigFormulas = listOf(
        FormulaItem(
            name = "Pythagorean Identity",
            formula = "sin²(θ) + cos²(θ) = 1",
            description = "Foundational trigonometry relationship",
            sampleProblem = "If sin(θ) = 3/5, find cos(θ) for acute θ"
        ),
        FormulaItem(
            name = "Double Angle (Sine)",
            formula = "sin(2θ) = 2 · sin(θ) · cos(θ)",
            description = "Expresses double angle in terms of single angle",
            sampleProblem = "Simplify: 2 * sin(15°) * cos(15°)"
        ),
        FormulaItem(
            name = "Law of Cosines",
            formula = "c² = a² + b² - 2ab · cos(C)",
            description = "Finds third side of any triangle given two sides and included angle",
            sampleProblem = "In triangle ABC, a = 5, b = 7, C = 60°. Find side c."
        )
    )

    val geometryFormulas = listOf(
        FormulaItem(
            name = "Circle Area & Circumference",
            formula = "Area = πr²  |  C = 2πr",
            description = "Standard circular geometry metrics",
            sampleProblem = "Find the area and circumference of a circle with radius 7 cm"
        ),
        FormulaItem(
            name = "Cylinder Volume",
            formula = "V = πr²h",
            description = "Volume of right circular cylinder",
            sampleProblem = "Calculate the volume of a cylinder with radius 3m and height 8m"
        ),
        FormulaItem(
            name = "Sphere Surface Area & Volume",
            formula = "A = 4πr²  |  V = (4/3)πr³",
            description = "Spherical geometry metrics",
            sampleProblem = "Find the volume and surface area of a sphere with diameter 12 cm"
        )
    )

    val statisticsFormulas = listOf(
        FormulaItem(
            name = "Arithmetic Mean",
            formula = "x̄ = (Σ xᵢ) / n",
            description = "Average value of dataset",
            sampleProblem = "Find mean of: 12, 15, 20, 25, 30"
        ),
        FormulaItem(
            name = "Sample Variance & Std Dev",
            formula = "s² = Σ(xᵢ - x̄)² / (n - 1)  |  s = √s²",
            description = "Measures dispersion of sample data",
            sampleProblem = "Calculate standard deviation of: 2, 4, 6, 8, 10"
        )
    )

    val currentList = when (selectedCategoryIndex) {
        0 -> algebraFormulas
        1 -> calculusFormulas
        2 -> trigFormulas
        3 -> geometryFormulas
        else -> statisticsFormulas
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = { Text(title, fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentList) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Monospace Formula Display
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.formula,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { onLoadProblem(item.sampleProblem) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Solve Sample Problem")
                        }
                    }
                }
            }
        }
    }
}
