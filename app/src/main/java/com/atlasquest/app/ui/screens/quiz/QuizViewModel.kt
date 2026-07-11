package com.atlasquest.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasquest.app.data.model.IsoNumeric
import com.atlasquest.app.data.model.Question
import com.atlasquest.app.data.repository.ProfileRepository
import com.atlasquest.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for a single quiz run. [score] is the number of pins that landed in
 * the answer country so far (the Results screen renders "score / total");
 * [xpEarned] is the difficulty-weighted XP accumulated so far — including
 * half-credit near-misses — persisted to the profile once the quiz finishes.
 */
data class QuizUiState(
    val loading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answerRevealed: Boolean = false,
    val score: Int = 0,
    val xpEarned: Int = 0,
    val finished: Boolean = false,
    // Reveal state for the current question; cleared on advance.
    val pinDistanceKm: Double? = null,
    val pinCorrect: Boolean? = null,
    val pinCountryName: String? = null,   // where the pin landed ("" = ocean)
    val lastAwardedXp: Int = 0,           // XP from the current question (0 = far miss)
) {
    val current: Question? get() = questions.getOrNull(currentIndex)
    val total: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val questions = repository.getRandomQuestions().first()
            _uiState.value = QuizUiState(loading = false, questions = questions)
        }
    }

    /**
     * Lock in a confirmed pin drop. The globe page reports the raw guess plus
     * which country polygon it hit; correctness and XP are scored natively here.
     */
    fun submitPin(lat: Double, lng: Double, countryId: String, countryName: String) {
        val state = _uiState.value
        val question = state.current ?: return
        if (state.answerRevealed) return
        val answerId = IsoNumeric.alpha2ToNumeric[question.countryCode]
        val result = GeoScoring.scorePin(
            hitCountry = countryId.isNotEmpty() && countryId == answerId,
            distanceKm = GeoScoring.haversineKm(lat, lng, question.answerLat, question.answerLng),
            difficulty = question.difficulty,
        )
        _uiState.value = state.copy(
            answerRevealed = true,
            pinDistanceKm = result.distanceKm,
            pinCorrect = result.correct,
            pinCountryName = countryName,
            lastAwardedXp = result.xp,
            score = state.score + if (result.correct) 1 else 0,
            xpEarned = state.xpEarned + result.xp,
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
                profileRepository.recordQuizResult(xpEarned = state.xpEarned)
                _uiState.value = state.copy(finished = true)
            }
        } else {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                answerRevealed = false,
                pinDistanceKm = null,
                pinCorrect = null,
                pinCountryName = null,
                lastAwardedXp = 0,
            )
        }
    }
}
