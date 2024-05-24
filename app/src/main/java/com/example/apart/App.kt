package com.example.apart

import android.app.Application
import androidx.room.Room
import com.example.apart.features.map.data.room.PlaceDatabase
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        database = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java,
            "places_database"
        ).build()
    }

    companion object {
        const val MAPKIT_API_KEY = "5e7641c5-d637-4e63-9378-2bdf5e9502d6"
        lateinit var database: PlaceDatabase
    }
}