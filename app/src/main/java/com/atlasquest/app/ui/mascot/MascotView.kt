package com.atlasquest.app.ui.mascot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

private fun MascotEmotion.assetPath() = "mascot/parrot_${name.lowercase()}.json"

// CORRECT and WRONG play once and hold the last frame; all others loop.
private fun MascotEmotion.iterations() = when (this) {
    MascotEmotion.CORRECT, MascotEmotion.WRONG -> 1
    else -> LottieConstants.IterateForever
}

// Visible until real Lottie art replaces the placeholder files.
private fun MascotEmotion.placeholderEmoji() = when (this) {
    MascotEmotion.IDLE -> "🦜"
    MascotEmotion.CORRECT -> "🦜✅"
    MascotEmotion.WRONG -> "🦜❌"
    MascotEmotion.CELEBRATE -> "🦜🎉"
    MascotEmotion.ENCOURAGE -> "🦜💪"
}

@Composable
fun MascotView(
    emotion: MascotEmotion,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(emotion.assetPath())
    )
    // composition.layers is empty on our placeholder files; show emoji until
    // real art (non-empty layers) replaces them.
    val hasLottieContent = composition?.layers?.isNotEmpty() == true

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (hasLottieContent) {
            LottieAnimation(
                composition = composition,
                iterations = emotion.iterations(),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(text = emotion.placeholderEmoji(), fontSize = 48.sp)
        }
    }
}
