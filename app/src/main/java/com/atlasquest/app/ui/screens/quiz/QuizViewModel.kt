package com.atlasquest.app.ui.screens.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasquest.app.data.model.Question
import com.atlasquest.app.data.model.Region
import com.atlasquest.app.data.repository.ProfileRepository
import com.atlasquest.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** XP awarded per correct answer scales with [Question.difficulty] (1-3). */
private const val XP_PER_DIFFICULTY_POINT = 10

/**
 * UI state for a single quiz run. [score] is the number of correct answers so far
 * (the Results screen renders "score / total"); [xpEarned] is the difficulty-weighted
 * XP accumulated so far, persisted to the profile once the quiz finishes.
 */
data class QuizUiState(
    val loading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: Int? = null,
    val answerRevealed: Boolean = false,
    val score: Int = 0,
    val xpEarned: Int = 0,
    val finished: Boolean = false,
) {
    val current: Question? get() = questions.getOrNull(currentIndex)
    val total: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // The nav arg is a Region.id ("western_europe", "eurasia", …).
    val region: Region? = savedStateHandle.get<String>("region")?.let(Region::fromId)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            // Questions are keyed by Region.id ("western_europe", "eurasia", …).
            // Fall back to a random mix if a region has no questions seeded yet.
            val byRegion = region?.id
                ?.let { repository.getQuestionsByRegion(it).first() }
                .orEmpty()
            val questions = byRegion.ifEmpty { repository.getRandomQuestions().first() }
            _uiState.value = QuizUiState(loading = false, questions = questions)
        }
    }

    /** Lock in the tapped answer and reveal correctness. No-op once revealed. */
    fun selectOption(index: Int) {
        val state = _uiState.value
        val question = state.current ?: return
        if (state.answerRevealed) return
        val correct = index == question.correctAnswerIndex
        _uiState.value = state.copy(
            selectedOption = index,
            answerRevealed = true,
            score = state.score + if (correct) 1 else 0,
            xpEarned = state.xpEarned + if (correct) question.difficulty * XP_PER_DIFFICULTY_POINT else 0,
        )
    }

    /** Advance to the next question, or mark the quiz finished after the last one. */
    fun next() {
        val state = _uiState.value
        if (!state.answerRevealed) return
        if (state.isLastQuestion) {
            // Persist before flipping `finished` so QuizScreen's navigation away
            // (triggered by `finished`) can never race the DB write.
            viewModelScope.launch {
                region?.let {
                    profileRepository.recordQuizResult(
                        regionId = it.id,
                        correct = state.score,
                        total = state.total,
                        xpEarned = state.xpEarned,
                    )
                }
                _uiState.value = state.copy(finished = true)
            }
        } else {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedOption = null,
                answerRevealed = false,
            )
        }
    }
}
