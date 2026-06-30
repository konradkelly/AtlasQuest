package com.atlasquest.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.atlasquest.app.data.repository.ProfileRepository
import com.atlasquest.app.notifications.StreakNotifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!profileRepository.hasPlayedToday()) {
            StreakNotifications.showStreakReminder(applicationContext)
        }
        return Result.success()
    }
}
