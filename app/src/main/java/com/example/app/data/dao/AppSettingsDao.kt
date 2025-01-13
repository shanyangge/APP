package com.example.app.data.dao

import androidx.room.*
import com.example.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettings>>

    @Query("SELECT * FROM app_settings WHERE packageName = :packageName")
    suspend fun getSettings(packageName: String): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)

    @Delete
    suspend fun deleteSettings(settings: AppSettings)
}