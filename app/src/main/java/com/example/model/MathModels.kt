package com.example.model

data class MathStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val expression: String
)

data class MathSolution(
    val question: String,
    val topic: String,
    val difficulty: String,
    val finalAnswer: String,
    val explanation: String,
    val steps: List<MathStep>,
    val formulasUsed: List<String>,
    val graphableFunction: String?, // e.g., "x^2 - 4" or null
    val verification: String,
    val proTip: String
)

data class FollowUpMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

enum class MathCategory(val title: String, val icon: String, val exampleQuestion: String) {
    ALGEBRA("Algebra", "x²", "Solve for x: 2x^2 + 5x - 3 = 0"),
    CALCULUS("Calculus", "∫dx", "Find the derivative of f(x) = x^3 * sin(x)"),
    TRIGONOMETRY("Trig", "sin θ", "Solve for θ in [0, 2π]: 2*cos^2(θ) + cos(θ) - 1 = 0"),
    GEOMETRY("Geometry", "📐", "A cylinder has radius 4 cm and height 10 cm. Find total surface area and volume."),
    ARITHMETIC("Arithmetic", "% ÷", "Calculate: (3/4 + 2/5) ÷ (7/10 - 1/3)"),
    STATISTICS("Statistics", "σ²", "Find mean, median, and standard deviation of: 4, 8, 6, 5, 3, 7"),
    WORD_PROBLEM("Word Qs", "💡", "A train travels 120 km at speed v. If speed is increased by 10 km/h, the journey takes 24 min less. Find v.")
}
