package com.example.apart.features.apartments.data

import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class ApartmentModelBackend(
    val rent: String?,
    val currency: String?,
    val address: String?,
    @SerializedName("metro_station")
    val undergroundStation: String?,
    val location: Location?,
    val area: Int?,
    @SerializedName("cover_image")
    val coverImage: String?,
    val images: List<String>?,
    val provider: String?,
    val url: String?,
    val additional: Additional?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("created_at")
    val createdAt: String?
) {
    fun toHolderItem(): ApartmentHolderItem {
        val address = address ?: "Null address"
        val price = rent.toString() + currency.toString()
        val information =
            "Площадь: ${area.toString()}m², Этаж: ${additional?.floor ?: "0"}/${additional?.floorsCount ?: "0"}"
        val undergroundStation = undergroundStation ?: "Random station"
        return ApartmentHolderItem(
            address,
            price,
            information,
            undergroundStation,
            coverImage ?: "https://example.com/",
            url ?: "https://www.cian.ru/rent/flat/300935887/",
            Point(location?.coordinates?.get(0) ?: 55.7670476, location?.coordinates?.get(1) ?: 37.5939051)
        )
    }
}

data class Location(
    val type: String?,
    val coordinates: List<Double>
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

