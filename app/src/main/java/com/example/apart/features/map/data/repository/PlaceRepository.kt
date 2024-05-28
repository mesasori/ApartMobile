package com.example.apart.features.map.data.repository

import com.example.apart.utils.room.PlaceDatabase
import com.example.apart.features.map.ui.places.PlaceHolderItem

class PlaceRepository(db: PlaceDatabase) {
    private val dao = db.placeDao()
    fun getAll() = dao.getAll()

    suspend fun getAllForPost() = dao.getAllForPost()

    suspend fun insert(place: PlaceHolderItem) = dao.insert(place.toEntity())

    suspend fun update(place: PlaceHolderItem) = dao.update(place.toEntity())

    suspend fun delete(place: PlaceHolderItem) = dao.delete(place.toEntity())
}