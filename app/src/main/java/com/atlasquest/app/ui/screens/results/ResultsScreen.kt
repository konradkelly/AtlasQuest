package com.atlasquest.app.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atlasquest.app.ui.mascot.MascotEmotion
import com.atlasquest.app.ui.mascot.MascotView
import com.atlasquest.app.ui.profile.ProfileViewModel

@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    xpEarned: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by profileViewModel.profile.collectAsState()
    val resultEmotion = if (score.toFloat() / total >= 0.7f || profile.currentStreak >= 3)
        MascotEmotion.CELEBRATE else MascotEmotion.ENCOURAGE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MascotView(emotion = resultEmotion, modifier = Modifier.size(140.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$score / $total",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Quiz complete!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "+$xpEarned XP earned",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "🔥 ${profile.currentStreak} day streak",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Play Again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("Home")
        }
    }
}
