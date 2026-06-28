package com.atlasquest.app.data.model

enum class QuestionCategory {
    CAPITALS, FLAGS, BORDERS, POPULATION, LANDMARKS, CURRENCY
}

enum class QuizMode {
    CLASSIC, MAP_TAP, STREAK, DAILY
}

data class Question(
    val id: Long,
    val category: QuestionCategory,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val region: String,
    val difficulty: Int,       // 1–3
    val imageResName: String? = null,
    val explanation: String? = null,   // shown after answering, when present
)
