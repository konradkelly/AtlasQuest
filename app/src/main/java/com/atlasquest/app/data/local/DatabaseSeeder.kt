package com.atlasquest.app.data.local

import android.content.Context
import com.atlasquest.app.data.local.dao.QuestionDao
import com.atlasquest.app.data.local.entity.QuestionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class QuestionSeed(
    val id: Long,
    val category: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val region: String,
    val difficulty: Int,
    val imageResName: String? = null,
)

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: QuestionDao
) {
    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return

        val json = context.assets.open("questions.json")
            .bufferedReader()
            .use { it.readText() }

        val seeds = Json.decodeFromString<List<QuestionSeed>>(json)
        val entities = seeds.map {
            QuestionEntity(
                id = it.id,
                category = it.category,
                text = it.text,
                optionsJson = Json.encodeToString(ListSerializer(String.serializer()), it.options),
                correctAnswerIndex = it.correctAnswerIndex,
                region = it.region,
                difficulty = it.difficulty,
                imageResName = it.imageResName,
            )
        }
        dao.insertAll(entities)
    }
}
