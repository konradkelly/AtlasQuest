package com.atlasquest.app.di

import android.content.Context
import androidx.room.Room
import com.atlasquest.app.data.local.AppDatabase
import com.atlasquest.app.data.local.dao.QuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // Questions are bundled seed data, so a schema bump can safely wipe and
            // re-seed rather than requiring a hand-written migration.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()
}
