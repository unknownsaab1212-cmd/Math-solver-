package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.MathSolution
import com.example.model.MathStep
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "MathSolverAiService"

class MathSolverAiService {

    // JSON Schema for structured MathSolution
    private val stepSchema = Schema.obj(
        properties = mapOf(
            "stepNumber" to Schema.integer(description = "1-based step index"),
            "title" to Schema.string(description = "Short descriptive title of this step, e.g. Factorize quadratic"),
            "explanation" to Schema.string(description = "Clear step-by-step reasoning explaining why and how"),
            "expression" to Schema.string(description = "Mathematical equation or expression result for this step")
        )
    )

    private val mathSolutionSchema = Schema.obj(
        properties = mapOf(
            "question" to Schema.string(description = "Normalized clean question text or equation"),
            "topic" to Schema.string(description = "Mathematical topic e.g. Algebra, Calculus, Trigonometry, Geometry, Arithmetic, Statistics, Word Problem"),
            "difficulty" to Schema.string(description = "Difficulty level: Easy, Medium, Hard, or Advanced"),
            "finalAnswer" to Schema.string(description = "Exact final answer clearly stated"),
            "explanation" to Schema.string(description = "Concise conceptual summary of how the problem is approached"),
            "steps" to Schema.array(items = stepSchema, description = "Ordered step-by-step resolution"),
            "formulasUsed" to Schema.array(items = Schema.string(), description = "Key mathematical formulas or theorems applied"),
            "graphableFunction" to Schema.string(description = "If this problem involves a 2D algebraic/calculus curve, provide a clean mathematical expression in terms of x suitable for plotting (e.g. 'x^2 - 4' or '2*x + 1' or 'sin(x)'). Otherwise empty string."),
            "verification" to Schema.string(description = "How to verify/check the result"),
            "proTip" to Schema.string(description = "Pro tip, alternative shortcut, or common pitfall to avoid")
        )
    )

