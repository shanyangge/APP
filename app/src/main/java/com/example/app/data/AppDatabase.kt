package com.example.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app.data.dao.AppSettingsDao
import com.example.app.domain.model.AppSettings

@Database(
    entities = [AppSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao
}