package com.atlasquest.app.ui.screens.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoScoringTest {

    @Test
    fun `haversine of identical points is zero`() {
        assertEquals(0.0, GeoScoring.haversineKm(48.86, 2.29, 48.86, 2.29), 0.001)
    }

    @Test
    fun `haversine Paris to London is about 344 km`() {
        val d = GeoScoring.haversineKm(48.8566, 2.3522, 51.5074, -0.1278)
        assertEquals(344.0, d, 5.0)
    }

    @Test
    fun `haversine of antipodal points is half Earth circumference`() {
        val d = GeoScoring.haversineKm(0.0, 0.0, 0.0, 180.0)
        assertEquals(20015.0, d, 20.0)
    }

    @Test
    fun `haversine crosses the antimeridian correctly`() {
        // Suva (178.44E) to Nuku'alofa (175.20W) is ~750 km, not most of the way around.
        val d = GeoScoring.haversineKm(-18.14, 178.44, -21.14, -175.20)
        assertTrue("expected < 1000 km, got $d", d < 1000.0)
    }

    @Test
    fun `pin inside the country is correct with full XP`() {
        val s = GeoScoring.scorePin(hitCountry = true, distanceKm = 3000.0, difficulty = 2)
        assertTrue(s.correct)
        assertEquals(20, s.xp)
    }

    @Test
    fun `country hit pays full XP no matter the distance to the answer point`() {
        // Eastern Siberia is ~6000 km from Moscow but still inside Russia.
        val s = GeoScoring.scorePin(hitCountry = true, distanceKm = 6000.0, difficulty = 1)
        assertTrue(s.correct)
        assertEquals(10, s.xp)
    }

    @Test
    fun `near miss outside the country pays half XP and is not correct`() {
        val s = GeoScoring.scorePin(hitCountry = false, distanceKm = 400.0, difficulty = 2)
        assertFalse(s.correct)
        assertEquals(10, s.xp)
    }

    @Test
    fun `near-miss boundary is inclusive per difficulty`() {
        assertEquals(5, GeoScoring.scorePin(false, 800.0, 1).xp)
        assertEquals(10, GeoScoring.scorePin(false, 500.0, 2).xp)
        assertEquals(15, GeoScoring.scorePin(false, 300.0, 3).xp)
        assertEquals(0, GeoScoring.scorePin(false, 301.0, 3).xp)
    }

    @Test
    fun `far miss scores nothing`() {
        val s = GeoScoring.scorePin(hitCountry = false, distanceKm = 5000.0, difficulty = 1)
        assertFalse(s.correct)
        assertEquals(0, s.xp)
    }

    @Test
    fun `difficulty outside 1-3 is clamped`() {
        assertEquals(5, GeoScoring.scorePin(false, 700.0, 0).xp)    // clamps to d1 (800 km)
        assertEquals(0, GeoScoring.scorePin(false, 700.0, 9).xp)    // clamps to d3 (300 km)
    }
}
