package com.atlasquest.app.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atlasquest.app.data.model.IsoNumeric
import com.atlasquest.app.ui.mascot.MascotEmotion
import com.atlasquest.app.ui.mascot.MascotView
import kotlin.math.roundToInt

private val CorrectGreen = Color(0xFF58CC02)
private val WrongRed = Color(0xFFFF4B4B)
private val XpAmber = Color(0xFFFFC107)

@Composable
fun QuizScreen(
    onQuizComplete: (score: Int, total: Int, xpEarned: Int) -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // One WebView per quiz — every question is a pin drop, so the globe page
    // is created up front and reused across questions.
    val context = LocalContext.current
    val pinController = remember { PinGlobeController(context) }
    DisposableEffect(Unit) {
        onDispose { pinController.destroy() }
    }

    // The ViewModel signals completion; the host owns navigation to Results.
    if (state.finished) {
        onQuizComplete(state.score, state.total, state.xpEarned)
        return
    }

    when {
        state.loading -> CenterMessage { CircularProgressIndicator() }
        state.questions.isEmpty() -> CenterMessage {
            MascotView(emotion = MascotEmotion.ENCOURAGE, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(16.dp))
            Text("No questions available yet.", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            // Bypasses the ViewModel's `finished` flag entirely, so no quiz
            // result (XP/streak) is ever recorded for an empty quiz.
            Button(onClick = { onQuizComplete(0, 0, 0) }) { Text("Back") }
        }
        else -> QuizContent(
            state = state,
            pinController = pinController,
            onSubmitPin = viewModel::submitPin,
            onNext = viewModel::next,
        )
    }
}

@Composable
private fun QuizContent(
    state: QuizUiState,
    pinController: PinGlobeController,
    onSubmitPin: (Double, Double, String, String) -> Unit,
    onNext: () -> Unit,
) {
    val question = state.current ?: return
    // Whether a pin has been dropped for the current question; gates the
    // Confirm button and resets when the question changes.
    var pinPlaced by remember(question.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
    ) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Question ${state.currentIndex + 1}/${state.total}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "✓ ${state.score} correct",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = CorrectGreen,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(text = question.text, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        // The globe takes every pixel the fixed elements don't need; the zoom
        // controls float over its bottom-right corner while answering.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            PinQuestionView(
                controller = pinController,
                questionId = question.id,
                answerRevealed = state.answerRevealed,
                answerLat = question.answerLat,
                answerLng = question.answerLng,
                answerCountryId = IsoNumeric.alpha2ToNumeric[question.countryCode].orEmpty(),
                pinCorrect = state.pinCorrect,
                onPinPlaced = { pinPlaced = true },
                onPinConfirmed = onSubmitPin,
                modifier = Modifier.fillMaxSize(),
            )
            if (!state.answerRevealed) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ZoomButton("+") { pinController.zoomIn() }
                    ZoomButton("−") { pinController.zoomOut() }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (!state.answerRevealed) {
            Button(
                onClick = { pinController.confirm() },
                enabled = pinPlaced,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (pinPlaced) "Confirm pin" else "Drop a pin on the globe")
            }
        } else {
            if (state.lastAwardedXp > 0) {
                XpBadge(state.lastAwardedXp)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = resultLine(state, question.countryName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (state.pinCorrect == true) CorrectGreen else WrongRed,
            )
            question.explanation?.let { explanation ->
                Spacer(Modifier.height(10.dp))
                ExplanationBox(explanation)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLastQuestion) "See results" else "Continue")
            }
        }
    }
}

private fun resultLine(state: QuizUiState, answerCountry: String): String {
    val landedIn = state.pinCountryName?.takeIf { it.isNotBlank() }
    val km = state.pinDistanceKm?.roundToInt()?.let { "%,d".format(it) }
    return when {
        state.pinCorrect == true -> "You found $answerCountry!"
        landedIn != null -> "Your pin landed in $landedIn, $km km away — the answer was $answerCountry."
        else -> "Your pin landed in the ocean, $km km away — the answer was $answerCountry."
    }
}

@Composable
private fun XpBadge(xp: Int) {
    // Amber pill with dark text, echoing the gold answer marker on the globe.
    Surface(shape = RoundedCornerShape(50), color = XpAmber) {
        Text(
            text = "+$xp XP",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A1628),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ZoomButton(symbol: String, onClick: () -> Unit) {
    // Translucent dark disc so the glyph stays legible over any country colour.
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xE60A1628),
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun ExplanationBox(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Did you know?",
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
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}
