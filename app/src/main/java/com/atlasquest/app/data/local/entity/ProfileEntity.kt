package com.atlasquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table — there is only ever one local profile, always at [id] = 0.
 * Room has no native "singleton row" concept, so the fixed id is the convention
 * that enforces it; always upsert with id = 0, never insert a second row.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val xpTotal: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    // Sentinel for "never played" so the first-ever quiz falls into the
    // streak-reset branch rather than needing special-case null handling.
    val lastPlayedEpochDay: Long = Long.MIN_VALUE,
)
