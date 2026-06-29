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
    val explanation: String? = null,
)

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: QuestionDao
) {
    /**
     * Loads `questions.json` into the database. Re-seeds (clear + insert) whenever
     * [SEED_VERSION] is bumped, so updated question content actually takes effect on
     * devices that were already seeded — `count() > 0` alone would skip the import.
     */
    suspend fun seedIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val seededVersion = prefs.getInt(KEY_SEED_VERSION, 0)
        if (seededVersion == SEED_VERSION && dao.count() > 0) return

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
                explanation = it.explanation,
            )
        }
        dao.clearAll()
        dao.insertAll(entities)
        prefs.edit().putInt(KEY_SEED_VERSION, SEED_VERSION).apply()
    }

    companion object {
        /** Bump whenever questions.json changes so devices re-import it. */
        private const val SEED_VERSION = 3
        private const val PREFS_NAME = "atlasquest_seed"
        private const val KEY_SEED_VERSION = "seed_version"
    }
}
