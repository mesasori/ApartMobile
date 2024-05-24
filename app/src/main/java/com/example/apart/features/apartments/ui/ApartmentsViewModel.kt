package com.example.apart.features.apartments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.features.apartments.data.ApartmentHolderItem
import com.example.apart.features.apartments.network.ApartApiService
import com.example.apart.features.apartments.repository.ApartmentRepository
import com.example.apart.utils.Result
import com.example.apart.utils.asResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ApartmentsViewModel : ViewModel() {
    private val apiService = ApartApiService.getInstance()
    private val apartmentRepository: ApartmentRepository = ApartmentRepository(apiService)

    val uiState: StateFlow<ApartmentsUiState> = apartmentRepository
        .getApartments()
        .asResult()
        .map { result ->
            when (result) {
                is Result.Error -> ApartmentsUiState.Error(result.exception)
                is Result.Loading -> ApartmentsUiState.Loading
                is Result.Success -> ApartmentsUiState.Success(result.data.map { it.toHolderItem() })
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = ApartmentsUiState.Loading,
            started = SharingStarted.Lazily
        )

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