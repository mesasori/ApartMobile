package com.example.apart.utils.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apart.features.map.ui.places.PlaceHolderItem
import com.yandex.mapkit.geometry.Point

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    var importance: Int
) {
    fun fromEntity(): PlaceHolderItem = PlaceHolderItem(
        title,
        subtitle,
        Point(latitude, longitude),
        importance
    )
}