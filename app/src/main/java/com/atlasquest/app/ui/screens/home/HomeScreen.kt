package com.atlasquest.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onStartQuiz: (mode: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AtlasQuest",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "How well do you know the world?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        QuizModeButton("Classic Quiz", "classic", onStartQuiz)
        Spacer(modifier = Modifier.height(12.dp))
        QuizModeButton("Streak Mode", "streak", onStartQuiz)
        Spacer(modifier = Modifier.height(12.dp))
        QuizModeButton("Daily Challenge", "daily", onStartQuiz)
    }
}

@Composable
private fun QuizModeButton(label: String, mode: String, onClick: (String) -> Unit) {
    Button(
        onClick = { onClick(mode) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}
