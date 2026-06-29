package com.atlasquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atlasquest.app.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuestions(limit: Int = 10): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE region = :region ORDER BY RANDOM() LIMIT :limit")
    fun getQuestionsByRegion(region: String, limit: Int = 10): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    fun getQuestionsByCategory(category: String, limit: Int = 10): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE difficulty <= :maxDifficulty ORDER BY RANDOM() LIMIT :limit")
    fun getQuestionsByDifficulty(maxDifficulty: Int, limit: Int = 10): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}
