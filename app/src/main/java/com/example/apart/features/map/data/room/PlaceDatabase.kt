package com.example.apart.features.map.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class], version = 1)
abstract class PlaceDatabase: RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}