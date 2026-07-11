package com.atlasquest.app.ui.mascot

enum class MascotEmotion {
    IDLE,       // home screen; between questions; globe browsing
    CORRECT,    // answer revealed as correct
    WRONG,      // answer revealed as wrong
    CELEBRATE,  // high score on results; streak milestone
    ENCOURAGE,  // low score on results; empty quiz; first play
}
