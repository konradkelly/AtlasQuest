package com.atlasquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Long,
    val category: String,
    val text: String,
    val countryName: String,
    val countryCode: String,    // ISO 3166-1 alpha-2 ("FR")
    val answerLat: Double,
    val answerLng: Double,
    val difficulty: Int,
    val explanation: String? = null,
)
