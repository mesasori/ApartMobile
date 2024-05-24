package com.example.apart.features.map.ui.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.App
import com.example.apart.features.map.data.Result
import com.example.apart.features.map.data.asResult
import com.example.apart.features.map.data.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlacesDialogViewModel : ViewModel() {
    private val repository = PlaceRepository(App.database)

    val uiState: StateFlow<PlaceUiState> = repository.getAll().asResult()
        .map { result ->
            when (result) {
                is Result.Loading -> PlaceUiState.Loading
                is Result.Error -> PlaceUiState.Error(result.exception)
                is Result.Success -> PlaceUiState.Success(result.data.map { it.fromEntity() })
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = PlaceUiState.Loading,
            started = SharingStarted.Lazily
        )

    fun removePlace(place: PlaceHolderItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(place)
        }
    }

    fun updatePlace(place: PlaceHolderItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(place)
        }
    }
}

sealed interface PlaceUiState {
    object Loading : PlaceUiState

    data class Success(
        val data: List<PlaceHolderItem>
    ) : PlaceUiState

    data class Error(
        val throwable: Throwable? = null
    ) : PlaceUiState
}