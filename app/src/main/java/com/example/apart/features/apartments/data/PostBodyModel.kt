package com.example.apart.features.apartments.data

data class PostBodyModel(
    val places: List<Place>
)

data class Place(
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val attendance: Int
)