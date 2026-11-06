package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.MathSolverAiService
import com.example.data.room.AppDatabase
import com.example.data.room.MathHistoryEntity
import com.example.model.FollowUpMessage
import com.example.model.MathSolution
import com.example.model.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private const val TAG = "MathSolverViewModel"

sealed interface SolverUiState {
    data object Idle : SolverUiState
    data object Loading : SolverUiState
    data class Success(val solution: MathSolution) : SolverUiState
    data class Error(val message: String) : SolverUiState
}

enum class Screen {
    SOLVER, CALCULATOR, FORMULAS, HISTORY
}

class MathSolverViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.mathHistoryDao()
    private val aiService = MathSolverAiService()

    private val _currentScreen = MutableStateFlow(Screen.SOLVER)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _uiState = MutableStateFlow<SolverUiState>(SolverUiState.Idle)
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    private val _currentSolution = MutableStateFlow<MathSolution?>(null)
    val currentSolution: StateFlow<MathSolution?> = _currentSolution.asStateFlow()

    private val _isCurrentBookmarked = MutableStateFlow(false)
    val isCurrentBookmarked: StateFlow<Boolean> = _isCurrentBookmarked.asStateFlow()

    private var currentEntityId: Long? = null

    private val _pendingSolverInput = MutableStateFlow<String?>(null)
    val pendingSolverInput: StateFlow<String?> = _pendingSolverInput.asStateFlow()

    val historyList: StateFlow<List<MathHistoryEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _followUpMessages = MutableStateFlow<List<FollowUpMessage>>(emptyList())
    val followUpMessages: StateFlow<List<FollowUpMessage>> = _followUpMessages.asStateFlow()

    private val _isFollowUpLoading = MutableStateFlow(false)
    val isFollowUpLoading: StateFlow<Boolean> = _isFollowUpLoading.asStateFlow()

    // TextToSpeech for reading math answers
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun loadProblemIntoSolver(problemText: String) {
        _pendingSolverInput.value = problemText
        _currentScreen.value = Screen.SOLVER
    }

    fun clearPendingInput() {
        _pendingSolverInput.value = null
    }

    fun solveTextProblem(questionText: String) {
        if (questionText.isBlank()) return
        _uiState.value = SolverUiState.Loading
        _followUpMessages.value = emptyList()

        viewModelScope.launch {
            val result = aiService.solveMathQuestion(questionText)
            result.onSuccess { solution ->
                _currentSolution.value = solution
                _uiState.value = SolverUiState.Success(solution)
                _isCurrentBookmarked.value = false

                // Save to Room history
                try {
                    val json = aiService.solutionToJson(solution)
                    val id = dao.insert(
                        MathHistoryEntity(
                            question = solution.question,
                            finalAnswer = solution.finalAnswer,
                            topic = solution.topic,
                            difficulty = solution.difficulty,
                            solutionJson = json,
                            isBookmarked = false
                        )
                    )
                    currentEntityId = id
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist to database", e)
                }
            }.onFailure { err ->
                _uiState.value = SolverUiState.Error(err.localizedMessage ?: "Failed to solve problem.")
            }
        }
    }

    fun solveMultimodalProblem(notes: String, bitmap: Bitmap) {
        _uiState.value = SolverUiState.Loading
        _followUpMessages.value = emptyList()

        viewModelScope.launch {
            val result = aiService.solveMathQuestion(notes, bitmap)
            result.onSuccess { solution ->
                _currentSolution.value = solution
                _uiState.value = SolverUiState.Success(solution)
                _isCurrentBookmarked.value = false

                // Save to Room history
                try {
                    val json = aiService.solutionToJson(solution)
                    val id = dao.insert(
                        MathHistoryEntity(
                            question = solution.question,
                            finalAnswer = solution.finalAnswer,
                            topic = solution.topic,
                            difficulty = solution.difficulty,
                            solutionJson = json,
                            isBookmarked = false
                        )
                    )
                    currentEntityId = id
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist to database", e)
                }
            }.onFailure { err ->
                _uiState.value = SolverUiState.Error(err.localizedMessage ?: "Failed to recognize and solve image problem.")
            }
        }
    }

    fun loadFromHistory(entity: MathHistoryEntity) {
        try {
            val solution = aiService.parseSolutionJson(entity.solutionJson)
            _currentSolution.value = solution
            _uiState.value = SolverUiState.Success(solution)
            _isCurrentBookmarked.value = entity.isBookmarked
            currentEntityId = entity.id
            _followUpMessages.value = emptyList()
            _currentScreen.value = Screen.SOLVER
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse history entity", e)
        }
    }

    fun toggleCurrentBookmark() {
        val id = currentEntityId ?: return
        val newVal = !_isCurrentBookmarked.value
        _isCurrentBookmarked.value = newVal
        viewModelScope.launch {
            dao.setBookmark(id, newVal)
        }
    }

    fun setHistoryBookmark(id: Long, isBookmarked: Boolean) {
        if (currentEntityId == id) {
            _isCurrentBookmarked.value = isBookmarked
        }
        viewModelScope.launch {
            dao.setBookmark(id, isBookmarked)
        }
    }

    fun deleteHistoryItem(entity: MathHistoryEntity) {
        viewModelScope.launch {
            dao.delete(entity)
            if (currentEntityId == entity.id) {
                currentEntityId = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearHistory()
            currentEntityId = null
        }
    }

    fun resetSolver() {
        _currentSolution.value = null
        _uiState.value = SolverUiState.Idle
        _followUpMessages.value = emptyList()
        currentEntityId = null
    }

    fun sendFollowUp(userQuestion: String) {
        val solution = _currentSolution.value ?: return
        val userMsg = FollowUpMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userQuestion
        )
        _followUpMessages.value = _followUpMessages.value + userMsg
        _isFollowUpLoading.value = true

        viewModelScope.launch {
            // Build history pairs
            val pairs = mutableListOf<Pair<String, String>>()
            val messages = _followUpMessages.value
            for (i in 0 until messages.size - 1 step 2) {
                if (i + 1 < messages.size) {
                    pairs.add(Pair(messages[i].text, messages[i + 1].text))
                }
            }

            val result = aiService.answerFollowUp(solution, pairs, userQuestion)
            _isFollowUpLoading.value = false
            result.onSuccess { reply ->
                val aiMsg = FollowUpMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = reply
                )
                _followUpMessages.value = _followUpMessages.value + aiMsg
            }.onFailure { err ->
                val errorMsg = FollowUpMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = "Sorry, I couldn't process your question: ${err.localizedMessage}"
                )
                _followUpMessages.value = _followUpMessages.value + errorMsg
            }
        }
    }

    fun speakAnswer(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MathAnswerTts")
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
