package com.atlasquest.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.atlasquest.app.data.local.dao.ProfileDao
import com.atlasquest.app.data.local.dao.QuestionDao
import com.atlasquest.app.data.local.entity.ProfileEntity
import com.atlasquest.app.data.local.entity.QuestionEntity

@Database(
    entities = [QuestionEntity::class, ProfileEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "atlasquest.db"
    }
}
