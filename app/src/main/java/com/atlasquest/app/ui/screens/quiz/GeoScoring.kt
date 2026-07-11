package com.atlasquest.app.ui.screens.quiz

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Scoring for pin-drop answers. Pure functions, no Android dependencies —
 * unit-tested in isolation.
 *
 * A pin anywhere inside the answer country is **correct** and pays the same
 * full XP regardless of where in the country it lands (fair for large
 * countries, where distance to the capital says little). A pin outside the
 * country still pays half XP when it's within the difficulty's near-miss
 * radius of the answer point: d1 = 800 km, d2 = 500 km, d3 = 300 km.
 * Half credit keeps `score` binary so "score / total" retains its meaning
 * on the Results screen.
 */
object GeoScoring {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val XP_PER_DIFFICULTY_POINT = 10

    private val nearMissKm = mapOf(1 to 800.0, 2 to 500.0, 3 to 300.0)

    data class PinScore(
        val distanceKm: Double,
        val correct: Boolean,
        val xp: Int,
    )

    /** Great-circle distance between two lat/lng points, in kilometres. */
    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    fun scorePin(hitCountry: Boolean, distanceKm: Double, difficulty: Int): PinScore {
        val d = difficulty.coerceIn(1, 3)
        return when {
            hitCountry ->
                PinScore(distanceKm, correct = true, xp = d * XP_PER_DIFFICULTY_POINT)
            distanceKm <= nearMissKm.getValue(d) ->
                PinScore(distanceKm, correct = false, xp = d * XP_PER_DIFFICULTY_POINT / 2)
            else -> PinScore(distanceKm, correct = false, xp = 0)
        }
    }
}
