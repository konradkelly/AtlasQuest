package com.atlasquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Long,
    val category: String,
    val text: String,
    val optionsJson: String,    // JSON array of strings
    val correctAnswerIndex: Int,
    val region: String,
    val difficulty: Int,
    val imageResName: String? = null,
)
