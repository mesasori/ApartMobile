package com.example.apart.features.apartments.data

import com.yandex.mapkit.geometry.Point

data class ApartmentHolderItem(
    val address: String,
    val price: String,
    val information: String,
    val undergroundStation: String,
    val image: String,
    val link: String,
    val coordinates: Point
)
