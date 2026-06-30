package com.atlasquest.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atlasquest.app.data.local.DatabaseSeeder
import com.atlasquest.app.notifications.StreakNotifications
import com.atlasquest.app.worker.StreakReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val STREAK_REMINDER_WORK_NAME = "streak_reminder"
private const val STREAK_REMINDER_HOUR = 19

@HiltAndroidApp
class AtlasQuestApp : Application(), Configuration.Provider {

    @Inject lateinit var seeder: DatabaseSeeder
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seeder.seedIfNeeded()
        }
        StreakNotifications.ensureChannel(this)
        scheduleStreakReminder()
    }

    private fun scheduleStreakReminder() {
        val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNextReminderHour(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            STREAK_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    // Targets a consistent local evening hour on first run; each subsequent
    // 24h cycle can drift a little under Doze/battery optimization rather
    // than recomputing the target each time — acceptable for v1.
    private fun millisUntilNextReminderHour(): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(STREAK_REMINDER_HOUR, 0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }
}
