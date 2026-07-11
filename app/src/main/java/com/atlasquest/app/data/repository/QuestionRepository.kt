package com.atlasquest.app.data.repository

import com.atlasquest.app.data.local.dao.QuestionDao
import com.atlasquest.app.data.local.entity.QuestionEntity
import com.atlasquest.app.data.model.Question
import com.atlasquest.app.data.model.QuestionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val dao: QuestionDao
) {
    fun getRandomQuestions(limit: Int = 10): Flow<List<Question>> =
        dao.getRandomQuestions(limit).map { it.map(QuestionEntity::toDomain) }

    suspend fun isDatabasePopulated(): Boolean = dao.count() > 0
}

private fun QuestionEntity.toDomain(): Question = Question(
    id = id,
    category = QuestionCategory.valueOf(category),
    text = text,
    countryName = countryName,
    countryCode = countryCode,
    answerLat = answerLat,
    answerLng = answerLng,
    difficulty = difficulty,
    explanation = explanation,
)
