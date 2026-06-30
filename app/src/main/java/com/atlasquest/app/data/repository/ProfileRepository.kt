package com.atlasquest.app.data.repository

import com.atlasquest.app.data.local.dao.ProfileDao
import com.atlasquest.app.data.local.entity.ProfileEntity
import com.atlasquest.app.data.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dao: ProfileDao
) {
    val profile: Flow<Profile> = dao.observeProfile().map { it?.toDomain() ?: Profile() }

    suspend fun hasPlayedToday(): Boolean =
        dao.getProfileOnce()?.lastPlayedEpochDay == LocalDate.now().toEpochDay()

    suspend fun recordQuizResult(regionId: String, correct: Int, total: Int, xpEarned: Int): Profile =
        dao.recordQuizResult(
            regionId = regionId,
            correct = correct,
            total = total,
            xpEarned = xpEarned,
            today = LocalDate.now().toEpochDay(),
        ).toDomain()
}

private fun ProfileEntity.toDomain(): Profile = Profile(
    xpTotal = xpTotal,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
)
