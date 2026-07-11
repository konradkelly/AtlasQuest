package com.atlasquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.atlasquest.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0")
    suspend fun getProfileOnce(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    /**
     * Applies one completed quiz: XP accumulates unconditionally, while the
     * streak only advances if [today] is consecutive with the last played day
     * (unchanged if replayed same day, reset to 1 on any gap).
     */
    @Transaction
    suspend fun recordQuizResult(
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
        return updated
    }
}
