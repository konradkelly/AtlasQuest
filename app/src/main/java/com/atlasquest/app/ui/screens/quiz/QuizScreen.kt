package com.atlasquest.app.ui.screens.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val CorrectGreen = Color(0xFF58CC02)
private val WrongRed = Color(0xFFFF4B4B)

@Composable
fun QuizScreen(
    onQuizComplete: (score: Int, total: Int) -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // The ViewModel signals completion; the host owns navigation to Results.
    if (state.finished) {
        onQuizComplete(state.score, state.total)
        return
    }

    when {
        state.loading -> CenterMessage { CircularProgressIndicator() }
        state.questions.isEmpty() -> CenterMessage {
            Text("No questions available yet.", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onQuizComplete(0, 0) }) { Text("Back") }
        }
        else -> QuizContent(
            state = state,
            regionName = viewModel.region?.displayName,
            onSelect = viewModel::selectOption,
            onNext = viewModel::next,
        )
    }
}

@Composable
private fun QuizContent(
    state: QuizUiState,
    regionName: String?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val question = state.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.total },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${regionName ?: "Quiz"} • ${state.currentIndex + 1}/${state.total}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        Text(text = question.text, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        question.options.forEachIndexed { index, option ->
            OptionRow(
                text = option,
                revealed = state.answerRevealed,
                isCorrect = index == question.correctAnswerIndex,
                isSelected = state.selectedOption == index,
                onClick = { onSelect(index) },
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Parrot reaction + advance, shown once an answer is locked in.
        if (state.answerRevealed) {
            val correct = state.selectedOption == question.correctAnswerIndex
            Text(
                text = if (correct) "🦜 Squawk! Nailed it!" else "🦜 Not quite — onward!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (correct) CorrectGreen else MaterialTheme.colorScheme.onSurface,
            )
            question.explanation?.let { explanation ->
                Spacer(Modifier.height(12.dp))
                ExplanationBox(explanation)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLastQuestion) "See results" else "Continue")
            }
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    revealed: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Before reveal: neutral and tappable. After reveal: the correct option turns
    // green, a wrong pick turns red, the rest stay neutral and locked.
    val container = when {
        !revealed -> MaterialTheme.colorScheme.surfaceVariant
        isCorrect -> CorrectGreen
        isSelected -> WrongRed
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = when {
        revealed && (isCorrect || isSelected) -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!revealed) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text = text,
            color = onContainer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun ExplanationBox(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ℹ️ Did you know?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}
