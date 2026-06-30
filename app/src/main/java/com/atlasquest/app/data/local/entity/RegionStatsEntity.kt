package com.atlasquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_stats")
data class RegionStatsEntity(
    @PrimaryKey val regionId: String,
    val quizzesPlayed: Int = 0,
    val questionsCorrect: Int = 0,
    val questionsTotal: Int = 0,
)
