package com.example.app

import android.app.Application
import androidx.room.Room
import com.example.app.data.AppDatabase

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
}