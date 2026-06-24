package com.atlasquest.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.atlasquest.app.data.local.dao.QuestionDao
import com.atlasquest.app.data.local.entity.QuestionEntity

@Database(
    entities = [QuestionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        const val DATABASE_NAME = "atlasquest.db"
    }
}
