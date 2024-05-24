package com.example.apart.features.apartments.data

import com.google.gson.annotations.SerializedName
import java.net.URL

data class ApartmentModelBackend(
    val rent: String,
    val currency: String,
    val location: Location,
    val area: Int,
    @SerializedName("cover_image")
    val coverImage: String,
    val images: List<String>?,
    val provider: String,
    val url: String,
    val additional: Additional,
    @SerializedName("_id")
    val id: String
) {
    fun toHolderItem(): ApartmentHolderItem {
        val address = location.address
        val price = "$rent $currency"
        val information = "Площадь: ${area}m², Этаж: ${additional.floor}/${additional.floorsCount}"
        val undergroundStation = location.undergroundStation
        return ApartmentHolderItem(address, price, information, undergroundStation, coverImage, url)
    }
}

data class Location(
    val address: String,
    val longitude: Double,
    val latitude: Double,
    @SerializedName("metro_station")
    val undergroundStation: String
)

data class Additional(
    val author: String = "Paxar",
    @SerializedName("deal_type")
    val dealType: String = "Rent",
    val floor: Int = 4,
    @SerializedName("floors_count")
    val floorsCount: Int = 23,
    @SerializedName("rooms_count")
    val roomsCount: Int = 3
)

