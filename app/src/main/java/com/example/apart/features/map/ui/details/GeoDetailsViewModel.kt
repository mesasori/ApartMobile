package com.example.apart.features.map.ui.details

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.App
import com.example.apart.utils.GeoObjectHolder
import com.example.apart.features.map.data.repository.PlaceRepository
import com.example.apart.utils.takeIfNotEmpty
import com.example.apart.features.map.ui.places.PlaceHolderItem
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.uri.UriObjectMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class DetailsDialogUiState(
    val title: String,
    val descriptionText: String,
    val location: Point?,
    val uri: String?,
    val typeSpecificState: TypeSpecificState,
)

sealed interface TypeSpecificState {
    data class Toponym(val address: String) : TypeSpecificState

    data class Business(
        val name: String,
        val workingHours: String?,
        val categories: String?,
        val phones: String?,
        val link: String?,
    ) : TypeSpecificState

    object Undefined : TypeSpecificState
}

class GeoDetailsViewModel(
    context: Context
) : ViewModel() {
    private val repository = PlaceRepository(App.database)

    fun uiState(): DetailsDialogUiState? {
        val geoObject = GeoObjectHolder.tappedObject ?: return null
        Log.d("GEO OBject", geoObject.toString())
        val uri = geoObject.metadataContainer.getItem(UriObjectMetadata::class.java)?.uris?.firstOrNull()

        val geoObjetTypeUiState = geoObject.metadataContainer.getItem(ToponymObjectMetadata::class.java)?.let {
            TypeSpecificState.Toponym(address = it.address.formattedAddress)
        } ?: geoObject.metadataContainer.getItem(BusinessObjectMetadata::class.java)?.let {
            TypeSpecificState.Business(
                name = it.name,
                workingHours = it.workingHours?.text,
                categories = it.categories.map { it.name }.takeIfNotEmpty()?.toSet()
                    ?.joinToString(", "),
                phones = it.phones.map { it.formattedNumber }.takeIfNotEmpty()?.joinToString(", "),
                link = it.links.firstOrNull()?.link?.href,
            )
        } ?: TypeSpecificState.Undefined

        return DetailsDialogUiState(
            title = geoObject.name ?: "No title",
            descriptionText = geoObject.descriptionText ?: "No description",
            location = geoObject.geometry.firstOrNull()?.point,
            uri = uri?.value,
            typeSpecificState = geoObjetTypeUiState,
        )
    }

    fun addPlace(place: PlaceHolderItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(place)
        }
    }
}