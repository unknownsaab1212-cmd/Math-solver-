package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.FormulaGuideScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SolverScreen
import com.example.ui.theme.MathSolverTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.MathSolverViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MathSolverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MathSolverTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

@Composable
fun MainAppContent(viewModel: MathSolverViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSolution by viewModel.currentSolution.collectAsStateWithLifecycle()
    val isCurrentBookmarked by viewModel.isCurrentBookmarked.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val followUpMessages by viewModel.followUpMessages.collectAsStateWithLifecycle()
    val isFollowUpLoading by viewModel.isFollowUpLoading.collectAsStateWithLifecycle()
    val pendingSolverInput by viewModel.pendingSolverInput.collectAsStateWithLifecycle()

    // Handle back button on secondary screens
    if (currentScreen != Screen.SOLVER) {
        BackHandler {
            viewModel.setScreen(Screen.SOLVER)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                NavigationBarItem(
                    selected = currentScreen == Screen.SOLVER,
                    onClick = { viewModel.setScreen(Screen.SOLVER) },
                    icon = { Icon(Icons.Default.Functions, contentDescription = "Solver") },
                    label = { Text("Solver") },
                    modifier = Modifier.testTag("nav_solver_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.CALCULATOR,
                    onClick = { viewModel.setScreen(Screen.CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calculator") },
                    modifier = Modifier.testTag("nav_calc_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.FORMULAS,
                    onClick = { viewModel.setScreen(Screen.FORMULAS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Formulas") },
                    label = { Text("Formulas") },
                    modifier = Modifier.testTag("nav_formulas_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.HISTORY,
                    onClick = { viewModel.setScreen(Screen.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("nav_history_tab")
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.SOLVER -> {
                SolverScreen(
                    uiState = uiState,
                    currentSolution = currentSolution,
                    isCurrentBookmarked = isCurrentBookmarked,
                    followUpMessages = followUpMessages,
                    isFollowUpLoading = isFollowUpLoading,
                    onSolveText = { question -> viewModel.solveTextProblem(question) },
                    onSolveMultimodal = { notes, bmp -> viewModel.solveMultimodalProblem(notes, bmp) },
                    onToggleBookmark = { viewModel.toggleCurrentBookmark() },
                    onSpeakAnswer = { text -> viewModel.speakAnswer(text) },
                    onSendFollowUp = { q -> viewModel.sendFollowUp(q) },
                    onResetSolver = { viewModel.resetSolver() },
                    initialQuestionText = pendingSolverInput,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.CALCULATOR -> {
                CalculatorScreen(
                    onSolveInAi = { expr ->
                        viewModel.loadProblemIntoSolver("Evaluate and explain: $expr")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.FORMULAS -> {
                FormulaGuideScreen(
                    onLoadProblem = { sampleProblem ->
                        viewModel.loadProblemIntoSolver(sampleProblem)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.HISTORY -> {
                HistoryScreen(
                    historyList = historyList,
                    onSelectHistoryItem = { entity -> viewModel.loadFromHistory(entity) },
                    onToggleBookmark = { id, bookmarked -> viewModel.setHistoryBookmark(id, bookmarked) },
                    onDeleteItem = { entity -> viewModel.deleteHistoryItem(entity) },
                    onClearAll = { viewModel.clearAllHistory() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
