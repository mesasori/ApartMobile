package com.example.apart.features.apartments.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.App
import com.example.apart.features.apartments.data.ApartmentHolderItem
import com.example.apart.features.apartments.data.Place
import com.example.apart.features.apartments.data.PostBodyModel
import com.example.apart.features.apartments.network.ApartApiService
import com.example.apart.features.apartments.repository.ApartmentRepository
import com.example.apart.features.map.data.repository.PlaceRepository
import com.example.apart.features.map.ui.places.PlaceUiState
import com.example.apart.utils.Result
import com.example.apart.utils.asResult
import com.example.apart.utils.room.PlaceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApartmentsViewModel : ViewModel() {
    private val apiService = ApartApiService.getInstance()
    private val apartmentRepository: ApartmentRepository = ApartmentRepository(apiService)
    private val repository = PlaceRepository(App.database)

    private val listWithData = mutableListOf<PlaceEntity>(
        PlaceEntity(title="Vorontsovo Pole Street, 4с1А,", subtitle="Moscow, Russia, ", latitude=55.752313, longitude=37.648564, importance=6),
        PlaceEntity(title="Tessinsky Lane, 1,", subtitle="Moscow, Russia,", latitude=55.750753, longitude=37.649346, importance=3),
        PlaceEntity(title="VK Cloud", subtitle="Russian Federation, Moscow, Leningradskiy Avenue, 39с79", latitude=55.796931, longitude=37.537847, importance=2)
    )

    var uiState: StateFlow<ApartmentsUiState> = apartmentRepository
        .getApartments()
        .asResult()
        .map { result ->
            when (result) {
                is Result.Error -> {
                    ApartmentsUiState.Error(result.exception)
                }
                is Result.Loading -> {
                    ApartmentsUiState.Loading
                }
                is Result.Success -> {
                    ApartmentsUiState.Success(result.data.map { it.toHolderItem() })
                }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = ApartmentsUiState.Loading,
            started = SharingStarted.Lazily
        )


//    suspend fun uploadData() {
//        viewModelScope.launch {
//            listWithData.addAll(repository.getAllForPost())
//            Log.d("APARTMENTS_VIEW_MODEL", listWithData.joinToString())
//        }.join()
//    }

}

sealed interface ApartmentsUiState {
    object Loading : ApartmentsUiState

    data class Success(
        val data: List<ApartmentHolderItem>
    ) : ApartmentsUiState

    data class Error(
        val throwable: Throwable?
    ) : ApartmentsUiState
}