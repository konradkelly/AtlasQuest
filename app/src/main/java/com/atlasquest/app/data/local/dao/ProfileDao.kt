package com.atlasquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.atlasquest.app.data.local.entity.ProfileEntity
import com.atlasquest.app.data.local.entity.RegionStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0")
    suspend fun getProfileOnce(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM region_stats WHERE regionId = :regionId")
    suspend fun getRegionStats(regionId: String): RegionStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegionStats(stats: RegionStatsEntity)

    /**
     * Applies one completed quiz: region stats accumulate unconditionally,
     * while the streak only advances if [today] is consecutive with the last
     * played day (unchanged if replayed same day, reset to 1 on any gap).
     */
    @Transaction
    suspend fun recordQuizResult(
        regionId: String,
        correct: Int,
        total: Int,
        xpEarned: Int,
        today: Long,
    ): ProfileEntity {
        val current = getProfileOnce() ?: ProfileEntity()
        val newStreak = when (today) {
            current.lastPlayedEpochDay -> current.currentStreak
            current.lastPlayedEpochDay + 1 -> current.currentStreak + 1
            else -> 1
        }
        val updated = current.copy(
            xpTotal = current.xpTotal + xpEarned,
            currentStreak = newStreak,
            longestStreak = maxOf(current.longestStreak, newStreak),
            lastPlayedEpochDay = today,
        )
        upsertProfile(updated)

        val stats = getRegionStats(regionId) ?: RegionStatsEntity(regionId = regionId)
        upsertRegionStats(
            stats.copy(
                quizzesPlayed = stats.quizzesPlayed + 1,
                questionsCorrect = stats.questionsCorrect + correct,
                questionsTotal = stats.questionsTotal + total,
            )
        )

        return updated
    }
}