    private val structuredConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = mathSolutionSchema
        temperature = 0.2f
    }

    // Ultra-fast model (gemini-3.5-flash-lite) for low-latency math solving
    private val fastSolverModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = structuredConfig,
            systemInstruction = content {
                text(
                    "You are an expert mathematics tutor and problem solver. " +
                    "Solve math questions accurately, clearly, and concisely in the required JSON format. " +
                    "Break down problems into clear, understandable steps with explanations and intermediate expressions."
                )
            }
        )
    }

    // High-reasoning model (gemini-3.8-flash) as backup
    private val standardSolverModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = structuredConfig,
            systemInstruction = content {
                text(
                    "You are an expert mathematics tutor and problem solver. " +
                    "Solve math questions accurately, clearly, and concisely in the required JSON format."
                )
            }
        )
    }

    // Conversational model for follow-up questions
    private val conversationalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text(
                    "You are an encouraging and articulate mathematics tutor. " +
                    "Answer follow-up questions regarding the solved math problem clearly, showing steps and mathematical insight."
                )
            }
        )
    }

    suspend fun solveMathQuestion(
        questionText: String,
        imageBitmap: Bitmap? = null
    ): Result<MathSolution> = withContext(Dispatchers.IO) {
        // Scale bitmap down to prevent massive payload transmission delays
        val preparedBitmap = imageBitmap?.let { scaleBitmapDown(it, 1024) }

        // Try fast model first; if it fails or times out, fallback to standard model
        val firstAttempt = runGeneration(fastSolverModel, questionText, preparedBitmap, timeoutMs = 45_000L)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

        Log.w(TAG, "Fast model attempt failed or timed out, trying standard model fallback...", firstAttempt.exceptionOrNull())
        runGeneration(standardSolverModel, questionText, preparedBitmap, timeoutMs = 60_000L)
    }

    private suspend fun runGeneration(
        model: GenerativeModel,
        questionText: String,
        preparedBitmap: Bitmap?,
        timeoutMs: Long
    ): Result<MathSolution> {
        return try {
            val response = withTimeout(timeoutMs) {
                if (preparedBitmap != null) {
                    val promptText = if (questionText.isNotBlank()) {
                        "Transcribe and solve the math problem in this image. Additional user notes: $questionText"
                    } else {
                        "Transcribe and solve the math problem shown in this image step-by-step."
                    }
                    model.generateContent(
                        content {
                            image(preparedBitmap)
                            text(promptText)
                        }
                    )
                } else {
                    model.generateContent(
                        content {
                            text("Solve this mathematical problem step-by-step: $questionText")
                        }
                    )
                }
            }

            val rawJson = response.text ?: throw IllegalStateException("Model returned empty text.")
            Log.d(TAG, "Raw solver JSON: $rawJson")

            val solution = parseSolutionJson(rawJson)
            Result.success(solution)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Solving timed out on model", e)
            Result.failure(Exception("Math solver timed out. Please check your internet connection or try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Solving failed on model", e)
            Result.failure(e)
        }
    }

    suspend fun answerFollowUp(
        contextSolution: MathSolution,
        history: List<Pair<String, String>>, // user to ai
        newQuestion: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contextPrompt = buildString {
                appendLine("ORIGINAL PROBLEM: ${contextSolution.question}")
                appendLine("FINAL ANSWER: ${contextSolution.finalAnswer}")
                appendLine("SOLUTION SUMMARY: ${contextSolution.explanation}")
                appendLine("STEPS:")
                contextSolution.steps.forEach { step ->
                    appendLine("Step ${step.stepNumber}: ${step.title} -> ${step.expression}. ${step.explanation}")
                }
                appendLine("\nPRIOR CHAT CONVERSATION:")
                history.forEach { (q, a) ->
                    appendLine("Student: $q")
                    appendLine("Tutor: $a")
                }
                appendLine("\nNEW STUDENT QUESTION: $newQuestion")
                appendLine("Provide a friendly, helpful explanation answering the student's question directly.")
            }

            val response = withTimeout(40_000L) {
                conversationalModel.generateContent(contextPrompt)
            }

            val reply = response.text
            if (!reply.isNullOrBlank()) {
                Result.success(reply)
            } else {
                Result.failure(Exception("Empty response from AI tutor."))
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tutor response timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Follow-up failed", e)
            Result.failure(e)
        }
    }

    fun parseSolutionJson(jsonStr: String): MathSolution {
        val root = JSONObject(jsonStr)
        val question = root.optString("question", "Math Problem")
        val topic = root.optString("topic", "General Math")
        val difficulty = root.optString("difficulty", "Medium")
        val finalAnswer = root.optString("finalAnswer", "No answer provided")
        val explanation = root.optString("explanation", "")

        val stepsList = mutableListOf<MathStep>()
        val stepsArray = root.optJSONArray("steps")
        if (stepsArray != null) {
            for (i in 0 until stepsArray.length()) {
                val stepObj = stepsArray.getJSONObject(i)
                stepsList.add(
                    MathStep(
                        stepNumber = stepObj.optInt("stepNumber", i + 1),
                        title = stepObj.optString("title", "Step ${i + 1}"),
                        explanation = stepObj.optString("explanation", ""),
                        expression = stepObj.optString("expression", "")
                    )
                )
            }
        }

        val formulasList = mutableListOf<String>()
        val formulasArray = root.optJSONArray("formulasUsed")
        if (formulasArray != null) {
            for (i in 0 until formulasArray.length()) {
                formulasList.add(formulasArray.getString(i))
            }
        }

        val graphableFunc = root.optString("graphableFunction", "").trim().takeIf { it.isNotBlank() }
        val verification = root.optString("verification", "")
        val proTip = root.optString("proTip", "")

        return MathSolution(
            question = question,
            topic = topic,
            difficulty = difficulty,
            finalAnswer = finalAnswer,
            explanation = explanation,
            steps = stepsList,
            formulasUsed = formulasList,
            graphableFunction = graphableFunc,
            verification = verification,
            proTip = proTip
        )
    }

    fun solutionToJson(solution: MathSolution): String {
        val json = JSONObject()
        json.put("question", solution.question)
        json.put("topic", solution.topic)
        json.put("difficulty", solution.difficulty)
        json.put("finalAnswer", solution.finalAnswer)
        json.put("explanation", solution.explanation)

        val stepsArr = org.json.JSONArray()
        solution.steps.forEach { step ->
            val s = JSONObject()
            s.put("stepNumber", step.stepNumber)
            s.put("title", step.title)
            s.put("explanation", step.explanation)
            s.put("expression", step.expression)
            stepsArr.put(s)
        }
        json.put("steps", stepsArr)

        val formArr = org.json.JSONArray()
        solution.formulasUsed.forEach { formArr.put(it) }
        json.put("formulasUsed", formArr)

        json.put("graphableFunction", solution.graphableFunction ?: "")
        json.put("verification", solution.verification)
        json.put("proTip", solution.proTip)

        return json.toString()
    }
}

fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int = 1024): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDimension && height <= maxDimension) return bitmap

    val ratio = width.toFloat() / height.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (ratio > 1) {
        newWidth = maxDimension
        newHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
    } else {
        newHeight = maxDimension
        newWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}
