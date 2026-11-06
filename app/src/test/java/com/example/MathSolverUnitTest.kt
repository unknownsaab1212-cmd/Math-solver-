package com.example

import com.example.ui.components.evalPolynomial
import com.example.ui.components.sampleFunction
import com.example.ui.screens.evaluateSimpleMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathSolverUnitTest {

    @Test
    fun testCalculatorEvaluation() {
        val result1 = evaluateSimpleMath("2+3*4", false)
        assertEquals(14.0, result1, 0.001)

        val result2 = evaluateSimpleMath("(10-4)/2", false)
        assertEquals(3.0, result2, 0.001)

        val result3 = evaluateSimpleMath("2^3", false)
        assertEquals(8.0, result3, 0.001)

        val result4 = evaluateSimpleMath("50% * 80", false)
        assertEquals(40.0, result4, 0.001)
    }

    @Test
    fun testFunctionSampling() {
        val points = sampleFunction("x^2 - 4", -3f, 3f, 10)
        assertTrue(points.isNotEmpty())
        val atZero = evalPolynomial("x^2 - 4", 0f)
        assertEquals(-4.0f, atZero, 0.01f)

        val atTwo = evalPolynomial("x^2 - 4", 2f)
        assertEquals(0.0f, atTwo, 0.01f)
    }

    @Test
    fun testPolynomialEvaluation() {
        val linear = evalPolynomial("2*x + 5", 3f)
        assertEquals(11.0f, linear, 0.01f)

        val cubic = evalPolynomial("x^3 - 2*x + 1", 2f)
        // 8 - 4 + 1 = 5
        assertEquals(5.0f, cubic, 0.01f)
    }
}
