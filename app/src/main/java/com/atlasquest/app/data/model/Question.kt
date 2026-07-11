package com.atlasquest.app.data.model

/** The kind of clue a question gives about its country. */
enum class QuestionCategory {
    CAPITALS, FLAGS, BORDERS, POPULATION, LANDMARKS, CURRENCY
}

/**
 * A single pin-drop question: [text] is a clue describing a country; the player
 * answers by dropping a pin anywhere on the world globe.
 *
 * [countryCode] (ISO 3166-1 alpha-2) identifies the answer country for the
 * polygon hit-test — a pin anywhere inside it is a correct answer.
 * [answerLat]/[answerLng] is a representative point (usually the capital),
 * used for the reveal marker and for near-miss distance scoring when the pin
 * lands outside the country.
 */
data class Question(
    val id: Long,
    val category: QuestionCategory,
    val text: String,
    val countryName: String,
    val countryCode: String,
    val answerLat: Double,
    val answerLng: Double,
    val difficulty: Int,       // 1–3
    val explanation: String? = null,   // shown after answering, when present
)
